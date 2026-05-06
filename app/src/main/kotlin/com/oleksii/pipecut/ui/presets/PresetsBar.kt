package com.oleksii.pipecut.ui.presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun LazyListScope.presetsBar(
    presets: List<Preset>,
    onLoad: (Preset) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteRequested: (Preset) -> Unit,
) {
    item("presets-bar") {
        PresetsBarContent(
            presets = presets,
            onLoad = onLoad,
            onSaveClicked = onSaveClicked,
            onDeleteRequested = onDeleteRequested,
        )
    }
}

@Composable
private fun PresetsBarContent(
    presets: List<Preset>,
    onLoad: (Preset) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteRequested: (Preset) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = PresetsStrings.BAR_TITLE,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onSaveClicked) {
                    Text(text = PresetsStrings.SAVE_BUTTON)
                }
            }
            if (presets.isEmpty()) {
                Text(
                    text = PresetsStrings.EMPTY_HINT,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = presets, key = { it.name }) { preset ->
                        PresetChip(
                            preset = preset,
                            onLoad = onLoad,
                            onDelete = onDeleteRequested,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: Preset,
    onLoad: (Preset) -> Unit,
    onDelete: (Preset) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AssistChip(
            onClick = { onLoad(preset) },
            label = { Text(preset.name) },
        )
        IconButton(
            onClick = { onDelete(preset) },
            modifier = Modifier.size(32.dp),
        ) {
            Text(
                text = "✕",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun PresetSaveDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = PresetsStrings.SAVE_DIALOG_TITLE) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(PresetsStrings.SAVE_DIALOG_NAME_LABEL) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = PresetsStrings.SAVE_DIALOG_OK)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = PresetsStrings.SAVE_DIALOG_CANCEL)
            }
        },
    )
}

@Composable
fun PresetDeleteDialog(
    presetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = PresetsStrings.deleteDialogTitle(presetName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(PresetsStrings.DELETE_DIALOG_OK) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(PresetsStrings.DELETE_DIALOG_CANCEL) }
        },
    )
}
