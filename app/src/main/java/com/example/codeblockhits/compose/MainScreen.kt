package com.example.codeblockhits.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*
import com.example.codeblockhits.R
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    var blocks by remember { mutableStateOf<List<CodeBlock>>(emptyList()) }
    var nextId by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State for scaling and panning
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    fun getVariablesMap(): Map<String, String> {
        return blocks.filterIsInstance<VariableBlock>()
            .associate { it.name to it.value }
    }

    fun evaluateAllBlocks() {
        val variablesMap = getVariablesMap()
        blocks.forEach { block ->
            when (block) {
                is VariableBlock -> {
                    val result = evaluateExpression(block.value, variablesMap)
                    if (result != block.value) {
                        blocks = blocks.map {
                            if (it.id == block.id) block.copy(value = result)
                            else it
                        }
                    }
                }
                is AssignmentBlock -> {
                    val result = evaluateExpression(block.expression, variablesMap)
                    blocks = blocks.map {
                        if (it is VariableBlock && it.name == block.target) {
                            it.copy(value = result)
                        } else {
                            it
                        }
                    }
                }
                is IfElseBlock -> {
                    val condition = evaluateExpression(
                        "${block.leftOperand} ${block.operator} ${block.rightOperand}",
                        variablesMap
                    )
                    if (condition == "true") {
                        block.thenBlocks.forEach { thenBlock ->
                            if (thenBlock is AssignmentBlock) {
                                val result = evaluateExpression(thenBlock.expression, variablesMap)
                                blocks = blocks.map {
                                    if (it is VariableBlock && it.name == thenBlock.target) {
                                        it.copy(value = result)
                                    } else {
                                        it
                                    }
                                }
                            }
                        }
                    } else {
                        block.elseBlocks.forEach { elseBlock ->
                            if (elseBlock is AssignmentBlock) {
                                val result = evaluateExpression(elseBlock.expression, variablesMap)
                                blocks = blocks.map {
                                    if (it is VariableBlock && it.name == elseBlock.target) {
                                        it.copy(value = result)
                                    } else {
                                        it
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        coroutineScope.launch {
            snackbarHostState.showSnackbar("All blocks evaluated successfully")
        }
    }

    Scaffold(
        topBar = {
            TopMenuPanel(
                onAddVariable = { name ->
                    val variableNames = blocks.filterIsInstance<VariableBlock>().map { it.name }
                    if (variableNames.contains(name)) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Variable '$name' already exists")
                        }
                    } else {
                        blocks = blocks + VariableBlock(id = nextId++, name = name, value = "0")
                    }
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                },
                onEvaluateAll = { evaluateAllBlocks() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Grid background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .drawGrid()
            )

            // Workspace content
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offset += pan
                        }
                    },
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
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
            }
        }
    }
}
