package com.oleksii.pipecut.core.model

/**
 * Cut plane definition.
 *
 * The cut plane is described relative to the pipe's longitudinal axis:
 *
 *  - [tiltDeg]      α — tilt angle of the plane from a perpendicular cross-section
 *                       of the pipe. α = 0 means a straight (flat) cut.
 *                       α = 28° in the reference drawing.
 *
 *  - [rotationDeg]  β — rotation of the cut plane around its own normal.
 *                       Equivalent to rotating the major axis of the resulting
 *                       cut ellipse around the pipe axis. β = 0 means the
 *                       ellipse major axis lies in the standard tilt plane.
 *                       β = 12° in the reference drawing — this is the
 *                       distinguishing feature this app supports.
 *
 *  - [offsetMm]     L₀ — distance from the pipe end face along the pipe axis
 *                        to the centerline of the cut. Determines where on the
 *                        pipe the cut sits.
 */
data class CutPlane(
    val tiltDeg: Double,
    val rotationDeg: Double,
    val offsetMm: Double
)
