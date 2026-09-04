package net.minex.nodeforge.client.render.cable;

import net.minex.nodeforge.api.graph.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CableStyle & Connection Geometries")
class CableStyleTest {

    @Test
    @DisplayName("generates valid path points for all cable styles")
    void pathGeneration() {
        Position start = new Position(100.0, 50.0);
        Position end = new Position(300.0, 150.0);

        // Cubic Bézier
        List<Position> bezierPath = CableStyle.CUBIC_BEZIER.generatePath(start, end, 16);
        assertEquals(17, bezierPath.size());
        assertEquals(start, bezierPath.get(0));
        assertEquals(end, bezierPath.get(bezierPath.size() - 1));

        // Straight
        List<Position> straightPath = CableStyle.STRAIGHT.generatePath(start, end, 16);
        assertEquals(2, straightPath.size());
        assertEquals(start, straightPath.get(0));
        assertEquals(end, straightPath.get(1));

        // Stepped / Orthogonal
        List<Position> steppedPath = CableStyle.STEPPED.generatePath(start, end, 16);
        assertEquals(4, steppedPath.size());
        assertEquals(start, steppedPath.get(0));
        assertEquals(new Position(200.0, 50.0), steppedPath.get(1));
        assertEquals(new Position(200.0, 150.0), steppedPath.get(2));
        assertEquals(end, steppedPath.get(3));

        // Overload with explicit port directions
        List<Position> withDirs = CableStyle.CUBIC_BEZIER.generatePath(
                start, net.minex.nodeforge.api.graph.PortDirection.OUTPUT,
                end, net.minex.nodeforge.api.graph.PortDirection.INPUT, 8
        );
        assertEquals(9, withDirs.size());
    }

    @Test
    @DisplayName("CableRenderer gets and sets cable styles")
    void cableRendererIntegration() {
        net.minex.nodeforge.client.render.CableRenderer renderer =
                new net.minex.nodeforge.client.render.CableRenderer();
        assertEquals(CableStyle.CUBIC_BEZIER, renderer.getCableStyle());

        renderer.setCableStyle(CableStyle.STRAIGHT);
        assertEquals(CableStyle.STRAIGHT, renderer.getCableStyle());

        renderer.setCableStyle(CableStyle.STEPPED);
        assertEquals(CableStyle.STEPPED, renderer.getCableStyle());

        assertThrows(NullPointerException.class, () -> renderer.setCableStyle(null));
    }
}
