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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TopMenuPanel(
    onAddVariable: (String) -> Unit,
    onAddIfElse: () -> Unit,
    onAddAssignment: (String, String) -> Unit,
    onAddPrint: () -> Unit,
    onEvaluateAll: () -> Unit,
    onArrowMode: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Select Block") }
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


    var arrayName by remember { mutableStateOf("") }
    var arraySizeText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = headerBackgroundColor,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "Visual Programming Workspace",
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
                    Text("Evaluate All")
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
                    Text("Добавить стрелку")
                }
            }
        }

        // ▼▼▼ Dropdown Block Menu ▼▼▼
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Block", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Variable", "If/Else", "Assignment", "Array").forEach { label ->
                            OutlinedButton(
                                onClick = {
                                    selectedOption = label
                                    expanded = false
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
                                selectedOption = "Print"
                                expanded = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Print")
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

        AnimatedVisibility(visible = selectedOption == "Variable") {
            BlockEntryCard(
                label = "Variable Name",
                inputText = inputText,
                onInputChange = { inputText = it },
                onSubmit = {
                    if (inputText.isNotBlank()) {
                        onAddVariable(inputText.trim())
                        inputText = ""
                        selectedOption = "Select Block"
                    }
                }
            )
        }

        AnimatedVisibility(visible = selectedOption == "If/Else") {
            SimpleAddButtonCard {
                onAddIfElse()
                selectedOption = "Select Block"
            }
        }

        AnimatedVisibility(visible = selectedOption == "Assignment") {
            SimpleAddButtonCard {
                onAddAssignment("", "")
                selectedOption = "Select Block"
            }
        }

        AnimatedVisibility(visible = selectedOption == "Array") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = arrayName,
                        onValueChange = { arrayName = it },
                        label = { Text("Array name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = arraySizeText,
                        onValueChange = { arraySizeText = it },
                        label = { Text("Array size") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val size = arraySizeText.toIntOrNull()
                            if (arrayName.isNotBlank() && size != null && size > 0) {
                                onAddVariable("$arrayName:$size")
                                selectedOption = "Select Block"
                                arrayName = ""
                                arraySizeText = ""
                            }
                        },
                        enabled = arrayName.isNotBlank() && arraySizeText.toIntOrNull() != null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add Array")
                    }
                }
            }
        }


        AnimatedVisibility(visible = selectedOption == "Print") {
            SimpleAddButtonCard {
                onAddPrint()
                selectedOption = "Select Block"
            }
        }
    }
}

@Composable
private fun BlockEntryCard(
    label: String,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                label = { Text(label) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = onSubmit,
                enabled = inputText.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Block")
            }
        }
    }
}

@Composable
private fun SimpleAddButtonCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onClick, shape = RoundedCornerShape(8.dp)) {
                Text("Add Block")
            }
        }
    }
}
