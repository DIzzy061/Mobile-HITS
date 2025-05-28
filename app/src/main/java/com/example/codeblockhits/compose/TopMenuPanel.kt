package com.example.codeblockhits.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R

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
    val addPrintText = stringResource(R.string.addPrint)
    var selectedOption by remember { mutableStateOf(selectBlockText) }
    var inputText by remember { mutableStateOf("") }

    val isDarkTheme = isSystemInDarkTheme()

    val headerBackgroundColor =
        if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer
    else Color(0xFFE3F2FD)

    val headerTextColor =
        if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer
        else Color(0xFF1565C0)

    val menuBackgroundColor =
        if (isDarkTheme) MaterialTheme.colorScheme.surface
        else Color(0xFFF5F5F5)

    val fabColor =
        if (isDarkTheme) MaterialTheme.colorScheme.tertiary
        else Color(0xFF26A69A)

    val fabContentColor =
        if (isDarkTheme) MaterialTheme.colorScheme.onTertiary
        else Color.White

    val evaluateButtonColor =
        if (isDarkTheme) MaterialTheme.colorScheme.primary
        else Color(0xFF42A5F5)

    val evaluateButtonContentColor =
        if (isDarkTheme) MaterialTheme.colorScheme.onPrimary
        else Color.White

    val arrowButtonColor =
        if (isDarkTheme) MaterialTheme.colorScheme.secondary
        else Color(0xFFFF7043)

    val arrowButtonContentColor =
        if (isDarkTheme) MaterialTheme.colorScheme.onSecondary
        else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = headerBackgroundColor,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = stringResource(R.string.appName),
                style = MaterialTheme.typography.titleMedium,
                color = headerTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(40.dp),
                    containerColor = fabColor,
                    contentColor = fabContentColor,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEvaluateAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = evaluateButtonColor,
                        contentColor = evaluateButtonContentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.calculate))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onArrowMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = arrowButtonColor,
                        contentColor = arrowButtonContentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.addBlock))
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.addBlock), style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            stringResource(R.string.variable),
                            stringResource(R.string.ifElse),
                            stringResource(R.string.addAssignment),
                            stringResource(R.string.addWhile)
                        ).forEach { label ->
                            OutlinedButton(
                                onClick = {
                                    when (label) {
                                        "Variable" -> selectedOption = label
                                        "If/Else" -> {
                                            onAddIfElse()
                                            selectedOption = "Select Block"
                                        }
                                        "Assignment" -> {
                                            onAddAssignment("", "")
                                            selectedOption = "Select Block"
                                        }
                                        "Array" -> selectedOption = label
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onAddPrint()
                                selectedOption = "Select Block"
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.addPrint))
                        }

                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            enabled = false
                        ) {
                            Text("Coming Soon")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.variable)) {
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
                }
            )
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.ifElse)) {
            SimpleAddButtonCard {
                onAddIfElse()
                selectedOption = selectBlockText
            }
        }

        AnimatedVisibility(visible = selectedOption == stringResource(R.string.addAssignment)) {
            SimpleAddButtonCard {
                onAddAssignment("", "")
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
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.addBlock))
        }
    }
}
