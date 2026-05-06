package com.oleksii.pipecut.ui.vm

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import com.oleksii.pipecut.ui.result.ResultUiState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResultViewModelTest {

    private fun planeRequest(
        diameterMm: Double = 114.3,
        tiltDeg: Double = 28.0,
        clockingDeg: Double = 12.0,
        offsetMm: Double = 100.0,
        pointCount: PointCount = PointCount.P36,
    ) = CutRequest(
        pipe = PipeSpec(diameterMm = diameterMm),
        cut = CutPlane(tiltDeg = tiltDeg, clockingDeg = clockingDeg, offsetMm = offsetMm),
        saddle = null,
        pointCount = pointCount,
    )

    private fun saddleRequest() = CutRequest(
        pipe = PipeSpec(diameterMm = 114.3),
        cut = CutPlane(tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 250.0),
        saddle = SaddleSpec(
            partnerDiameterMm = 914.4,
            intersectionAngleDeg = 62.0,
            clockingDeg = 0.0,
            offsetMm = 0.0,
        ),
        pointCount = PointCount.P36,
    )

    @Test
    fun `initial state is Empty`() {
        val vm = ResultViewModel()
        assertSame(ResultUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `submitting null while still Empty stays Empty`() {
        val vm = ResultViewModel()
        vm.submit(null)
        assertSame(ResultUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `submitting a valid plane request yields Computed`() {
        val vm = ResultViewModel()
        vm.submit(planeRequest())
        val state = vm.uiState.value
        assertTrue(state is ResultUiState.Computed, "got $state")
        state as ResultUiState.Computed
        assertEquals(36, state.development.points.size)
    }

    @Test
    fun `submitting a saddle request yields SaddleNotImplemented`() {
        val vm = ResultViewModel()
        vm.submit(saddleRequest())
        assertSame(ResultUiState.SaddleNotImplemented, vm.uiState.value)
    }

    @Test
    fun `submitting an offset-too-small request yields Error and preserves previous`() {
        val vm = ResultViewModel()
        // First, a successful calculation to populate `previous`.
        vm.submit(planeRequest())
        val firstDev = (vm.uiState.value as ResultUiState.Computed).development
        // Then, an invalid one (offset too small for tilt = 60° on D = 200).
        vm.submit(planeRequest(diameterMm = 200.0, tiltDeg = 60.0, offsetMm = 10.0))
        val state = vm.uiState.value
        assertTrue(state is ResultUiState.Error, "got $state")
        state as ResultUiState.Error
        assertTrue(state.message.contains("offsetMm", ignoreCase = true), state.message)
        assertNotNull(state.previous)
        assertSame(firstDev, state.previous)
    }

    @Test
    fun `submitting null after a Computed result keeps Computed visible`() {
        val vm = ResultViewModel()
        vm.submit(planeRequest())
        val firstDev = (vm.uiState.value as ResultUiState.Computed).development
        vm.submit(null)
        assertSame(firstDev, (vm.uiState.value as ResultUiState.Computed).development)
    }
}
