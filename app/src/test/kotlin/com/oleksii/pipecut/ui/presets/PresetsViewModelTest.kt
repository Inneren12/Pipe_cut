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

private class SpyRepository(
    private val returnError: PresetSaveError? = null,
) : PresetsRepository {
    private val flow = MutableStateFlow<List<Preset>>(emptyList())
    var saveIfAllowedCalls: Int = 0
        private set
    var lastAllowOverwrite: Boolean? = null
        private set

    override fun observe(): Flow<List<Preset>> = flow

    override suspend fun saveIfAllowed(
        preset: Preset,
        maxPresets: Int,
        allowOverwrite: Boolean,
    ): PresetSaveError? {
        saveIfAllowedCalls += 1
        lastAllowOverwrite = allowOverwrite
        if (returnError != null) return returnError
        flow.value = (flow.value.filter { !it.name.equals(preset.name, ignoreCase = true) } + preset)
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return null
    }

    @Suppress("DEPRECATION")
    override suspend fun save(preset: Preset) {}

    override suspend fun delete(name: String) {
        flow.value = flow.value.filter { !it.name.equals(name, ignoreCase = true) }
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
    fun `trySave with empty name fails preflight without touching repository`() = runTest {
        val repo = SpyRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("   ", planeRequest())
        assertSame(PresetSaveError.EmptyName, vm.lastSaveError.value)
        assertEquals(0, repo.saveIfAllowedCalls)
    }

    @Test
    fun `trySave with null request fails preflight without touching repository`() = runTest {
        val repo = SpyRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", null)
        assertSame(PresetSaveError.NoValidRequest, vm.lastSaveError.value)
        assertEquals(0, repo.saveIfAllowedCalls)
    }

    @Test
    fun `trySave with name longer than max fails preflight without touching repository`() = runTest {
        val repo = SpyRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("a".repeat(MAX_NAME_LENGTH + 1), planeRequest())
        assertSame(PresetSaveError.NameTooLong, vm.lastSaveError.value)
        assertEquals(0, repo.saveIfAllowedCalls)
    }

    @Test
    fun `trySave with valid input delegates to saveIfAllowed and surfaces its error`() = runTest {
        val repo = SpyRepository(returnError = PresetSaveError.NameAlreadyExists)
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", planeRequest())
        assertSame(PresetSaveError.NameAlreadyExists, vm.lastSaveError.value)
        assertEquals(1, repo.saveIfAllowedCalls)
        assertEquals(false, repo.lastAllowOverwrite)
    }

    @Test
    fun `trySave with allowOverwrite forwards the flag to the repository`() = runTest {
        val repo = SpyRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", planeRequest(), allowOverwrite = true)
        assertEquals(true, repo.lastAllowOverwrite)
    }

    @Test
    fun `successful trySave clears the error`() = runTest {
        val vm = PresetsViewModel(SpyRepository())
        vm.trySave("", planeRequest())
        assertSame(PresetSaveError.EmptyName, vm.lastSaveError.value)
        vm.trySave("alpha", planeRequest())
        vm.presets.first { it.size == 1 }
        assertNull(vm.lastSaveError.value)
    }

    @Test
    fun `delete removes the preset`() = runTest {
        val repo = SpyRepository()
        val vm = PresetsViewModel(repo)
        vm.trySave("alpha", planeRequest())
        vm.presets.first { it.size == 1 }
        vm.delete("alpha")
        vm.presets.first { it.isEmpty() }
    }
}
