package com.oleksii.pipecut.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.ui.presets.MAX_NAME_LENGTH
import com.oleksii.pipecut.ui.presets.MAX_PRESETS
import com.oleksii.pipecut.ui.presets.Preset
import com.oleksii.pipecut.ui.presets.PresetSaveError
import com.oleksii.pipecut.ui.presets.PresetsRepository
import com.oleksii.pipecut.ui.presets.toPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PresetsViewModel(
    private val repository: PresetsRepository,
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = repository.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )

    private val _lastSaveError = MutableStateFlow<PresetSaveError?>(null)
    val lastSaveError: StateFlow<PresetSaveError?> = _lastSaveError.asStateFlow()

    fun trySave(rawName: String, request: CutRequest?, allowOverwrite: Boolean = false) {
        val name = rawName.trim()
        val preflight: PresetSaveError? = when {
            request == null -> PresetSaveError.NoValidRequest
            name.isEmpty() -> PresetSaveError.EmptyName
            name.length > MAX_NAME_LENGTH -> PresetSaveError.NameTooLong
            else -> null
        }
        if (preflight != null) {
            _lastSaveError.value = preflight
            return
        }
        viewModelScope.launch {
            val error = repository.saveIfAllowed(
                preset = request!!.toPreset(name),
                maxPresets = MAX_PRESETS,
                allowOverwrite = allowOverwrite,
            )
            _lastSaveError.value = error
        }
    }

    fun delete(name: String) {
        viewModelScope.launch { repository.delete(name) }
    }

    fun acknowledgeError() {
        _lastSaveError.value = null
    }
}
