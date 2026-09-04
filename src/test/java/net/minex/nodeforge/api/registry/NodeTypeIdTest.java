package net.minex.nodeforge.api.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeTypeId")
class NodeTypeIdTest {

    @Test
    @DisplayName("creates with string and namespaced factories")
    void creation() {
        NodeTypeId id1 = NodeTypeId.of("nodeforge:math_add");
        assertEquals("nodeforge:math_add", id1.value());
        assertEquals("NodeTypeId[nodeforge:math_add]", id1.toString());

        NodeTypeId id2 = NodeTypeId.of("testmod", "heal_player");
        assertEquals("testmod:heal_player", id2.value());
    }

    @Test
    @DisplayName("rejects null values")
    void nullValues() {
        assertThrows(NullPointerException.class, () -> new NodeTypeId(null));
        assertThrows(NullPointerException.class, () -> NodeTypeId.of(null));
        assertThrows(NullPointerException.class, () -> NodeTypeId.of("ns", null));
        assertThrows(NullPointerException.class, () -> NodeTypeId.of(null, "path"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank values")
    void blankValues(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new NodeTypeId(blank));
        assertThrows(IllegalArgumentException.class, () -> NodeTypeId.of(blank));
        assertThrows(IllegalArgumentException.class, () -> NodeTypeId.of("ns", blank));
        assertThrows(IllegalArgumentException.class, () -> NodeTypeId.of(blank, "path"));
    }

    @Test
    @DisplayName("equality and hashCode")
    void equalsAndHashCode() {
        NodeTypeId id1 = NodeTypeId.of("testmod:heal");
        NodeTypeId id2 = NodeTypeId.of("testmod", "heal");
        NodeTypeId id3 = NodeTypeId.of("testmod:damage");

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
    }
}
