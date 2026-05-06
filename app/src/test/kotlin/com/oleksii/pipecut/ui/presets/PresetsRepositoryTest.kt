package com.oleksii.pipecut.ui.presets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PresetsRepositoryTest {

    @TempDir lateinit var tmp: File

    private val sharedStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { File(tmp, "test.preferences_pb") }
    }

    private fun newRepo(): PresetsRepository = DataStorePresetsRepository(sharedStore)

    private fun newRepoSharingTheSameStore(): PresetsRepository =
        DataStorePresetsRepository(sharedStore)

    private fun samplePreset(name: String) = Preset(
        name = name,
        pipe = PresetPipe(100.0),
        cut = PresetCutPlane(0.0, 0.0, 200.0),
        saddle = null,
        pointCountValue = 36,
    )

    @Suppress("DEPRECATION")
    @Test
    fun `save and observe round-trip`() = runTest {
        val repo = newRepo()
        repo.save(samplePreset("alpha"))
        repo.save(samplePreset("bravo"))
        val first = repo.observe().first()
        assertEquals(2, first.size)
        assertEquals(listOf("alpha", "bravo"), first.map { it.name })
    }

    @Suppress("DEPRECATION")
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
        repo.saveIfAllowed(samplePreset("alpha"), maxPresets = 5, allowOverwrite = false)
        repo.delete("alpha")
        assertTrue(repo.observe().first().isEmpty())
    }

    @Test
    fun `saveIfAllowed adds a new preset under the limit`() = runTest {
        val repo = newRepo()
        val err = repo.saveIfAllowed(
            preset = samplePreset("alpha"),
            maxPresets = 5,
            allowOverwrite = false,
        )
        assertNull(err)
        assertEquals(listOf("alpha"), repo.observe().first().map { it.name })
    }

    @Test
    fun `saveIfAllowed rejects duplicate name when allowOverwrite is false`() = runTest {
        val repo = newRepo()
        repo.saveIfAllowed(samplePreset("alpha"), maxPresets = 5, allowOverwrite = false)
        val err = repo.saveIfAllowed(
            preset = samplePreset("alpha").copy(cut = PresetCutPlane(99.0, 0.0, 200.0)),
            maxPresets = 5,
            allowOverwrite = false,
        )
        assertSame(PresetSaveError.NameAlreadyExists, err)
        val list = repo.observe().first()
        assertEquals(1, list.size)
        assertEquals(0.0, list[0].cut.tiltDeg, 1e-9)
    }

    @Test
    fun `saveIfAllowed treats names case-insensitively for duplicate detection`() = runTest {
        val repo = newRepo()
        repo.saveIfAllowed(samplePreset("Alpha"), maxPresets = 5, allowOverwrite = false)
        val err = repo.saveIfAllowed(samplePreset("alpha"), maxPresets = 5, allowOverwrite = false)
        assertSame(PresetSaveError.NameAlreadyExists, err)
    }

    @Test
    fun `saveIfAllowed with allowOverwrite replaces and does not grow the set`() = runTest {
        val repo = newRepo()
        repeat(5) { i ->
            repo.saveIfAllowed(samplePreset("name-$i"), maxPresets = 5, allowOverwrite = false)
        }
        val err = repo.saveIfAllowed(
            preset = samplePreset("name-3").copy(cut = PresetCutPlane(99.0, 0.0, 200.0)),
            maxPresets = 5,
            allowOverwrite = true,
        )
        assertNull(err)
        val list = repo.observe().first()
        assertEquals(5, list.size)
        val replaced = list.firstOrNull { it.name == "name-3" }
        assertEquals(99.0, replaced?.cut?.tiltDeg ?: 0.0, 1e-9)
    }

    @Test
    fun `saveIfAllowed enforces the limit on new preset additions`() = runTest {
        val repo = newRepo()
        repeat(5) { i ->
            repo.saveIfAllowed(samplePreset("name-$i"), maxPresets = 5, allowOverwrite = false)
        }
        val err = repo.saveIfAllowed(samplePreset("overflow"), maxPresets = 5, allowOverwrite = false)
        assertSame(PresetSaveError.LimitReached, err)
    }

    @Test
    fun `saveIfAllowed limit check sees the storage state and not a stale view`() = runTest {
        val repo = newRepo()
        repeat(20) { i ->
            repo.saveIfAllowed(samplePreset("name-$i"), maxPresets = 20, allowOverwrite = false)
        }
        val freshRepo = newRepoSharingTheSameStore()
        val err = freshRepo.saveIfAllowed(
            samplePreset("overflow"),
            maxPresets = 20,
            allowOverwrite = false,
        )
        assertSame(PresetSaveError.LimitReached, err)
    }
}
