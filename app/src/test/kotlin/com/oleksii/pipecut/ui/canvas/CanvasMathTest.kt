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
    fun `developmentToCanvas2D appends a virtual seam point at the right edge`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 50.0),
                DevPoint(90.0, 70.0),
                DevPoint(180.0, 50.0),
                DevPoint(270.0, 30.0),
            ),
        )
        val pts = developmentToCanvas2D(dev, pipe, box)
        assertEquals(5, pts.size, "must include 4 sampled points + 1 virtual seam point")
        assertEquals(0f, pts[0].x, 1f)
        assertEquals(box.widthPx, pts.last().x, 1f)
        assertEquals(pts[0].y, pts.last().y, 1e-3f)
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
    fun `pipePreviewGeometry3D returns one cut point per development point and uniform rim sampling`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 100.0),
                DevPoint(90.0, 80.0),
                DevPoint(180.0, 100.0),
                DevPoint(270.0, 120.0),
            ),
        )
        val geometry = pipePreviewGeometry3D(dev, pipe, box, samples = 36)
        assertEquals(4, geometry.cut.size)
        assertEquals(36, geometry.topRim.size)
        assertEquals(36, geometry.bottomRim.size)
        val all = geometry.cut + geometry.topRim + geometry.bottomRim
        for (p in all) {
            assertTrue(p.x.isFinite() && p.y.isFinite())
        }
    }

    @Test
    fun `clampTransform clips scale below 0 5 and above 8 0`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val small = clampTransform(CanvasTransform(scalePx = 0.1f), b)
        val big = clampTransform(CanvasTransform(scalePx = 100f), b)
        assertEquals(CanvasTransform.MIN_SCALE, small.scalePx, 1e-6f)
        assertEquals(CanvasTransform.MAX_SCALE, big.scalePx, 1e-6f)
    }

    @Test
    fun `clampTransform clamps pan to stay in bounds`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val proposed = CanvasTransform(scalePx = 1f, panXPx = 1e6f, panYPx = -1e6f)
        val clamped = clampTransform(proposed, b)
        val maxPanX = b.widthPx / 2f * 1f * 0.75f
        val maxPanY = b.heightPx / 2f * 1f * 0.75f
        assertEquals(maxPanX, clamped.panXPx, 1e-3f)
        assertEquals(-maxPanY, clamped.panYPx, 1e-3f)
    }

    @Test
    fun `clampTransform pan limits scale with the zoom factor`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val proposed = CanvasTransform(scalePx = 4f, panXPx = 1e6f, panYPx = 1e6f)
        val clamped = clampTransform(proposed, b)
        val maxPanX = b.widthPx / 2f * 4f * 0.75f
        assertEquals(maxPanX, clamped.panXPx, 1e-3f)
    }

    @Test
    fun `applyTransform with identity returns the input`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val p = CanvasPoint(500f, 100f)
        val q = applyTransform(p, b, CanvasTransform.Identity)
        assertEquals(p.x, q.x, 1e-6f)
        assertEquals(p.y, q.y, 1e-6f)
    }

    @Test
    fun `applyTransform with scale 2 zooms around the canvas center`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val center = CanvasPoint(500f, 100f)
        val q = applyTransform(center, b, CanvasTransform(scalePx = 2f))
        assertEquals(center.x, q.x, 1e-3f)
        assertEquals(center.y, q.y, 1e-3f)
        val edge = CanvasPoint(1000f, 200f)
        val edgeMapped = applyTransform(edge, b, CanvasTransform(scalePx = 2f))
        assertEquals(1500f, edgeMapped.x, 1e-3f)
        assertEquals(300f, edgeMapped.y, 1e-3f)
    }

    @Test
    fun `composeTransform clamps the result`() {
        val b = CanvasBox(1000f, 200f, 0f)
        val current = CanvasTransform(scalePx = 4f, panXPx = 0f, panYPx = 0f)
        val composed = composeTransform(
            current = current,
            panChangePx = 1e6f to 0f,
            zoomChange = 100f,
            box = b,
        )
        assertEquals(CanvasTransform.MAX_SCALE, composed.scalePx, 1e-6f)
        val maxPanX = b.widthPx / 2f * CanvasTransform.MAX_SCALE * 0.75f
        assertEquals(maxPanX, composed.panXPx, 1e-3f)
    }

    @Test
    fun `rotateAroundY at 0 degrees is identity`() {
        val (x, z) = rotateAroundY(3.0, 4.0, 0f)
        assertEquals(3.0, x, 1e-12)
        assertEquals(4.0, z, 1e-12)
    }

    @Test
    fun `rotateAroundY at 90 degrees rotates 1 0 to 0 -1`() {
        val (x, z) = rotateAroundY(1.0, 0.0, 90f)
        assertEquals(0.0, x, 1e-9)
        assertEquals(-1.0, z, 1e-9)
    }

    @Test
    fun `rotateAroundY at 360 degrees is identity within tolerance`() {
        val (x, z) = rotateAroundY(3.0, 4.0, 360f)
        assertEquals(3.0, x, 1e-9)
        assertEquals(4.0, z, 1e-9)
    }

    @Test
    fun `pipePreviewGeometry3D with rotation 0 matches the un-rotated overload`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 100.0),
                DevPoint(90.0, 80.0),
                DevPoint(180.0, 100.0),
                DevPoint(270.0, 120.0),
            ),
        )
        val a = pipePreviewGeometry3D(dev, pipe, box, samples = 36, rotationYDeg = 0f)
        val b = pipePreviewGeometry3D(dev, pipe, box, samples = 36)
        for (i in a.cut.indices) {
            assertEquals(a.cut[i].x, b.cut[i].x, 1e-3f)
            assertEquals(a.cut[i].y, b.cut[i].y, 1e-3f)
        }
    }

    @Test
    fun `pipePreviewGeometry3D fits cut and rims into the same canvas box`() {
        val dev = Development(
            points = listOf(
                DevPoint(0.0, 100.0),
                DevPoint(90.0, 80.0),
                DevPoint(180.0, 100.0),
                DevPoint(270.0, 120.0),
            ),
        )
        val geometry = pipePreviewGeometry3D(dev, pipe, box, samples = 36)
        val all = geometry.cut + geometry.topRim + geometry.bottomRim
        for (p in all) {
            assertTrue(p.x in 0f..box.widthPx, "x out of box: ${p.x}")
            assertTrue(p.y in 0f..box.heightPx, "y out of box: ${p.y}")
        }
    }
}
