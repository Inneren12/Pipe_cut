package com.oleksii.pipecut.core.validation

/**
 * Result of a validation pass. Either the input is [Valid] and carries the
 * (unchanged) value, or it is [Invalid] and carries one or more
 * [ValidationError]s collected in a single pass.
 *
 * This type is intentionally local to the project — not [kotlin.Result] (which
 * is for exceptions) and not Arrow's `Either` (avoiding a heavy dependency).
 */
sealed interface Validated<out T> {

    data class Valid<T>(val value: T) : Validated<T>

    data class Invalid(val errors: List<ValidationError>) : Validated<Nothing> {
        init {
            require(errors.isNotEmpty()) { "Invalid must contain at least one error" }
        }
    }

    /** Convenience: true when this is [Valid]. */
    val isValid: Boolean get() = this is Valid

    /** Returns the value if [Valid], or null otherwise. */
    fun valueOrNull(): T? = (this as? Valid<T>)?.value

    /** Returns the errors if [Invalid], or an empty list otherwise. */
    fun errorsOrEmpty(): List<ValidationError> = (this as? Invalid)?.errors ?: emptyList()

    companion object {
        /** Builds a [Valid] result. */
        fun <T> valid(value: T): Validated<T> = Valid(value)

        /** Builds an [Invalid] result from one or more errors. */
        fun invalid(first: ValidationError, vararg rest: ValidationError): Validated<Nothing> =
            Invalid(listOf(first) + rest.toList())

        /** Builds an [Invalid] result from a non-empty list of errors. */
        fun invalid(errors: List<ValidationError>): Validated<Nothing> = Invalid(errors)
    }
}
