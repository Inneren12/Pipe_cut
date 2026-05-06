package com.oleksii.pipecut.ui.result

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development

/**
 * UI state for the result panel beneath the input form.
 *
 * Distinct states drive what the user sees:
 *
 *  - [Empty] — no valid request yet. Show a hint.
 *  - [Computed] — last successful calculation. Carries the [request]
 *    that produced [development] so downstream consumers (canvas,
 *    future labels) cannot drift to a different request while the
 *    user keeps editing.
 *  - [Error] — calculator rejected the request. Carries the optional
 *    previous development AND the request that produced it, so the
 *    canvas keeps drawing the right curve scaled by the right pipe.
 *  - [SaddleNotImplemented] — historical placeholder; unreachable
 *    from `ResultViewModel` since the dispatcher swap. Kept
 *    temporarily for sealed-exhaustiveness in callers; removal
 *    scheduled for PR12.
 */
sealed interface ResultUiState {
    object Empty : ResultUiState

    @Deprecated(
        message = "Saddle is now handled by CutCalculatorDispatcher and this state " +
            "is no longer reachable from ResultViewModel. Scheduled for removal in PR12.",
        level = DeprecationLevel.WARNING,
    )
    object SaddleNotImplemented : ResultUiState

    data class Computed(
        val request: CutRequest,
        val development: Development,
    ) : ResultUiState

    data class Error(
        val message: String,
        val previousRequest: CutRequest?,
        val previous: Development?,
    ) : ResultUiState
}
