package com.example.codeblockhits.compose

import com.example.codeblockhits.data.*
import com.example.codeblockhits.data.VariableValue
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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

    val errorMessage = stringResource(R.string.error)
    val successMessage = stringResource(R.string.programOutput)
    val variableExistsMessage = stringResource(R.string.variableExists)

    fun getVariablesMap(): Map<String, VariableValue> {
        return buildMap {
            blocks.filterIsInstance<VariableBlock>()
                .forEach { put(it.name, VariableValue.Scalar(it.value)) }
        }
    }

    fun evaluateAllBlocks() {
        erroredBlockId = null
        val result = interpreterBlocks(
            blocks = blocks.toMutableList(),
            startBlockId = blocks.firstOrNull()?.id,
            variables = getVariablesMap().toMutableMap()
        )
        programOutput = result.output
        erroredBlockId = result.errorBlockId
        showOutputDialog = true
        coroutineScope.launch {
            val msg = if (result.errorBlockId != null) {
                errorMessage
            } else {
                successMessage
            }
            snackbarHostState.showSnackbar(msg)
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
                        is WhileBlock -> it.copy(nextBlockId = blockId)
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
                onAddVariable = { input ->
                    val name = input.trim()
                    val allNames = blocks.filterIsInstance<VariableBlock>().map { it.name }

                    if (name.isNotBlank() && name !in allNames) {
                        blocks = blocks + VariableBlock(id = nextId++, name = name, value = "0")
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(variableExistsMessage)
                        }
                    }
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                },
                onAddAssignment = { target, expression ->
                    blocks = blocks + AssignmentBlock(
                        id = nextId++,
                        target = target,
                        expression = expression
                    )
                },
                onAddPrint = {
                    blocks = blocks + PrintBlock(id = nextId++)
                },
                onAddWhile = {
                    blocks = blocks + WhileBlock(id = nextId++)
                },
                onEvaluateAll = { evaluateAllBlocks() },
                onArrowMode = {
                    isArrowMode = true
                    selectedSourceBlockId = null
                }
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
                        onRemove = { removedId ->
                            val removedBlock = blocks.find { it.id == removedId }
                            val targetOfRemovedBlockId = removedBlock?.nextBlockId

                            var updatedBlocks = blocks.filter { it.id != removedId }

                            updatedBlocks = updatedBlocks.map {
                                if (it.nextBlockId == removedId) {
                                    val newNextId =
                                        if (it.id == targetOfRemovedBlockId) null else targetOfRemovedBlockId
                                    when (it) {
                                        is VariableBlock -> it.copy(nextBlockId = newNextId)
                                        is AssignmentBlock -> it.copy(nextBlockId = newNextId)
                                        is IfElseBlock -> it.copy(nextBlockId = newNextId)
                                        is PrintBlock -> it.copy(nextBlockId = newNextId)
                                        is WhileBlock -> it.copy(nextBlockId = newNextId)
                                    }
                                } else {
                                    it
                                }
                            }
                            blocks = updatedBlocks
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
                                } else it
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
