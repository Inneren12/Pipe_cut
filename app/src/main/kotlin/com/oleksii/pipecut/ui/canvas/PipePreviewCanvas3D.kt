package com.oleksii.pipecut.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.ui.vm.CanvasViewModel

private const val PAD_PX: Float = 8f
private const val DRAG_DEG_PER_PX: Float = 1.0f

@Composable
fun PipePreviewCanvas3D(
    development: Development,
    pipe: PipeSpec,
    canvasViewModel: CanvasViewModel,
    modifier: Modifier = Modifier,
) {
    val cutColor = MaterialTheme.colorScheme.primary
    val pipeColor = MaterialTheme.colorScheme.outline
    val transform by canvasViewModel.transform3D.collectAsStateWithLifecycle()
    val rotation by canvasViewModel.rotation3D.collectAsStateWithLifecycle()
    var currentBox by remember { mutableStateOf(CanvasBox(0f, 0f, PAD_PX)) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        canvasViewModel.on3DTransform(
            panChangePx = panChange.x to panChange.y,
            zoomChange = zoomChange,
            box = currentBox,
        )
    }
    Canvas(
        modifier = modifier
            .size(120.dp)
            .transformable(state = transformState)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    canvasViewModel.on3DRotationDelta(dragAmount.x * DRAG_DEG_PER_PX)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { canvasViewModel.reset3D() },
                )
            },
    ) {
        val box = CanvasBox(size.width, size.height, PAD_PX)
        currentBox = box
        val geometry = pipePreviewGeometry3D(
            development = development,
            pipe = pipe,
            box = box,
            rotationYDeg = rotation,
        )
        if (geometry.topRim.size < 2 || geometry.cut.size < 2) return@Canvas
        val topRim = geometry.topRim.map { applyTransform(it, box, transform) }
        val bottomRim = geometry.bottomRim.map { applyTransform(it, box, transform) }
        val cut = geometry.cut.map { applyTransform(it, box, transform) }
        drawClosedPolyline(topRim, pipeColor, strokeWidth = 1.5f)
        drawClosedPolyline(bottomRim, pipeColor, strokeWidth = 1.5f)
        drawClosedPolyline(cut, cutColor, strokeWidth = 2.5f)
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
