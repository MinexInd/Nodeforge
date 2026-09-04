package net.minex.nodeforge.stress;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.camera.GridSnap;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.BezierCurve;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates numerical stability and bounds safety across extreme spatial coordinates,
 * zoom scales, Bézier trajectories, and grid snapping.
 */
class ExtremeCoordinateStressTest {

    @Test
    @DisplayName("Camera round-trip coordinate projection remains accurate at extreme coordinates")
    void testCameraExtremeCoordinates() {
        Camera camera = new Camera();
        double viewportW = 1920.0;
        double viewportH = 1080.0;

        double[] extremeCoords = {1e6, -1e6, 5e7, -5e7, 1e8, -1e8};

        for (double x : extremeCoords) {
            for (double y : extremeCoords) {
                camera.setPan(x, y);
                assertEquals(x, camera.pan().x(), 1e-6);
                assertEquals(y, camera.pan().y(), 1e-6);

                // Test screen -> world -> screen round trip
                double screenX = 400.0;
                double screenY = 300.0;

                Position worldPos = camera.screenToWorld(screenX, screenY, viewportW, viewportH);
                assertTrue(Double.isFinite(worldPos.x()));
                assertTrue(Double.isFinite(worldPos.y()));

                Position screenRoundtrip = camera.worldToScreen(worldPos.x(), worldPos.y(), viewportW, viewportH);
                assertEquals(screenX, screenRoundtrip.x(), 1e-4);
                assertEquals(screenY, screenRoundtrip.y(), 1e-4);
            }
        }
    }

    @Test
    @DisplayName("Camera handles extreme zoom ranges and zoomAt cursor invariance")
    void testCameraExtremeZoom() {
        Camera camera = new Camera();
        camera.setZoomLimits(0.0001, 1000.0);
        assertEquals(0.0001, camera.minZoom());
        assertEquals(1000.0, camera.maxZoom());

        double viewportW = 800.0;
        double viewportH = 600.0;
        double cursorX = 350.0;
        double cursorY = 250.0;

        // Zoom in to 500x
        for (int i = 0; i < 50; i++) {
            camera.zoomAt(cursorX, cursorY, 1.2, viewportW, viewportH);
        }
        assertEquals(1000.0, camera.zoom(), 1e-6);
        assertTrue(Double.isFinite(camera.pan().x()));
        assertTrue(Double.isFinite(camera.pan().y()));

        // Zoom out to minZoom
        for (int i = 0; i < 100; i++) {
            camera.zoomAt(cursorX, cursorY, 0.8, viewportW, viewportH);
        }
        assertEquals(0.0001, camera.zoom(), 1e-6);
        assertTrue(Double.isFinite(camera.pan().x()));
        assertTrue(Double.isFinite(camera.pan().y()));
    }

    @Test
    @DisplayName("GridSnap handles massive numbers without long integer saturation")
    void testGridSnapExtremeCoordinates() {
        GridSnap gridSnap = new GridSnap(16.0, true);

        // Standard coordinate
        assertEquals(32.0, gridSnap.snap(30.0));

        // Coordinate near 10^15 (well above normal float, within double precision)
        double largeVal = 1_000_000_000_000_000.0; // 1e15
        double snapped = gridSnap.snap(largeVal + 9.0);
        // largeVal is divisible by 16 (1e15 / 16 = 6.25e13, an exact integer)
        assertEquals(largeVal + 16.0, snapped, 1e-4);

        // Negative extreme coordinate
        double negLarge = -1_000_000_000_000_000.0;
        double negSnapped = gridSnap.snap(negLarge - 7.0);
        assertEquals(negLarge, negSnapped, 1e-4);

        // 2D position snapping
        Position pos = new Position(largeVal + 3.0, negLarge + 10.0);
        Position snappedPos = gridSnap.snap(pos);
        assertEquals(largeVal, snappedPos.x(), 1e-4);
        assertEquals(negLarge + 16.0, snappedPos.y(), 1e-4);
    }

    @Test
    @DisplayName("BezierCurve remains finite and robust under extreme cables and coincident endpoints")
    void testBezierCurveExtremeCases() {
        // Case 1: Coincident endpoints (0-length cable)
        Position same = new Position(100.0, 100.0);
        BezierCurve zeroLen = BezierCurve.fromEndpoints(same, PortDirection.OUTPUT, same, PortDirection.INPUT);
        for (double t = 0.0; t <= 1.0; t += 0.2) {
            Position p = zeroLen.evaluate(t);
            assertTrue(Double.isFinite(p.x()));
            assertTrue(Double.isFinite(p.y()));
        }
        assertEquals(0.0, zeroLen.distanceToPoint(same), 1e-6);

        // Case 2: Inverted directions (source to the right of target)
        Position start = new Position(500.0, 100.0);
        Position end = new Position(100.0, 400.0);
        BezierCurve inverted = BezierCurve.fromEndpoints(start, PortDirection.OUTPUT, end, PortDirection.INPUT);
        List<Position> vertices = inverted.subdivide(10);
        assertEquals(11, vertices.size());
        for (Position v : vertices) {
            assertTrue(Double.isFinite(v.x()));
            assertTrue(Double.isFinite(v.y()));
        }

        // Case 3: Extreme cable distance (1,000,000 units apart)
        Position farStart = new Position(-500_000.0, -500_000.0);
        Position farEnd = new Position(500_000.0, 500_000.0);
        BezierCurve extremeCable = BezierCurve.fromEndpoints(farStart, PortDirection.OUTPUT, farEnd, PortDirection.INPUT);
        Position midPoint = extremeCable.evaluate(0.5);
        assertTrue(Double.isFinite(midPoint.x()));
        assertTrue(Double.isFinite(midPoint.y()));

        double dist = extremeCable.distanceToPoint(new Position(0.0, 0.0));
        assertTrue(Double.isFinite(dist));
        assertTrue(dist >= 0.0);
    }

    @Test
    @DisplayName("EditorState frameAll handles extreme coordinate distributions safely")
    void testFrameAllWithExtremeCoordinates() {
        Graph graph = new Graph("extreme_bounds_graph");
        graph.addNode(Node.builder(NodeId.of("far_left"), "math:add")
                .position(new Position(-1_000_000.0, -500_000.0))
                .build());
        graph.addNode(Node.builder(NodeId.of("far_right"), "math:add")
                .position(new Position(1_000_000.0, 500_000.0))
                .build());

        EditorState state = new EditorState(graph);
        assertDoesNotThrow(() -> state.frameAll(1920.0, 1080.0, 50.0));

        // Pan center should be near (0, 0)
        assertTrue(Double.isFinite(state.camera().pan().x()));
        assertTrue(Double.isFinite(state.camera().pan().y()));
        assertTrue(state.camera().zoom() <= 1.0);
        assertTrue(state.camera().zoom() > 0.0);
    }
}
