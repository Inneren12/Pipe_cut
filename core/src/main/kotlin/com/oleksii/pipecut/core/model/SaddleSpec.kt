package com.oleksii.pipecut.core.model

/**
 * Saddle joint specification: this pipe is fitted onto a partner cylinder.
 *
 * The cut is computed as the intersection between this pipe and the partner.
 *
 *  - [partnerDiameterMm]      outside diameter of the partner pipe in mm.
 *
 *  - [intersectionAngleDeg]   angle between the axes of the two pipes, in
 *                             degrees. 90° = perpendicular tee; smaller values
 *                             produce the elongated "fishmouth" profiles.
 *
 *  - [clockingDeg]            angular position of the partner pipe axis around
 *                             this pipe's circumference, measured with the
 *                             same convention as [DevPoint.phiDeg]. 0° puts
 *                             the partner above the top generatrix.
 *
 *  - [offsetMm]               eccentric offset between the two axes in the
 *                             plane perpendicular to this pipe's axis. 0 means
 *                             the axes intersect; non-zero means an offset
 *                             saddle. Sign convention is fixed in PR5.
 */
data class SaddleSpec(
    val partnerDiameterMm: Double,
    val intersectionAngleDeg: Double,
    val clockingDeg: Double,
    val offsetMm: Double
)
