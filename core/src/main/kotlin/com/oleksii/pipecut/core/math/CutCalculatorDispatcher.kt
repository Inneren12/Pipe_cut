package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development

/**
 * Dispatches a [CutRequest] to the appropriate calculator strategy based on
 * whether the request carries a saddle.
 *
 * This is the single entry point that the UI layer (PR7+) should use.
 * Direct use of [PlaneCutCalculator] or [SaddleCutCalculator] from `:app`
 * is discouraged.
 */
object CutCalculatorDispatcher : CutCalculator {
    override fun calculate(request: CutRequest): Development =
        if (request.saddle == null) {
            PlaneCutCalculator.calculate(request)
        } else {
            SaddleCutCalculator.calculate(request)
        }
}
