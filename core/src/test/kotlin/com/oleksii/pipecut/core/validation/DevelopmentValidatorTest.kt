package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DevelopmentValidatorTest {

    private fun development(vararg points: Pair<Double, Double>): Development =
        Development(points.map { (phi, length) -> DevPoint(phiDeg = phi, lengthMm = length) })

    @Test
    fun `accepts a typical development with 12 ascending points, all in 0 to 360, positive lengths`() {
        val dev = Development(
            (0 until 12).map { i ->
                DevPoint(phiDeg = i * 30.0, lengthMm = 100.0 + i)
            }
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Valid, "Expected Valid but was $result")
        result as Validated.Valid
        assertSame(dev, result.value)
    }

    @Test
    fun `rejects empty points list with DevelopmentEmpty and stops there`() {
        val dev = Development(emptyList())
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid
        assertEquals(1, result.errors.size)
        assertSame(ValidationError.DevelopmentEmpty, result.errors.single())
    }

    @Test
    fun `reports phi out of range for index 0 and index 5`() {
        val dev = Development(
            listOf(
                DevPoint(phiDeg = -1.0, lengthMm = 10.0),
                DevPoint(phiDeg = 30.0, lengthMm = 10.0),
                DevPoint(phiDeg = 60.0, lengthMm = 10.0),
                DevPoint(phiDeg = 90.0, lengthMm = 10.0),
                DevPoint(phiDeg = 120.0, lengthMm = 10.0),
                DevPoint(phiDeg = 360.0, lengthMm = 10.0),
            )
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid

        val phiErrors = result.errors.filterIsInstance<ValidationError.DevPointPhiOutOfRange>()
        assertEquals(2, phiErrors.size, "Got: ${result.errors}")

        val first = phiErrors.first { it.indexInList == 0 }
        assertEquals(-1.0, first.actualDeg, 1e-9)

        val sixth = phiErrors.first { it.indexInList == 5 }
        assertEquals(360.0, sixth.actualDeg, 1e-9)
    }

    @Test
    fun `reports negative length`() {
        val dev = development(
            0.0 to 10.0,
            30.0 to -1.0,
            60.0 to 10.0,
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid

        val lenErrors = result.errors.filterIsInstance<ValidationError.DevPointLengthNegative>()
        assertEquals(1, lenErrors.size, "Got: ${result.errors}")
        assertEquals(1, lenErrors.single().indexInList)
        assertEquals(-1.0, lenErrors.single().actualMm, 1e-9)
    }

    @Test
    fun `reports non-ascending pair`() {
        val dev = development(
            0.0 to 10.0,
            30.0 to 10.0,
            20.0 to 10.0,
            60.0 to 10.0,
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid

        val nonAsc = result.errors.filterIsInstance<ValidationError.DevPointsNotAscending>()
        assertEquals(1, nonAsc.size, "Got: ${result.errors}")
        val err = nonAsc.single()
        assertEquals(2, err.indexInList)
        assertEquals(30.0, err.previousPhiDeg, 1e-9)
        assertEquals(20.0, err.currentPhiDeg, 1e-9)
    }

    @Test
    fun `reports duplicate phi`() {
        val dev = development(
            0.0 to 10.0,
            30.0 to 10.0,
            30.0 to 10.0,
            60.0 to 10.0,
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid

        val dups = result.errors.filterIsInstance<ValidationError.DevPointsDuplicatePhi>()
        assertEquals(1, dups.size, "Got: ${result.errors}")
        val err = dups.single()
        assertEquals(1, err.firstIndex)
        assertEquals(2, err.secondIndex)
        assertEquals(30.0, err.phiDeg, 1e-9)

        // Duplicate is reported instead of NotAscending for that pair.
        assertTrue(result.errors.none { it is ValidationError.DevPointsNotAscending })
    }

    @Test
    fun `accumulates multiple errors in one development`() {
        val dev = Development(
            listOf(
                DevPoint(phiDeg = -5.0, lengthMm = 10.0),   // phi out of range (idx 0)
                DevPoint(phiDeg = 30.0, lengthMm = -2.0),   // length negative (idx 1)
                DevPoint(phiDeg = 20.0, lengthMm = 10.0),   // non-ascending (idx 2)
                DevPoint(phiDeg = 20.0, lengthMm = 10.0),   // duplicate phi (idx 2,3)
            )
        )
        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid

        assertTrue(
            result.errors.any {
                it is ValidationError.DevPointPhiOutOfRange && it.indexInList == 0
            },
            "Got: ${result.errors}",
        )
        assertTrue(
            result.errors.any {
                it is ValidationError.DevPointLengthNegative && it.indexInList == 1
            },
            "Got: ${result.errors}",
        )
        assertTrue(
            result.errors.any {
                it is ValidationError.DevPointsNotAscending && it.indexInList == 2
            },
            "Got: ${result.errors}",
        )
        assertTrue(
            result.errors.any {
                it is ValidationError.DevPointsDuplicatePhi &&
                    it.firstIndex == 2 && it.secondIndex == 3
            },
            "Got: ${result.errors}",
        )
        assertEquals(4, result.errors.size, "Got: ${result.errors}")
    }

    @Test
    fun `rejects non-finite phi and length`() {
        val dev = Development(
            listOf(
                DevPoint(phiDeg = Double.NaN, lengthMm = 10.0),
                DevPoint(phiDeg = 30.0, lengthMm = Double.NaN),
                DevPoint(phiDeg = Double.POSITIVE_INFINITY, lengthMm = 10.0),
                DevPoint(phiDeg = 90.0, lengthMm = Double.NEGATIVE_INFINITY),
            )
        )

        val result = DevelopmentValidator.validate(dev)
        assertTrue(result is Validated.Invalid, "Expected Invalid but was $result")
        result as Validated.Invalid

        val phiErrors = result.errors.filterIsInstance<ValidationError.DevPointPhiOutOfRange>()
        val lenErrors = result.errors.filterIsInstance<ValidationError.DevPointLengthNegative>()

        assertTrue(phiErrors.any { it.indexInList == 0 }, "expected phi error at idx 0")
        assertTrue(phiErrors.any { it.indexInList == 2 }, "expected phi error at idx 2")
        assertTrue(lenErrors.any { it.indexInList == 1 }, "expected length error at idx 1")
        assertTrue(lenErrors.any { it.indexInList == 3 }, "expected length error at idx 3")
    }
}
