package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

@Composable
fun CodeBlocksList(
    blocks: List<CodeBlock>,
    onRemove: (Int) -> Unit,
    onUpdate: (CodeBlock) -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, String>,
    nextId: Int,
    onIdIncrement: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        blocks.forEach { block ->
            when (block) {
                is VariableBlock -> VariableBlockView(
                    block = block,
                    onValueChange = { newValue ->
                        onUpdate(block.copy(value = newValue))
                    },
                    onRemove = { onRemove(block.id) },
                    variablesMap = variablesMap
                )
                is IfElseBlock -> IfElseBlockView(
                    block = block,
                    onUpdate = onUpdate,
                    onRemove = { onRemove(block.id) },
                    onAddToIfElse = onAddToIfElse,
                    variablesMap = variablesMap,
                    nextId = nextId,
                    onIdIncrement = onIdIncrement
                )
                is AssignmentBlock -> AssignmentBlockView(
                    block = block,
                    onUpdate = { updated -> onUpdate(updated) },
                    onRemove = { onRemove(block.id) },
                    variablesMap = variablesMap
                )
            }
        }
    }
}
