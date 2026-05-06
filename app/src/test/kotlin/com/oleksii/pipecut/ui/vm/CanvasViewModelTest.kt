package com.oleksii.pipecut.ui.vm

import com.oleksii.pipecut.ui.canvas.CanvasBox
import com.oleksii.pipecut.ui.canvas.CanvasTransform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CanvasViewModelTest {
    private val box = CanvasBox(1000f, 200f, 0f)

    @Test
    fun `initial state is identity transforms and zero rotation`() {
        val vm = CanvasViewModel()
        assertEquals(CanvasTransform.Identity, vm.transform2D.value)
        assertEquals(CanvasTransform.Identity, vm.transform3D.value)
        assertEquals(0f, vm.rotation3D.value, 1e-6f)
    }

    @Test
    fun `on2DTransform composes pan and zoom independently of 3D state`() {
        val vm = CanvasViewModel()
        vm.on2DTransform(panChangePx = 50f to 0f, zoomChange = 1.5f, box = box)
        assertEquals(1.5f, vm.transform2D.value.scalePx, 1e-6f)
        assertEquals(50f, vm.transform2D.value.panXPx, 1e-3f)
        assertEquals(CanvasTransform.Identity, vm.transform3D.value)
    }

    @Test
    fun `on3DTransform composes independently of 2D state`() {
        val vm = CanvasViewModel()
        vm.on3DTransform(panChangePx = 25f to 5f, zoomChange = 1.2f, box = box)
        assertEquals(1.2f, vm.transform3D.value.scalePx, 1e-6f)
        assertEquals(CanvasTransform.Identity, vm.transform2D.value)
    }

    @Test
    fun `on3DRotationDelta accumulates and stays in 0 to 360`() {
        val vm = CanvasViewModel()
        vm.on3DRotationDelta(45f)
        assertEquals(45f, vm.rotation3D.value, 1e-6f)
        vm.on3DRotationDelta(360f)
        assertEquals(45f, vm.rotation3D.value, 1e-6f)
        vm.on3DRotationDelta(-90f)
        assertEquals(315f, vm.rotation3D.value, 1e-6f)
    }

    @Test
    fun `reset2D clears only the 2D transform`() {
        val vm = CanvasViewModel()
        vm.on2DTransform(50f to 0f, 2f, box)
        vm.on3DTransform(50f to 0f, 2f, box)
        vm.on3DRotationDelta(45f)
        vm.reset2D()
        assertEquals(CanvasTransform.Identity, vm.transform2D.value)
        assertEquals(2f, vm.transform3D.value.scalePx, 1e-6f)
        assertEquals(45f, vm.rotation3D.value, 1e-6f)
    }

    @Test
    fun `reset3D clears the 3D transform and rotation but not 2D`() {
        val vm = CanvasViewModel()
        vm.on2DTransform(50f to 0f, 2f, box)
        vm.on3DTransform(50f to 0f, 2f, box)
        vm.on3DRotationDelta(45f)
        vm.reset3D()
        assertEquals(2f, vm.transform2D.value.scalePx, 1e-6f)
        assertEquals(CanvasTransform.Identity, vm.transform3D.value)
        assertEquals(0f, vm.rotation3D.value, 1e-6f)
    }
}
