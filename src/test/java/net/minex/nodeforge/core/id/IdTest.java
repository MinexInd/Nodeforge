package net.minex.nodeforge.core.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Identifier Types")
class IdTest {

    @Nested
    @DisplayName("NodeId")
    class NodeIdTests {
        @Test
        @DisplayName("creates with string value")
        void createsWithValue() {
            NodeId id = NodeId.of("node_1");
            assertEquals("node_1", id.value());
            assertEquals("NodeId[node_1]", id.toString());
        }

        @Test
        @DisplayName("throws on null value")
        void throwsOnNull() {
            assertThrows(NullPointerException.class, () -> new NodeId(null));
            assertThrows(NullPointerException.class, () -> NodeId.of(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("throws on blank value")
        void throwsOnBlank(String blankValue) {
            assertThrows(IllegalArgumentException.class, () -> new NodeId(blankValue));
            assertThrows(IllegalArgumentException.class, () -> NodeId.of(blankValue));
        }

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            NodeId id1 = NodeId.of("node_1");
            NodeId id2 = NodeId.of("node_1");
            NodeId id3 = NodeId.of("node_2");

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
            assertNotEquals(id1, id3);
            assertNotEquals(id1, null);
            assertNotEquals(id1, "node_1");
        }

        @Test
        @DisplayName("random generates unique IDs")
        void randomGeneratesUnique() {
            Set<NodeId> generated = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                NodeId id = NodeId.random();
                assertNotNull(id.value());
                assertFalse(id.value().isEmpty());
                assertTrue(generated.add(id), "UUID collision detected at iteration " + i);
            }
        }
    }

    @Nested
    @DisplayName("PortId")
    class PortIdTests {
        @Test
        @DisplayName("creates with string value")
        void createsWithValue() {
            PortId id = PortId.of("port_in");
            assertEquals("port_in", id.value());
            assertEquals("PortId[port_in]", id.toString());
        }

        @Test
        @DisplayName("throws on null value")
        void throwsOnNull() {
            assertThrows(NullPointerException.class, () -> new PortId(null));
            assertThrows(NullPointerException.class, () -> PortId.of(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("throws on blank value")
        void throwsOnBlank(String blankValue) {
            assertThrows(IllegalArgumentException.class, () -> new PortId(blankValue));
            assertThrows(IllegalArgumentException.class, () -> PortId.of(blankValue));
        }

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            PortId id1 = PortId.of("input");
            PortId id2 = PortId.of("input");
            PortId id3 = PortId.of("output");

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
            assertNotEquals(id1, id3);
        }
    }

    @Nested
    @DisplayName("ConnectionId")
    class ConnectionIdTests {
        @Test
        @DisplayName("creates with string value")
        void createsWithValue() {
            ConnectionId id = ConnectionId.of("conn_1");
            assertEquals("conn_1", id.value());
            assertEquals("ConnectionId[conn_1]", id.toString());
        }

        @Test
        @DisplayName("throws on null value")
        void throwsOnNull() {
            assertThrows(NullPointerException.class, () -> new ConnectionId(null));
            assertThrows(NullPointerException.class, () -> ConnectionId.of(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("throws on blank value")
        void throwsOnBlank(String blankValue) {
            assertThrows(IllegalArgumentException.class, () -> new ConnectionId(blankValue));
            assertThrows(IllegalArgumentException.class, () -> ConnectionId.of(blankValue));
        }

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            ConnectionId id1 = ConnectionId.of("c1");
            ConnectionId id2 = ConnectionId.of("c1");
            ConnectionId id3 = ConnectionId.of("c2");

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
            assertNotEquals(id1, id3);
        }

        @Test
        @DisplayName("random generates unique IDs")
        void randomGeneratesUnique() {
            Set<ConnectionId> generated = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                ConnectionId id = ConnectionId.random();
                assertNotNull(id.value());
                assertFalse(id.value().isEmpty());
                assertTrue(generated.add(id));
            }
        }
    }
}
