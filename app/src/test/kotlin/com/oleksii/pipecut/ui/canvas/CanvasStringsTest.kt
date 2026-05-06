package com.oleksii.pipecut.ui.canvas

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class CanvasStringsTest {
    @Test
    fun `no constant leaks PR numbers`() {
        val all = listOf(
            CanvasStrings.SECTION_TITLE,
            CanvasStrings.DEV_2D_LABEL,
            CanvasStrings.PREVIEW_3D_LABEL,
            CanvasStrings.LEGEND_AXIS_PHI,
            CanvasStrings.LEGEND_AXIS_LENGTH,
            CanvasStrings.EMPTY_HINT,
        )
        val pattern = Regex("\\bPR\\d+\\b")
        for (s in all) {
            assertFalse(pattern.containsMatchIn(s), "leak in: $s")
        }
    }
}
