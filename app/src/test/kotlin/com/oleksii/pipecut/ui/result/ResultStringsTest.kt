package com.oleksii.pipecut.ui.result

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResultStringsTest {

    @Test
    fun `errorMessage prefixes the throwable message`() {
        val ex = IllegalArgumentException("offsetMm too small")
        val msg = ResultStrings.errorMessage(ex)
        assertTrue(msg.startsWith(ResultStrings.ERROR_PREFIX), msg)
        assertTrue(msg.contains("offsetMm"), msg)
    }

    @Test
    fun `errorMessage falls back to class name when message is null`() {
        val ex = IllegalStateException()
        val msg = ResultStrings.errorMessage(ex)
        assertTrue(msg.startsWith(ResultStrings.ERROR_PREFIX), msg)
        assertTrue(msg.contains("IllegalStateException"), msg)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `saddle banner does not leak PR numbers`() {
        val text = ResultStrings.SADDLE_NOT_IMPLEMENTED
        assertFalse(
            text.contains(Regex("\\bPR\\d+\\b")),
            "Banner must not mention PR numbers, got: $text",
        )
    }
}
