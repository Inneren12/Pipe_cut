package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CutRequestValidatorTest {

    private val validPipe = PipeSpec(diameterMm = 114.3)
    private val validCut = CutPlane(tiltDeg = 28.0, clockingDeg = 12.0, offsetMm = 150.0)
    private val validSaddle = SaddleSpec(
        partnerDiameterMm = 914.4,
        intersectionAngleDeg = 62.0,
        clockingDeg = 0.0,
        offsetMm = 0.0,
    )

    private fun request(
        pipe: PipeSpec = validPipe,
        cut: CutPlane = validCut,
        saddle: SaddleSpec? = null,
        pointCount: PointCount = PointCount.P36,
    ): CutRequest = CutRequest(pipe = pipe, cut = cut, saddle = saddle, pointCount = pointCount)

    private fun assertValid(result: Validated<CutRequest>, expected: CutRequest) {
        assertTrue(result is Validated.Valid, "Expected Valid but was $result")
        result as Validated.Valid
        assertSame(expected, result.value)
    }

    private fun assertInvalidContains(
        result: Validated<CutRequest>,
        vararg expectedTypes: Class<out ValidationError>,
    ): List<ValidationError> {
        assertTrue(result is Validated.Invalid, "Expected Invalid but was $result")
        result as Validated.Invalid
        for (type in expectedTypes) {
            assertTrue(
                result.errors.any { type.isInstance(it) },
                "Expected an error of type ${type.simpleName} in ${result.errors}",
            )
        }
        return result.errors
    }

    // --- Positive cases ----------------------------------------------------

    @Test
    fun `accepts a typical reference request — D=114, alpha=28, beta=12, no saddle`() {
        val req = request()
        assertValid(CutRequestValidator.validate(req), req)
    }

    @Test
    fun `accepts a saddle request — partner=914, theta=62, psi=0, eccentric=0`() {
        val req = request(saddle = validSaddle)
        assertValid(CutRequestValidator.validate(req), req)
    }

    @Test
    fun `accepts boundary tilt 0 and 89 degrees`() {
        val low = request(cut = validCut.copy(tiltDeg = 0.0))
        val high = request(cut = validCut.copy(tiltDeg = 89.0))
        assertValid(CutRequestValidator.validate(low), low)
        assertValid(CutRequestValidator.validate(high), high)
    }

    @Test
    fun `accepts boundary clocking -360 and 360 degrees`() {
        val low = request(cut = validCut.copy(clockingDeg = -360.0))
        val high = request(cut = validCut.copy(clockingDeg = 360.0))
        assertValid(CutRequestValidator.validate(low), low)
        assertValid(CutRequestValidator.validate(high), high)
    }

    @Test
    fun `accepts cut offset 0`() {
        val req = request(cut = validCut.copy(offsetMm = 0.0))
        assertValid(CutRequestValidator.validate(req), req)
    }

    // --- Negative cases ----------------------------------------------------

    @Test
    fun `rejects zero diameter`() {
        val req = request(pipe = validPipe.copy(diameterMm = 0.0))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.DiameterMustBePositive::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.DiameterMustBePositive>().single()
        assertEquals(0.0, err.actualMm, 1e-9)
    }

    @Test
    fun `rejects negative diameter`() {
        val req = request(pipe = validPipe.copy(diameterMm = -1.0))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.DiameterMustBePositive::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.DiameterMustBePositive>().single()
        assertEquals(-1.0, err.actualMm, 1e-9)
    }

    @Test
    fun `rejects tilt below zero`() {
        val req = request(cut = validCut.copy(tiltDeg = -0.1))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.TiltOutOfRange::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.TiltOutOfRange>().single()
        assertEquals(-0.1, err.actualDeg, 1e-9)
    }

    @Test
    fun `rejects tilt above 89`() {
        val req = request(cut = validCut.copy(tiltDeg = 89.1))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.TiltOutOfRange::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.TiltOutOfRange>().single()
        assertEquals(89.1, err.actualDeg, 1e-9)
    }

    @Test
    fun `rejects clocking outside +- 360`() {
        val tooLow = request(cut = validCut.copy(clockingDeg = -360.1))
        val tooHigh = request(cut = validCut.copy(clockingDeg = 360.1))

        val lowErrors = assertInvalidContains(
            CutRequestValidator.validate(tooLow),
            ValidationError.ClockingOutOfRange::class.java,
        )
        assertEquals(
            -360.1,
            lowErrors.filterIsInstance<ValidationError.ClockingOutOfRange>().single().actualDeg,
            1e-9,
        )

        val highErrors = assertInvalidContains(
            CutRequestValidator.validate(tooHigh),
            ValidationError.ClockingOutOfRange::class.java,
        )
        assertEquals(
            360.1,
            highErrors.filterIsInstance<ValidationError.ClockingOutOfRange>().single().actualDeg,
            1e-9,
        )
    }

    @Test
    fun `rejects negative cut offset`() {
        val req = request(cut = validCut.copy(offsetMm = -0.5))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.CutOffsetNegative::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.CutOffsetNegative>().single()
        assertEquals(-0.5, err.actualMm, 1e-9)
    }

    @Test
    fun `rejects partner diameter less than or equal to 0`() {
        val zero = request(saddle = validSaddle.copy(partnerDiameterMm = 0.0))
        val negative = request(saddle = validSaddle.copy(partnerDiameterMm = -10.0))

        val zeroErrors = assertInvalidContains(
            CutRequestValidator.validate(zero),
            ValidationError.PartnerDiameterMustBePositive::class.java,
        )
        assertEquals(
            0.0,
            zeroErrors.filterIsInstance<ValidationError.PartnerDiameterMustBePositive>()
                .single().actualMm,
            1e-9,
        )

        val negErrors = assertInvalidContains(
            CutRequestValidator.validate(negative),
            ValidationError.PartnerDiameterMustBePositive::class.java,
        )
        assertEquals(
            -10.0,
            negErrors.filterIsInstance<ValidationError.PartnerDiameterMustBePositive>()
                .single().actualMm,
            1e-9,
        )
    }

    @Test
    fun `rejects intersection angle 0 and 180 (exclusive bounds)`() {
        val zero = request(saddle = validSaddle.copy(intersectionAngleDeg = 0.0))
        val oneEighty = request(saddle = validSaddle.copy(intersectionAngleDeg = 180.0))

        assertInvalidContains(
            CutRequestValidator.validate(zero),
            ValidationError.IntersectionAngleOutOfRange::class.java,
        )
        assertInvalidContains(
            CutRequestValidator.validate(oneEighty),
            ValidationError.IntersectionAngleOutOfRange::class.java,
        )
    }

    @Test
    fun `rejects intersection angle outside (0, 180)`() {
        val tooLow = request(saddle = validSaddle.copy(intersectionAngleDeg = -1.0))
        val tooHigh = request(saddle = validSaddle.copy(intersectionAngleDeg = 181.0))

        val lowErrors = assertInvalidContains(
            CutRequestValidator.validate(tooLow),
            ValidationError.IntersectionAngleOutOfRange::class.java,
        )
        assertEquals(
            -1.0,
            lowErrors.filterIsInstance<ValidationError.IntersectionAngleOutOfRange>()
                .single().actualDeg,
            1e-9,
        )

        val highErrors = assertInvalidContains(
            CutRequestValidator.validate(tooHigh),
            ValidationError.IntersectionAngleOutOfRange::class.java,
        )
        assertEquals(
            181.0,
            highErrors.filterIsInstance<ValidationError.IntersectionAngleOutOfRange>()
                .single().actualDeg,
            1e-9,
        )
    }

    @Test
    fun `rejects negative saddle offset`() {
        val req = request(saddle = validSaddle.copy(offsetMm = -0.001))
        val errors = assertInvalidContains(
            CutRequestValidator.validate(req),
            ValidationError.SaddleOffsetNegative::class.java,
        )
        val err = errors.filterIsInstance<ValidationError.SaddleOffsetNegative>().single()
        assertEquals(-0.001, err.actualMm, 1e-9)
    }

    @Test
    fun `rejects saddle clocking outside +- 360`() {
        val tooLow = request(saddle = validSaddle.copy(clockingDeg = -360.5))
        val tooHigh = request(saddle = validSaddle.copy(clockingDeg = 360.5))

        val lowErrors = assertInvalidContains(
            CutRequestValidator.validate(tooLow),
            ValidationError.SaddleClockingOutOfRange::class.java,
        )
        assertEquals(
            -360.5,
            lowErrors.filterIsInstance<ValidationError.SaddleClockingOutOfRange>()
                .single().actualDeg,
            1e-9,
        )

        val highErrors = assertInvalidContains(
            CutRequestValidator.validate(tooHigh),
            ValidationError.SaddleClockingOutOfRange::class.java,
        )
        assertEquals(
            360.5,
            highErrors.filterIsInstance<ValidationError.SaddleClockingOutOfRange>()
                .single().actualDeg,
            1e-9,
        )
    }

    // --- Accumulation ------------------------------------------------------

    @Test
    fun `accumulates multiple errors in one pass`() {
        val req = request(
            pipe = PipeSpec(diameterMm = -5.0),
            cut = CutPlane(tiltDeg = 95.0, clockingDeg = 0.0, offsetMm = 0.0),
            saddle = SaddleSpec(
                partnerDiameterMm = 0.0,
                intersectionAngleDeg = 200.0,
                clockingDeg = 0.0,
                offsetMm = 0.0,
            ),
        )
        val result = CutRequestValidator.validate(req)
        assertTrue(result is Validated.Invalid, "Expected Invalid but was $result")
        result as Validated.Invalid
        assertEquals(4, result.errors.size, "Got: ${result.errors}")
        assertTrue(result.errors.any { it is ValidationError.DiameterMustBePositive })
        assertTrue(result.errors.any { it is ValidationError.TiltOutOfRange })
        assertTrue(result.errors.any { it is ValidationError.PartnerDiameterMustBePositive })
        assertTrue(result.errors.any { it is ValidationError.IntersectionAngleOutOfRange })
    }

    @Test
    fun `does not validate saddle when saddle is null`() {
        val req = request(
            pipe = PipeSpec(diameterMm = -1.0),
            saddle = null,
        )
        val result = CutRequestValidator.validate(req)
        assertTrue(result is Validated.Invalid)
        result as Validated.Invalid
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.single() is ValidationError.DiameterMustBePositive)
    }
}
