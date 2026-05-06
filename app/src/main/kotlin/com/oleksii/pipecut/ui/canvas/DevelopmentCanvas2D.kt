package com.oleksii.pipecut.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec

private const val PAD_PX: Float = 16f

@Composable
fun DevelopmentCanvas2D(
    development: Development,
    pipe: PipeSpec,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val box = CanvasBox(size.width, size.height, PAD_PX)
        drawRect(
            color = gridColor,
            topLeft = Offset(box.padPx, box.padPx),
            size = Size(box.innerWidth, box.innerHeight),
            style = Stroke(width = 1f),
        )
        val canvasPoints = developmentToCanvas2D(development, pipe, box)
        if (canvasPoints.size < 2) return@Canvas
        val path = Path().apply {
            moveTo(canvasPoints.first().x, canvasPoints.first().y)
            for (i in 1 until canvasPoints.size) {
                lineTo(canvasPoints[i].x, canvasPoints[i].y)
            }
            // No lineTo(first) — φ = 0 and φ = 360 sit at OPPOSITE edges of
            // the unwrapped sheet. The virtual seam point appended by
            // developmentToCanvas2D already extends the polyline to the
            // right margin.
        }
        drawPath(path = path, color = outlineColor, style = Stroke(width = 2.5f))
    }
}
