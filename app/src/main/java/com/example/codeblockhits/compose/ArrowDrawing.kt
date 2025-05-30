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

    val dx = targetCenter.x - sourceCenter.x
    val dy = targetCenter.y - sourceCenter.y

    val startPoint: Offset
    val endPoint: Offset

    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
        if (dx > 0) {
            startPoint = Offset(sourceBlockOffset.x + sourceBlockSize.width, sourceCenter.y)
            endPoint = Offset(targetBlockOffset.x, targetCenter.y)
        } else {
            startPoint = Offset(sourceBlockOffset.x, sourceCenter.y)
            endPoint = Offset(targetBlockOffset.x + targetBlockSize.width, targetCenter.y)
        }
    } else {
        if (dy > 0) {
            startPoint = Offset(sourceCenter.x, sourceBlockOffset.y + sourceBlockSize.height)
            endPoint = Offset(targetCenter.x, targetBlockOffset.y)
        } else {
            startPoint = Offset(sourceCenter.x, sourceBlockOffset.y)
            endPoint = Offset(targetCenter.x, targetBlockOffset.y + targetBlockSize.height)
        }
    }

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