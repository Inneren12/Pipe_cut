package com.oleksii.pipecut.ui.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.oleksii.pipecut.core.model.PointCount

@Composable
fun InputForm(
    state: InputUiState,
    onDiameterChange: (String) -> Unit,
    onTiltChange: (String) -> Unit,
    onClockingChange: (String) -> Unit,
    onOffsetChange: (String) -> Unit,
    onPointCountChange: (PointCount) -> Unit,
    onSaddleEnabledChange: (Boolean) -> Unit,
    onPartnerDiameterChange: (String) -> Unit,
    onIntersectionAngleChange: (String) -> Unit,
    onSaddleClockingChange: (String) -> Unit,
    onSaddleOffsetChange: (String) -> Unit,
    onCalculateClicked: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NumericField(
            value = state.diameterMm,
            onValueChange = onDiameterChange,
            label = "Pipe diameter (mm)",
            key = FieldKey.DIAMETER,
            state = state,
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Decimal,
        )
        NumericField(
            value = state.tiltDeg,
            onValueChange = onTiltChange,
            label = "Tilt α (°)",
            key = FieldKey.TILT,
            state = state,
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Decimal,
        )
        NumericField(
            value = state.clockingDeg,
            onValueChange = onClockingChange,
            label = "Clocking β (°)",
            key = FieldKey.CLOCKING,
            state = state,
            imeAction = ImeAction.Next,
            // Phone keyboard exposes the minus sign that Decimal hides on most IMEs.
            keyboardType = KeyboardType.Phone,
        )
        NumericField(
            value = state.offsetMm,
            onValueChange = onOffsetChange,
            label = "Offset L₀ (mm)",
            key = FieldKey.OFFSET,
            state = state,
            imeAction = if (state.saddleEnabled) ImeAction.Next else ImeAction.Done,
            keyboardType = KeyboardType.Decimal,
        )

        PointCountDropdown(
            value = state.pointCount,
            onValueChange = onPointCountChange,
        )

        SaddleSection(
            state = state,
            onSaddleEnabledChange = onSaddleEnabledChange,
            onPartnerDiameterChange = onPartnerDiameterChange,
            onIntersectionAngleChange = onIntersectionAngleChange,
            onSaddleClockingChange = onSaddleClockingChange,
            onSaddleOffsetChange = onSaddleOffsetChange,
        )

        Button(
            onClick = onCalculateClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Calculate")
        }
    }
}

@Composable
private fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    key: FieldKey,
    state: InputUiState,
    imeAction: ImeAction,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    val showError = key in state.touched && key in state.errors
    val errorText = state.errors[key]?.let { InputFieldStrings.message(it) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = showError,
        singleLine = true,
        supportingText = if (showError && errorText != null) {
            { Text(errorText, color = MaterialTheme.colorScheme.error) }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointCountDropdown(
    value: PointCount,
    onValueChange: (PointCount) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = value.value.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Points") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PointCount.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.value.toString()) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SaddleSection(
    state: InputUiState,
    onSaddleEnabledChange: (Boolean) -> Unit,
    onPartnerDiameterChange: (String) -> Unit,
    onIntersectionAngleChange: (String) -> Unit,
    onSaddleClockingChange: (String) -> Unit,
    onSaddleOffsetChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Saddle joint",
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = state.saddleEnabled,
                onCheckedChange = onSaddleEnabledChange,
            )
        }
        AnimatedVisibility(visible = state.saddleEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumericField(
                    value = state.partnerDiameterMm,
                    onValueChange = onPartnerDiameterChange,
                    label = "Partner Ø (mm)",
                    key = FieldKey.PARTNER_DIAMETER,
                    state = state,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                )
                NumericField(
                    value = state.intersectionAngleDeg,
                    onValueChange = onIntersectionAngleChange,
                    label = "Intersection (°)",
                    key = FieldKey.INTERSECTION_ANGLE,
                    state = state,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                )
                NumericField(
                    value = state.saddleClockingDeg,
                    onValueChange = onSaddleClockingChange,
                    label = "Saddle clocking (°)",
                    key = FieldKey.SADDLE_CLOCKING,
                    state = state,
                    imeAction = ImeAction.Next,
                    // Phone keyboard exposes the minus sign that Decimal hides on most IMEs.
                    keyboardType = KeyboardType.Phone,
                )
                NumericField(
                    value = state.saddleOffsetMm,
                    onValueChange = onSaddleOffsetChange,
                    label = "Saddle offset (mm)",
                    key = FieldKey.SADDLE_OFFSET,
                    state = state,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal,
                )
            }
        }
    }
}
