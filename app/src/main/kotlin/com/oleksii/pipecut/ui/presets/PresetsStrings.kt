package com.oleksii.pipecut.ui.presets

object PresetsStrings {
    const val BAR_TITLE: String = "Presets"
    const val SAVE_BUTTON: String = "Save preset"
    const val SAVE_DIALOG_TITLE: String = "Save current values as preset"
    const val SAVE_DIALOG_NAME_LABEL: String = "Name"
    const val SAVE_DIALOG_OK: String = "Save"
    const val SAVE_DIALOG_CANCEL: String = "Cancel"
    const val EMPTY_HINT: String = "No saved presets yet."

    const val ERROR_EMPTY_NAME: String = "Name cannot be empty."
    const val ERROR_NAME_TOO_LONG: String = "Name is too long (max 40 characters)."
    const val ERROR_NAME_EXISTS: String = "A preset with this name already exists."
    const val ERROR_LIMIT_REACHED: String = "Preset limit reached (20). Delete one first."
    const val ERROR_NO_VALID_REQUEST: String = "Enter valid values before saving."

    const val OVERWRITE_DIALOG_TITLE: String = "Name already exists"
    const val OVERWRITE_DIALOG_OK: String = "Overwrite"
    const val OVERWRITE_DIALOG_CANCEL: String = "Cancel"
    fun overwriteDialogMessage(name: String): String =
        "A preset called \"$name\" already exists. Overwrite it?"

    const val DELETE_DIALOG_OK: String = "Delete"
    const val DELETE_DIALOG_CANCEL: String = "Cancel"
    fun deleteDialogTitle(name: String): String = "Delete preset \"$name\"?"
    fun deleteContentDescription(name: String): String = "Delete preset $name"

    const val LOAD_ERROR_DIALOG_TITLE: String = "Could not load preset"
    const val LOAD_ERROR_DIALOG_OK: String = "Ok"

    fun message(error: PresetSaveError): String = when (error) {
        PresetSaveError.EmptyName -> ERROR_EMPTY_NAME
        PresetSaveError.NameTooLong -> ERROR_NAME_TOO_LONG
        PresetSaveError.NameAlreadyExists -> ERROR_NAME_EXISTS
        PresetSaveError.LimitReached -> ERROR_LIMIT_REACHED
        PresetSaveError.NoValidRequest -> ERROR_NO_VALID_REQUEST
    }

    fun loadErrorMessage(error: PresetLoadError): String = when (error) {
        is PresetLoadError.InvalidPointCount ->
            "Stored preset has an unknown point count (${error.rawValue}). " +
                "Cannot load it without losing data; please delete or recreate it."
    }
}
