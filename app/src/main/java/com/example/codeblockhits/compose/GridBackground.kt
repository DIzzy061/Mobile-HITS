package com.example.codeblockhits.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.drawGrid(
    gridColor: Color = Color.Gray.copy(alpha = 0.1f),
    gridSpacing: Float = 20.dp.value,
    gridLineWidth: Float = 1.dp.value
) = drawBehind {
    val width = size.width
    val height = size.height


    for (x in 0..width.toInt() step gridSpacing.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), height),
            strokeWidth = gridLineWidth
        )
    }


    for (y in 0..height.toInt() step gridSpacing.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(width, y.toFloat()),
            strokeWidth = gridLineWidth
        )
    }
} 