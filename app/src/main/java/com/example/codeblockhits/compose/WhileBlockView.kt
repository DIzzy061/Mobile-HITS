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
fun WhileBlockView(
    block: WhileBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, VariableValue>,
    modifier: Modifier = Modifier
) {
    var leftOperand by remember { mutableStateOf("") }
    var rightOperand by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("!=") }

    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(block.condition) {
        val match = Regex("""(.+?)\s*(==|!=|>|<|>=|<=)\s*(.+)""").find(block.condition)
        if (match != null) {
            val (left, op, right) = match.destructured
            leftOperand = left
            operator = op
            rightOperand = right
        } else {
            leftOperand = ""
            operator = "!="
            rightOperand = "0"
        }
    }

    fun updateCondition() {
        onUpdate(block.copy(condition = "$leftOperand $operator $rightOperand"))
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = { Button(onClick = { showErrorDialog = false }) { Text("OK") } },
            title = { Text("Error") },
            text = { Text("Variable with this name already exists") }
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
                                    id = block.innerBlocks.maxOfOrNull { it.id }?.plus(1) ?: 1000,
                                    name = newVarName,
                                    value = "0"
                                )
                            )
                        )
                        showDialog = false
                        newVarName = ""
                    }
                }) { Text("Add Block") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newVarName = ""
                }) { Text("Cancel") }
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
                Text("🔄 While", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = leftOperand,
                    onValueChange = {
                        leftOperand = it
                        updateCondition()
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
                    label = { Text("Right Operand") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Loop body (${block.innerBlocks.size})", style = MaterialTheme.typography.labelLarge)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                block.innerBlocks.forEach { child ->
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
                        is VariableBlock -> VariableBlockView(child, { v -> updateInner(child.copy(value = v)) }, removeInner, variablesMap)
                        is AssignmentBlock -> AssignmentBlockView(child, updateInner, removeInner, variablesMap)
                        is PrintBlock -> PrintBlockView(child, updateInner, removeInner, variablesMap)
                        is IfElseBlock -> IfElseBlockView(child, updateInner, removeInner, { _, _, _ -> }, variablesMap, 9999, {})
                        is WhileBlock -> WhileBlockView(child, updateInner, removeInner, variablesMap)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) { 
                        Text("Add Variable") 
                    }

                    Button(
                        onClick = {
                    val id = block.innerBlocks.maxOfOrNull { it.id }?.plus(1) ?: 1000
                    onUpdate(block.copy(innerBlocks = block.innerBlocks + AssignmentBlock(id, "", "")))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Text("Add Assignment")
                }

                    Button(
                        onClick = {
                    val id = block.innerBlocks.maxOfOrNull { it.id }?.plus(1) ?: 1000
                    onUpdate(block.copy(innerBlocks = block.innerBlocks + PrintBlock(id)))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Text("Add Print")
                }

                    Button(
                        onClick = {
                    val id = block.innerBlocks.maxOfOrNull { it.id }?.plus(1) ?: 1000
                    onUpdate(block.copy(innerBlocks = block.innerBlocks + IfElseBlock(id)))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Text("Add If/Else")
                }

                    Button(
                        onClick = {
                    val id = block.innerBlocks.maxOfOrNull { it.id }?.plus(1) ?: 1000
                    onUpdate(block.copy(innerBlocks = block.innerBlocks + WhileBlock(id, condition = "1")))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Text("Add While")
                    }
                }
            }
        }
    }
}
