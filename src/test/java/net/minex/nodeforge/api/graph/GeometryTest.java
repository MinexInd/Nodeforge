package net.minex.nodeforge.api.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Graph Geometry Types")
class GeometryTest {

    @Nested
    @DisplayName("Position")
    class PositionTests {
        @Test
        @DisplayName("creates and reads coordinates")
        void createsCoordinates() {
            Position pos = new Position(12.5, -45.0);
            assertEquals(12.5, pos.x());
            assertEquals(-45.0, pos.y());
            assertEquals("(12.5, -45.0)", pos.toString());
        }

        @Test
        @DisplayName("rejects non-finite coordinates")
        void rejectsNonFinite() {
            assertThrows(IllegalArgumentException.class, () -> new Position(Double.NaN, 0));
            assertThrows(IllegalArgumentException.class, () -> new Position(0, Double.NaN));
            assertThrows(IllegalArgumentException.class, () -> new Position(Double.POSITIVE_INFINITY, 0));
            assertThrows(IllegalArgumentException.class, () -> new Position(0, Double.NEGATIVE_INFINITY));
        }

        @Test
        @DisplayName("offset creates shifted position")
        void offset() {
            Position pos = new Position(10, 20);
            Position shifted = pos.offset(5, -10);
            assertEquals(15, shifted.x());
            assertEquals(10, shifted.y());
        }

        @Test
        @DisplayName("distance and distanceSquared calculations")
        void distanceCalculations() {
            Position p1 = new Position(0, 0);
            Position p2 = new Position(3, 4);

            assertEquals(5.0, p1.distanceTo(p2), 1e-9);
            assertEquals(25.0, p1.distanceSquaredTo(p2), 1e-9);
            assertEquals(0.0, p1.distanceTo(Position.ZERO), 1e-9);
        }

        @Test
        @DisplayName("equals and hashCode")
        void equalsAndHashCode() {
            Position p1 = new Position(10.0, 20.0);
            Position p2 = new Position(10.0, 20.0);
            Position p3 = new Position(10.1, 20.0);

            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
            assertNotEquals(p1, p3);
        }
    }

    @Nested
    @DisplayName("Size")
    class SizeTests {
        @Test
        @DisplayName("creates and reads valid dimensions")
        void createsDimensions() {
            Size size = new Size(160, 80);
            assertEquals(160, size.width());
            assertEquals(80, size.height());
            assertEquals("160.0x80.0", size.toString());
        }

        @Test
        @DisplayName("rejects negative dimensions")
        void rejectsNegative() {
            assertThrows(IllegalArgumentException.class, () -> new Size(-1, 10));
            assertThrows(IllegalArgumentException.class, () -> new Size(10, -0.1));
            assertDoesNotThrow(() -> new Size(0, 0));
        }

        @Test
        @DisplayName("rejects non-finite dimensions")
        void rejectsNonFinite() {
            assertThrows(IllegalArgumentException.class, () -> new Size(Double.NaN, 50));
            assertThrows(IllegalArgumentException.class, () -> new Size(50, Double.NaN));
            assertThrows(IllegalArgumentException.class, () -> new Size(Double.POSITIVE_INFINITY, 50));
            assertThrows(IllegalArgumentException.class, () -> new Size(50, Double.NEGATIVE_INFINITY));
        }

        @Test
        @DisplayName("normalizes negative zero")
        void normalizesNegativeZero() {
            Size size = new Size(-0.0, -0.0);
            assertEquals(0.0, size.width());
            assertEquals(0.0, size.height());
            assertEquals("0.0x0.0", size.toString());
            assertFalse(Double.toString(size.width()).contains("-"));
        }

        @Test
        @DisplayName("scale multiplies dimensions")
        void scale() {
            Size size = new Size(100, 50);
            Size scaled = size.scale(1.5);
            assertEquals(150, scaled.width());
            assertEquals(75, scaled.height());
            assertThrows(IllegalArgumentException.class, () -> size.scale(-1));
            assertThrows(IllegalArgumentException.class, () -> size.scale(Double.NaN));
            assertThrows(IllegalArgumentException.class, () -> size.scale(Double.POSITIVE_INFINITY));
        }

        @Test
        @DisplayName("defaults and constants")
        void constants() {
            assertEquals(0, Size.ZERO.width());
            assertEquals(0, Size.ZERO.height());
            assertTrue(Size.DEFAULT.width() > 0);
            assertTrue(Size.DEFAULT.height() > 0);
        }
    }

    @Nested
    @DisplayName("PortDirection")
    class PortDirectionTests {
        @Test
        @DisplayName("opposite returns inverted direction")
        void opposite() {
            assertEquals(PortDirection.OUTPUT, PortDirection.INPUT.opposite());
            assertEquals(PortDirection.INPUT, PortDirection.OUTPUT.opposite());
        }
    }
}
