package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI

class PipeSpecTest {

    @Test
    fun `equality and hashCode work for identical values`() {
        val a = PipeSpec(diameterMm = 219.1)
        val b = PipeSpec(diameterMm = 219.1)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `radiusMm equals diameterMm divided by two`() {
        val pipe = PipeSpec(diameterMm = 219.1)
        assertEquals(109.55, pipe.radiusMm, 1e-9)
    }

    @Test
    fun `circumferenceMm equals PI times diameterMm`() {
        val pipe = PipeSpec(diameterMm = 219.1)
        assertEquals(PI * 219.1, pipe.circumferenceMm, 1e-9)
    }

    @Test
    fun `copy produces a new instance with overridden field`() {
        val original = PipeSpec(diameterMm = 219.1)
        val copied = original.copy(diameterMm = 323.9)
        assertNotEquals(original, copied)
        assertEquals(323.9, copied.diameterMm, 1e-9)
    }
}
