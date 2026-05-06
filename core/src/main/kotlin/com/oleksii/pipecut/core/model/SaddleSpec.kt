package com.oleksii.pipecut.core.model

/**
 * Optional saddle joint specification.
 *
 * When present, the cut is computed as the intersection between this pipe and
 * a partner cylinder (e.g., a pipe brace fitted onto a Ø914.4 main pipe).
 * When null in a CutRequest, the cut is a flat plane only.
 *
 * @property partnerDiameterMm outside diameter of the partner pipe in mm; > 0
 */
data class SaddleSpec(
    val partnerDiameterMm: Double
)
