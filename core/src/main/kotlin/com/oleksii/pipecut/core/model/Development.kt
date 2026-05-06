package com.oleksii.pipecut.core.model

/**
 * Result of a cut development calculation: an ordered list of points sampled
 * around the pipe, suitable for marking on the metal or rendering on screen.
 *
 * Invariant (enforced by validation in PR3): non-empty, ascending [DevPoint.phiDeg],
 * unique [DevPoint.phiDeg] values. The `Development` data class itself does
 * not enforce these — it is the validator's job. Callers that bypass the
 * validator (e.g., empty initial UI state) must handle the null case below.
 *
 * [maxLengthMm] and [minLengthMm] are nullable to remain safe on an empty
 * list. In production flows they are always non-null because the validator
 * rejects empty results before this type reaches the UI.
 */
data class Development(
    val points: List<DevPoint>
) {
    val maxLengthMm: Double? get() = points.maxOfOrNull { it.lengthMm }
    val minLengthMm: Double? get() = points.minOfOrNull { it.lengthMm }
}
