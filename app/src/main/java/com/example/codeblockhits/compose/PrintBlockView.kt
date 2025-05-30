package com.example.codeblockhits.compose

import com.example.codeblockhits.data.VariableValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R
import com.example.codeblockhits.data.PrintBlock
import com.example.codeblockhits.data.evaluateExpression
import androidx.compose.foundation.BorderStroke

@Composable
fun PrintBlockView(
    block: PrintBlock,
    onUpdate: (PrintBlock) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, VariableValue>,
    modifier: Modifier = Modifier
) {
    var expressions by remember { mutableStateOf(block.expressions) }
    val computedValues = remember(expressions, variablesMap) {
        expressions.map { evaluateExpression(it, variablesMap) }
    }

    Card(
        modifier = modifier
            .width(280.dp)
            .padding(0.dp),
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
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🖨️ ${stringResource(R.string.addPrint)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                            label = { Text("${stringResource(R.string.expression)} ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
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
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (index < expressions.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val newExpressions = expressions.toMutableList()
                        newExpressions.add("")
                        expressions = newExpressions
                        onUpdate(block.copy(expressions = newExpressions))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.addBlock),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.addBlock))
                }

                if (computedValues.any { it.isNotEmpty() }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${stringResource(R.string.programOutput)}: ${
                            computedValues.joinToString(
                                ", "
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
} 