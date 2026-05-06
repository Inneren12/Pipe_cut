package com.oleksii.pipecut.ui.presets

sealed interface PresetSaveError {
    object EmptyName : PresetSaveError
    object NameTooLong : PresetSaveError
    object NameAlreadyExists : PresetSaveError
    object LimitReached : PresetSaveError
    object NoValidRequest : PresetSaveError
}
