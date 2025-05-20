package com.example.codeblockhits.compose

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

@Composable
fun CodeBlocksList(
    blocks: List<CodeBlock>,
    erroredBlockId: Int?,
    onRemove: (Int) -> Unit,
    onUpdate: (CodeBlock) -> Unit,
    onAddToIfElse: (Int, CodeBlock, Boolean) -> Unit,
    variablesMap: Map<String, String>,
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
    val arrowColor = if (isDarkTheme) Color.White else Color.Black
    val errorHighlightColor = MaterialTheme.colorScheme.error

    Box(modifier = Modifier.fillMaxSize()) {
        blocks.forEach { block ->
            val fromCenter = blockCenters[block.id]
            val toId = when (block) {
                is VariableBlock -> block.nextBlockId
                is AssignmentBlock -> block.nextBlockId
                is IfElseBlock -> block.nextBlockId
                is PrintBlock -> block.nextBlockId
            }
            val toCenter = toId?.let { blockCenters[it] }
            if (fromCenter != null && toCenter != null) {
                ArrowLineThemeAware(from = fromCenter, to = toCenter, color = arrowColor)
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
                            onDragStart = {
                                draggingBlockId = block.id
                            },
                            onDragEnd = {
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
                        blockSizes[block.id] = Size(size.width.toFloat(), size.height.toFloat())
                    }
                    .background(
                        when {
                            erroredBlockId == block.id -> errorHighlightColor.copy(alpha = 0.3f)
                            draggingBlockId == block.id -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            isArrowMode && selectedSourceBlockId == block.id -> Color.Yellow.copy(alpha = 0.3f)
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = if (erroredBlockId == block.id) 2.dp else 0.dp,
                        color = if (erroredBlockId == block.id) errorHighlightColor else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
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

                    is PrintBlock -> PrintBlockView(
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

@Composable
fun ArrowLineThemeAware(from: Offset, to: Offset, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = color,
            start = from,
            end = to,
            strokeWidth = 3f,
            cap = StrokeCap.Round,
            alpha = 0.9f
        )
        val arrowHeadLength = 18f
        val arrowHeadAngle = 28f
        val angle = atan2(to.y - from.y, to.x - from.x) * 180f / PI
        rotate(angle.toFloat(), pivot = to) {
            val path = Path().apply {
                moveTo(to.x, to.y)
                lineTo(
                    to.x - arrowHeadLength * cos(Math.toRadians(arrowHeadAngle.toDouble())).toFloat(),
                    to.y - arrowHeadLength * sin(Math.toRadians(arrowHeadAngle.toDouble())).toFloat()
                )
                lineTo(
                    to.x - arrowHeadLength * cos(Math.toRadians(-arrowHeadAngle.toDouble())).toFloat(),
                    to.y - arrowHeadLength * sin(Math.toRadians(-arrowHeadAngle.toDouble())).toFloat()
                )
                close()
            }
            drawPath(path, color = color, alpha = 0.9f)
        }
    }
}
