package com.oleksii.pipecut.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec

private const val PAD_PX: Float = 8f

@Composable
fun PipePreviewCanvas3D(
    development: Development,
    pipe: PipeSpec,
    modifier: Modifier = Modifier,
) {
    val cutColor = MaterialTheme.colorScheme.primary
    val pipeColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = modifier.size(120.dp),
    ) {
        val box = CanvasBox(size.width, size.height, PAD_PX)
        val geometry = pipePreviewGeometry3D(development, pipe, box)
        if (geometry.topRim.size < 2 || geometry.cut.size < 2) return@Canvas
        // 3D cut closes correctly: φ = 0 and φ = 360 are the same point on
        // the cylinder. (The 2D unwrapped cut intentionally stays open.)
        drawClosedPolyline(geometry.topRim, pipeColor, strokeWidth = 1.5f)
        drawClosedPolyline(geometry.bottomRim, pipeColor, strokeWidth = 1.5f)
        drawClosedPolyline(geometry.cut, cutColor, strokeWidth = 2.5f)
    }
}

private fun DrawScope.drawClosedPolyline(
    points: List<CanvasPoint>,
    color: Color,
    strokeWidth: Float,
) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        lineTo(points.first().x, points.first().y)
    }
    drawPath(path, color, style = Stroke(width = strokeWidth))
}
