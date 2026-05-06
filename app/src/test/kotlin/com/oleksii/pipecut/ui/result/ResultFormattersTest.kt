package com.oleksii.pipecut.ui.result

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

/**
 * Locale-safety tests for the result panel formatters. Engineering output
 * must use a `.` decimal separator regardless of the device default locale.
 */
class ResultFormattersTest {

    private lateinit var defaultLocale: Locale

    @BeforeEach
    fun snapshotLocale() {
        defaultLocale = Locale.getDefault()
    }

    @AfterEach
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `formatPhi uses US decimal separator regardless of default locale`() {
        // Russia / Germany / France use "," as decimal separator.
        Locale.setDefault(Locale("ru", "RU"))
        assertEquals("12.3", formatPhi(12.34))

        Locale.setDefault(Locale.GERMANY)
        assertEquals("0.0", formatPhi(0.0))
    }

    @Test
    fun `formatLength uses US decimal separator regardless of default locale`() {
        Locale.setDefault(Locale("ru", "RU"))
        assertEquals("100.50", formatLength(100.5))

        Locale.setDefault(Locale.GERMANY)
        assertEquals("1.23", formatLength(1.234))
    }
}
