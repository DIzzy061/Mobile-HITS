package com.example.codeblockhits.compose

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.R

@Composable
fun TopMenuPanel(
    onAddVariable: (String) -> Unit,
    onAddIfElse: () -> Unit
) {
    var SelectBlock = stringResource(R.string.Select_Block)
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(SelectBlock) }
    var inputText by remember { mutableStateOf("") }
    var variableLabel = stringResource(R.string.Variable)

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(8.dp)) {

        Text(
            text = selectedOption,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box {
            Button(onClick = { expanded = true }) {
                Text(stringResource(R.string.Add_Block))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.Variable)) },
                    onClick = {
                        selectedOption = variableLabel
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("If/Else") },
                    onClick = {
                        selectedOption = "If/Else"
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedOption == "Переменная") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(stringResource(R.string.Variable_Name)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (inputText.isNotBlank()) {
                        onAddVariable(inputText.trim())
                        inputText = ""
                    }
                }) {
                    Text(stringResource(R.string.Add_Block))
                }
            }
        }

        if (selectedOption == "If/Else") {
            Button(onClick = onAddIfElse, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.Add_Block))            }
        }
    }
}