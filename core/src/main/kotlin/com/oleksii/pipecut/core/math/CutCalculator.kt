package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development

/**
 * Strategy that turns a [CutRequest] into a [Development].
 *
 * Implementations come in later PRs:
 *  - PR4: PlaneCutCalculator (saddle = null)
 *  - PR5: SaddleCutCalculator (saddle != null)
 */
fun interface CutCalculator {
    fun calculate(request: CutRequest): Development
}
