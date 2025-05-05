package com.example.codeblockhits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.ui.theme.CodeBlockHITSTheme
import kotlin.math.roundToInt

data class VariableBlock(
    val id: Int,
    val name: String,
    val value: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeBlockHITSTheme {
                var blocks by remember { mutableStateOf<List<VariableBlock>>(emptyList()) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        content = { padding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            ) {
                                TopPanel(onAddBlock = { name ->
                                    val newBlock = VariableBlock(
                                        id = blocks.size,
                                        name = name
                                    )
                                    blocks = blocks + newBlock
                                })

                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    )

                    blocks.forEach { block ->
                        BlockField(
                            block = block,
                            onRemove = { blocks = blocks.filter { it.id != block.id } },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopPanel(onAddBlock: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText -> text = newText },
                label = { Text("Имя переменной") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onAddBlock(text)
                        text = ""
                    }
                },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Добавить")
            }
        }
    }
}

@Composable
fun BlockField(
    block: VariableBlock,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    onRemove: () -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var currentValue by remember { mutableStateOf(block.value) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Card(
            modifier = Modifier
                .width(200.dp)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = block.name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить"
                        )
                    }
                }
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = {
                        currentValue = it
                        onValueChange(it)
                    },
                    label = { Text("Значение") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeBlockHITSTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                TopPanel(onAddBlock = {})
                BlockField(
                    block = VariableBlock(1, "Пример переменной", "42"),
                    onRemove = {}
                )
            }
        }
    }
}