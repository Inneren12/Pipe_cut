package com.oleksii.pipecut.core.validation

/**
 * Result of a validation pass.
 *
 * `Valid<T>` carries the validated value through unchanged; `Invalid` carries
 * the accumulated list of [ValidationError]s. Validators accumulate all
 * errors before returning, so the caller sees every problem at once instead
 * of having to fix-and-retry.
 */
sealed interface Validated<out T> {
    data class Valid<T>(val value: T) : Validated<T>
    data class Invalid(val errors: List<ValidationError>) : Validated<Nothing>
}
