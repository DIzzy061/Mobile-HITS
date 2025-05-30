package com.example.codeblockhits.compose

import com.example.codeblockhits.data.VariableValue
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.isSystemInDarkTheme
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow

@Composable
fun CodeBlocksList(
    blocks: List<CodeBlock>,
    erroredBlockId: Int?,
    onRemove: (Int) -> Unit,
    onUpdate: (CodeBlock) -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, VariableValue>,
    nextId: Int,
    onIdIncrement: () -> Unit,
    isArrowMode: Boolean = false,
    selectedSourceBlockId: Int? = null,
    onBlockClickedForArrow: (Int) -> Unit = {}
) {
    val blockOffsets = remember { mutableStateMapOf<Int, Offset>() }
    val blockCenters = remember { mutableStateMapOf<Int, Offset>() }
    val blockSizes = remember { mutableStateMapOf<Int, Size>() }
    var draggingBlockId by remember { mutableStateOf<Int?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val arrowColor = MaterialTheme.colorScheme.outline
    val errorHighlightColor = MaterialTheme.colorScheme.error

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()) {
        blocks.forEach { block ->
            val sourceOffset = blockOffsets[block.id]
            val sourceSize = blockSizes[block.id]

            val targetId = block.nextBlockId
            val targetOffset = targetId?.let { blockOffsets[it] }
            val targetSize = targetId?.let { blockSizes[it] }

            if (sourceOffset != null && sourceSize != null && targetOffset != null && targetSize != null) {
                DrawBlockConnectionArrow(
                    sourceBlockOffset = sourceOffset,
                    sourceBlockSize = sourceSize,
                    targetBlockOffset = targetOffset,
                    targetBlockSize = targetSize,
                    color = arrowColor
                )
            }
        }

        blocks.forEachIndexed { index, block ->
            val offset = blockOffsets.getOrPut(block.id) {
                Offset(100f, 100f + index * 200f)
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .pointerInput(block.id) {
                        detectDragGestures(
                            onDragStart = { draggingBlockId = block.id },
                            onDragEnd = { draggingBlockId = null },
                            onDrag = { _, dragAmount ->
                                val current =
                                    blockOffsets[block.id] ?: Offset(100f, 100f + index * 200f)
                                blockOffsets[block.id] = current + dragAmount
                            }
                        )
                    }
                    .onGloballyPositioned { layoutCoordinates: LayoutCoordinates ->
                        val size = layoutCoordinates.size
                        val position = layoutCoordinates.positionInRoot()
                        blockCenters[block.id] =
                            position + Offset(size.width / 2f, size.height / 2f)
                        blockSizes[block.id] = Size(size.width.toFloat(), size.height.toFloat())
                    }
                    .then(
                        if (isArrowMode && selectedSourceBlockId == block.id) {
                            Modifier
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        } else if (erroredBlockId == block.id) {
                            Modifier.border(
                                width = 2.dp,
                                color = errorHighlightColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        when {
                            erroredBlockId == block.id -> errorHighlightColor.copy(alpha = 0.3f)
                            draggingBlockId == block.id -> MaterialTheme.colorScheme.secondary.copy(
                                alpha = 0.3f
                            )

                            isArrowMode && selectedSourceBlockId == block.id -> MaterialTheme.colorScheme.tertiaryContainer.copy(
                                alpha = 0.5f
                            )

                            else -> Color.Transparent
                        }
                    )
                    .padding(8.dp)
                    .then(
                        if (isArrowMode) Modifier.pointerInput(block.id) {
                            detectTapGestures {
                                onBlockClickedForArrow(block.id)
                            }
                        } else Modifier
                    )
            ) {
                when (block) {
                    is VariableBlock -> VariableBlockView(
                        block = block,
                        onValueChange = { newVal -> onUpdate(block.copy(value = newVal)) },
                        onRemove = { onRemove(block.id) },
                        variablesMap = variablesMap
                    )


                    is AssignmentBlock -> AssignmentBlockView(
                        block,
                        onUpdate,
                        { onRemove(block.id) },
                        variablesMap
                    )

                    is PrintBlock -> PrintBlockView(
                        block,
                        onUpdate,
                        { onRemove(block.id) },
                        variablesMap
                    )

                    is IfElseBlock -> IfElseBlockView(
                        block,
                        onUpdate,
                        { onRemove(block.id) },
                        onAddToIfElse,
                        variablesMap,
                        nextId,
                        onIdIncrement
                    )

                    is WhileBlock -> WhileBlockView(
                        block = block,
                        onUpdate = onUpdate,
                        onRemove = { onRemove(block.id) },
                        variablesMap = variablesMap,
                        nextId = nextId,
                        onIdIncrement = onIdIncrement
                    )
                }
            }
        }
    }
}


