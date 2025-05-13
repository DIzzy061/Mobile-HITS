package com.example.codeblockhits.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*

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