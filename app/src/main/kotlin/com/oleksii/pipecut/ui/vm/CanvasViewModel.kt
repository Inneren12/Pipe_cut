package com.oleksii.pipecut.ui.vm

import androidx.lifecycle.ViewModel
import com.oleksii.pipecut.ui.canvas.CanvasBox
import com.oleksii.pipecut.ui.canvas.CanvasTransform
import com.oleksii.pipecut.ui.canvas.composeTransform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds gesture state for the two canvas panels:
 *
 *   - 2D unwrapped development: pan + scale
 *   - 3D pipe preview:           pan + scale + rotation around the
 *                                 pipe's axial direction
 */
class CanvasViewModel : ViewModel() {
    private val _transform2D = MutableStateFlow(CanvasTransform.Identity)
    val transform2D: StateFlow<CanvasTransform> = _transform2D.asStateFlow()

    private val _transform3D = MutableStateFlow(CanvasTransform.Identity)
    val transform3D: StateFlow<CanvasTransform> = _transform3D.asStateFlow()

    private val _rotation3D = MutableStateFlow(0f)
    val rotation3D: StateFlow<Float> = _rotation3D.asStateFlow()

    fun on2DTransform(panChangePx: Pair<Float, Float>, zoomChange: Float, box: CanvasBox) {
        _transform2D.value = composeTransform(_transform2D.value, panChangePx, zoomChange, box)
    }

    fun on3DTransform(panChangePx: Pair<Float, Float>, zoomChange: Float, box: CanvasBox) {
        _transform3D.value = composeTransform(_transform3D.value, panChangePx, zoomChange, box)
    }

    /** Drag rotation on the 3D canvas. Stored modulo 360 for stable display. */
    fun on3DRotationDelta(deltaDeg: Float) {
        val next = (_rotation3D.value + deltaDeg) % 360f
        _rotation3D.value = if (next < 0f) next + 360f else next
    }

    fun reset2D() {
        _transform2D.value = CanvasTransform.Identity
    }

    fun reset3D() {
        _transform3D.value = CanvasTransform.Identity
        _rotation3D.value = 0f
    }
}
