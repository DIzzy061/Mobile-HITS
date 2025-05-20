package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.PrintBlock
import com.example.codeblockhits.data.evaluateExpression

@Composable
fun PrintBlockView(
    block: PrintBlock,
    onUpdate: (PrintBlock) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var expressions by remember { mutableStateOf(block.expressions) }
    val computedValues = remember(expressions, variablesMap) {
        expressions.map { evaluateExpression(it, variablesMap) }
    }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🖨️ Print",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Expression fields
                expressions.forEachIndexed { index, expr ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = expr,
                            onValueChange = { newValue ->
                                val newExpressions = expressions.toMutableList()
                                newExpressions[index] = newValue
                                expressions = newExpressions
                                onUpdate(block.copy(expressions = newExpressions))
                            },
                            label = { Text("Выражение ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val newExpressions = expressions.toMutableList()
                                newExpressions.removeAt(index)
                                if (newExpressions.isEmpty()) {
                                    newExpressions.add("")
                                }
                                expressions = newExpressions
                                onUpdate(block.copy(expressions = newExpressions))
                            },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Удалить выражение",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (index < expressions.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Add expression button
                Button(
                    onClick = {
                        val newExpressions = expressions.toMutableList()
                        newExpressions.add("")
                        expressions = newExpressions
                        onUpdate(block.copy(expressions = newExpressions))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить выражение",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить выражение")
                }

                // Show computed values
                if (computedValues.any { it.isNotEmpty() }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Результат: ${computedValues.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
} 