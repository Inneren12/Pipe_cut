package com.oleksii.pipecut.ui.presets

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.ui.vm.PresetsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakePresetsRepository : PresetsRepository {
    private val flow = MutableStateFlow<List<Preset>>(emptyList())
    override fun observe(): Flow<List<Preset>> = flow
    override suspend fun save(preset: Preset) {
        flow.value = (flow.value.filter { it.name != preset.name } + preset).sortedBy { it.name }
    }
    override suspend fun delete(name: String) {
        flow.value = flow.value.filter { it.name != name }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PresetsViewModelTest {

    @BeforeEach
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun planeRequest() = CutRequest(
        pipe = PipeSpec(diameterMm = 100.0),
        cut = CutPlane(0.0, 0.0, 200.0),
        saddle = null,
        pointCount = PointCount.P36,
    )

    @Test
    fun `trySave with empty name yields EmptyName error and saves nothing`() = runTest {
        val repo = FakePresetsRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("   ", planeRequest())
        assertSame(PresetSaveError.EmptyName, vm.lastSaveError.value)
        assertEquals(0, vm.presets.value.size)
    }

    @Test
    fun `trySave with null request yields NoValidRequest`() = runTest {
        val vm = PresetsViewModel(FakePresetsRepository())
        vm.trySave("alpha", null)
        assertSame(PresetSaveError.NoValidRequest, vm.lastSaveError.value)
    }

    @Test
    fun `trySave with duplicate name yields NameAlreadyExists`() = runTest {
        val repo = FakePresetsRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", planeRequest())
        vm.presets.first { it.size == 1 }
        vm.trySave("alpha", planeRequest())
        assertSame(PresetSaveError.NameAlreadyExists, vm.lastSaveError.value)
    }

    @Test
    fun `trySave with name longer than max yields NameTooLong`() = runTest {
        val vm = PresetsViewModel(FakePresetsRepository())
        vm.trySave("a".repeat(MAX_NAME_LENGTH + 1), planeRequest())
        assertSame(PresetSaveError.NameTooLong, vm.lastSaveError.value)
    }

    @Test
    fun `trySave at MAX_PRESETS yields LimitReached`() = runTest {
        val repo = FakePresetsRepository()
        val vm = PresetsViewModel(repo)
        repeat(MAX_PRESETS) { i ->
            vm.trySave("name-$i", planeRequest())
            vm.presets.first { it.size == i + 1 }
        }
        vm.trySave("overflow", planeRequest())
        assertSame(PresetSaveError.LimitReached, vm.lastSaveError.value)
    }

    @Test
    fun `successful trySave clears the error`() = runTest {
        val vm = PresetsViewModel(FakePresetsRepository())
        vm.trySave("", planeRequest())
        assertSame(PresetSaveError.EmptyName, vm.lastSaveError.value)
        vm.trySave("alpha", planeRequest())
        vm.presets.first { it.size == 1 }
        assertNull(vm.lastSaveError.value)
    }

    @Test
    fun `delete removes the preset`() = runTest {
        val repo = FakePresetsRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", planeRequest())
        vm.presets.first { it.size == 1 }
        vm.delete("alpha")
        vm.presets.first { it.isEmpty() }
    }
}
