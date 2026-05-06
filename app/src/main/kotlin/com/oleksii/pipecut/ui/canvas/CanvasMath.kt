package com.oleksii.pipecut.ui.canvas

import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A 2D point in canvas pixel space. */
data class CanvasPoint(val x: Float, val y: Float)

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

/** Convenience: full development → list of canvas points, ready to stroke as a polyline. */
fun developmentToCanvas2D(
    development: Development,
    pipe: PipeSpec,
    box: CanvasBox,
): List<CanvasPoint> {
    val yMin = development.minLengthMm ?: 0.0
    val yMax = development.maxLengthMm ?: yMin
    val range = yMin..yMax
    return development.points.map { unwrappedToCanvas(it, pipe, range, box) }
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

private fun cutWorld(
    development: Development,
    pipe: PipeSpec,
): List<Pair<Double, Double>> {
    val r = pipe.radiusMm
    return development.points.map { p ->
        val phiRad = Math.toRadians(p.phiDeg)
        cabinetProject(
            x = r * cos(phiRad),
            y = p.lengthMm,
            z = r * sin(phiRad),
        )
    }
}

private fun rimWorld(
    pipe: PipeSpec,
    zAxial: Double,
    samples: Int,
): List<Pair<Double, Double>> {
    val r = pipe.radiusMm
    return (0 until samples).map {
        val phi = 2.0 * PI * it / samples
        cabinetProject(r * cos(phi), zAxial, r * sin(phi))
    }
}

/** Builds the 3D cut curve points in projected canvas space, aligned to the same fit as the rims. */
fun cutCurve3D(
    development: Development,
    pipe: PipeSpec,
    box: CanvasBox,
    samples: Int = 72,
): List<CanvasPoint> {
    val zTop = development.maxLengthMm ?: 0.0
    val cut = cutWorld(development, pipe)
    val top = rimWorld(pipe, zTop, samples)
    val bottom = rimWorld(pipe, 0.0, samples)
    val combined = cut + top + bottom
    val fitted = fitWorldToCanvas(combined, box)
    return fitted.subList(0, cut.size)
}

/** Top and bottom rim ellipses for the 3D preview, sampled and projected. */
fun rimCurves3D(
    development: Development,
    pipe: PipeSpec,
    box: CanvasBox,
    samples: Int = 72,
): Pair<List<CanvasPoint>, List<CanvasPoint>> {
    val zTop = development.maxLengthMm ?: 0.0
    val cut = cutWorld(development, pipe)
    val top = rimWorld(pipe, zTop, samples)
    val bottom = rimWorld(pipe, 0.0, samples)
    val combined = cut + top + bottom
    val fitted = fitWorldToCanvas(combined, box)
    val cutEnd = cut.size
    val topEnd = cutEnd + top.size
    return fitted.subList(cutEnd, topEnd) to fitted.subList(topEnd, topEnd + bottom.size)
}
