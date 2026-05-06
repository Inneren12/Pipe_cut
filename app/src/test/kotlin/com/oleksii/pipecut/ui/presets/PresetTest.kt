package com.oleksii.pipecut.ui.presets

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PresetTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `round-trip plane request`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 114.3),
            cut = CutPlane(tiltDeg = 28.0, clockingDeg = 12.0, offsetMm = 250.0),
            saddle = null,
            pointCount = PointCount.P36,
        )
        val preset = req.toPreset(name = "drawing-A")
        val s = json.encodeToString(Preset.serializer(), preset)
        val back = json.decodeFromString(Preset.serializer(), s)
        assertEquals(preset, back)
        assertEquals("drawing-A", back.name)
        assertEquals(36, back.pointCountValue)
    }

    @Test
    fun `round-trip saddle request`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 114.3),
            cut = CutPlane(tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 600.0),
            saddle = SaddleSpec(914.4, 62.0, 0.0, 0.0),
            pointCount = PointCount.P36,
        )
        val preset = req.toPreset("brace-DEW-VB1")
        val s = json.encodeToString(Preset.serializer(), preset)
        val back = json.decodeFromString(Preset.serializer(), s)
        assertEquals(preset, back)
    }
}
