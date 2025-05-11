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
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

sealed interface CodeBlock {
    val id: Int
}

data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String = "0"
) : CodeBlock

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
        setContent {
            CodeBlockHITSTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}



fun evaluateExpression(expression: String, variables: Map<String, String>): String {
    return try {

        var processedExpr = expression
        variables.forEach { (name, value) ->
            processedExpr = processedExpr.replace(name, value)
        }


        val result = evaluateMathExpression(processedExpr)
        result.toString()
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}




@Composable
fun MainScreen() {
    var blocks by remember { mutableStateOf<List<CodeBlock>>(emptyList()) }
    var nextId by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun getVariablesMap(): Map<String, String> {
        return blocks.filterIsInstance<VariableBlock>()
            .associate { it.name to it.value }
    }

    Scaffold(
        topBar = {
            TopMenuPanel(
                onAddVariable = { name ->
                    val variableNames = blocks.filterIsInstance<VariableBlock>().map { it.name }
                    if (variableNames.contains(name)) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Переменная с именем '$name' уже существует")
                        }
                    } else {
                        blocks = blocks + VariableBlock(id = nextId++, name = name, value = "0")
                    }
                },
                onAddIfElse = {
                    blocks = blocks + IfElseBlock(id = nextId++)
                }
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
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
                Box(modifier = Modifier.weight(1f)) {
                    CodeBlocksList(
                        blocks = blocks,
                        onRemove = { id ->
                            blocks = blocks.filter { it.id != id }
                        },
                        onUpdate = { updated ->
                            blocks = blocks.map { if (it.id == updated.id) updated else it }
                        },
                        onAddToIfElse = { parentId, block, isThenBlock ->
                            blocks = blocks.map {
                                if (it.id == parentId && it is IfElseBlock) {
                                    if (isThenBlock) {
                                        it.copy(thenBlocks = it.thenBlocks + block)
                                    } else {
                                        it.copy(elseBlocks = it.elseBlocks + block)
                                    }
                                } else {
                                    it
                                }
                            }
                        },
                        variablesMap = getVariablesMap()
                    )
                }

                Button(
                    onClick = {
                        val variablesMap = getVariablesMap()
                        val evaluatedBlocks = mutableListOf<CodeBlock>()

                        for (block in blocks) {
                            when (block) {
                                is VariableBlock -> {
                                    evaluatedBlocks += block.copy(
                                        value = evaluateExpression(block.value, variablesMap)
                                    )
                                }
                                is IfElseBlock -> {
                                    val resultBlocks = evaluateIfElseBlock(block, variablesMap)
                                    evaluatedBlocks += resultBlocks
                                }
                            }
                        }

                        blocks = evaluatedBlocks
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(stringResource(R.string.Calculate))
                }
            }
        }
    }
}

@Composable
fun CodeBlocksList(
    blocks: List<CodeBlock>,
    onRemove: (Int) -> Unit,
    onUpdate: (CodeBlock) -> Unit,
    onAddToIfElse: ((Int, CodeBlock, Boolean) -> Unit)? = null,
    variablesMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is VariableBlock -> VariableBlockView(
                    block = block,
                    onValueChange = { newValue ->
                        onUpdate(block.copy(value = newValue))
                    },
                    onRemove = { onRemove(block.id) },
                    variablesMap = variablesMap,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                is IfElseBlock -> IfElseBlockView(
                    block = block,
                    onUpdate = onUpdate,
                    onRemove = { onRemove(block.id) },
                    onAddToIfElse = { parentId, newBlock, isThen ->
                        onAddToIfElse?.invoke(parentId, newBlock, isThen)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

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


fun evaluateMathExpression(expression: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0.toChar()

        fun nextChar() {
            ch = if (++pos < expression.length) expression[pos] else (-1).toChar()
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expression.length) throw RuntimeException("Unexpected: " + ch)
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    eat('%') -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (ch in '0'..'9' || ch == '.') {
                while (ch in '0'..'9' || ch == '.') nextChar()
                x = expression.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch)
            }

            if (eat('^')) x = Math.pow(x, parseFactor())

            return x
        }
    }.parse()
}

@Composable
fun VariableBlockView(
    block: VariableBlock,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    variablesMap: Map<String, String>
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showResult by remember { mutableStateOf(false) }

    val animatedX by animateFloatAsState(targetValue = offsetX, label = "")
    val animatedY by animateFloatAsState(targetValue = offsetY, label = "")

    val computedValue = remember(block.value, variablesMap) {
        evaluateExpression(block.value, variablesMap)
    }

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
                    value = block.value,
                    onValueChange = onValueChange,
                    label = { Text(stringResource(R.string.Variable_Meaning)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "= $computedValue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )


                Button(
                    onClick = {
                        onValueChange(computedValue)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.Calculate_Variable_Meaning_Now))
                }
            }
        }
    }
}

@Composable
fun IfElseBlockView(
    block: IfElseBlock,
    onUpdate: (CodeBlock) -> Unit,
    onRemove: () -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var leftOperand by remember { mutableStateOf(block.leftOperand) }
    var rightOperand by remember { mutableStateOf(block.rightOperand) }
    var operator by remember { mutableStateOf(block.operator) }
    val operatorOptions = listOf("==", "!=", ">", "<", ">=", "<=")
    var operatorMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = leftOperand,
                    onValueChange = {
                        leftOperand = it
                        onUpdate(block.copy(leftOperand = it, operator = operator, rightOperand = rightOperand))
                    },
                    label = { Text(stringResource(R.string.Left_Operand)) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Button(onClick = { operatorMenuExpanded = true }) {
                        Text(operator)
                    }
                    DropdownMenu(
                        expanded = operatorMenuExpanded,
                        onDismissRequest = { operatorMenuExpanded = false }
                    ) {
                        operatorOptions.forEach { op ->
                            DropdownMenuItem(
                                text = { Text(op) },
                                onClick = {
                                    operator = op
                                    operatorMenuExpanded = false
                                    onUpdate(block.copy(leftOperand = leftOperand, operator = op, rightOperand = rightOperand))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = rightOperand,
                    onValueChange = {
                        rightOperand = it
                        onUpdate(block.copy(leftOperand = leftOperand, operator = operator, rightOperand = it))
                    },
                    label = { Text(stringResource(R.string.Right_Operand)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Тело Then-блока (${block.thenBlocks.size} блоков)", color = Color.Green)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Тело Else-блока (${block.elseBlocks.size} блоков)", color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRemove,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.Delete_If_Else_Block))
            }
        }
    }
}

fun evaluateIfElseBlock(block: IfElseBlock, variables: Map<String, String>): List<CodeBlock> {
    val left = evaluateExpression(block.leftOperand, variables).toDoubleOrNull() ?: return block.elseBlocks
    val right = evaluateExpression(block.rightOperand, variables).toDoubleOrNull() ?: return block.elseBlocks

    val condition = when (block.operator) {
        "==" -> left == right
        "!=" -> left != right
        ">"  -> left > right
        "<"  -> left < right
        ">=" -> left >= right
        "<=" -> left <= right
        else -> false
    }

    return if (condition) block.thenBlocks else block.elseBlocks
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeBlockHITSTheme {
        MainScreen()
    }
}