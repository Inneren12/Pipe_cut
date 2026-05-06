package com.oleksii.pipecut.core.model

/**
 * Constrained set of supported sample counts for the developed cut curve.
 *
 * Higher counts produce a smoother curve at the cost of more marking points
 * on the pipe surface. P360 corresponds to one mark per degree of pipe
 * circumference and is the densest grid useful for hand marking.
 */
enum class PointCount(val value: Int) {
    P12(12),
    P24(24),
    P36(36),
    P72(72),
    P120(120),
    P180(180),
    P360(360);

    companion object {
        fun fromInt(n: Int): PointCount? = entries.firstOrNull { it.value == n }
    }
}
