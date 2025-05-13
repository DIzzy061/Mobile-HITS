package com.example.codeblockhits.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.compose.CodeBlocksList
import com.example.codeblockhits.data.*
import kotlin.math.roundToInt

@Composable
fun IfElseBlockView(
    block: IfElseBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
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
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                        label = { Text("Левый операнд") },
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
                        label = { Text("Правый операнд") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Тело Then-блока (${block.thenBlocks.size} блоков)", color = Color.Green)
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
                                variablesMap = emptyMap()
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
                                onAddToIfElse = onAddToIfElse
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                Text("Тело Else-блока (${block.elseBlocks.size} блоков)", color = Color.Red)
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
                                variablesMap = emptyMap()
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
                                onAddToIfElse = onAddToIfElse
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Удалить If/Else блок")
                }
            }
        }
    }}
