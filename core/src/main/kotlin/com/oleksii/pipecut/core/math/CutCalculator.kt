package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development

/**
 * Strategy that turns a [CutRequest] into a [Development].
 *
 * Implementations may support plane-only cuts, saddle cuts, or dispatch
 * between both. Concrete strategies arrive in later PRs.
 */
fun interface CutCalculator {
    fun calculate(request: CutRequest): Development
}
