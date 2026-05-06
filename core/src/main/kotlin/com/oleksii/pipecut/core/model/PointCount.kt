package com.oleksii.pipecut.core.model

/**
 * Constrained set of supported sample counts for a developed cut curve.
 *
 * Higher counts produce a smoother curve at the cost of more marking points
 * on the pipe surface.
 */
enum class PointCount(val value: Int) {
    P12(12),
    P24(24),
    P36(36),
    P72(72);

    companion object {
        fun fromInt(n: Int): PointCount? = entries.firstOrNull { it.value == n }
    }
}
