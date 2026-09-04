package net.minex.nodeforge.client.render;

import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BezierCurve & Math")
class BezierCurveTest {

    @Test
    @DisplayName("computes control points from port directions and endpoints")
    void controlPointCalculation() {
        Position start = new Position(100.0, 100.0);
        Position end = new Position(300.0, 100.0);

        BezierCurve curve = BezierCurve.fromEndpoints(start, PortDirection.OUTPUT, end, PortDirection.INPUT);

        assertEquals(start, curve.p0());
        assertEquals(end, curve.p3());

        // Output tangent extends right (+X), Input tangent extends left (-X)
        assertTrue(curve.p1().x() > start.x());
        assertTrue(curve.p2().x() < end.x());
        assertEquals(start.y(), curve.p1().y(), 1e-6);
        assertEquals(end.y(), curve.p2().y(), 1e-6);
    }

    @Test
    @DisplayName("evaluates curve at endpoints and midpoints")
    void curveEvaluation() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(100.0, 100.0);
        BezierCurve curve = BezierCurve.fromEndpoints(start, PortDirection.OUTPUT, end, PortDirection.INPUT);

        Position atZero = curve.evaluate(0.0);
        assertEquals(start.x(), atZero.x(), 1e-6);
        assertEquals(start.y(), atZero.y(), 1e-6);

        Position atOne = curve.evaluate(1.0);
        assertEquals(end.x(), atOne.x(), 1e-6);
        assertEquals(end.y(), atOne.y(), 1e-6);

        Position atHalf = curve.evaluate(0.5);
        assertEquals(50.0, atHalf.x(), 1e-6);
        assertEquals(50.0, atHalf.y(), 1e-6);
    }

    @Test
    @DisplayName("subdivides curve into polyline vertices")
    void subdivision() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(100.0, 100.0);
        BezierCurve curve = BezierCurve.fromEndpoints(start, PortDirection.OUTPUT, end, PortDirection.INPUT);

        List<Position> points = curve.subdivide(10);
        assertEquals(11, points.size());
        assertEquals(start, points.get(0));
        assertEquals(end, points.get(10));
    }

    @Test
    @DisplayName("calculates distance from query point to curve for hover detection")
    void distanceToPoint() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(100.0, 0.0);
        BezierCurve curve = BezierCurve.fromEndpoints(start, PortDirection.OUTPUT, end, PortDirection.INPUT);

        // Point directly on the curve (midpoint is (50, 0))
        double distOnCurve = curve.distanceToPoint(new Position(50.0, 0.0));
        assertEquals(0.0, distOnCurve, 1e-3);

        // Point 5 units above the curve
        double distAbove = curve.distanceToPoint(new Position(50.0, 5.0));
        assertEquals(5.0, distAbove, 1e-2);

        // Point far away
        double distFar = curve.distanceToPoint(new Position(50.0, 100.0));
        assertEquals(100.0, distFar, 1e-1);
    }

    @Test
    @DisplayName("rejects non-finite t parameters in evaluate")
    void rejectsNonFiniteParameter() {
        BezierCurve curve = BezierCurve.fromEndpoints(Position.ZERO, PortDirection.OUTPUT, new Position(10, 10), PortDirection.INPUT);
        assertThrows(IllegalArgumentException.class, () -> curve.evaluate(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> curve.evaluate(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> curve.evaluate(Double.NEGATIVE_INFINITY));
    }
}
