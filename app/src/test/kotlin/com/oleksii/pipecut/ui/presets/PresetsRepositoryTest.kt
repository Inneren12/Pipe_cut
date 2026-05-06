package com.oleksii.pipecut.ui.presets

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PresetsRepositoryTest {

    @TempDir lateinit var tmp: File

    private fun newRepo(): PresetsRepository {
        val store = PreferenceDataStoreFactory.create {
            File(tmp, "test.preferences_pb")
        }
        return DataStorePresetsRepository(store)
    }

    private fun samplePreset(name: String) = Preset(
        name = name,
        pipe = PresetPipe(100.0),
        cut = PresetCutPlane(0.0, 0.0, 200.0),
        saddle = null,
        pointCountValue = 36,
    )

    @Test
    fun `save and observe round-trip`() = runTest {
        val repo = newRepo()
        repo.save(samplePreset("alpha"))
        repo.save(samplePreset("bravo"))
        val first = repo.observe().first()
        assertEquals(2, first.size)
        assertEquals(listOf("alpha", "bravo"), first.map { it.name })
    }

    @Test
    fun `save with same name overwrites`() = runTest {
        val repo = newRepo()
        repo.save(samplePreset("alpha"))
        repo.save(samplePreset("alpha").copy(cut = PresetCutPlane(45.0, 0.0, 200.0)))
        val list = repo.observe().first()
        assertEquals(1, list.size)
        assertEquals(45.0, list[0].cut.tiltDeg, 1e-9)
    }

    @Test
    fun `delete removes the preset`() = runTest {
        val repo = newRepo()
        repo.save(samplePreset("alpha"))
        repo.delete("alpha")
        assertTrue(repo.observe().first().isEmpty())
    }
}
