package net.minex.nodeforge.client.render;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortLayout & Socket Hit-Testing")
class PortLayoutTest {

    @Test
    @DisplayName("positions input ports on left edge and output ports on right edge")
    void portPositions() {
        Node node = Node.builder(NodeId.of("calc"), "type")
                .position(100.0, 200.0)
                .size(new Size(160.0, 80.0))
                .inputPort("in_a", "A", BuiltinPortTypes.DOUBLE)
                .outputPort("out_res", "Res", BuiltinPortTypes.DOUBLE)
                .build();

        Port inPort = node.getPort(PortId.of("in_a"));
        Port outPort = node.getPort(PortId.of("out_res"));

        Position inPos = PortLayout.getPortPosition(node, inPort);
        Position outPos = PortLayout.getPortPosition(node, outPort);

        // Input on left (x = 100.0)
        assertEquals(100.0, inPos.x(), 1e-6);
        assertTrue(inPos.y() > 200.0 + PortLayout.HEADER_HEIGHT);

        // Output on right (x = 100.0 + 160.0 = 260.0)
        assertEquals(260.0, outPos.x(), 1e-6);
        assertTrue(outPos.y() > 200.0 + PortLayout.HEADER_HEIGHT);
    }

    @Test
    @DisplayName("hit-tests port socket accurately under world coordinates")
    void hitTesting() {
        Node node = Node.builder(NodeId.of("calc"), "type")
                .position(100.0, 200.0)
                .size(new Size(160.0, 80.0))
                .inputPort("in_a", "A", BuiltinPortTypes.DOUBLE)
                .build();

        Port inPort = node.getPort(PortId.of("in_a"));
        Position inPos = PortLayout.getPortPosition(node, inPort);

        // Query right at socket center
        Optional<Port> hitCenter = PortLayout.getPortAt(node, inPos);
        assertTrue(hitCenter.isPresent());
        assertSame(inPort, hitCenter.get());

        // Query within 5 units
        Optional<Port> hitNear = PortLayout.getPortAt(node, new Position(inPos.x() + 3.0, inPos.y() - 3.0));
        assertTrue(hitNear.isPresent());

        // Query far away
        Optional<Port> hitFar = PortLayout.getPortAt(node, new Position(inPos.x() + 50.0, inPos.y()));
        assertTrue(hitFar.isEmpty());
    }
}
