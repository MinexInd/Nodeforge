package net.minex.nodeforge.api.serialization;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphSerializer & JSON Persistence")
class GraphSerializerTest {

    private GraphSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new GraphSerializer();
    }

    @Test
    @DisplayName("full roundtrip serialization preserves nodes, ports, properties, metadata, and connections")
    void fullRoundtrip() {
        Graph original = new Graph("skill_tree");
        original.setMetadata("author", "Minex");
        original.setMetadata("version", "1.0");

        Node n1 = Node.builder(NodeId.of("start"), "event:start")
                .displayName("Game Start")
                .position(100.0, 150.0)
                .size(new Size(180.0, 90.0))
                .outputPort("exec_out", "Out", BuiltinPortTypes.EXECUTION)
                .metadata("initial_score", "10.0")
                .metadata("is_active", "true")
                .metadata("tag", "root")
                .build();

        Node n2 = Node.builder(NodeId.of("reward"), "action:reward")
                .displayName("Give Reward")
                .position(350.0, 150.0)
                .size(new Size(200.0, 100.0))
                .inputPort("exec_in", "In", BuiltinPortTypes.EXECUTION)
                .inputPort("multiplier", "Mult", BuiltinPortTypes.DOUBLE)
                .metadata("item_id", "minecraft:diamond")
                .build();

        original.addNode(n1);
        original.addNode(n2);

        original.connect(NodeId.of("start"), PortId.of("exec_out"), NodeId.of("reward"), PortId.of("exec_in"));

        // Serialize to JSON
        String json = serializer.toJson(original);
        assertNotNull(json);
        assertTrue(json.contains("\"schema_version\": 1"));
        assertTrue(json.contains("\"id\": \"skill_tree\""));

        // Deserialize back
        Graph restored = serializer.fromJson(json);
        assertNotNull(restored);
        assertEquals("skill_tree", restored.id());
        assertEquals("Minex", restored.getMetadata("author"));
        assertEquals("1.0", restored.getMetadata("version"));

        assertEquals(2, restored.nodeCount());
        assertEquals(1, restored.connectionCount());

        Node r1 = restored.getNode(NodeId.of("start"));
        assertNotNull(r1);
        assertEquals("Game Start", r1.displayName());
        assertEquals(new Position(100.0, 150.0), r1.position());
        assertEquals(new Size(180.0, 90.0), r1.size());
        assertEquals("10.0", r1.getMetadata("initial_score"));
        assertEquals("true", r1.getMetadata("is_active"));
        assertEquals("root", r1.getMetadata("tag"));

        Node r2 = restored.getNode(NodeId.of("reward"));
        assertNotNull(r2);
        assertEquals("minecraft:diamond", r2.getMetadata("item_id"));
        assertEquals(2, r2.ports().size());

        assertFalse(restored.getConnectionsForPort(NodeId.of("start"), PortId.of("exec_out")).isEmpty());
    }

    @Test
    @DisplayName("rejects missing or unsupported schema_version")
    void schemaValidation() {
        String noVersion = "{\"id\":\"test\",\"nodes\":[],\"connections\":[]}";
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(noVersion));

        String badVersion = "{\"schema_version\":999,\"id\":\"test\",\"nodes\":[],\"connections\":[]}";
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(badVersion));
    }

    @Test
    @DisplayName("rejects malformed JSON syntax")
    void malformedJson() {
        String badJson = "{ invalid json here ...";
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(badJson));
    }

    @Test
    @DisplayName("rejects connections with non-existent node endpoints")
    void danglingConnections() {
        String invalidConn = """
                {
                  "schema_version": 1,
                  "id": "bad_conn",
                  "nodes": [],
                  "connections": [
                    { "id": "c1", "from_node": "n1", "from_port": "p1", "to_node": "n2", "to_port": "p2" }
                  ]
                }
                """;
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(invalidConn));
    }
}
