package com.oleksii.pipecut.ui.result

import com.oleksii.pipecut.core.model.Development

/**
 * UI state for the result panel beneath the input form.
 *
 * Distinct states drive what the user sees:
 *
 *  - [Empty] — no valid request yet. Show a hint.
 *  - [Computed] — last successful calculation. Show the table.
 *  - [Error] — calculator rejected the request (e.g., offset too small).
 *    Show the error message and keep [previous] visible if any.
 *  - [SaddleNotImplemented] — historical placeholder; unreachable from
 *    `ResultViewModel` since the dispatcher swap. Kept temporarily for
 *    sealed-exhaustiveness in callers; scheduled for removal in PR12.
 *
 * `previous` is kept across [Error] states so that an unrelated edit that
 * temporarily breaks the geometry does not blank the table on screen.
 * This mirrors the sticky `lastValidRequest` contract from PR6.
 */
sealed interface ResultUiState {
    object Empty : ResultUiState

    @Deprecated(
        message = "Saddle is now handled by CutCalculatorDispatcher and this state " +
            "is no longer reachable from ResultViewModel. Scheduled for removal in PR12.",
        level = DeprecationLevel.WARNING,
    )
    object SaddleNotImplemented : ResultUiState

    data class Computed(val development: Development) : ResultUiState
    data class Error(
        val message: String,
        val previous: Development?,
    ) : ResultUiState
}
