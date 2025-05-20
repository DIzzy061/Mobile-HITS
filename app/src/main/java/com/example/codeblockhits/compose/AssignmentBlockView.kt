package com.example.codeblockhits.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*
import kotlin.math.roundToInt


@Composable
fun AssignmentBlockView(
    block: AssignmentBlock,
    onUpdate: (AssignmentBlock) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var target by remember { mutableStateOf(block.target) }
    var expr by remember { mutableStateOf(block.expression) }

    Box(
        modifier = modifier

    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 Присваивание",
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

                Row {
                    OutlinedTextField(
                        value = target,
                        onValueChange = {
                            target = it
                            onUpdate(block.copy(target = it, expression = expr))
                        },
                        label = { Text("Переменная") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "=",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    OutlinedTextField(
                        value = expr,
                        onValueChange = {
                            expr = it
                            onUpdate(block.copy(target = target, expression = it))
                        },
                        label = { Text("Выражение") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
