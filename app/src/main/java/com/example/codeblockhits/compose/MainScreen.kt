package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.CodeBlock
import com.example.codeblockhits.data.IfElseBlock
import com.example.codeblockhits.R
import com.example.codeblockhits.data.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    var blocks by remember { mutableStateOf<List<CodeBlock>>(emptyList()) }
    var nextId by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun getVariablesMap(): Map<String, String> {
        return blocks.filterIsInstance<VariableBlock>()
            .associate { it.name to it.value }
    }

    Scaffold(
        topBar = {
            TopMenuPanel(
                onAddVariable = { name ->
                    val variableNames = blocks.filterIsInstance<VariableBlock>().map { it.name }
                    if (variableNames.contains(name)) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Переменная с именем '$name' уже существует")
                        }
                    } else {
                        blocks = blocks + VariableBlock(id = nextId++, name = name, value = "0")
                    }
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                }
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CodeBlocksList(
                        blocks = blocks,
                        onRemove = { id ->
                            blocks = blocks.filter { it.id != id }
                        },
                        onUpdate = { updated ->
                            blocks = blocks.map { if (it.id == updated.id) updated else it }
                        },
                        onAddToIfElse = { parentId, block, isThenBlock ->
                            blocks = blocks.map {
                                if (it.id == parentId && it is IfElseBlock) {
                                    if (isThenBlock) {
                                        it.copy(thenBlocks = it.thenBlocks + block)
                                    } else {
                                        it.copy(elseBlocks = it.elseBlocks + block)
                                    }
                                } else {
                                    it
                                }
                            }
                        },
                        variablesMap = getVariablesMap(),
                        nextId = nextId,
                        onIdIncrement = { nextId++ }
                    )
                }

                Button(
                    onClick = {
                        val variables = mutableMapOf<String, String>()
                        val updatedBlocks = mutableListOf<CodeBlock>()

                        fun evaluate(block: CodeBlock): CodeBlock {
                            return when (block) {
                                is VariableBlock -> {
                                    val newValue = evaluateExpression(block.value, variables)
                                    variables[block.name] = newValue
                                    block.copy(value = newValue)
                                }
                                is AssignmentBlock -> {
                                    val result = evaluateExpression(block.expression, variables)
                                    variables[block.target] = result

                                    val index = updatedBlocks.indexOfFirst {
                                        it is VariableBlock && it.name == block.target
                                    }

                                    if (index != -1) {
                                        val vb = updatedBlocks[index] as VariableBlock
                                        updatedBlocks[index] = vb.copy(value = result)
                                    }

                                    block.copy(expression = result)
                                }
                                is IfElseBlock -> {
                                    val left = evaluateExpression(block.leftOperand, variables).toDoubleOrNull()
                                    val right = evaluateExpression(block.rightOperand, variables).toDoubleOrNull()
                                    val condition = when (block.operator) {
                                        "==" -> left == right
                                        "!=" -> left != right
                                        ">" -> left != null && right != null && left > right
                                        "<" -> left != null && right != null && left < right
                                        ">=" -> left != null && right != null && left >= right
                                        "<=" -> left != null && right != null && left <= right
                                        else -> false
                                    }

                                    val thenBlocks = block.thenBlocks.map { evaluate(it) }
                                    val elseBlocks = block.elseBlocks.map { evaluate(it) }

                                    block.copy(
                                        thenBlocks = thenBlocks,
                                        elseBlocks = elseBlocks
                                    )
                                }
                                else -> block
                            }
                        }

                        blocks.forEach { original ->
                            updatedBlocks.add(evaluate(original))
                        }

                        blocks = updatedBlocks
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(stringResource(R.string.Calculate))
                }
            }
        }
    }
}
