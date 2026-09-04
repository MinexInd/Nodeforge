package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Node")
class NodeTest {

    @Test
    @DisplayName("builds basic node with defaults")
    void basicNode() {
        NodeId id = NodeId.of("math_add");
        Node node = Node.builder(id, "nodeforge:add")
                .build();

        assertEquals(id, node.id());
        assertEquals("nodeforge:add", node.typeKey());
        assertEquals("nodeforge:add", node.displayName());
        assertEquals(Position.ZERO, node.position());
        assertEquals(Size.DEFAULT, node.size());
        assertEquals(0, node.portCount());
        assertTrue(node.ports().isEmpty());
        assertTrue(node.inputPorts().isEmpty());
        assertTrue(node.outputPorts().isEmpty());
        assertTrue(node.metadata().isEmpty());
    }

    @Test
    @DisplayName("builds node with ports and customized properties")
    void configuredNode() {
        NodeId id = NodeId.of("node_1");
        Node node = Node.builder(id, "example:operation")
                .displayName("My Operation")
                .position(100, 200)
                .size(200, 100)
                .inputPort("in_a", "A", "number")
                .inputPort("in_b", "B", "number")
                .outputPort("out_result", "Result", "number")
                .metadata("author", "tester")
                .build();

        assertEquals("My Operation", node.displayName());
        assertEquals(new Position(100, 200), node.position());
        assertEquals(new Size(200, 100), node.size());
        assertEquals(3, node.portCount());

        assertEquals(2, node.inputPorts().size());
        assertEquals(1, node.outputPorts().size());

        assertTrue(node.hasPort(PortId.of("in_a")));
        assertTrue(node.hasPort(PortId.of("out_result")));
        assertFalse(node.hasPort(PortId.of("nonexistent")));
        assertFalse(node.hasPort(null));
        assertNull(node.getPort(null));

        Port portA = node.getPort(PortId.of("in_a"));
        assertNotNull(portA);
        assertEquals("A", portA.name());

        assertEquals("tester", node.getMetadata("author"));
        assertThrows(NullPointerException.class, () -> node.getMetadata(null));
        assertThrows(NullPointerException.class, () -> node.removeMetadata(null));
    }

    @Test
    @DisplayName("rejects duplicate port IDs immediately at builder.port()")
    void rejectsDuplicatePortsImmediately() {
        Node.Builder builder = Node.builder(NodeId.of("n1"), "type")
                .inputPort("dup", "First", "typeA");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                builder.outputPort("dup", "Second", "typeB"));
        assertTrue(ex.getMessage().contains("Duplicate port ID 'dup'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank typeKey and displayName")
    void rejectsBlankStrings(String blank) {
        assertThrows(IllegalArgumentException.class, () -> Node.builder(NodeId.of("n1"), blank));
        assertThrows(IllegalArgumentException.class, () ->
                Node.builder(NodeId.of("n1"), "type").displayName(blank));

        Node node = Node.builder(NodeId.of("n1"), "type").build();
        assertThrows(IllegalArgumentException.class, () -> node.setDisplayName(blank));
    }

    @Test
    @DisplayName("mutates position, size, and display name")
    void mutations() {
        Node node = Node.builder(NodeId.of("n1"), "type").build();

        node.setPosition(new Position(50, 60));
        assertEquals(new Position(50, 60), node.position());

        node.setSize(new Size(120, 60));
        assertEquals(new Size(120, 60), node.size());

        node.setDisplayName("Updated Name");
        assertEquals("Updated Name", node.displayName());

        assertThrows(NullPointerException.class, () -> node.setPosition(null));
        assertThrows(NullPointerException.class, () -> node.setSize(null));
        assertThrows(NullPointerException.class, () -> node.setDisplayName(null));
    }

    @Test
    @DisplayName("manages metadata entries")
    void metadataManagement() {
        Node node = Node.builder(NodeId.of("n1"), "type").build();

        node.setMetadata("color", "#FF0000");
        node.setMetadata("version", "1");
        assertEquals("#FF0000", node.getMetadata("color"));
        assertEquals("1", node.getMetadata("version"));

        assertEquals("#FF0000", node.removeMetadata("color"));
        assertNull(node.getMetadata("color"));
        assertNull(node.removeMetadata("color"));
    }

    @Test
    @DisplayName("equality based on NodeId")
    void equalsAndHashCode() {
        Node n1 = Node.builder(NodeId.of("same"), "typeA").displayName("A").build();
        Node n2 = Node.builder(NodeId.of("same"), "typeB").displayName("B").build();
        Node n3 = Node.builder(NodeId.of("other"), "typeA").build();

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
        assertNotEquals(n1, n3);
    }

    @Test
    @DisplayName("unmodifiable port collections")
    void unmodifiablePorts() {
        Node node = Node.builder(NodeId.of("n1"), "type")
                .inputPort("in", "In", "type")
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                node.ports().put(PortId.of("hack"), Port.input("hack", "H", "t")));
        assertThrows(UnsupportedOperationException.class, () ->
                node.inputPorts().add(Port.input("hack", "H", "t")));
    }

    @Test
    @DisplayName("rejects builder reuse after build()")
    void rejectsBuilderReuse() {
        Node.Builder builder = Node.builder(NodeId.of("n1"), "type")
                .inputPort("in", "In", "type");

        Node node1 = builder.build();
        assertNotNull(node1);

        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.displayName("New Name"));
        assertThrows(IllegalStateException.class, () -> builder.position(10, 10));
        assertThrows(IllegalStateException.class, () -> builder.size(10, 10));
        assertThrows(IllegalStateException.class, () -> builder.inputPort("in2", "In2", "type"));
        assertThrows(IllegalStateException.class, () -> builder.outputPort("out2", "Out2", "type"));
        assertThrows(IllegalStateException.class, () -> builder.metadata("k", "v"));
    }
}
