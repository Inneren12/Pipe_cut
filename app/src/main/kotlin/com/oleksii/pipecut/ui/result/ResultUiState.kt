package com.oleksii.pipecut.ui.result

import com.oleksii.pipecut.core.model.Development

/**
 * UI state for the result panel beneath the input form.
 *
 * Distinct states drive what the user sees:
 *
 *  - [Empty] — no valid request yet. Show a hint.
 *  - [SaddleNotImplemented] — current request is a saddle; calculator
 *    arrives in PR5. Show a placeholder banner.
 *  - [Computed] — last successful calculation. Show the table.
 *  - [Error] — calculator rejected the request (e.g., offset too small).
 *    Show the error message and keep [previous] visible if any.
 *
 * `previous` is kept across [Error] states so that an unrelated edit that
 * temporarily breaks the geometry does not blank the table on screen.
 * This mirrors the sticky `lastValidRequest` contract from PR6.
 */
sealed interface ResultUiState {
    object Empty : ResultUiState
    object SaddleNotImplemented : ResultUiState
    data class Computed(val development: Development) : ResultUiState
    data class Error(
        val message: String,
        val previous: Development?,
    ) : ResultUiState
}
