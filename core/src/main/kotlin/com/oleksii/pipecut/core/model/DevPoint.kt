package com.oleksii.pipecut.core.model

/**
 * One point on the developed cut curve.
 *
 * @property phiDeg     angular position around the pipe circumference,
 *                      measured **clockwise when looking from the cut end face
 *                      toward the pipe body**, starting from the top
 *                      generatrix. 0° = top, 90° = right, 180° = bottom,
 *                      270° = left. Range: [0, 360).
 *
 * @property lengthMm   distance from the pipe end face to the cut, measured
 *                      along the pipe surface generatrix at angle [phiDeg].
 */
data class DevPoint(
    val phiDeg: Double,
    val lengthMm: Double
)
