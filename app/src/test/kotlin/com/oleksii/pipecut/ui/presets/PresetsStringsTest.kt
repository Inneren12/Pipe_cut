package com.oleksii.pipecut.ui.presets

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PresetsStringsTest {

    @Test
    fun `every save error variant has a non-empty message`() {
        for (e in listOf(
            PresetSaveError.EmptyName,
            PresetSaveError.NameTooLong,
            PresetSaveError.NameAlreadyExists,
            PresetSaveError.LimitReached,
            PresetSaveError.NoValidRequest,
        )) {
            val msg = PresetsStrings.message(e)
            assertNotNull(msg)
            assertFalse(msg.isBlank(), "empty message for $e")
        }
    }

    @Test
    fun `every load error variant has a non-empty message`() {
        val msg = PresetsStrings.loadErrorMessage(PresetLoadError.InvalidPointCount(999))
        assertFalse(msg.isBlank())
        assertTrue(msg.contains("999"))
    }

    @Test
    fun `no constant leaks PR numbers`() {
        val all = listOf(
            PresetsStrings.BAR_TITLE,
            PresetsStrings.SAVE_BUTTON,
            PresetsStrings.SAVE_DIALOG_TITLE,
            PresetsStrings.SAVE_DIALOG_NAME_LABEL,
            PresetsStrings.SAVE_DIALOG_OK,
            PresetsStrings.SAVE_DIALOG_CANCEL,
            PresetsStrings.EMPTY_HINT,
            PresetsStrings.ERROR_EMPTY_NAME,
            PresetsStrings.ERROR_NAME_TOO_LONG,
            PresetsStrings.ERROR_NAME_EXISTS,
            PresetsStrings.ERROR_LIMIT_REACHED,
            PresetsStrings.ERROR_NO_VALID_REQUEST,
            PresetsStrings.OVERWRITE_DIALOG_TITLE,
            PresetsStrings.OVERWRITE_DIALOG_OK,
            PresetsStrings.OVERWRITE_DIALOG_CANCEL,
            PresetsStrings.overwriteDialogMessage("sample"),
            PresetsStrings.DELETE_DIALOG_OK,
            PresetsStrings.DELETE_DIALOG_CANCEL,
            PresetsStrings.deleteDialogTitle("sample"),
            PresetsStrings.deleteContentDescription("sample"),
            PresetsStrings.LOAD_ERROR_DIALOG_TITLE,
            PresetsStrings.LOAD_ERROR_DIALOG_OK,
            PresetsStrings.loadErrorMessage(PresetLoadError.InvalidPointCount(999)),
        )
        val pattern = Regex("\\bPR\\d+\\b")
        for (s in all) {
            assertFalse(pattern.containsMatchIn(s), "leak in: $s")
        }
    }
}
