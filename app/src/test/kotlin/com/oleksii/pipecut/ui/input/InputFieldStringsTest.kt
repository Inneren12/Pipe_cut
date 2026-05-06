package com.oleksii.pipecut.ui.input

import com.oleksii.pipecut.core.validation.ValidationError
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InputFieldStringsTest {

    @Test
    fun `NotANumber message`() {
        val msg = InputFieldStrings.message(FieldError.NotANumber)
        assertTrue(msg.contains("number", ignoreCase = true), msg)
    }

    @Test
    fun `Required message`() {
        val msg = InputFieldStrings.message(FieldError.Required)
        assertTrue(msg.contains("Required", ignoreCase = true), msg)
    }

    @Test
    fun `DiameterMustBePositive message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.DiameterMustBePositive(-1.5)))
        assertTrue(msg.contains("Diameter"), msg)
        assertTrue(msg.contains("-1.5"), msg)
    }

    @Test
    fun `TiltOutOfRange message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.TiltOutOfRange(120.0)))
        assertTrue(msg.contains("Tilt"), msg)
        assertTrue(msg.contains("120.0"), msg)
    }

    @Test
    fun `ClockingOutOfRange message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.ClockingOutOfRange(400.0)))
        assertTrue(msg.contains("Clocking"), msg)
        assertTrue(msg.contains("400.0"), msg)
    }

    @Test
    fun `CutOffsetNegative message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.CutOffsetNegative(-2.0)))
        assertTrue(msg.contains("Offset"), msg)
        assertTrue(msg.contains("-2.0"), msg)
    }

    @Test
    fun `PartnerDiameterMustBePositive message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.PartnerDiameterMustBePositive(0.0)))
        assertTrue(msg.contains("Partner"), msg)
        assertTrue(msg.contains("0.0"), msg)
    }

    @Test
    fun `IntersectionAngleOutOfRange message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.IntersectionAngleOutOfRange(200.0)))
        assertTrue(msg.contains("Intersection"), msg)
        assertTrue(msg.contains("200.0"), msg)
    }

    @Test
    fun `SaddleClockingOutOfRange message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.SaddleClockingOutOfRange(-500.0)))
        assertTrue(msg.contains("Saddle"), msg)
        assertTrue(msg.contains("-500.0"), msg)
    }

    @Test
    fun `SaddleOffsetNegative message includes value and keyword`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.SaddleOffsetNegative(-3.0)))
        assertTrue(msg.contains("Saddle"), msg)
        assertTrue(msg.contains("-3.0"), msg)
    }

    @Test
    fun `DevelopmentEmpty falls back to internal error`() {
        val msg = InputFieldStrings.message(FieldError.Domain(ValidationError.DevelopmentEmpty))
        assertTrue(msg.contains("Internal error"), msg)
    }

    @Test
    fun `DevPointPhiOutOfRange falls back to internal error`() {
        val msg = InputFieldStrings.message(
            FieldError.Domain(ValidationError.DevPointPhiOutOfRange(0, 720.0)),
        )
        assertTrue(msg.contains("Internal error"), msg)
    }

    @Test
    fun `DevPointLengthNegative falls back to internal error`() {
        val msg = InputFieldStrings.message(
            FieldError.Domain(ValidationError.DevPointLengthNegative(0, -1.0)),
        )
        assertTrue(msg.contains("Internal error"), msg)
    }

    @Test
    fun `DevPointsNotAscending falls back to internal error`() {
        val msg = InputFieldStrings.message(
            FieldError.Domain(ValidationError.DevPointsNotAscending(1, 10.0, 5.0)),
        )
        assertTrue(msg.contains("Internal error"), msg)
    }

    @Test
    fun `DevPointsDuplicatePhi falls back to internal error`() {
        val msg = InputFieldStrings.message(
            FieldError.Domain(ValidationError.DevPointsDuplicatePhi(0, 1, 10.0)),
        )
        assertTrue(msg.contains("Internal error"), msg)
    }
}
