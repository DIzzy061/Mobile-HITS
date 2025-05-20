package com.example.codeblockhits.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.ArrayBlock
import com.example.codeblockhits.data.CodeBlock

@Composable
fun ArrayBlockView(
    block: ArrayBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(block.name) }
    var sizeText by remember { mutableStateOf(block.size.toString()) }

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(Color(0xFFD1C4E9), RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📦 Array",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить массив",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onUpdate(block.copy(name = name))
                },
                label = { Text("Array name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = sizeText,
                onValueChange = {
                    sizeText = it
                    val newSize = it.toIntOrNull()
                    if (newSize != null && newSize > 0) {
                        val newValues = MutableList(newSize) { i -> block.values.getOrNull(i) ?: "0" }
                        onUpdate(block.copy(size = newSize, values = newValues))
                    }
                },
                label = { Text("Array size") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
