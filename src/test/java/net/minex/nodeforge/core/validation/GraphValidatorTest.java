package net.minex.nodeforge.core.validation;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphValidator & ValidationError")
class GraphValidatorTest {

    @Test
    @DisplayName("valid graph returns no errors")
    void validGraph() {
        Graph graph = new Graph("valid");
        Node n1 = Node.builder(NodeId.of("n1"), "t").outputPort("out", "Out", "data").build();
        Node n2 = Node.builder(NodeId.of("n2"), "t").inputPort("in", "In", "data").build();

        graph.addNode(n1);
        graph.addNode(n2);
        graph.connect(NodeId.of("n1"), PortId.of("out"), NodeId.of("n2"), PortId.of("in"));

        List<ValidationError> errors = GraphValidator.validate(graph);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("ValidationError factory methods and string formats")
    void errorFactories() {
        ValidationError graphErr = ValidationError.graphError(ValidationSeverity.ERROR, "Global issue");
        assertEquals(ValidationSeverity.ERROR, graphErr.severity());
        assertEquals("Global issue", graphErr.message());
        assertNull(graphErr.nodeId());
        assertNull(graphErr.portId());
        assertEquals("[ERROR] Global issue", graphErr.toString());

        ValidationError nodeErr = ValidationError.nodeError(ValidationSeverity.WARNING, "Node issue", NodeId.of("n1"));
        assertEquals(NodeId.of("n1"), nodeErr.nodeId());
        assertNull(nodeErr.portId());
        assertEquals("[WARNING] Node issue (node: n1)", nodeErr.toString());

        ValidationError portErr = ValidationError.portError(ValidationSeverity.INFO, "Port info", NodeId.of("n1"), PortId.of("p1"));
        assertEquals(NodeId.of("n1"), portErr.nodeId());
        assertEquals(PortId.of("p1"), portErr.portId());
        assertEquals("[INFO] Port info (node: n1, port: p1)", portErr.toString());
    }

    @Test
    @DisplayName("detects invalid connection states if graph internals contain corrupted state")
    @SuppressWarnings("unchecked")
    void corruptedGraphValidation() throws Exception {
        Graph graph = new Graph("corrupted");

        Node n1 = Node.builder(NodeId.of("n1"), "t")
                .inputPort("in", "In", "data")
                .outputPort("out", "Out", "data")
                .outputPort("str_out", "Str Out", BuiltinPortTypes.STRING)
                .build();
        graph.addNode(n1);

        // Inject invalid connections directly via reflection to simulate corrupted/imported state
        Field connField = Graph.class.getDeclaredField("connections");
        connField.setAccessible(true);
        Map<ConnectionId, Connection> conns = (Map<ConnectionId, Connection>) connField.get(graph);

        // 1. Missing target node
        conns.put(ConnectionId.of("c1"), new Connection(
                ConnectionId.of("c1"), NodeId.of("n1"), PortId.of("out"), NodeId.of("missing_target"), PortId.of("in")));

        List<ValidationError> errors = GraphValidator.validate(graph);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).message().contains("references missing target node"));

        conns.clear();

        // 2. Missing source node
        conns.put(ConnectionId.of("c2"), new Connection(
                ConnectionId.of("c2"), NodeId.of("missing_src"), PortId.of("out"), NodeId.of("n1"), PortId.of("in")));

        errors = GraphValidator.validate(graph);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).message().contains("references missing source node"));

        conns.clear();

        // 3. Self-connection + missing port
        conns.put(ConnectionId.of("c3"), new Connection(
                ConnectionId.of("c3"), NodeId.of("n1"), PortId.of("out"), NodeId.of("n1"), PortId.of("nonexistent_port")));

        errors = GraphValidator.validate(graph);
        assertEquals(2, errors.size()); // 1 warning for self connection, 1 error for missing port
        assertTrue(errors.stream().anyMatch(e -> e.severity() == ValidationSeverity.WARNING && e.message().contains("self-connection")));
        assertTrue(errors.stream().anyMatch(e -> e.severity() == ValidationSeverity.ERROR && e.message().contains("missing target port")));

        conns.clear();

        // 4. Inverted directions (source is input, target is output)
        Node n2 = Node.builder(NodeId.of("n2"), "t")
                .inputPort("in", "In", "data")
                .outputPort("out", "Out", "data")
                .inputPort("int_in", "Int In", BuiltinPortTypes.INTEGER)
                .build();
        graph.addNode(n2);

        conns.put(ConnectionId.of("c4"), new Connection(
                ConnectionId.of("c4"), NodeId.of("n1"), PortId.of("in"), NodeId.of("n2"), PortId.of("out")));

        errors = GraphValidator.validate(graph);
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("source port 'in' is not an output port")));
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("target port 'out' is not an input port")));

        conns.clear();

        // 5. Incompatible port types (String -> Integer)
        conns.put(ConnectionId.of("c5"), new Connection(
                ConnectionId.of("c5"), NodeId.of("n1"), PortId.of("str_out"), NodeId.of("n2"), PortId.of("int_in")));

        errors = GraphValidator.validate(graph);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).message().contains("incompatible types"));
    }
}
