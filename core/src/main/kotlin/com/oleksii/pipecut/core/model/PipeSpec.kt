package com.oleksii.pipecut.core.model

import kotlin.math.PI

/**
 * Geometric specification of a pipe.
 *
 * @property diameterMm outside diameter in millimeters; must be > 0.
 *                      Validation is enforced in PR3, not here.
 */
data class PipeSpec(
    val diameterMm: Double
) {
    val radiusMm: Double get() = diameterMm / 2.0
    val circumferenceMm: Double get() = PI * diameterMm
}
