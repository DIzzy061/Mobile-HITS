package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R
import com.example.codeblockhits.data.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke

@Composable
fun IfElseBlockView(
    block: IfElseBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, VariableValue>,
    nextId: Int,
    onIdIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    var leftOperand by remember { mutableStateOf(block.leftOperand) }
    var rightOperand by remember { mutableStateOf(block.rightOperand) }
    var operator by remember { mutableStateOf(block.operator) }
    var showThenBlocks by remember { mutableStateOf(false) }
    var showElseBlocks by remember { mutableStateOf(false) }

    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    var showAddBlockDialog by remember { mutableStateOf(false) }
    var isAddingToThen by remember { mutableStateOf(true) }
    var showVariableNameDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorMessageText = stringResource(R.string.variableExists)

    val dialogButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    if (showAddBlockDialog) {
        AlertDialog(
            onDismissRequest = { showAddBlockDialog = false },
            title = { Text(stringResource(R.string.addBlock)) },
            text = {
                Column {
                    Text(
                        text = if (isAddingToThen) stringResource(R.string.addBlockToThen) else stringResource(
                            R.string.addBlockToElse
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showAddBlockDialog = false
                            showVariableNameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogButtonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🧩 ${stringResource(R.string.addVariable)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddToIfElse(
                                block.id,
                                AssignmentBlock(id = nextId, target = "", expression = "0"),
                                isAddingToThen
                            )
                            onIdIncrement()
                            showAddBlockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogButtonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📝 ${stringResource(R.string.addAssignment)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddToIfElse(
                                block.id,
                                PrintBlock(id = nextId),
                                isAddingToThen
                            )
                            onIdIncrement()
                            showAddBlockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogButtonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🖨️ ${stringResource(R.string.addPrint)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddToIfElse(
                                block.id,
                                IfElseBlock(id = nextId),
                                isAddingToThen
                            )
                            onIdIncrement()
                            showAddBlockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogButtonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔀 ${stringResource(R.string.addIfElse)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddToIfElse(
                                block.id,
                                WhileBlock(id = nextId),
                                isAddingToThen
                            )
                            onIdIncrement()
                            showAddBlockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogButtonColors,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔄 ${stringResource(R.string.addWhile)}")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddBlockDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showVariableNameDialog) {
        val duplicate = if (isAddingToThen) {
            block.thenBlocks.filterIsInstance<VariableBlock>().any { it.name == newVarName }
        } else {
            block.elseBlocks.filterIsInstance<VariableBlock>().any { it.name == newVarName }
        }
        AlertDialog(
            onDismissRequest = {
                showVariableNameDialog = false
                newVarName = ""
            },
            confirmButton = {
                Button(onClick = {
                    if (duplicate) {
                        errorMessage = errorMessageText
                        showErrorDialog = true
                    } else {
                        onAddToIfElse(
                            block.id,
                            VariableBlock(id = nextId, name = newVarName, value = "0"),
                            isAddingToThen
                        )
                        onIdIncrement()
                        showVariableNameDialog = false
                        newVarName = ""
                    }
                }) { Text(stringResource(R.string.addBlock)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVariableNameDialog = false
                    newVarName = ""
                }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.variableName)) },
            text = {
                OutlinedTextField(
                    value = newVarName,
                    onValueChange = { newVarName = it },
                    label = { Text(stringResource(R.string.variableName)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage) }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔀 ${stringResource(R.string.addIfElse)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = leftOperand,
                        onValueChange = {
                            leftOperand = it
                            onUpdate(
                                block.copy(
                                    leftOperand = it,
                                    operator = operator,
                                    rightOperand = rightOperand
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.leftOperand)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        Button(
                            onClick = { operatorMenuExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(operator)
                        }
                        DropdownMenu(
                            expanded = operatorMenuExpanded,
                            onDismissRequest = { operatorMenuExpanded = false }
                        ) {
                            operatorOptions.forEach { op ->
                                DropdownMenuItem(
                                    text = { Text(op) },
                                    onClick = {
                                        operator = op
                                        operatorMenuExpanded = false
                                        onUpdate(
                                            block.copy(
                                                leftOperand = leftOperand,
                                                operator = op,
                                                rightOperand = rightOperand
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = rightOperand,
                        onValueChange = {
                            rightOperand = it
                            onUpdate(
                                block.copy(
                                    leftOperand = leftOperand,
                                    operator = operator,
                                    rightOperand = it
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.rightOperand)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${stringResource(R.string.thenBlocks)} (${block.thenBlocks.size})",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = { showThenBlocks = !showThenBlocks }) {
                        Icon(
                            if (showThenBlocks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showThenBlocks) stringResource(R.string.hideBlocks) else stringResource(
                                R.string.showBlocks
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = showThenBlocks) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(start = 16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(block.thenBlocks) { childBlock ->
                                val updateList =
                                    { updated: CodeBlock -> onUpdate(block.copy(thenBlocks = block.thenBlocks.map { if (it.id == updated.id) updated else it })) }
                                val removeBlock =
                                    { onUpdate(block.copy(thenBlocks = block.thenBlocks.filter { it.id != childBlock.id })) }

                                BlockViewSwitcher(
                                    childBlock,
                                    updateList,
                                    removeBlock,
                                    onAddToIfElse,
                                    variablesMap,
                                    nextId,
                                    onIdIncrement
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isAddingToThen = true
                                showAddBlockDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = dialogButtonColors,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.addBlockToThen))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${stringResource(R.string.elseBlocks)} (${block.elseBlocks.size})",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = { showElseBlocks = !showElseBlocks }) {
                        Icon(
                            if (showElseBlocks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showElseBlocks) stringResource(R.string.hideBlocks) else stringResource(
                                R.string.showBlocks
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = showElseBlocks) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(start = 16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(block.elseBlocks) { childBlock ->
                                val updateList =
                                    { updated: CodeBlock -> onUpdate(block.copy(elseBlocks = block.elseBlocks.map { if (it.id == updated.id) updated else it })) }
                                val removeBlock =
                                    { onUpdate(block.copy(elseBlocks = block.elseBlocks.filter { it.id != childBlock.id })) }

                                BlockViewSwitcher(
                                    childBlock,
                                    updateList,
                                    removeBlock,
                                    onAddToIfElse,
                                    variablesMap,
                                    nextId,
                                    onIdIncrement
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isAddingToThen = false
                                showAddBlockDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = dialogButtonColors,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.addBlockToElse))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockViewSwitcher(
    childBlock: CodeBlock,
    updateList: (CodeBlock) -> Unit,
    removeBlock: () -> Unit,
    onAddToIfElseNested: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, VariableValue>,
    nextId: Int,
    onIdIncrement: () -> Unit
) {
    when (childBlock) {
        is VariableBlock -> VariableBlockView(
            block = childBlock,
            onValueChange = { newValue -> updateList(childBlock.copy(value = newValue)) },
            onRemove = removeBlock,
            variablesMap = variablesMap
        )

        is AssignmentBlock -> AssignmentBlockView(childBlock, updateList, removeBlock, variablesMap)
        is PrintBlock -> PrintBlockView(childBlock, updateList, removeBlock, variablesMap)
        is IfElseBlock -> {
            val localOnAddToIfElse = { parentId: Int, newBlock: CodeBlock, toThen: Boolean ->
                val updatedBlock = if (toThen) {
                    childBlock.copy(thenBlocks = childBlock.thenBlocks + newBlock)
                } else {
                    childBlock.copy(elseBlocks = childBlock.elseBlocks + newBlock)
                }
                updateList(updatedBlock)
            }
            IfElseBlockView(
                childBlock,
                updateList,
                removeBlock,
                localOnAddToIfElse,
                variablesMap,
                nextId,
                onIdIncrement
            )
        }

        is WhileBlock -> WhileBlockView(
            block = childBlock,
            onUpdate = updateList,
            onRemove = removeBlock,
            variablesMap = variablesMap,
            nextId = nextId,
            onIdIncrement = onIdIncrement
        )
    }
}