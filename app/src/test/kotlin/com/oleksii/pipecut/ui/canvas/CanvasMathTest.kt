package com.oleksii.pipecut.ui.canvas

import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.model.PipeSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.PI

class CanvasMathTest {
    private val pipe = PipeSpec(diameterMm = 100.0)
    private val box = CanvasBox(widthPx = 1000f, heightPx = 200f, padPx = 0f)

    @Test
    fun `unwrappedToCanvas places phi=0 at left edge and phi=360 at right edge`() {
        val left = unwrappedToCanvas(
            DevPoint(phiDeg = 0.0, lengthMm = 50.0),
            pipe = pipe,
            yRangeMm = 0.0..100.0,
            box = box,
        )
        val right = unwrappedToCanvas(
            DevPoint(phiDeg = 359.999, lengthMm = 50.0),
            pipe = pipe,
            yRangeMm = 0.0..100.0,
            box = box,
        )
        assertEquals(0f, left.x, 1f)
        assertEquals(box.widthPx, right.x, 1f)
    }

    @Test
    fun `unwrappedToCanvas inverts y so larger length sits higher`() {
        val low = unwrappedToCanvas(
            DevPoint(phiDeg = 0.0, lengthMm = 0.0),
            pipe = pipe,
            yRangeMm = 0.0..100.0,
            box = box,
        )
        val high = unwrappedToCanvas(
            DevPoint(phiDeg = 0.0, lengthMm = 100.0),
            pipe = pipe,
            yRangeMm = 0.0..100.0,
            box = box,
        )
        assertTrue(high.y < low.y, "expected high.y < low.y, got ${high.y} vs ${low.y}")
    }

    @Test
    fun `unwrappedToCanvas handles degenerate yRange without throwing`() {
        val p = unwrappedToCanvas(
            DevPoint(phiDeg = 0.0, lengthMm = 50.0),
            pipe = pipe,
            yRangeMm = 50.0..50.0,
            box = box,
        )
        assertTrue(p.y.isFinite(), "y must stay finite when yRange is degenerate")
    }

    @Test
    fun `developmentToCanvas2D returns one point per development point`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 50.0),
                DevPoint(90.0, 70.0),
                DevPoint(180.0, 50.0),
                DevPoint(270.0, 30.0),
            ),
        )
        val pts = developmentToCanvas2D(dev, pipe, box)
        assertEquals(4, pts.size)
        assertEquals(0f, pts[0].x, 1f)
    }

    @Test
    fun `cabinetProject is identity on z=0 plane`() {
        val (px, py) = cabinetProject(x = 3.0, y = 4.0, z = 0.0)
        assertEquals(3.0, px, 1e-12)
        assertEquals(4.0, py, 1e-12)
    }

    @Test
    fun `cabinetProject shifts x and y by foreshortened z`() {
        val (px, py) = cabinetProject(x = 0.0, y = 0.0, z = 10.0)
        assertEquals(0.5 * 10.0 * kotlin.math.cos(PI / 6.0), px, 1e-9)
        assertEquals(0.5 * 10.0 * kotlin.math.sin(PI / 6.0), py, 1e-9)
    }

    @Test
    fun `fitWorldToCanvas centers a single point`() {
        val pts = fitWorldToCanvas(listOf(0.0 to 0.0), box)
        assertEquals(box.widthPx / 2f, pts[0].x, 1f)
        assertEquals(box.heightPx / 2f, pts[0].y, 1f)
    }

    @Test
    fun `cutCurve3D returns one point per development point`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 100.0),
                DevPoint(90.0, 80.0),
                DevPoint(180.0, 100.0),
                DevPoint(270.0, 120.0),
            ),
        )
        val pts = cutCurve3D(dev, pipe, box)
        assertEquals(4, pts.size)
        for (p in pts) {
            assertTrue(p.x.isFinite() && p.y.isFinite())
        }
    }

    @Test
    fun `rimCurves3D returns the requested sample count for each rim`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 100.0),
                DevPoint(180.0, 100.0),
            ),
        )
        val (top, bottom) = rimCurves3D(dev, pipe, box, samples = 36)
        assertEquals(36, top.size)
        assertEquals(36, bottom.size)
    }
}
