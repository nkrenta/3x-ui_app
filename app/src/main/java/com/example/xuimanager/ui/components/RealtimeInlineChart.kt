package com.example.xuimanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.xuimanager.ui.theme.AccentBlue
import com.example.xuimanager.ui.theme.AccentCyan

@Composable
fun RealtimeLineChart(
    dataPoints: List<Float>, // Значения от 0.0f до 100.0f
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
) {
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val maxVal = 100f

        val path = Path()
        val fillPath = Path()

        val stepX = width / (dataPoints.size - 1)

        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - (point / maxVal * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevY = height - (dataPoints[index - 1] / maxVal * height)

                // Сглаживание Безье для красоты как в Ant Design
                val controlX1 = prevX + (x - prevX) / 2
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) / 2
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // 1. Заливка градиентом под графиком
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(AccentBlue.copy(alpha = 0.4f), Color.Transparent)
            )
        )

        // 2. Основная синяя линия
        drawPath(
            path = path,
            color = AccentCyan,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}