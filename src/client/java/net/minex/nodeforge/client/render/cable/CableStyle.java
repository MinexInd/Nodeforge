package net.minex.nodeforge.client.render.cable;

import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.render.BezierCurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Geometric connection routing style connecting an output port to an input port.
 */
public enum CableStyle {

    /** Smooth horizontal cubic Bézier curve (standard node graph aesthetic). */
    CUBIC_BEZIER,

    /** Direct linear straight segment. */
    STRAIGHT,

    /** Orthogonal Manhattan 90-degree stepped line. */
    STEPPED;

    /**
     * Generates a sequence of polyline points approximating this cable geometry between two endpoints
     * taking port directions into account for Bézier tangents.
     *
     * @param start    start position (output socket)
     * @param startDir output port direction
     * @param end      end position (input socket)
     * @param endDir   input port direction
     * @param segments subdivision segment count for curves
     * @return unmodifiable list of evaluated path points
     */
    public List<Position> generatePath(Position start, net.minex.nodeforge.api.graph.PortDirection startDir,
                                       Position end, net.minex.nodeforge.api.graph.PortDirection endDir, int segments) {
        Objects.requireNonNull(start, "start position must not be null");
        Objects.requireNonNull(startDir, "startDir must not be null");
        Objects.requireNonNull(end, "end position must not be null");
        Objects.requireNonNull(endDir, "endDir must not be null");

        return switch (this) {
            case CUBIC_BEZIER -> {
                BezierCurve curve = BezierCurve.fromEndpoints(start, startDir, end, endDir);
                yield curve.subdivide(Math.max(4, segments));
            }
            case STRAIGHT -> List.of(start, end);
            case STEPPED -> {
                double midX = (start.x() + end.x()) / 2.0;
                yield List.of(
                        start,
                        new Position(midX, start.y()),
                        new Position(midX, end.y()),
                        end
                );
            }
        };
    }

    /**
     * Generates a sequence of polyline points approximating this cable geometry between two endpoints
     * assuming standard left-to-right (OUTPUT -> INPUT) flow.
     *
     * @param start    start position (output socket)
     * @param end      end position (input socket)
     * @param segments subdivision segment count for curves
     * @return unmodifiable list of evaluated path points
     */
    public List<Position> generatePath(Position start, Position end, int segments) {
        return generatePath(start, net.minex.nodeforge.api.graph.PortDirection.OUTPUT,
                end, net.minex.nodeforge.api.graph.PortDirection.INPUT, segments);
    }
}
