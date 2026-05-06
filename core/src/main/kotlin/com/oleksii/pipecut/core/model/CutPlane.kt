package com.oleksii.pipecut.core.model

/**
 * Cut plane definition relative to the cut pipe.
 *
 *  - [tiltDeg]      α — angle between the cut plane and a perpendicular
 *                       cross-section of the pipe. α = 0 means a straight
 *                       (square) cut. α = 28° in the reference drawing.
 *
 *  - [clockingDeg]  β — angular direction of the plane tilt around the pipe
 *                       axis. 0° means the tilt direction is aligned with the
 *                       reference (top) generatrix; positive direction follows
 *                       the same convention as [DevPoint.phiDeg]. Equivalent
 *                       to clocking the high/low points of the developed cut
 *                       curve around the pipe circumference. β = 12° in the
 *                       reference drawing — this is the distinguishing feature
 *                       this app aims to support.
 *
 *  - [offsetMm]     L₀ — axial coordinate where the cut plane intersects the
 *                        pipe axis, measured from the pipe end face.
 */
data class CutPlane(
    val tiltDeg: Double,
    val clockingDeg: Double,
    val offsetMm: Double
)
