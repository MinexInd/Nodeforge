package net.minex.nodeforge.client.editor.camera;

import net.minex.nodeforge.api.graph.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GridSnap & Quantization")
class GridSnapTest {

    @Test
    @DisplayName("snaps scalar coordinates to nearest grid interval")
    void scalarSnapping() {
        GridSnap snap = GridSnap.of(16.0);

        assertEquals(0.0, snap.snap(0.0));
        assertEquals(0.0, snap.snap(7.9));
        assertEquals(16.0, snap.snap(8.0));
        assertEquals(16.0, snap.snap(15.5));
        assertEquals(32.0, snap.snap(31.9));

        // Negative coordinates
        assertEquals(0.0, snap.snap(-7.9));
        assertEquals(0.0, snap.snap(-8.0)); // Math.round(-0.5) is 0 in Java
        assertEquals(-16.0, snap.snap(-8.1));
        assertEquals(-16.0, snap.snap(-23.9));
        assertEquals(-16.0, snap.snap(-24.0)); // Math.round(-1.5) is -1
        assertEquals(-32.0, snap.snap(-24.1));
    }

    @Test
    @DisplayName("snaps 2D positions")
    void positionSnapping() {
        GridSnap snap = GridSnap.of(20.0);

        Position pos = new Position(19.5, 41.2);
        Position snapped = snap.snap(pos);

        assertEquals(20.0, snapped.x());
        assertEquals(40.0, snapped.y());
    }

    @Test
    @DisplayName("snaps with distance tolerance")
    void toleranceSnapping() {
        GridSnap snap = GridSnap.of(16.0);

        // Grid lines at 0.0 and 16.0; tolerance is 2.0
        Position nearGrid = new Position(15.0, 1.0);
        Position snappedNear = snap.snap(nearGrid, 2.0);
        assertEquals(16.0, snappedNear.x());
        assertEquals(0.0, snappedNear.y());

        // Coordinate far from grid line (8.0 is 8.0 away from 0 and 16)
        Position farFromGrid = new Position(8.0, 8.0);
        Position snappedFar = snap.snap(farFromGrid, 2.0);
        assertEquals(8.0, snappedFar.x(), "Should remain unsnapped beyond tolerance");
        assertEquals(8.0, snappedFar.y(), "Should remain unsnapped beyond tolerance");
    }

    @Test
    @DisplayName("disabled grid leaves coordinates unchanged")
    void disabledGrid() {
        GridSnap disabled = GridSnap.DISABLED;

        assertEquals(12.345, disabled.snap(12.345));
        Position pos = new Position(7.89, 99.1);
        assertEquals(pos, disabled.snap(pos));
    }

    @Test
    @DisplayName("rejects invalid grid sizes")
    void invalidGridSizes() {
        assertThrows(IllegalArgumentException.class, () -> new GridSnap(0.0, true));
        assertThrows(IllegalArgumentException.class, () -> new GridSnap(-16.0, true));
        assertThrows(IllegalArgumentException.class, () -> new GridSnap(Double.NaN, true));
    }
}
