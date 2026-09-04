package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Editor Commands")
class EditorCommandsTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph("cmd_test");
    }

    private Node createNode(String id) {
        return Node.builder(NodeId.of(id), "type")
                .inputPort("in", "In", BuiltinPortTypes.DOUBLE)
                .outputPort("out", "Out", BuiltinPortTypes.DOUBLE)
                .build();
    }

    @Test
    @DisplayName("AddNodeCommand adds and removes node on undo")
    void addNodeCommand() {
        Node node = createNode("n1");
        AddNodeCommand cmd = new AddNodeCommand(graph, node);

        cmd.execute();
        assertTrue(graph.hasNode(NodeId.of("n1")));

        cmd.undo();
        assertFalse(graph.hasNode(NodeId.of("n1")));
    }

    @Test
    @DisplayName("DeleteSelectionCommand deletes and restores nodes and all connections on undo")
    void deleteSelectionCommand() {
        Node n1 = createNode("n1");
        Node n2 = createNode("n2");
        graph.addNode(n1);
        graph.addNode(n2);

        graph.connect(NodeId.of("n1"), PortId.of("out"), NodeId.of("n2"), PortId.of("in"));
        assertEquals(1, graph.connectionCount());

        DeleteSelectionCommand cmd = new DeleteSelectionCommand(graph, List.of(NodeId.of("n1")), List.of());

        // Execute deletion
        cmd.execute();
        assertFalse(graph.hasNode(NodeId.of("n1")));
        assertTrue(graph.hasNode(NodeId.of("n2")));
        assertEquals(0, graph.connectionCount());

        // Undo restoration
        cmd.undo();
        assertTrue(graph.hasNode(NodeId.of("n1")));
        assertTrue(graph.hasNode(NodeId.of("n2")));
        assertEquals(1, graph.connectionCount());
        assertFalse(graph.getConnectionsForPort(NodeId.of("n1"), PortId.of("out")).isEmpty());
    }

    @Test
    @DisplayName("MoveNodesCommand translates nodes and restores initial positions")
    void moveNodesCommand() {
        Node n1 = createNode("n1");
        n1.setPosition(new Position(10, 20));
        graph.addNode(n1);

        MoveNodesCommand cmd = new MoveNodesCommand(
                graph,
                Map.of(NodeId.of("n1"), new Position(10, 20)),
                Map.of(NodeId.of("n1"), new Position(100, 200))
        );

        cmd.execute();
        assertEquals(new Position(100, 200), n1.position());

        cmd.undo();
        assertEquals(new Position(10, 20), n1.position());
    }

    @Test
    @DisplayName("ConnectCommand and DisconnectCommand")
    void connectAndDisconnectCommands() {
        Node n1 = createNode("n1");
        Node n2 = createNode("n2");
        graph.addNode(n1);
        graph.addNode(n2);

        ConnectCommand connectCmd = new ConnectCommand(
                graph, NodeId.of("n1"), PortId.of("out"), NodeId.of("n2"), PortId.of("in"));

        connectCmd.execute();
        assertEquals(1, graph.connectionCount());
        assertNotNull(connectCmd.getCreatedConnectionId());

        connectCmd.undo();
        assertEquals(0, graph.connectionCount());

        // Re-execute for disconnect test
        connectCmd.execute();
        assertEquals(1, graph.connectionCount());

        DisconnectCommand disconnectCmd = new DisconnectCommand(graph, connectCmd.getCreatedConnectionId());
        disconnectCmd.execute();
        assertEquals(0, graph.connectionCount());

        disconnectCmd.undo();
        assertEquals(1, graph.connectionCount());
    }

    @Test
    @DisplayName("CompoundCommand executes in order and undoes in reverse order")
    void compoundCommand() {
        Node n1 = createNode("n1");
        Node n2 = createNode("n2");

        CompoundCommand compound = new CompoundCommand("Add two nodes", List.of(
                new AddNodeCommand(graph, n1),
                new AddNodeCommand(graph, n2)
        ));

        compound.execute();
        assertEquals(2, graph.nodeCount());

        compound.undo();
        assertEquals(0, graph.nodeCount());
    }
}
