package com.oleksii.pipecut.ui.vm

import androidx.lifecycle.ViewModel
import com.oleksii.pipecut.core.math.CutCalculatorDispatcher
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.ui.result.ResultStrings
import com.oleksii.pipecut.ui.result.ResultUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Runs the calculator for the latest valid `CutRequest` and publishes
 * a [ResultUiState] for the table.
 *
 * Wired by [InputScreen] which collects [InputViewModel.lastValidRequest]
 * and forwards it to [submit].
 */
class ResultViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Empty)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    /**
     * Called by the screen whenever the input ViewModel emits a new value.
     * Null means the form has never been valid yet.
     */
    fun submit(request: CutRequest?) {
        if (request == null) {
            // Sticky semantics on the input side mean we should not see this
            // after the first successful submission. Keep Empty for the
            // pre-first-submission window.
            if (_uiState.value !is ResultUiState.Computed && _uiState.value !is ResultUiState.Error) {
                _uiState.value = ResultUiState.Empty
            }
            return
        }
        _uiState.value = compute(request)
    }

    private fun compute(request: CutRequest): ResultUiState {
        return try {
            val development: Development = CutCalculatorDispatcher.calculate(request)
            ResultUiState.Computed(development)
        } catch (e: IllegalArgumentException) {
            ResultUiState.Error(
                message = ResultStrings.errorMessage(e),
                previous = previousDevelopmentOrNull(),
            )
        } catch (e: IllegalStateException) {
            // Calculator self-check failed. Treat as a programmer/math bug
            // surfaced to the user — same UI treatment, distinct log later.
            ResultUiState.Error(
                message = ResultStrings.errorMessage(e),
                previous = previousDevelopmentOrNull(),
            )
        }
    }

    private fun previousDevelopmentOrNull(): Development? = when (val s = _uiState.value) {
        is ResultUiState.Computed -> s.development
        is ResultUiState.Error -> s.previous
        ResultUiState.Empty,
        @Suppress("DEPRECATION")
        ResultUiState.SaddleNotImplemented -> null
    }

}
