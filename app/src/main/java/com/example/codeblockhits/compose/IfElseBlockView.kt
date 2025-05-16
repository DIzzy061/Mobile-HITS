package com.example.codeblockhits.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R
import com.example.codeblockhits.data.*
import kotlin.math.roundToInt

@Composable
fun IfElseBlockView(
    block: IfElseBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, String>,
    nextId: Int,
    onIdIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    var leftOperand by remember { mutableStateOf(block.leftOperand) }
    var rightOperand by remember { mutableStateOf(block.rightOperand) }
    var operator by remember { mutableStateOf(block.operator) }
    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val animatedX by animateFloatAsState(targetValue = offsetX, label = "")
    val animatedY by animateFloatAsState(targetValue = offsetY, label = "")

    var showDialog by remember { mutableStateOf(false) }
    var dialogTargetThen by remember { mutableStateOf(true) }
    var newVarName by remember { mutableStateOf("") }

    val targetList = if (dialogTargetThen) block.thenBlocks else block.elseBlocks
    val duplicate = targetList.filterIsInstance<VariableBlock>().any { it.name == newVarName }

    var showErrorDialog by remember { mutableStateOf(false) }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.OK))
                }
            },
            title = { Text(stringResource(R.string.Error)) },
            text = { Text(stringResource(R.string.This_Block_Has_Variable_Whith_This_Name)) }
        )
    }

    if (showDialog) {
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
                        onAddToIfElse(
                            block.id,
                            VariableBlock(id = nextId, name = newVarName, value = "0"),
                            dialogTargetThen
                        )
                        onIdIncrement()
                        showDialog = false
                        newVarName = ""
                    }
                }) {
                    Text(stringResource(R.string.Add_Block))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newVarName = ""
                }) {
                    Text(stringResource(R.string.Cansel))
                }
            },
            title = { Text(stringResource(R.string.Variable_Name))},
            text = {
                OutlinedTextField(
                    value = newVarName,
                    onValueChange = { newVarName = it },
                    label = { Text(stringResource(R.string.Variable_Name)) }
                )
            }
        )
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔀 If/Else",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = leftOperand,
                        onValueChange = {
                            leftOperand = it
                            onUpdate(block.copy(leftOperand = it, operator = operator, rightOperand = rightOperand))
                        },
                        label = { Text(stringResource(R.string.Left_Operand)) },
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
                                        onUpdate(block.copy(leftOperand = leftOperand, operator = op, rightOperand = rightOperand))
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
                            onUpdate(block.copy(leftOperand = leftOperand, operator = operator, rightOperand = it))
                        },
                        label = { Text(stringResource(R.string.Right_Operand)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Then блоки (${block.thenBlocks.size})", color = Color.Green)
                Column {
                    block.thenBlocks.forEach { childBlock ->
                        when (childBlock) {
                            is VariableBlock -> VariableBlockView(
                                block = childBlock,
                                onValueChange = { newValue ->
                                    val updated = childBlock.copy(value = newValue)
                                    val newThenBlocks = block.thenBlocks.map {
                                        if (it.id == updated.id) updated else it
                                    }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                onRemove = {
                                    val newThenBlocks = block.thenBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                variablesMap = variablesMap
                            )
                            is IfElseBlock -> IfElseBlockView(
                                block = childBlock,
                                onUpdate = { updatedChild ->
                                    val newThenBlocks = block.thenBlocks.map {
                                        if (it.id == updatedChild.id) updatedChild else it
                                    }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                onRemove = {
                                    val newThenBlocks = block.thenBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                onAddToIfElse = onAddToIfElse,
                                variablesMap = variablesMap,
                                nextId = nextId,
                                onIdIncrement = onIdIncrement
                            )
                            is AssignmentBlock -> AssignmentBlockView(
                                block = childBlock,
                                onUpdate = { updatedChild ->
                                    val newThenBlocks = block.thenBlocks.map {
                                        if (it.id == updatedChild.id) updatedChild else it
                                    }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                onRemove = {
                                    val newThenBlocks = block.thenBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(thenBlocks = newThenBlocks))
                                },
                                variablesMap = variablesMap
                            )
                        }
                    }
                    Button(onClick = {
                        dialogTargetThen = true
                        showDialog = true
                    }, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.Add_Block_To_Then))
                    }
                    Button(onClick = {
                        onAddToIfElse(block.id, AssignmentBlock(id = nextId, target = "", expression = ""), true)
                        onIdIncrement()
                    }, modifier = Modifier.padding(top = 4.dp)) {
                        Text(stringResource(R.string.Add_Assignment_To_Then))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Else блоки (${block.elseBlocks.size})", color = Color.Red)
                Column {
                    block.elseBlocks.forEach { childBlock ->
                        when (childBlock) {
                            is VariableBlock -> VariableBlockView(
                                block = childBlock,
                                onValueChange = { newValue ->
                                    val updated = childBlock.copy(value = newValue)
                                    val newElseBlocks = block.elseBlocks.map {
                                        if (it.id == updated.id) updated else it
                                    }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                onRemove = {
                                    val newElseBlocks = block.elseBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                variablesMap = variablesMap
                            )
                            is IfElseBlock -> IfElseBlockView(
                                block = childBlock,
                                onUpdate = { updatedChild ->
                                    val newElseBlocks = block.elseBlocks.map {
                                        if (it.id == updatedChild.id) updatedChild else it
                                    }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                onRemove = {
                                    val newElseBlocks = block.elseBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                onAddToIfElse = onAddToIfElse,
                                variablesMap = variablesMap,
                                nextId = nextId,
                                onIdIncrement = onIdIncrement
                            )
                            is AssignmentBlock -> AssignmentBlockView(
                                block = childBlock,
                                onUpdate = { updatedChild ->
                                    val newElseBlocks = block.elseBlocks.map {
                                        if (it.id == updatedChild.id) updatedChild else it
                                    }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                onRemove = {
                                    val newElseBlocks = block.elseBlocks.filter { it.id != childBlock.id }
                                    onUpdate(block.copy(elseBlocks = newElseBlocks))
                                },
                                variablesMap = variablesMap
                            )
                        }
                    }
                    Button(onClick = {
                        dialogTargetThen = false
                        showDialog = true
                    }, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.Add_Block_To_Else))
                    }
                    Button(onClick = {
                        onAddToIfElse(block.id, AssignmentBlock(id = nextId, target = "", expression = ""), false)
                        onIdIncrement()
                    }, modifier = Modifier.padding(top = 4.dp)) {
                        Text(stringResource(R.string.Add_Assignment_To_Else))
                    }
                }
            }
        }
    }
}