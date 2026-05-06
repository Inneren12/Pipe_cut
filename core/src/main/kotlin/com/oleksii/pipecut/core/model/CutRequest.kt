package com.oleksii.pipecut.core.model

/**
 * Complete input bundle passed to a [com.oleksii.pipecut.core.math.CutCalculator].
 *
 * @property pipe        the pipe being cut
 * @property cut         cut plane parameters
 * @property saddle      optional partner pipe; null = flat cut only
 * @property pointCount  how many points to produce on the developed curve
 */
data class CutRequest(
    val pipe: PipeSpec,
    val cut: CutPlane,
    val saddle: SaddleSpec?,
    val pointCount: PointCount
)
