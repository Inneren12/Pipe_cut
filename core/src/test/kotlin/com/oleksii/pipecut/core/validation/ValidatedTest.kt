package com.oleksii.pipecut.core.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidatedTest {

    private val errorA = ValidationError.DiameterMustBePositive(actualMm = -1.0)
    private val errorB = ValidationError.CutOffsetNegative(actualMm = -2.0)

    @Test
    fun `Valid carries the value`() {
        val valid: Validated<String> = Validated.valid("ok")
        assertTrue(valid.isValid)
        assertTrue(valid is Validated.Valid)
        valid as Validated.Valid
        assertEquals("ok", valid.value)
    }

    @Test
    fun `Invalid carries the errors list`() {
        val invalid = Validated.invalid(listOf(errorA, errorB))
        assertFalse(invalid.isValid)
        assertTrue(invalid is Validated.Invalid)
        invalid as Validated.Invalid
        assertEquals(2, invalid.errors.size)
        assertSame(errorA, invalid.errors[0])
        assertSame(errorB, invalid.errors[1])
    }

    @Test
    fun `Invalid requires at least one error`() {
        assertThrows(IllegalArgumentException::class.java) {
            Validated.Invalid(emptyList())
        }
    }

    @Test
    fun `valueOrNull returns the value when Valid and null when Invalid`() {
        val valid: Validated<Int> = Validated.valid(42)
        assertEquals(42, valid.valueOrNull())

        val invalid: Validated<Int> = Validated.invalid(errorA)
        assertNull(invalid.valueOrNull())
    }

    @Test
    fun `errorsOrEmpty returns the list when Invalid and empty when Valid`() {
        val valid: Validated<Int> = Validated.valid(7)
        assertTrue(valid.errorsOrEmpty().isEmpty())

        val invalid: Validated<Int> = Validated.invalid(listOf(errorA, errorB))
        val errors = invalid.errorsOrEmpty()
        assertEquals(2, errors.size)
        assertSame(errorA, errors[0])
        assertSame(errorB, errors[1])
    }

    @Test
    fun `companion factory invalid vararg builds Invalid with all errors`() {
        val invalid = Validated.invalid(errorA, errorB)
        assertTrue(invalid is Validated.Invalid)
        invalid as Validated.Invalid
        assertEquals(listOf(errorA, errorB), invalid.errors)
    }

    @Test
    fun `invalid factory defensively copies the errors list`() {
        val source = mutableListOf<ValidationError>(errorA)
        val invalid = Validated.invalid(source)

        source.clear()

        assertTrue(invalid is Validated.Invalid)
        invalid as Validated.Invalid
        assertEquals(1, invalid.errors.size)
        assertSame(errorA, invalid.errors.single())
    }
}
