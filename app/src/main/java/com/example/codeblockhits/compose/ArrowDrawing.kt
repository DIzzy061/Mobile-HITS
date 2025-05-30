package com.example.codeblockhits.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun getRectEdgeIntersection(center: Offset, other: Offset, size: Size): Offset {
    val hw = size.width / 2f
    val hh = size.height / 2f
    val dx = other.x - center.x
    val dy = other.y - center.y
    if (dx == 0f && dy == 0f) return center
    val absDx = kotlin.math.abs(dx)
    val absDy = kotlin.math.abs(dy)
    val scale = if (absDx * hh > absDy * hw) hw / absDx else hh / absDy
    return Offset(center.x + dx * scale, center.y + dy * scale)
}

@Composable
fun DrawBlockConnectionArrow(
    sourceBlockOffset: Offset,
    sourceBlockSize: Size,
    targetBlockOffset: Offset,
    targetBlockSize: Size,
    color: Color
) {
    val sourceCenter = sourceBlockOffset + Offset(sourceBlockSize.width / 2f, sourceBlockSize.height / 2f)
    val targetCenter = targetBlockOffset + Offset(targetBlockSize.width / 2f, targetBlockSize.height / 2f)

    val startPoint = getRectEdgeIntersection(sourceCenter, targetCenter, sourceBlockSize)
    val endPoint = getRectEdgeIntersection(targetCenter, sourceCenter, targetBlockSize)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = color,
            start = startPoint,
            end = endPoint,
            strokeWidth = 5f,
            cap = StrokeCap.Round,
            alpha = 0.9f
        )

        val arrowHeadLength = 24f
        val arrowHeadAngle = 28f
        val angle = atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x) * 180f / PI.toFloat()

        rotate(degrees = angle, pivot = endPoint) {
            val path = Path().apply {
                moveTo(endPoint.x, endPoint.y)
                lineTo(
                    endPoint.x - arrowHeadLength * cos(Math.toRadians(arrowHeadAngle.toDouble())).toFloat(),
                    endPoint.y - arrowHeadLength * sin(Math.toRadians(arrowHeadAngle.toDouble())).toFloat()
                )
                lineTo(
                    endPoint.x - arrowHeadLength * cos(Math.toRadians(-arrowHeadAngle.toDouble())).toFloat(),
                    endPoint.y - arrowHeadLength * sin(Math.toRadians(-arrowHeadAngle.toDouble())).toFloat()
                )
                close()
            }
            drawPath(path, color = color, alpha = 0.9f)
        }
    }
} 