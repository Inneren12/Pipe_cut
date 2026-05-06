package com.oleksii.pipecut.ui.presets

sealed interface PresetSaveError {
    object EmptyName : PresetSaveError
    object NameTooLong : PresetSaveError
    object NameAlreadyExists : PresetSaveError
    object LimitReached : PresetSaveError
    object NoValidRequest : PresetSaveError
}

/** Errors surfaced by [com.oleksii.pipecut.ui.vm.InputViewModel.applyPreset]. */
sealed interface PresetLoadError {
    /** Stored preset has a `pointCountValue` that does not map to any [com.oleksii.pipecut.core.model.PointCount]. */
    data class InvalidPointCount(val rawValue: Int) : PresetLoadError
}
