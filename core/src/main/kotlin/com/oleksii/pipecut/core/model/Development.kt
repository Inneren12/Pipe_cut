package com.oleksii.pipecut.core.model

/**
 * Result of a cut development calculation: an ordered list of points sampled
 * around the pipe, suitable for marking on the metal or rendering on screen.
 *
 * Points are ordered by ascending phiDeg.
 */
data class Development(
    val points: List<DevPoint>
) {
    val maxLengthMm: Double get() = points.maxOf { it.lengthMm }
    val minLengthMm: Double get() = points.minOf { it.lengthMm }
}
