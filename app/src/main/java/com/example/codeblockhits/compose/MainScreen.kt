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
    var showOutputDialog by remember { mutableStateOf(false) }
    var programOutput by remember { mutableStateOf<List<String>>(emptyList()) }
    var erroredBlockId by remember { mutableStateOf<Int?>(null) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var isArrowMode by remember { mutableStateOf(false) }
    var selectedSourceBlockId by remember { mutableStateOf<Int?>(null) }

    fun getVariablesMap(): Map<String, String> {
        return blocks.filterIsInstance<VariableBlock>()
            .associate { it.name to it.value }
    }

    fun evaluateAllBlocks() {
        erroredBlockId = null
        val result = interpretBlocksRPN(blocks.toMutableList(), startBlockId = blocks.firstOrNull()?.id, variables = mutableMapOf())
        programOutput = result.output
        erroredBlockId = result.errorBlockId
        showOutputDialog = true
        coroutineScope.launch {
            if (result.errorBlockId != null) {
                snackbarHostState.showSnackbar("Error during evaluation. Check highlighted block and output.")
            } else {
                snackbarHostState.showSnackbar("All blocks evaluated successfully (RPN)")
            }
        }
    }

    fun onBlockClickedForArrow(blockId: Int) {
        if (!isArrowMode) return
        if (selectedSourceBlockId == null) {
            selectedSourceBlockId = blockId
        } else if (selectedSourceBlockId != blockId) {
            blocks = blocks.map {
                if (it.id == selectedSourceBlockId) {
                    when (it) {
                        is VariableBlock -> it.copy(nextBlockId = blockId)
                        is AssignmentBlock -> it.copy(nextBlockId = blockId)
                        is IfElseBlock -> it.copy(nextBlockId = blockId)
                        is PrintBlock -> it.copy(nextBlockId = blockId)
                    }
                } else it
            }
            selectedSourceBlockId = null
            isArrowMode = false
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
                onAddAssignment = { target, expression ->
                    blocks = blocks + AssignmentBlock(id = nextId++, target = target, expression = expression)
                },
                onAddPrint = {
                    blocks = blocks + PrintBlock(id = nextId++)
                },
                onEvaluateAll = { evaluateAllBlocks() },
                onArrowMode = { isArrowMode = true; selectedSourceBlockId = null }
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .drawGrid()
            )

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
                        erroredBlockId = erroredBlockId,
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
                        onIdIncrement = { nextId++ },
                        isArrowMode = isArrowMode,
                        selectedSourceBlockId = selectedSourceBlockId,
                        onBlockClickedForArrow = ::onBlockClickedForArrow
                    )
                }
            }
        }
    }

    if (showOutputDialog) {
        OutputDialog(
            output = programOutput,
            onDismiss = { showOutputDialog = false }
        )
    }
}
