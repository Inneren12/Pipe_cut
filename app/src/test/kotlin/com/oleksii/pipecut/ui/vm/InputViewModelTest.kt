package com.oleksii.pipecut.ui.vm

import com.oleksii.pipecut.core.validation.ValidationError
import com.oleksii.pipecut.ui.input.FieldError
import com.oleksii.pipecut.ui.input.FieldKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InputViewModelTest {

    private val tol = 1e-9

    private fun fillValid(vm: InputViewModel) {
        vm.onDiameterChange("114.3")
        vm.onTiltChange("28")
        vm.onClockingChange("12")
        vm.onOffsetChange("100")
    }

    @Test
    fun `initial state has no errors and no last valid request`() {
        val vm = InputViewModel()
        assertTrue(vm.uiState.value.errors.isEmpty())
        assertTrue(vm.uiState.value.touched.isEmpty())
        assertNull(vm.lastValidRequest.value)
    }

    @Test
    fun `single field change marks it touched`() {
        val vm = InputViewModel()
        vm.onDiameterChange("100")
        assertTrue(FieldKey.DIAMETER in vm.uiState.value.touched)
    }

    @Test
    fun `entering a complete valid form yields a non-null lastValidRequest`() {
        val vm = InputViewModel()
        fillValid(vm)
        val req = vm.lastValidRequest.value
        assertNotNull(req)
        assertEquals(114.3, req!!.pipe.diameterMm, tol)
        assertEquals(28.0, req.cut.tiltDeg, tol)
    }

    @Test
    fun `entering a non-numeric diameter yields FieldError NotANumber on DIAMETER`() {
        val vm = InputViewModel()
        vm.onDiameterChange("abc")
        assertEquals(FieldError.NotANumber, vm.uiState.value.errors[FieldKey.DIAMETER])
    }

    @Test
    fun `blank required field yields FieldError Required after Calculate is tapped`() {
        val vm = InputViewModel()
        vm.onCalculateClicked()
        assertEquals(FieldError.Required, vm.uiState.value.errors[FieldKey.DIAMETER])
        assertTrue(FieldKey.DIAMETER in vm.uiState.value.touched)
    }

    @Test
    fun `domain error from validator surfaces as FieldError Domain on the right key`() {
        val vm = InputViewModel()
        vm.onDiameterChange("-1")
        vm.onTiltChange("28")
        vm.onClockingChange("12")
        vm.onOffsetChange("100")
        val err = vm.uiState.value.errors[FieldKey.DIAMETER]
        assertTrue(err is FieldError.Domain, "expected Domain error, got $err")
        val cause = (err as FieldError.Domain).cause
        assertTrue(cause is ValidationError.DiameterMustBePositive, "expected DiameterMustBePositive, got $cause")
        assertNull(vm.lastValidRequest.value)
    }

    @Test
    fun `toggling saddle off clears saddle errors`() {
        val vm = InputViewModel()
        vm.onSaddleEnabledChange(true)
        vm.onCalculateClicked()
        assertTrue(vm.uiState.value.errors.keys.any { it == FieldKey.PARTNER_DIAMETER })
        vm.onSaddleEnabledChange(false)
        val state = vm.uiState.value
        assertTrue(state.errors.keys.none {
            it == FieldKey.PARTNER_DIAMETER ||
                it == FieldKey.INTERSECTION_ANGLE ||
                it == FieldKey.SADDLE_CLOCKING ||
                it == FieldKey.SADDLE_OFFSET
        })
    }

    @Test
    fun `toggling saddle on requires the four saddle fields after Calculate`() {
        val vm = InputViewModel()
        fillValid(vm)
        vm.onSaddleEnabledChange(true)
        vm.onCalculateClicked()
        val errs = vm.uiState.value.errors
        assertEquals(FieldError.Required, errs[FieldKey.PARTNER_DIAMETER])
        assertEquals(FieldError.Required, errs[FieldKey.INTERSECTION_ANGLE])
        assertEquals(FieldError.Required, errs[FieldKey.SADDLE_CLOCKING])
        assertEquals(FieldError.Required, errs[FieldKey.SADDLE_OFFSET])
    }

    @Test
    fun `lastValidRequest stays non-null while user keeps editing valid values`() {
        val vm = InputViewModel()
        fillValid(vm)
        assertNotNull(vm.lastValidRequest.value)
        vm.onTiltChange("30")
        val req = vm.lastValidRequest.value
        assertNotNull(req)
        assertEquals(30.0, req!!.cut.tiltDeg, tol)
    }

    @Test
    fun `lastValidRequest is sticky once it has captured a valid request`() {
        val vm = InputViewModel()
        fillValid(vm)
        val firstValid = vm.lastValidRequest.value
        assertNotNull(firstValid, "form should be valid after fillValid")
        // Make the form invalid by erasing the diameter.
        vm.onDiameterChange("")
        val afterErase = vm.lastValidRequest.value
        assertNotNull(afterErase, "lastValidRequest must stay non-null after invalidation")
        // Sticky means we still see the previous successful request.
        assertEquals(firstValid, afterErase)
        // Type something invalid in another field too.
        vm.onTiltChange("not a number")
        assertEquals(firstValid, vm.lastValidRequest.value)
    }

    @Test
    fun `lastValidRequest updates on the next successful validation`() {
        val vm = InputViewModel()
        fillValid(vm)
        val firstValid = vm.lastValidRequest.value!!
        assertEquals(28.0, firstValid.cut.tiltDeg, 1e-9)
        // Break, then fix to a different valid value.
        vm.onTiltChange("invalid")
        assertEquals(firstValid, vm.lastValidRequest.value, "should still hold the first valid")
        vm.onTiltChange("30")
        val second = vm.lastValidRequest.value!!
        assertEquals(30.0, second.cut.tiltDeg, 1e-9)
    }

    @Test
    fun `lastValidRequest survives switching saddle on and back off`() {
        val vm = InputViewModel()
        fillValid(vm)
        val flatValid = vm.lastValidRequest.value
        assertNotNull(flatValid)
        // Turn saddle on; the form is now invalid (saddle fields are blank).
        vm.onSaddleEnabledChange(true)
        assertEquals(flatValid, vm.lastValidRequest.value, "sticky carries over on toggle")
        // Turn it back off; sticky still holds the original flat request.
        vm.onSaddleEnabledChange(false)
        assertEquals(flatValid, vm.lastValidRequest.value)
    }

    @Test
    fun `comma decimal separator is accepted`() {
        val vm = InputViewModel()
        vm.onDiameterChange("114,3")
        vm.onTiltChange("28")
        vm.onClockingChange("12")
        vm.onOffsetChange("100")
        assertNull(vm.uiState.value.errors[FieldKey.DIAMETER])
        assertEquals(114.3, vm.lastValidRequest.value!!.pipe.diameterMm, tol)
    }
}
