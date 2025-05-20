package com.example.codeblockhits.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

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
    var variableLabel = "Variable"

    val isDarkTheme = isSystemInDarkTheme()

    val headerBackgroundColor = if (isDarkTheme)
        MaterialTheme.colorScheme.primaryContainer
    else
        Color(0xFFE3F2FD)

    val headerTextColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        Color(0xFF1565C0)

    val menuBackgroundColor = if (isDarkTheme)
        MaterialTheme.colorScheme.surface
    else
        Color(0xFFF5F5F5)

    val fabColor = if (isDarkTheme)
        MaterialTheme.colorScheme.tertiary
    else
        Color(0xFF26A69A)

    val fabContentColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onTertiary
    else
        Color.White

    val evaluateButtonColor = if (isDarkTheme)
        MaterialTheme.colorScheme.primary
    else
        Color(0xFF42A5F5)

    val evaluateButtonContentColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onPrimary
    else
        Color.White

    val arrowButtonColor = if (isDarkTheme)
        MaterialTheme.colorScheme.secondary
    else
        Color(0xFFFF7043)

    val arrowButtonContentColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSecondary
    else
        Color.White

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
                        contentDescription = if (expanded) "Collapse menu" else "Expand menu",
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
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Evaluate All",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Добавить стрелку",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Добавить стрелку",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Add New Block",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedOption = variableLabel
                                expanded = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Variable",
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                selectedOption = "If/Else"
                                expanded = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "If/Else",
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                selectedOption = "Assignment"
                                expanded = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Assignment",
                                maxLines = 1
                            )
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
                            Text(
                                text = "Print",
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            enabled = false
                        ) {
                            Text(
                                text = "Coming Soon",
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            enabled = false
                        ) {
                            Text(
                                text = "Coming Soon",
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == variableLabel) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                        onValueChange = { inputText = it },
                        label = { Text("Variable Name") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onAddVariable(inputText.trim())
                                inputText = ""
                                selectedOption = "Select Block"
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Block")
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == "If/Else") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onAddIfElse()
                            selectedOption = "Select Block"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Block")
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == "Assignment") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onAddAssignment("", "")
                            selectedOption = "Select Block"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Block")
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedOption == "Print") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onAddPrint()
                            selectedOption = "Select Block"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Block")
                    }
                }
            }
        }
    }
}