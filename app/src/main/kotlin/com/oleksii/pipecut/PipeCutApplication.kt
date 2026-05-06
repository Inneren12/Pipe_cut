package com.oleksii.pipecut

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import com.oleksii.pipecut.ui.presets.DataStorePresetsRepository
import com.oleksii.pipecut.ui.presets.PresetsRepository

private val Application.presetsDataStore by preferencesDataStore("pipe_cut_presets")

class PipeCutApplication : Application() {
    val presetsRepository: PresetsRepository by lazy {
        DataStorePresetsRepository(presetsDataStore)
    }
}
