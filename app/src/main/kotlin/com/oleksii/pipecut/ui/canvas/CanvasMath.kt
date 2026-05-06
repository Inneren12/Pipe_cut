package com.oleksii.pipecut.ui.canvas

import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A 2D point in canvas pixel space. */
data class CanvasPoint(val x: Float, val y: Float)

/**
 * Pan + scale applied to a single canvas. Stored in canvas-pixel
 * units. Default is identity. Limits enforced by [clampTransform].
 */
data class CanvasTransform(
    val scalePx: Float = 1.0f,
    val panXPx: Float = 0.0f,
    val panYPx: Float = 0.0f,
) {
    companion object {
        const val MIN_SCALE: Float = 0.5f
        const val MAX_SCALE: Float = 8.0f
        val Identity: CanvasTransform = CanvasTransform()
    }
}

/** Apply a transform to a canvas-pixel point, around the canvas center. */
fun applyTransform(point: CanvasPoint, box: CanvasBox, t: CanvasTransform): CanvasPoint {
    val cx = box.widthPx / 2f
    val cy = box.heightPx / 2f
    val x = cx + (point.x - cx) * t.scalePx + t.panXPx
    val y = cy + (point.y - cy) * t.scalePx + t.panYPx
    return CanvasPoint(x, y)
}

/**
 * Clamp [proposed] to the documented limits. Scale is clamped to
 * [MIN_SCALE, MAX_SCALE]. Pan is limited so at least ~25% of the
 * scaled content rectangle stays inside the viewport.
 */
fun clampTransform(proposed: CanvasTransform, box: CanvasBox): CanvasTransform {
    val scale = proposed.scalePx.coerceIn(CanvasTransform.MIN_SCALE, CanvasTransform.MAX_SCALE)
    val halfContentW = box.widthPx / 2f * scale
    val halfContentH = box.heightPx / 2f * scale
    val maxPanX = halfContentW * 0.75f
    val maxPanY = halfContentH * 0.75f
    return CanvasTransform(
        scalePx = scale,
        panXPx = proposed.panXPx.coerceIn(-maxPanX, maxPanX),
        panYPx = proposed.panYPx.coerceIn(-maxPanY, maxPanY),
    )
}

/** Compose a delta from a transformable callback into the current transform. */
fun composeTransform(
    current: CanvasTransform,
    panChangePx: Pair<Float, Float>,
    zoomChange: Float,
    box: CanvasBox,
): CanvasTransform {
    val proposed = CanvasTransform(
        scalePx = current.scalePx * zoomChange,
        panXPx = current.panXPx + panChangePx.first,
        panYPx = current.panYPx + panChangePx.second,
    )
    return clampTransform(proposed, box)
}

/**
 * Rotate a (x, z) world point by [degrees] around the y-axis (the
 * pipe's axial direction). Used by the 3D preview to let the user
 * inspect the cut from a different angle.
 */
fun rotateAroundY(x: Double, z: Double, degrees: Float): Pair<Double, Double> {
    val rad = Math.toRadians(degrees.toDouble())
    val c = kotlin.math.cos(rad)
    val s = kotlin.math.sin(rad)
    return (x * c + z * s) to (-x * s + z * c)
}

/** Padded canvas rectangle for layout math. */
data class CanvasBox(
    val widthPx: Float,
    val heightPx: Float,
    val padPx: Float,
) {
    val innerWidth: Float get() = widthPx - 2f * padPx
    val innerHeight: Float get() = heightPx - 2f * padPx
}

/** Screen-y is inverted: large [DevPoint.lengthMm] → top of canvas. */
fun unwrappedToCanvas(
    point: DevPoint,
    pipe: PipeSpec,
    yRangeMm: ClosedFloatingPointRange<Double>,
    box: CanvasBox,
): CanvasPoint {
    val phiRad = Math.toRadians(point.phiDeg)
    val xWorld = pipe.radiusMm * phiRad
    val xFraction = (xWorld / pipe.circumferenceMm).toFloat().coerceIn(0f, 1f)
    val xCanvas = box.padPx + xFraction * box.innerWidth
    val span = (yRangeMm.endInclusive - yRangeMm.start).coerceAtLeast(1.0)
    val yFraction = ((point.lengthMm - yRangeMm.start) / span).toFloat().coerceIn(0f, 1f)
    val yCanvas = box.padPx + (1f - yFraction) * box.innerHeight
    return CanvasPoint(xCanvas, yCanvas)
}

/**
 * Convenience: full development → list of canvas points, ready to
 * stroke as an open polyline. Appends one virtual seam point at the
 * right edge so the line visually reaches φ = 360° without faking a
 * diagonal close.
 */
fun developmentToCanvas2D(
    development: Development,
    pipe: PipeSpec,
    box: CanvasBox,
): List<CanvasPoint> {
    if (development.points.isEmpty()) return emptyList()
    val yMin = development.minLengthMm ?: 0.0
    val yMax = development.maxLengthMm ?: yMin
    val range = yMin..yMax
    val sampled = development.points.map { unwrappedToCanvas(it, pipe, range, box) }
    val firstY = sampled.first().y
    val rightEdgeX = box.padPx + box.innerWidth
    return sampled + CanvasPoint(rightEdgeX, firstY)
}

/** Cabinet projection of a 3D point. */
fun cabinetProject(
    x: Double,
    y: Double,
    z: Double,
    alphaRad: Double = PI / 6.0,
    scaleZ: Double = 0.5,
): Pair<Double, Double> {
    val px = x + cos(alphaRad) * z * scaleZ
    val py = y + sin(alphaRad) * z * scaleZ
    return px to py
}

/** Map a list of `(x_world, y_world)` pairs to canvas pixels, fitting the longest dimension. */
fun fitWorldToCanvas(
    worldPoints: List<Pair<Double, Double>>,
    box: CanvasBox,
): List<CanvasPoint> {
    if (worldPoints.isEmpty()) return emptyList()
    val xMin = worldPoints.minOf { it.first }
    val xMax = worldPoints.maxOf { it.first }
    val yMin = worldPoints.minOf { it.second }
    val yMax = worldPoints.maxOf { it.second }
    val xSpan = (xMax - xMin).coerceAtLeast(1e-9)
    val ySpan = (yMax - yMin).coerceAtLeast(1e-9)
    val scale = minOf(box.innerWidth / xSpan, box.innerHeight / ySpan).toFloat()
    val xCenter = box.padPx + box.innerWidth / 2f
    val yCenter = box.padPx + box.innerHeight / 2f
    val xWorldCenter = (xMin + xMax) / 2.0
    val yWorldCenter = (yMin + yMax) / 2.0
    return worldPoints.map { (xw, yw) ->
        val xCanvas = xCenter + ((xw - xWorldCenter) * scale).toFloat()
        val yCanvas = yCenter - ((yw - yWorldCenter) * scale).toFloat()
        CanvasPoint(xCanvas, yCanvas)
    }
}

private fun cutWorldRotated(
    development: Development,
    pipe: PipeSpec,
    rotationYDeg: Float,
): List<Pair<Double, Double>> {
    val r = pipe.radiusMm
    return development.points.map { p ->
        val phiRad = Math.toRadians(p.phiDeg)
        val (xr, zr) = rotateAroundY(r * cos(phiRad), r * sin(phiRad), rotationYDeg)
        cabinetProject(x = xr, y = p.lengthMm, z = zr)
    }
}

private fun rimWorldRotated(
    pipe: PipeSpec,
    zAxial: Double,
    samples: Int,
    rotationYDeg: Float,
): List<Pair<Double, Double>> {
    val r = pipe.radiusMm
    return (0 until samples).map {
        val phi = 2.0 * PI * it / samples
        val (xr, zr) = rotateAroundY(r * cos(phi), r * sin(phi), rotationYDeg)
        cabinetProject(xr, zAxial, zr)
    }
}

/**
 * Projected canvas-space geometry for the 3D pipe preview, all three
 * curves fitted together so the cut visibly aligns with the rims.
 */
data class PipePreviewGeometry(
    val cut: List<CanvasPoint>,
    val topRim: List<CanvasPoint>,
    val bottomRim: List<CanvasPoint>,
)

/** Builds the 3D pipe preview in projected canvas space. */
fun pipePreviewGeometry3D(
    development: Development,
    pipe: PipeSpec,
    box: CanvasBox,
    samples: Int = 72,
    rotationYDeg: Float = 0f,
): PipePreviewGeometry {
    val zTop = development.maxLengthMm ?: 0.0
    val cut = cutWorldRotated(development, pipe, rotationYDeg)
    val top = rimWorldRotated(pipe, zTop, samples, rotationYDeg)
    val bottom = rimWorldRotated(pipe, 0.0, samples, rotationYDeg)
    val combined = cut + top + bottom
    val fitted = fitWorldToCanvas(combined, box)
    val cutEnd = cut.size
    val topEnd = cutEnd + top.size
    return PipePreviewGeometry(
        cut = fitted.subList(0, cutEnd),
        topRim = fitted.subList(cutEnd, topEnd),
        bottomRim = fitted.subList(topEnd, topEnd + bottom.size),
    )
}
