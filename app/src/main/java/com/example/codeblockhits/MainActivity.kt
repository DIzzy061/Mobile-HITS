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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextFieldDefaults

sealed interface CodeBlock {
    val id: Int
}

data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String = ""
) : CodeBlock

data class AssignmentBlock(
    override val id: Int,
    val variableName: String,
    val expression: String = ""
): CodeBlock

data class IfElseBlock(
    override val id: Int,
    val leftOperand: String = "",
    val operator: String = "==",
    val rightOperand: String = "",
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
        topBar = {
            TopMenuPanel(
                onAddVariable = { name ->
                    blocks = blocks + VariableBlock(id = nextId++, name = name)
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                },
                onAddAssignment = {
                    blocks = blocks + AssignmentBlock(id = nextId++, variableName = "var${nextId}")
                }
            )
        }
    )  { paddingValues ->
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
fun TopMenuPanel(
    onAddVariable: (String) -> Unit,
    onAddIfElse: () -> Unit,
    onAddAssignment: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var variableName by remember { mutableStateOf("") }
    var showVariableDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp , 20.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = (stringResource(R.string.Select_Block)),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp)
            )

            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(stringResource(R.string.Add_Block))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {(stringResource(R.string.Variable))},
                        onClick = {
                            showVariableDialog = true
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { (stringResource(R.string.If_Else)) },
                        onClick = {
                            onAddIfElse()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {(stringResource(R.string.Assignment))},
                        onClick = {
                            onAddAssignment()
                            expanded=false
                        }
                    )
                }
            }
        }
    }

    if (showVariableDialog) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = variableName,
                    onValueChange = { variableName = it },
                    label = { (stringResource(R.string.Variable_Name))},
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showVariableDialog = false },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.Cansel))
                    }
                    Button(
                        onClick = {
                            if (variableName.isNotBlank()) {
                                onAddVariable(variableName)
                                variableName = ""
                                showVariableDialog = false
                            }
                        }
                    ) {
                        Text((stringResource(R.string.Create)))
                    }
                }
            }
        }
    }
}

@Composable
fun VariableBlockView(
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🧩 ${block.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onRemove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentValue,
                    onValueChange = {
                        currentValue = it
                        onValueChange(it)
                    },
                    label = { Text((stringResource(R.string.Variable_Meaning))) },
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
fun AssignmentBlockView(
    block: AssignmentBlock,
    modifier: Modifier = Modifier,
    onExpressionChange: (String) -> Unit = {},
    onRemove: () -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var currentExpr by remember { mutableStateOf(block.expression) }

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
                .width(250.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 ${block.variableName} =",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onRemove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentExpr,
                    onValueChange = {
                        currentExpr = it
                        onExpressionChange(it)
                    },
                    label = { (Text(stringResource(R.string.Expression))) },
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
fun IfElseBlockView(
    block: IfElseBlock,
    modifier: Modifier = Modifier,
    onUpdate: (IfElseBlock) -> Unit,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    val operators = listOf("==", "!=", "<", ">", "<=", ">=")

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
        Card(modifier = Modifier.width(250.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IF", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = block.leftOperand,
                        onValueChange = { newLeft ->
                            onUpdate(block.copy(leftOperand = newLeft))
                        },
                        modifier = Modifier.width(60.dp),
                        label = { Text("Var") }
                    )

                    Box {
                        Text(
                            text = block.operator,
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            operators.forEach { op ->
                                DropdownMenuItem(
                                    text = { Text(op) },
                                    onClick = {
                                        onUpdate(block.copy(operator = op))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = block.rightOperand,
                        onValueChange = { newRight ->
                            onUpdate(block.copy(rightOperand = newRight))
                        },
                        modifier = Modifier.width(60.dp),
                        label = { Text("Value") }
                    )

                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, "Remove")
                    }
                }

                Text("THEN:", style = MaterialTheme.typography.labelMedium)
                CodeBlocksList(
                    blocks = block.thenBlocks,
                    onRemove = { },
                    onUpdate = { }
                )

                Text("ELSE:", style = MaterialTheme.typography.labelMedium)
                CodeBlocksList(
                    blocks = block.elseBlocks,
                    onRemove = { },
                    onUpdate = { }
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
                is VariableBlock -> VariableBlockView(
                    block = block,
                    onRemove = { onRemove(block.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                is AssignmentBlock -> AssignmentBlockView(
                    block = block,
                    onExpressionChange = { newExpr ->
                        onUpdate(block.copy(expression = newExpr))
                    },
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeBlockHITSTheme {
        MainScreen()
    }
}