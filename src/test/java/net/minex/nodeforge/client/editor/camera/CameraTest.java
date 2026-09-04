package net.minex.nodeforge.client.editor.camera;

import net.minex.nodeforge.api.graph.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Camera & Coordinate Transformations")
class CameraTest {

    private Camera camera;
    private static final double VW = 800.0;
    private static final double VH = 600.0;

    @BeforeEach
    void setUp() {
        camera = new Camera();
    }

    @Test
    @DisplayName("initial default state")
    void initialState() {
        assertEquals(Position.ZERO, camera.pan());
        assertEquals(1.0, camera.zoom());
        assertEquals(0.1, camera.minZoom());
        assertEquals(3.0, camera.maxZoom());
    }

    @Test
    @DisplayName("clamps zoom to limits")
    void zoomClamping() {
        camera.setZoom(5.0);
        assertEquals(3.0, camera.zoom(), "Should clamp to maxZoom");

        camera.setZoom(0.01);
        assertEquals(0.1, camera.zoom(), "Should clamp to minZoom");

        assertThrows(IllegalArgumentException.class, () -> camera.setZoom(-1.0));
        assertThrows(IllegalArgumentException.class, () -> camera.setZoom(Double.NaN));
    }

    @Test
    @DisplayName("screen to world conversion at zoom 1.0")
    void screenToWorldStandard() {
        // Screen center (400, 300) should map to world origin (0, 0)
        Position center = camera.screenToWorld(400.0, 300.0, VW, VH);
        assertEquals(0.0, center.x(), 1e-6);
        assertEquals(0.0, center.y(), 1e-6);

        // Top-left screen (0, 0) should map to world (-400, -300)
        Position topLeft = camera.screenToWorld(0.0, 0.0, VW, VH);
        assertEquals(-400.0, topLeft.x(), 1e-6);
        assertEquals(-300.0, topLeft.y(), 1e-6);
    }

    @Test
    @DisplayName("world to screen conversion at zoom 1.0")
    void worldToScreenStandard() {
        // World origin (0, 0) should map to screen center (400, 300)
        Position screenPos = camera.worldToScreen(0.0, 0.0, VW, VH);
        assertEquals(400.0, screenPos.x(), 1e-6);
        assertEquals(300.0, screenPos.y(), 1e-6);

        // World (100, 50) should map to screen (500, 350)
        Position offsetPos = camera.worldToScreen(100.0, 50.0, VW, VH);
        assertEquals(500.0, offsetPos.x(), 1e-6);
        assertEquals(350.0, offsetPos.y(), 1e-6);
    }

    @Test
    @DisplayName("coordinate transformation roundtrips")
    void coordinateRoundtrips() {
        camera.setPan(150.0, -75.0);
        camera.setZoom(1.75);

        Position world = new Position(325.5, -112.25);
        Position screen = camera.worldToScreen(world.x(), world.y(), VW, VH);
        Position roundtripWorld = camera.screenToWorld(screen.x(), screen.y(), VW, VH);

        assertEquals(world.x(), roundtripWorld.x(), 1e-6);
        assertEquals(world.y(), roundtripWorld.y(), 1e-6);
    }

    @Test
    @DisplayName("zoomAt keeps the world coordinate under the mouse stationary")
    void zoomAtAnchoring() {
        double mouseScreenX = 550.0;
        double mouseScreenY = 220.0;

        camera.setPan(50.0, 50.0);
        camera.setZoom(1.0);

        Position worldPosBefore = camera.screenToWorld(mouseScreenX, mouseScreenY, VW, VH);

        // Zoom in by factor 1.5
        camera.zoomAt(mouseScreenX, mouseScreenY, 1.5, VW, VH);
        assertEquals(1.5, camera.zoom(), 1e-6);

        Position worldPosAfter = camera.screenToWorld(mouseScreenX, mouseScreenY, VW, VH);

        // The world coordinate under the mouse cursor must remain identical
        assertEquals(worldPosBefore.x(), worldPosAfter.x(), 1e-6);
        assertEquals(worldPosBefore.y(), worldPosAfter.y(), 1e-6);
    }

    @Test
    @DisplayName("calculates visible world culling bounds")
    void visibleBounds() {
        camera.setPan(0.0, 0.0);
        camera.setZoom(2.0); // 2x zoom -> visible area is halved

        BoundingBox bounds = camera.getVisibleWorldBounds(VW, VH);
        assertEquals(-200.0, bounds.minX(), 1e-6);
        assertEquals(-150.0, bounds.minY(), 1e-6);
        assertEquals(200.0, bounds.maxX(), 1e-6);
        assertEquals(150.0, bounds.maxY(), 1e-6);
    }
}
