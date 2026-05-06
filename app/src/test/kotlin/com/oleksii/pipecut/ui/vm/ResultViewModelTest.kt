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
        cut = CutPlane(tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 600.0),
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
        val req = planeRequest()
        vm.submit(req)
        val state = vm.uiState.value
        assertTrue(state is ResultUiState.Computed, "got $state")
        state as ResultUiState.Computed
        assertEquals(36, state.development.points.size)
        assertSame(req, state.request, "Computed must carry the request that produced it")
    }

    @Test
    fun `submitting a valid saddle request yields Computed`() {
        val vm = ResultViewModel()
        val req = saddleRequest()
        vm.submit(req)
        val state = vm.uiState.value
        assertTrue(state is ResultUiState.Computed, "got $state")
        state as ResultUiState.Computed
        assertEquals(36, state.development.points.size)
        assertSame(req, state.request)
    }

    @Test
    fun `submitting an offset-too-small request yields Error and preserves previous`() {
        val vm = ResultViewModel()
        val firstReq = planeRequest()
        vm.submit(firstReq)
        val firstDev = (vm.uiState.value as ResultUiState.Computed).development
        val brokenReq = planeRequest(diameterMm = 200.0, tiltDeg = 60.0, offsetMm = 10.0)
        vm.submit(brokenReq)
        val state = vm.uiState.value
        assertTrue(state is ResultUiState.Error, "got $state")
        state as ResultUiState.Error
        assertTrue(state.message.contains("offsetMm", ignoreCase = true), state.message)
        assertNotNull(state.previous)
        assertSame(firstDev, state.previous)
        assertSame(firstReq, state.previousRequest, "Error must remember the request that produced previous")
    }

    @Test
    fun `submitting null after a Computed result keeps Computed visible`() {
        val vm = ResultViewModel()
        val req = planeRequest()
        vm.submit(req)
        val firstDev = (vm.uiState.value as ResultUiState.Computed).development
        vm.submit(null)
        val current = vm.uiState.value as ResultUiState.Computed
        assertSame(firstDev, current.development)
        assertSame(req, current.request, "request must survive null-submit while Computed is sticky")
    }

    @Test
    fun `Error previousRequest tracks the last successful request, not the last submitted`() {
        val vm = ResultViewModel()
        val requestA = planeRequest(diameterMm = 100.0, tiltDeg = 28.0, offsetMm = 100.0)
        vm.submit(requestA)
        vm.uiState.value as ResultUiState.Computed
        val requestB = planeRequest(diameterMm = 200.0, tiltDeg = 20.0, offsetMm = 200.0)
        vm.submit(requestB)
        val computedB = vm.uiState.value as ResultUiState.Computed
        assertSame(requestB, computedB.request)
        val requestC = planeRequest(diameterMm = 200.0, tiltDeg = 60.0, offsetMm = 10.0)
        vm.submit(requestC)
        val errorState = vm.uiState.value as ResultUiState.Error
        assertSame(computedB.development, errorState.previous)
        assertSame(requestB, errorState.previousRequest)
    }
}
