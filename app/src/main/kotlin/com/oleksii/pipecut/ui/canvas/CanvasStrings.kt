package com.oleksii.pipecut.ui.canvas

object CanvasStrings {
    const val SECTION_TITLE: String = "Cut development"
    const val DEV_2D_LABEL: String = "Unwrapped pipe surface"
    const val PREVIEW_3D_LABEL: String = "Pipe preview"
    const val LEGEND_AXIS_PHI: String = "x: arc around pipe (φ = 0 to 360°)"
    const val LEGEND_AXIS_LENGTH: String = "y: cut length from end face (mm)"

    @Deprecated(
        message = "Canvas empty state was unified with the result panel hint in PR8 fixup. " +
            "Constant kept for one release; removal scheduled for PR12.",
        level = DeprecationLevel.WARNING,
    )
    const val EMPTY_HINT: String = "Enter values and tap Calculate to see the development"
}
