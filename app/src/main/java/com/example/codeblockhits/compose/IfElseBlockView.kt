package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*

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

    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTargetThen by remember { mutableStateOf(true) }
    var newVarName by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val duplicate = (if (dialogTargetThen) block.thenBlocks else block.elseBlocks)
            .filterIsInstance<VariableBlock>()
            .any { it.name == newVarName }

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
                    Text("Add Block")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newVarName = ""
                }) {
                    Text("Cancel")
                }
            },
            title = { Text("Variable Name") },
            text = {
                OutlinedTextField(
                    value = newVarName,
                    onValueChange = { newVarName = it },
                    label = { Text("Variable Name") }
                )
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text("Variable with this name already exists") }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔀 If/Else", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = leftOperand,
                    onValueChange = {
                        leftOperand = it
                        onUpdate(block.copy(leftOperand = it, operator = operator, rightOperand = rightOperand))
                    },
                    label = { Text("Left Operand") },
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
                    label = { Text("Right Operand") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            RenderBlockColumn(
                title = "Then blocks (${block.thenBlocks.size})",
                blocks = block.thenBlocks,
                parentBlock = block,
                isThen = true,
                onUpdate = onUpdate,
                onAddToIfElse = onAddToIfElse,
                variablesMap = variablesMap,
                nextId = nextId,
                onIdIncrement = onIdIncrement,
                onAddVariable = {
                    dialogTargetThen = true
                    showDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            RenderBlockColumn(
                title = "Else blocks (${block.elseBlocks.size})",
                blocks = block.elseBlocks,
                parentBlock = block,
                isThen = false,
                onUpdate = onUpdate,
                onAddToIfElse = onAddToIfElse,
                variablesMap = variablesMap,
                nextId = nextId,
                onIdIncrement = onIdIncrement,
                onAddVariable = {
                    dialogTargetThen = false
                    showDialog = true
                }
            )
        }
    }
}

@Composable
private fun RenderBlockColumn(
    title: String,
    blocks: List<CodeBlock>,
    parentBlock: IfElseBlock,
    isThen: Boolean,
    onUpdate: (CodeBlock) -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, VariableValue>,
    nextId: Int,
    onIdIncrement: () -> Unit,
    onAddVariable: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
) {
        Text(title, color = if (isThen) Color.Green else Color.Red)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            blocks.forEach { childBlock ->
                val updateList = { updated: CodeBlock ->
                    val newList = blocks.map { if (it.id == updated.id) updated else it }
                    if (isThen)
                        onUpdate(parentBlock.copy(thenBlocks = newList))
                    else
                        onUpdate(parentBlock.copy(elseBlocks = newList))
                }

                val removeBlock = {
                    val newList = blocks.filter { it.id != childBlock.id }
                    if (isThen)
                        onUpdate(parentBlock.copy(thenBlocks = newList))
                    else
                        onUpdate(parentBlock.copy(elseBlocks = newList))
                }

                when (childBlock) {
                    is VariableBlock -> VariableBlockView(
                        block = childBlock,
                        onValueChange = { newValue ->
                            val updated = childBlock.copy(value = newValue)
                            updateList(updated)
                        },
                        onRemove = removeBlock,
                        variablesMap = variablesMap
                    )
                    is AssignmentBlock -> AssignmentBlockView(childBlock, updateList, removeBlock, variablesMap)
                    is PrintBlock -> PrintBlockView(childBlock, updateList, removeBlock, variablesMap)
                    is IfElseBlock -> IfElseBlockView(childBlock, updateList, removeBlock, onAddToIfElse, variablesMap, nextId, onIdIncrement)
                    is WhileBlock -> WhileBlockView(
                        block = childBlock,
                        onUpdate = updateList,
                        onRemove = removeBlock,
                        variablesMap = variablesMap
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAddVariable, modifier = Modifier.padding(top = 4.dp)) {
                Text("Add Variable to ${if (isThen) "Then" else "Else"}")
            }

            Button(onClick = {
                onAddToIfElse(parentBlock.id, AssignmentBlock(id = nextId, target = "", expression = ""), isThen)
                onIdIncrement()
            }, modifier = Modifier.padding(top = 4.dp)) {
                Text("Add Assignment to ${if (isThen) "Then" else "Else"}")
            }

            Button(onClick = {
                onAddToIfElse(parentBlock.id, PrintBlock(id = nextId), isThen)
                onIdIncrement()
            }, modifier = Modifier.padding(top = 4.dp)) {
                Text("Add Print to ${if (isThen) "Then" else "Else"}")
            }

            Button(onClick = {
                onAddToIfElse(
                    parentBlock.id,
                    WhileBlock(id = nextId, condition = "1", innerBlocks = emptyList()),
                    isThen
                )
                onIdIncrement()
            }, modifier = Modifier.padding(top = 4.dp)) {
                Text("Add While to ${if (isThen) "Then" else "Else"}")
            }
            }
        }
    }


