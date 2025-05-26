package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

@Composable
fun WhileBlockView(
    block: WhileBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, VariableValue>,
    nextId: Int,
    onIdIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    var leftOperand by remember { mutableStateOf(block.leftOperand) }
    var rightOperand by remember { mutableStateOf(block.rightOperand) }
    var operator by remember { mutableStateOf(block.operator) }
    var showInnerBlocks by remember { mutableStateOf(false) }

    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    fun updateCondition() {
        onUpdate(block.copy(
            leftOperand = leftOperand,
            operator = operator,
            rightOperand = rightOperand
        ))
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = { Button(onClick = { showErrorDialog = false }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.variableExists)) }
        )
    }

    if (showDialog) {
        val duplicate = block.innerBlocks.filterIsInstance<VariableBlock>().any { it.name == newVarName }
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                newVarName = ""
            },
            confirmButton = {
                Button(onClick = {
                    if (duplicate) {
                        showErrorDialog = true
                    } else {
                        onUpdate(
                            block.copy(
                                innerBlocks = block.innerBlocks + VariableBlock(
                                    id = nextId,
                                    name = newVarName,
                                    value = "0"
                                )
                            )
                        )
                        onIdIncrement()
                        showDialog = false
                        newVarName = ""
                    }
                }) { Text(stringResource(R.string.addBlock)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newVarName = ""
                }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.variableName)) },
            text = {
                OutlinedTextField(
                    value = newVarName,
                    onValueChange = { newVarName = it },
                    label = { Text(stringResource(R.string.variableName)) }
                )
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄 While", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = leftOperand,
                    onValueChange = {
                        leftOperand = it
                        updateCondition()
                    },
                    label = { Text(stringResource(R.string.leftOperand)) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Button(onClick = { operatorMenuExpanded = true }) {
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
                                    updateCondition()
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
                        updateCondition()
                    },
                    label = { Text(stringResource(R.string.rightOperand)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stringResource(R.string.loopBody)} (${block.innerBlocks.size})", 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showInnerBlocks = !showInnerBlocks }) {
                    Icon(
                        if (showInnerBlocks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showInnerBlocks) stringResource(R.string.hideBlocks) else stringResource(R.string.showBlocks)
                    )
                }
            }

            AnimatedVisibility(visible = showInnerBlocks) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(start = 16.dp)
                    ) {
                        items(block.innerBlocks) { child ->
                            val updateInner = { updated: CodeBlock ->
                                onUpdate(block.copy(innerBlocks = block.innerBlocks.map {
                                    if (it.id == updated.id) updated else it
                                }))
                            }
                            val removeInner = {
                                onUpdate(block.copy(innerBlocks = block.innerBlocks.filter {
                                    it.id != child.id
                                }))
                            }

                            when (child) {
                                is VariableBlock -> VariableBlockView(
                                    block = child,
                                    onValueChange = { v -> updateInner(child.copy(value = v)) },
                                    onRemove = removeInner,
                                    variablesMap = variablesMap
                                )
                                is AssignmentBlock -> AssignmentBlockView(child, updateInner, removeInner, variablesMap)
                                is PrintBlock -> PrintBlockView(child, updateInner, removeInner, variablesMap)
                                is IfElseBlock -> IfElseBlockView(
                                    child,
                                    updateInner,
                                    removeInner,
                                    { parentId, newBlock, isThen ->
                                        val updatedInner = if (isThen) {
                                            child.thenBlocks + newBlock
                                        } else {
                                            child.elseBlocks + newBlock
                                        }
                                        updateInner(child.copy(
                                            thenBlocks = if (isThen) updatedInner else child.thenBlocks,
                                            elseBlocks = if (!isThen) updatedInner else child.elseBlocks
                                        ))
                                    },
                                    variablesMap,
                                    nextId,
                                    onIdIncrement
                                )
                                is WhileBlock -> WhileBlockView(
                                    child,
                                    updateInner,
                                    removeInner,
                                    variablesMap,
                                    nextId,
                                    onIdIncrement
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.addVariable))
                        }

                        Button(
                            onClick = {
                                onUpdate(block.copy(innerBlocks = block.innerBlocks + AssignmentBlock(nextId, "", "")))
                                onIdIncrement()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.addAssignment))
                        }

                        Button(
                            onClick = {
                                onUpdate(block.copy(innerBlocks = block.innerBlocks + PrintBlock(nextId)))
                                onIdIncrement()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.addPrint))
                        }

                        Button(
                            onClick = {
                                onUpdate(block.copy(innerBlocks = block.innerBlocks + IfElseBlock(nextId)))
                                onIdIncrement()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.addIfElse))
                        }

                        Button(
                            onClick = {
                                onUpdate(block.copy(innerBlocks = block.innerBlocks + WhileBlock(nextId)))
                                onIdIncrement()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.addWhile))
                        }
                    }
                }
            }
        }
    }
}
