package com.oleksii.pipecut.ui.presets

import com.oleksii.pipecut.core.model.CutRequest
import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    val name: String,
    val pipe: PresetPipe,
    val cut: PresetCutPlane,
    val saddle: PresetSaddleSpec? = null,
    val pointCountValue: Int,
)

@Serializable
data class PresetPipe(val diameterMm: Double)

@Serializable
data class PresetCutPlane(
    val tiltDeg: Double,
    val clockingDeg: Double,
    val offsetMm: Double,
)

@Serializable
data class PresetSaddleSpec(
    val partnerDiameterMm: Double,
    val intersectionAngleDeg: Double,
    val clockingDeg: Double,
    val offsetMm: Double,
)

fun CutRequest.toPreset(name: String): Preset = Preset(
    name = name,
    pipe = PresetPipe(diameterMm = pipe.diameterMm),
    cut = PresetCutPlane(
        tiltDeg = cut.tiltDeg,
        clockingDeg = cut.clockingDeg,
        offsetMm = cut.offsetMm,
    ),
    saddle = saddle?.let {
        PresetSaddleSpec(
            partnerDiameterMm = it.partnerDiameterMm,
            intersectionAngleDeg = it.intersectionAngleDeg,
            clockingDeg = it.clockingDeg,
            offsetMm = it.offsetMm,
        )
    },
    pointCountValue = pointCount.value,
)
