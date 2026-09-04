package net.minex.nodeforge.client.render;

import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure mathematical model for a 2D cubic Bézier curve.
 *
 * <p>Used for calculating smooth connection cable trajectories between node ports,
 * segment subdivision for rendering, and distance-based hover hit-testing.
 *
 * @param p0 starting endpoint (e.g. source output port)
 * @param p1 first control point (tangent handle for p0)
 * @param p2 second control point (tangent handle for p3)
 * @param p3 ending endpoint (e.g. target input port)
 */
public record BezierCurve(Position p0, Position p1, Position p2, Position p3) {

    public BezierCurve {
        Objects.requireNonNull(p0, "p0 must not be null");
        Objects.requireNonNull(p1, "p1 must not be null");
        Objects.requireNonNull(p2, "p2 must not be null");
        Objects.requireNonNull(p3, "p3 must not be null");
    }

    /**
     * Constructs a cubic Bézier curve between two port endpoints with automatic tangent calculation.
     *
     * @param start    starting port world position
     * @param startDir starting port direction
     * @param end      ending port world position
     * @param endDir   ending port direction
     * @return a new cubic Bézier curve
     */
    public static BezierCurve fromEndpoints(Position start, PortDirection startDir,
                                            Position end, PortDirection endDir) {
        Objects.requireNonNull(start, "start position must not be null");
        Objects.requireNonNull(startDir, "startDir must not be null");
        Objects.requireNonNull(end, "end position must not be null");
        Objects.requireNonNull(endDir, "endDir must not be null");

        double dx = Math.abs(end.x() - start.x());
        double dy = Math.abs(end.y() - start.y());
        double curvature = Math.max(30.0, Math.max(dx * 0.5, dy * 0.15));

        double startTangentX = startDir == PortDirection.OUTPUT ? curvature : -curvature;
        double endTangentX = endDir == PortDirection.INPUT ? -curvature : curvature;

        Position p1 = new Position(start.x() + startTangentX, start.y());
        Position p2 = new Position(end.x() + endTangentX, end.y());

        return new BezierCurve(start, p1, p2, end);
    }

    /**
     * Evaluates the 2D position along the curve at parameter {@code t} in range [0, 1].
     *
     * @param t normalized parameter [0.0, 1.0]
     * @return the interpolated position on the curve
     */
    public Position evaluate(double t) {
        if (!Double.isFinite(t)) {
            throw new IllegalArgumentException("t parameter must be finite: " + t);
        }
        t = Math.clamp(t, 0.0, 1.0);
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        double x = uuu * p0.x() + 3.0 * uu * t * p1.x() + 3.0 * u * tt * p2.x() + ttt * p3.x();
        double y = uuu * p0.y() + 3.0 * uu * t * p1.y() + 3.0 * u * tt * p2.y() + ttt * p3.y();

        return new Position(x, y);
    }

    /**
     * Subdivides the curve into a polyline with the specified number of segments.
     *
     * @param segments number of linear segments (minimum 1)
     * @return list of (segments + 1) vertices along the curve
     */
    public List<Position> subdivide(int segments) {
        int count = Math.max(1, segments);
        List<Position> points = new ArrayList<>(count + 1);
        for (int i = 0; i <= count; i++) {
            double t = (double) i / (double) count;
            points.add(evaluate(t));
        }
        return points;
    }

    /**
     * Computes the minimum Euclidean distance from a world position to the nearest point on the curve.
     *
     * @param point the query position
     * @return the shortest distance to the curve
     */
    public double distanceToPoint(Position point) {
        Objects.requireNonNull(point, "point must not be null");
        List<Position> points = subdivide(20);
        double minDistanceSq = Double.MAX_VALUE;

        for (int i = 0; i < points.size() - 1; i++) {
            Position a = points.get(i);
            Position b = points.get(i + 1);
            double distSq = distanceSquaredToSegment(point, a, b);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
            }
        }

        return Math.sqrt(minDistanceSq);
    }

    private static double distanceSquaredToSegment(Position p, Position a, Position b) {
        double abx = b.x() - a.x();
        double aby = b.y() - a.y();
        double apx = p.x() - a.x();
        double apy = p.y() - a.y();

        double segLenSq = abx * abx + aby * aby;
        if (segLenSq == 0.0) {
            return p.distanceSquaredTo(a);
        }

        double t = Math.clamp((apx * abx + apy * aby) / segLenSq, 0.0, 1.0);
        double projX = a.x() + t * abx;
        double projY = a.y() + t * aby;

        double dx = p.x() - projX;
        double dy = p.y() - projY;
        return dx * dx + dy * dy;
    }
}
