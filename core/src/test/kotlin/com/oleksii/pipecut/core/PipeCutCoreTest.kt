package com.oleksii.pipecut.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PipeCutCoreTest {

    @Test
    fun `core module exposes its version`() {
        assertEquals("0.1.0", PipeCutCore.VERSION)
    }
}
