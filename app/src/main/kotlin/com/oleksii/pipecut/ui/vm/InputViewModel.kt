package com.oleksii.pipecut.ui.vm

import androidx.lifecycle.ViewModel
import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import com.oleksii.pipecut.core.validation.CutRequestValidator
import com.oleksii.pipecut.core.validation.Validated
import com.oleksii.pipecut.core.validation.ValidationError
import com.oleksii.pipecut.ui.input.FieldError
import com.oleksii.pipecut.ui.input.FieldKey
import com.oleksii.pipecut.ui.input.InputUiState
import com.oleksii.pipecut.ui.presets.Preset
import com.oleksii.pipecut.ui.presets.PresetLoadError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class InputViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    /**
     * Sticky last `CutRequest` that successfully passed validation.
     *
     *  - Null until the user submits a valid request for the first time.
     *  - Once non-null, **stays** non-null. Subsequent invalid edits do not
     *    wipe it. The next successful validation overwrites it.
     *
     * This contract lets downstream consumers (table, canvas) keep showing
     * the previous result while the user is mid-edit, instead of flickering
     * to an empty state on every keystroke.
     */
    private val _lastValidRequest = MutableStateFlow<CutRequest?>(null)
    val lastValidRequest: StateFlow<CutRequest?> = _lastValidRequest.asStateFlow()

    fun onDiameterChange(text: String) = updateField(FieldKey.DIAMETER) { it.copy(diameterMm = text) }
    fun onTiltChange(text: String) = updateField(FieldKey.TILT) { it.copy(tiltDeg = text) }
    fun onClockingChange(text: String) = updateField(FieldKey.CLOCKING) { it.copy(clockingDeg = text) }
    fun onOffsetChange(text: String) = updateField(FieldKey.OFFSET) { it.copy(offsetMm = text) }
    fun onPointCountChange(value: PointCount) =
        updateField(FieldKey.POINT_COUNT) { it.copy(pointCount = value) }

    fun onSaddleEnabledChange(enabled: Boolean) {
        _uiState.update { state ->
            val touched = state.touched.filterNot { it.isSaddleField() }.toSet()
            val errors = state.errors.filterKeys { !it.isSaddleField() }
            state.copy(saddleEnabled = enabled, touched = touched, errors = errors)
        }
        revalidate()
    }

    fun onPartnerDiameterChange(text: String) =
        updateField(FieldKey.PARTNER_DIAMETER) { it.copy(partnerDiameterMm = text) }
    fun onIntersectionAngleChange(text: String) =
        updateField(FieldKey.INTERSECTION_ANGLE) { it.copy(intersectionAngleDeg = text) }
    fun onSaddleClockingChange(text: String) =
        updateField(FieldKey.SADDLE_CLOCKING) { it.copy(saddleClockingDeg = text) }
    fun onSaddleOffsetChange(text: String) =
        updateField(FieldKey.SADDLE_OFFSET) { it.copy(saddleOffsetMm = text) }

    /**
     * Atomically apply the given preset to every form field.
     *
     *  - Plane fields: diameter, tilt, clocking, offset overwritten.
     *  - Saddle: when present, all four saddle fields populated and
     *    `saddleEnabled = true`. When absent, saddle is fully cleared
     *    so a later toggle-on does not surface leftover values.
     *  - PointCount: validated against [PointCount.entries]. An unknown
     *    `pointCountValue` aborts the load with
     *    [PresetLoadError.InvalidPointCount] and no field is touched.
     */
    fun applyPreset(preset: Preset): PresetLoadError? {
        val resolvedPointCount = PointCount.entries
            .firstOrNull { it.value == preset.pointCountValue }
            ?: return PresetLoadError.InvalidPointCount(preset.pointCountValue)
        onDiameterChange(formatNumber(preset.pipe.diameterMm))
        onTiltChange(formatNumber(preset.cut.tiltDeg))
        onClockingChange(formatNumber(preset.cut.clockingDeg))
        onOffsetChange(formatNumber(preset.cut.offsetMm))
        val saddle = preset.saddle
        if (saddle != null) {
            onSaddleEnabledChange(true)
            onPartnerDiameterChange(formatNumber(saddle.partnerDiameterMm))
            onIntersectionAngleChange(formatNumber(saddle.intersectionAngleDeg))
            onSaddleClockingChange(formatNumber(saddle.clockingDeg))
            onSaddleOffsetChange(formatNumber(saddle.offsetMm))
        } else {
            onSaddleEnabledChange(false)
            onPartnerDiameterChange("")
            onIntersectionAngleChange("")
            onSaddleClockingChange("")
            onSaddleOffsetChange("")
        }
        onPointCountChange(resolvedPointCount)
        onCalculateClicked()
        return null
    }

    private fun formatNumber(value: Double): String {
        val s = String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        return if (s.isEmpty()) "0" else s
    }

    /** Called when the user taps "Calculate". Marks every field touched and validates. */
    fun onCalculateClicked() {
        _uiState.update { state ->
            state.copy(touched = allFieldKeys(state.saddleEnabled))
        }
        revalidate()
    }

    private inline fun updateField(key: FieldKey, transform: (InputUiState) -> InputUiState) {
        _uiState.update { state ->
            transform(state).copy(touched = state.touched + key)
        }
        revalidate()
    }

    private fun revalidate() {
        val state = _uiState.value
        val (request, parseErrors) = parse(state)
        val errors = mutableMapOf<FieldKey, FieldError>()
        errors.putAll(parseErrors)

        val validatedRequest: CutRequest? = if (request != null) {
            when (val v = CutRequestValidator.validate(request)) {
                is Validated.Valid -> request
                is Validated.Invalid -> {
                    v.errors.forEach { e -> mapDomainError(e, errors) }
                    null
                }
            }
        } else null

        _uiState.update { it.copy(errors = errors) }
        if (validatedRequest != null) {
            _lastValidRequest.value = validatedRequest
        }
    }

    private fun parse(state: InputUiState): Pair<CutRequest?, Map<FieldKey, FieldError>> {
        val parseErrors = mutableMapOf<FieldKey, FieldError>()
        val diameter = parseDouble(state.diameterMm, FieldKey.DIAMETER, parseErrors)
        val tilt = parseDouble(state.tiltDeg, FieldKey.TILT, parseErrors)
        val clocking = parseDouble(state.clockingDeg, FieldKey.CLOCKING, parseErrors)
        val offset = parseDouble(state.offsetMm, FieldKey.OFFSET, parseErrors)

        val saddle: SaddleSpec? = if (state.saddleEnabled) {
            val partner = parseDouble(state.partnerDiameterMm, FieldKey.PARTNER_DIAMETER, parseErrors)
            val intersection = parseDouble(state.intersectionAngleDeg, FieldKey.INTERSECTION_ANGLE, parseErrors)
            val saddleClocking = parseDouble(state.saddleClockingDeg, FieldKey.SADDLE_CLOCKING, parseErrors)
            val saddleOffset = parseDouble(state.saddleOffsetMm, FieldKey.SADDLE_OFFSET, parseErrors)
            if (partner != null && intersection != null && saddleClocking != null && saddleOffset != null) {
                SaddleSpec(
                    partnerDiameterMm = partner,
                    intersectionAngleDeg = intersection,
                    clockingDeg = saddleClocking,
                    offsetMm = saddleOffset,
                )
            } else null
        } else null

        val request = if (diameter != null && tilt != null && clocking != null && offset != null &&
            (!state.saddleEnabled || saddle != null)
        ) {
            CutRequest(
                pipe = PipeSpec(diameterMm = diameter),
                cut = CutPlane(tiltDeg = tilt, clockingDeg = clocking, offsetMm = offset),
                saddle = saddle,
                pointCount = state.pointCount,
            )
        } else null

        return request to parseErrors
    }

    private fun parseDouble(
        text: String,
        key: FieldKey,
        sink: MutableMap<FieldKey, FieldError>,
    ): Double? {
        if (text.isBlank()) {
            sink[key] = FieldError.Required
            return null
        }
        val normalized = text.trim().replace(',', '.')
        val value = normalized.toDoubleOrNull()
        if (value == null) {
            sink[key] = FieldError.NotANumber
        }
        return value
    }

    private fun mapDomainError(error: ValidationError, sink: MutableMap<FieldKey, FieldError>) {
        val key = when (error) {
            is ValidationError.DiameterMustBePositive -> FieldKey.DIAMETER
            is ValidationError.TiltOutOfRange -> FieldKey.TILT
            is ValidationError.ClockingOutOfRange -> FieldKey.CLOCKING
            is ValidationError.CutOffsetNegative -> FieldKey.OFFSET
            is ValidationError.PartnerDiameterMustBePositive -> FieldKey.PARTNER_DIAMETER
            is ValidationError.IntersectionAngleOutOfRange -> FieldKey.INTERSECTION_ANGLE
            is ValidationError.SaddleClockingOutOfRange -> FieldKey.SADDLE_CLOCKING
            is ValidationError.SaddleOffsetNegative -> FieldKey.SADDLE_OFFSET
            ValidationError.DevelopmentEmpty,
            is ValidationError.DevPointPhiOutOfRange,
            is ValidationError.DevPointLengthNegative,
            is ValidationError.DevPointsNotAscending,
            is ValidationError.DevPointsDuplicatePhi -> return
        }
        sink[key] = FieldError.Domain(error)
    }

    private fun FieldKey.isSaddleField(): Boolean = when (this) {
        FieldKey.PARTNER_DIAMETER,
        FieldKey.INTERSECTION_ANGLE,
        FieldKey.SADDLE_CLOCKING,
        FieldKey.SADDLE_OFFSET -> true
        else -> false
    }

    private fun allFieldKeys(saddleEnabled: Boolean): Set<FieldKey> {
        val base = setOf(
            FieldKey.DIAMETER,
            FieldKey.TILT,
            FieldKey.CLOCKING,
            FieldKey.OFFSET,
            FieldKey.POINT_COUNT,
        )
        return if (saddleEnabled) {
            base + setOf(
                FieldKey.PARTNER_DIAMETER,
                FieldKey.INTERSECTION_ANGLE,
                FieldKey.SADDLE_CLOCKING,
                FieldKey.SADDLE_OFFSET,
            )
        } else base
    }
}
