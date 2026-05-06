package com.oleksii.pipecut.core.model

/**
 * Geometric specification of a pipe.
 *
 * @property diameterMm outside diameter in millimeters; must be > 0
 *                      (validation is enforced in PR3, not here)
 */
data class PipeSpec(
    val diameterMm: Double
) {
    val radiusMm: Double get() = diameterMm / 2.0
    val circumferenceMm: Double get() = Math.PI * diameterMm
}
