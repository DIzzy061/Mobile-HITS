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
import com.example.codeblockhits.compose.CodeBlocksList
import com.example.codeblockhits.data.IfElseBlock
import com.example.codeblockhits.R
import com.example.codeblockhits.compose.TopMenuPanel
import com.example.codeblockhits.data.VariableBlock
import com.example.codeblockhits.data.*
import com.example.codeblockhits.compose.*
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
                        variablesMap = getVariablesMap()
                    )
                }

                Button(
                    onClick = {
                        val variablesMap = getVariablesMap()
                        val evaluatedBlocks = mutableListOf<CodeBlock>()

                        for (block in blocks) {
                            when (block) {
                                is VariableBlock -> {
                                    evaluatedBlocks += block.copy(
                                        value =evaluateExpression(block.value, variablesMap)
                                    )
                                }
                                is IfElseBlock -> {
                                    val resultBlocks =evaluateIfElseBlock(block, variablesMap)
                                    evaluatedBlocks += resultBlocks
                                }
                            }
                        }

                        blocks = evaluatedBlocks
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
