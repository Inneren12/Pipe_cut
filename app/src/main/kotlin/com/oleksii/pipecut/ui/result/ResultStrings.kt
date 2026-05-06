package com.oleksii.pipecut.ui.result

/**
 * English literals for the result panel. PR11 will replace these with
 * `stringResource` lookups; for now they are plain literals so the file
 * stays unit-testable on the JVM.
 */
object ResultStrings {
    const val SADDLE_NOT_IMPLEMENTED: String = "Saddle cuts arrive in PR5"
    const val EMPTY_HINT: String = "Enter values and tap Calculate"
    const val TABLE_HEADER_PHI: String = "φ (°)"
    const val TABLE_HEADER_LENGTH: String = "L (mm)"
    const val ERROR_PREFIX: String = "Cannot compute: "

    fun errorMessage(throwable: Throwable): String {
        val raw = throwable.message ?: throwable::class.java.simpleName
        return ERROR_PREFIX + raw
    }
}
