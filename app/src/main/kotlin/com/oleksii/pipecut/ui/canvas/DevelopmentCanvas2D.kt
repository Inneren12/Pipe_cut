package com.oleksii.pipecut.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.ui.vm.CanvasViewModel

private const val PAD_PX: Float = 16f

@Composable
fun DevelopmentCanvas2D(
    development: Development,
    pipe: PipeSpec,
    canvasViewModel: CanvasViewModel,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val transform by canvasViewModel.transform2D.collectAsStateWithLifecycle()
    var currentBox by remember { mutableStateOf(CanvasBox(0f, 0f, PAD_PX)) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        canvasViewModel.on2DTransform(
            panChangePx = panChange.x to panChange.y,
            zoomChange = zoomChange,
            box = currentBox,
        )
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .transformable(state = transformState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { canvasViewModel.reset2D() },
                )
            },
    ) {
        val box = CanvasBox(size.width, size.height, PAD_PX)
        currentBox = box
        drawRect(
            color = gridColor,
            topLeft = Offset(box.padPx, box.padPx),
            size = Size(box.innerWidth, box.innerHeight),
            style = Stroke(width = 1f),
        )
        val rawPoints = developmentToCanvas2D(development, pipe, box)
        if (rawPoints.size < 2) return@Canvas
        val canvasPoints = rawPoints.map { applyTransform(it, box, transform) }
        val path = Path().apply {
            moveTo(canvasPoints.first().x, canvasPoints.first().y)
            for (i in 1 until canvasPoints.size) {
                lineTo(canvasPoints[i].x, canvasPoints[i].y)
            }
        }
        drawPath(path = path, color = outlineColor, style = Stroke(width = 2.5f))
    }
}
