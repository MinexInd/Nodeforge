package net.minex.nodeforge.client.editor.state;

import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InteractionState & HoverState")
class InteractionStateTest {

    @Test
    @DisplayName("interaction state variants and payloads")
    void interactionStateVariants() {
        InteractionState idle = InteractionState.Idle.INSTANCE;
        assertInstanceOf(InteractionState.Idle.class, idle);

        InteractionState.Panning panning = new InteractionState.Panning(new Position(10, 20), 100, 200);
        assertEquals(new Position(10, 20), panning.startPan());
        assertEquals(100, panning.startScreenX());
        assertEquals(200, panning.startScreenY());

        InteractionState.DraggingNodes dragging = new InteractionState.DraggingNodes(
                Map.of(NodeId.of("n1"), new Position(50, 50)), 150, 250);
        assertEquals(1, dragging.initialPositions().size());

        InteractionState.ConnectingCable connecting = new InteractionState.ConnectingCable(
                NodeId.of("src"), PortId.of("out"), PortDirection.OUTPUT, BuiltinPortTypes.FLOAT, new Position(300, 400));
        assertEquals(NodeId.of("src"), connecting.sourceNode());
        assertEquals(PortId.of("out"), connecting.sourcePort());
        assertEquals(PortDirection.OUTPUT, connecting.sourceDirection());
        assertSame(BuiltinPortTypes.FLOAT, connecting.portType());
        assertEquals(new Position(300, 400), connecting.currentWorldPos());

        InteractionState.BoxSelecting box = new InteractionState.BoxSelecting(10, 20, 100, 200);
        assertEquals(10, box.startScreenX());
        assertEquals(20, box.startScreenY());
        assertEquals(100, box.currentScreenX());
        assertEquals(200, box.currentScreenY());
    }

    @Test
    @DisplayName("hover state tracking")
    void hoverStateTracking() {
        HoverState empty = HoverState.NONE;
        assertFalse(empty.hasHover());

        NodeId n1 = NodeId.of("n1");
        PortId p1 = PortId.of("p1");

        HoverState nodeHover = HoverState.node(n1);
        assertTrue(nodeHover.hasHover());
        assertTrue(nodeHover.isNodeHovered(n1));
        assertFalse(nodeHover.isPortHovered(n1, p1));

        HoverState portHover = HoverState.port(n1, p1);
        assertTrue(portHover.isPortHovered(n1, p1));
        assertTrue(portHover.isNodeHovered(n1));
    }
}
