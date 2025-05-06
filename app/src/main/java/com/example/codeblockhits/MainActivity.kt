package com.example.codeblockhits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.ui.theme.CodeBlockHITSTheme
import kotlin.collections.filter
import kotlin.collections.toMutableList
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextFieldDefaults


sealed interface CodeBlock {
    val id: Int
}

data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String = ""
) : CodeBlock

data class IfElseBlock(
    override val id: Int,
    val condition: String = "",
    val thenBlocks: List<CodeBlock> = emptyList(),
    val elseBlocks: List<CodeBlock> = emptyList()
) : CodeBlock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeBlockHITSTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var blocks by remember { mutableStateOf<List<CodeBlock>>(emptyList()) }
    var nextId by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomPanel(
                onAddAssignment = {
                    blocks = blocks + VariableBlock(id = nextId++, name = "var${nextId}")
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                TopPanel(onAddBlock = { name ->
                    blocks = blocks + VariableBlock(id = nextId++, name = name)
                })

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    CodeBlocksList(
                        blocks = blocks,
                        onRemove = { id ->
                            blocks = blocks.filter { it.id != id }
                        },
                        onUpdate = { updated ->
                            blocks = blocks.map { if (it.id == updated.id) updated else it }
                        }
                    )
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
                label = { Text(text = stringResource(R.string.Text_Label)) },
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
                Text(text = stringResource(R.string.Create))
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

    val animatedX by animateFloatAsState(targetValue = offsetX, label = "")
    val animatedY by animateFloatAsState(targetValue = offsetY, label = "")

    Box(
        modifier = modifier
            .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Card(
            modifier = Modifier
                .width(220.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onRemove),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "🧩 ${block.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentValue,
                    onValueChange = {
                        currentValue = it
                        onValueChange(it)
                    },
                    label = { Text("Значение переменной") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}


@Composable
fun BottomPanel(
    onAddAssignment: () -> Unit,
    onAddIfElse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddAssignment) {
                Text("Добавить переменную")
            }
            Button(onClick = onAddIfElse) {
                Text("Add IF-Else")
            }
        }
    }
}


@Composable
fun IfElseBlockView(
    block: IfElseBlock,
    modifier: Modifier = Modifier,
    onUpdate: (IfElseBlock) -> Unit,
    onRemove: () -> Unit
) {
    var condition by remember { mutableStateOf(block.condition) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Card(modifier = Modifier.width(250.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IF", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {
                            condition = it
                            onUpdate(block.copy(condition = it))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Condition") }
                    )
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, "Remove")
                    }
                }

                Text("THEN:", style = MaterialTheme.typography.labelMedium)
                CodeBlocksList(
                    blocks = block.thenBlocks,
                    onRemove = { id ->
                        onUpdate(block.copy(thenBlocks = block.thenBlocks.filter { it.id != id }))
                    },
                    onUpdate = { updated ->
                        val newThen = block.thenBlocks.map { if (it.id == updated.id) updated else it }
                        onUpdate(block.copy(thenBlocks = newThen))
                    },
                    modifier = Modifier.padding(start = 16.dp)
                )

                Text("ELSE:", style = MaterialTheme.typography.labelMedium)
                CodeBlocksList(
                    blocks = block.elseBlocks,
                    onRemove = { id ->
                        onUpdate(block.copy(elseBlocks = block.elseBlocks.filter { it.id != id }))
                    },
                    onUpdate = { updated ->
                        val newElse = block.elseBlocks.map { if (it.id == updated.id) updated else it }
                        onUpdate(block.copy(elseBlocks = newElse))
                    },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CodeBlocksList(
    blocks: List<CodeBlock>,
    onRemove: (Int) -> Unit,
    onUpdate: (CodeBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is VariableBlock -> BlockField(
                    block = block,
                    onRemove = { onRemove(block.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                is IfElseBlock -> IfElseBlockView(
                    block = block,
                    onUpdate = onUpdate,
                    onRemove = { onRemove(block.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}


@Preview(showBackground =true)
@Composable
fun GreetingPreview()
{
    CodeBlockHITSTheme {
    MainScreen()
    }
}
@Preview(showBackground = true)
@Composable
fun BlockFieldPreview() {
    CodeBlockHITSTheme {
        Column {
            TopPanel(onAddBlock = {})
            BlockField(
                block = VariableBlock(1, "Пример переменной", "42"),
                onRemove = {}
            )

        }
    }
}
