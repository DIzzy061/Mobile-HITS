package com.example.codeblockhits.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke

@Composable
fun TopMenuPanel(
    onAddVariable: (String) -> Unit,
    onAddIfElse: () -> Unit,
    onAddAssignment: (String, String) -> Unit,
    onAddPrint: () -> Unit,
    onEvaluateAll: () -> Unit,
    onArrowMode: () -> Unit,
    onAddWhile: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectBlockText = stringResource(R.string.selectBlock)
    val variableText = stringResource(R.string.addVariable)
    val ifElseText = stringResource(R.string.addIfElse)
    val assignmentText = stringResource(R.string.addAssignment)
    val whileText = stringResource(R.string.addWhile)
    val printText = stringResource(R.string.addPrint)
    var selectedOption by remember { mutableStateOf(selectBlockText) }
    var inputText by remember { mutableStateOf("") }

    val headerBackgroundColor = MaterialTheme.colorScheme.primaryContainer

    val headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    val menuBackgroundColor = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = headerBackgroundColor,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = stringResource(R.string.appName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = headerTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = menuBackgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.addBlock))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEvaluateAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.startProgram))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onArrowMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.connect))
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.addBlock),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            variableText,
                            ifElseText,
                            printText,
                            whileText
                        ).forEach { label ->
                            Button(
                                onClick = {
                                    when (label) {
                                        variableText -> selectedOption = label
                                        ifElseText -> {
                                            onAddIfElse()
                                            selectedOption = selectBlockText
                                        }

                                        printText -> {
                                            onAddPrint()
                                            selectedOption = selectBlockText
                                        }

                                        whileText -> {
                                            onAddWhile()
                                            selectedOption = selectBlockText
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 72.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                Text(label, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onAddAssignment("", "")
                                selectedOption = selectBlockText
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 72.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(assignmentText, textAlign = TextAlign.Center)
                        }

                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 72.dp),
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.12f
                                ),
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.12f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f
                                )
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                stringResource(R.string.comingSoon),
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.addVariable)) {
            BlockEntryCard(
                label = stringResource(R.string.variableName),
                inputText = inputText,
                onInputChange = { inputText = it },
                onSubmit = {
                    if (inputText.isNotBlank()) {
                        onAddVariable(inputText.trim())
                        inputText = ""
                        selectedOption = selectBlockText
                    }
                },
                onCancel = {
                    inputText = ""
                    selectedOption = selectBlockText
                }
            )
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.addIfElse)) {
            SimpleAddButtonCard {
                onAddIfElse()
                selectedOption = selectBlockText
            }
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.addWhile)) {
            SimpleAddButtonCard {
                onAddWhile()
                selectedOption = selectBlockText
            }
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.addPrint)) {
            SimpleAddButtonCard {
                onAddPrint()
                selectedOption = selectBlockText
            }
        }
    }
}

@Composable
fun BlockEntryCard(
    label: String,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.addBlock))
            }
        }
    }
}

@Composable
fun SimpleAddButtonCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(stringResource(R.string.addBlock))
        }
    }
}
