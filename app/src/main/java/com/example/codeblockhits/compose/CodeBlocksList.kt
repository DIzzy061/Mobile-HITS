package com.example.codeblockhits.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codeblockhits.data.*
import kotlin.math.hypot
import kotlin.math.roundToInt


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
    val blockOffsets = remember { mutableStateMapOf<Int, Offset>() }
    val blockCenters = remember { mutableStateMapOf<Int, Offset>() }
    var draggingBlockId by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        blocks.forEachIndexed { index, block ->
            val offset = blockOffsets.getOrPut(block.id) {
                Offset(100f, 100f + index * 200f)
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .pointerInput(block.id) {
                        detectDragGestures(
                            onDragStart = {
                                draggingBlockId = block.id
                            },
                            onDragEnd = {
                                val thisCenter = blockCenters[block.id] ?: return@detectDragGestures
                                val nearby = blockCenters.entries
                                    .firstOrNull { it.key != block.id && distance(thisCenter, it.value) < 80f }

                                if (nearby != null) {
                                    val updated = when (block) {
                                        is VariableBlock -> block.copy(nextBlockId = nearby.key)
                                        is AssignmentBlock -> block.copy(nextBlockId = nearby.key)
                                        is IfElseBlock -> block.copy(nextBlockId = nearby.key)
                                    }
                                    onUpdate(updated)
                                }
                                draggingBlockId = null
                            },
                            onDrag = { _, dragAmount ->
                                val current = blockOffsets[block.id] ?: Offset(100f, 100f + index * 200f)
                                blockOffsets[block.id] = current + dragAmount
                            }
                        )
                    }
                    .onGloballyPositioned { layoutCoordinates: LayoutCoordinates ->
                        val size = layoutCoordinates.size
                        val position = layoutCoordinates.positionInRoot()
                        blockCenters[block.id] = position + Offset(size.width / 2f, size.height / 2f)
                    }
                    .background(
                        if (draggingBlockId == block.id) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(8.dp)
            ) {
                when (block) {
                    is VariableBlock -> VariableBlockView(
                        block = block,
                        onValueChange = { newValue -> onUpdate(block.copy(value = newValue)) },
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
}

private fun distance(a: Offset, b: Offset): Float {
    return hypot(a.x - b.x, a.y - b.y)
}
