package net.minex.nodeforge.stress;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.minex.nodeforge.api.serialization.GraphSerializationException;
import net.minex.nodeforge.api.serialization.GraphSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fuzzing and corrupted payload stress tests for graph serialization and deserialization.
 */
class SerializationFuzzingTest {

    private final GraphSerializer serializer = new GraphSerializer();

    @Test
    @DisplayName("Corrupt and invalid JSON strings throw GraphSerializationException")
    void testCorruptedJsonStrings() {
        // 1. Broken syntax
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{ broken json: ["));

        // 2. Empty string
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(""));

        // 3. Root is not an object
        assertThrows(GraphSerializationException.class, () -> serializer.fromJsonTree(new JsonArray()));
        assertThrows(GraphSerializationException.class, () -> serializer.fromJsonTree(new JsonPrimitive(42)));

        // 4. Missing schema_version
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"id\": \"g1\"}"));

        // 5. Unsupported future schema_version
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"schema_version\": 999, \"id\": \"g1\"}"));

        // 6. Missing or blank id
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"schema_version\": 1}"));
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"schema_version\": 1, \"id\": \"   \"}"));
    }

    @Test
    @DisplayName("Invalid node and port payloads throw GraphSerializationException")
    void testCorruptNodesAndPorts() {
        // Missing node id
        String missingNodeId = """
                {
                    "schema_version": 1,
                    "id": "test_graph",
                    "nodes": [
                        { "type": "math:add" }
                    ]
                }
                """;
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(missingNodeId));

        // Missing node type
        String missingNodeType = """
                {
                    "schema_version": 1,
                    "id": "test_graph",
                    "nodes": [
                        { "id": "n1" }
                    ]
                }
                """;
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(missingNodeType));

        // Invalid port direction
        String invalidPortDir = """
                {
                    "schema_version": 1,
                    "id": "test_graph",
                    "nodes": [
                        {
                            "id": "n1",
                            "type": "math:add",
                            "ports": [
                                { "id": "p1", "direction": "NON_EXISTENT_DIR" }
                            ]
                        }
                    ]
                }
                """;
        GraphSerializationException ex = assertThrows(GraphSerializationException.class, () -> serializer.fromJson(invalidPortDir));
        assertTrue(ex.getMessage().contains("Invalid port direction") || ex.getMessage().contains("direction"));
    }

    @Test
    @DisplayName("Broken or dangling connections in JSON payload throw GraphSerializationException")
    void testCorruptConnections() {
        // Connection referencing non-existent nodes
        String danglingConn = """
                {
                    "schema_version": 1,
                    "id": "test_graph",
                    "nodes": [],
                    "connections": [
                        {
                            "id": "c1",
                            "from_node": "non_existent_1",
                            "from_port": "out",
                            "to_node": "non_existent_2",
                            "to_port": "in"
                        }
                    ]
                }
                """;
        GraphSerializationException ex = assertThrows(GraphSerializationException.class, () -> serializer.fromJson(danglingConn));
        assertTrue(ex.getMessage().contains("Failed to restore connection") || ex.getMessage().contains("not found"));
    }
}
