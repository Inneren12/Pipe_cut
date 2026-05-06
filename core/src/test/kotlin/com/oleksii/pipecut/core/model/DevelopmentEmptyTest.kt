package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DevelopmentEmptyTest {

    @Test
    fun `maxLengthMm and minLengthMm return null for an empty development`() {
        val empty = Development(points = emptyList())
        assertNull(empty.maxLengthMm)
        assertNull(empty.minLengthMm)
    }
}
