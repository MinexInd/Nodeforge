package net.minex.nodeforge.client.editor.state;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.client.editor.camera.GridSnap;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EditorState & Gestures")
class EditorStateTest {

    private Graph graph;
    private EditorState editor;

    @BeforeEach
    void setUp() {
        graph = new Graph("editor_test");
        editor = new EditorState(graph);
    }

    @Test
    @DisplayName("panning gesture updates camera pan")
    void panningGesture() {
        editor.camera().setPan(0.0, 0.0);
        editor.camera().setZoom(1.0);

        editor.startPanning(100.0, 100.0);
        assertInstanceOf(InteractionState.Panning.class, editor.interactionState());

        // Drag mouse 50 pixels right and 25 pixels down
        editor.updatePanning(150.0, 125.0);
        assertEquals(new Position(-50.0, -25.0), editor.camera().pan());

        editor.finishGesture();
        assertInstanceOf(InteractionState.Idle.class, editor.interactionState());
    }

    @Test
    @DisplayName("node dragging gesture translates nodes with grid snapping")
    void nodeDraggingGesture() {
        NodeId n1Id = NodeId.of("n1");
        Node node1 = Node.builder(n1Id, "type").position(0.0, 0.0).build();
        graph.addNode(node1);

        editor.setGridSnap(GridSnap.of(16.0));
        editor.camera().setZoom(1.0);

        editor.startDraggingNodes(List.of(n1Id), 200.0, 200.0);
        assertInstanceOf(InteractionState.DraggingNodes.class, editor.interactionState());

        // Drag 30 pixels right (snaps to nearest 16 -> 32) and 10 pixels down (snaps to 16 -> 16)
        editor.updateDraggingNodes(230.0, 210.0);
        assertEquals(new Position(32.0, 16.0), node1.position());

        editor.finishGesture();
        assertInstanceOf(InteractionState.Idle.class, editor.interactionState());
    }

    @Test
    @DisplayName("connecting cable gesture tracking")
    void connectingGesture() {
        NodeId src = NodeId.of("src");
        PortId port = PortId.of("out");

        editor.startConnecting(src, port, PortDirection.OUTPUT, BuiltinPortTypes.DOUBLE, new Position(10, 20));
        assertInstanceOf(InteractionState.ConnectingCable.class, editor.interactionState());

        editor.updateConnecting(new Position(100, 150));
        InteractionState.ConnectingCable conn = (InteractionState.ConnectingCable) editor.interactionState();
        assertEquals(new Position(100, 150), conn.currentWorldPos());

        editor.finishGesture();
        assertInstanceOf(InteractionState.Idle.class, editor.interactionState());
    }

    @Test
    @DisplayName("box selecting gesture tracking")
    void boxSelectingGesture() {
        editor.startBoxSelecting(50.0, 50.0);
        assertInstanceOf(InteractionState.BoxSelecting.class, editor.interactionState());

        editor.updateBoxSelecting(150.0, 250.0);
        InteractionState.BoxSelecting box = (InteractionState.BoxSelecting) editor.interactionState();
        assertEquals(50.0, box.startScreenX());
        assertEquals(50.0, box.startScreenY());
        assertEquals(150.0, box.currentScreenX());
        assertEquals(250.0, box.currentScreenY());

        editor.finishGesture();
        assertInstanceOf(InteractionState.Idle.class, editor.interactionState());
    }

    @Test
    @DisplayName("frameAll centers camera on graph bounding box")
    void frameAllCentering() {
        Node n1 = Node.builder(NodeId.of("n1"), "type").position(-100.0, -100.0).size(new Size(50, 50)).build();
        Node n2 = Node.builder(NodeId.of("n2"), "type").position(100.0, 100.0).size(new Size(50, 50)).build();
        graph.addNode(n1);
        graph.addNode(n2);

        // Bounding box from (-100, -100) to (150, 150), center is (25, 25)
        editor.frameAll(800.0, 600.0, 50.0);

        assertEquals(25.0, editor.camera().pan().x(), 1e-6);
        assertEquals(25.0, editor.camera().pan().y(), 1e-6);
        assertTrue(editor.camera().zoom() <= 1.0);
    }
}
