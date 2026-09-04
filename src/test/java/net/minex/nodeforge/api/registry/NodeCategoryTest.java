package net.minex.nodeforge.api.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeCategory")
class NodeCategoryTest {

    @Test
    @DisplayName("built-in categories are properly configured")
    void builtInCategories() {
        assertNotNull(NodeCategory.ACTION);
        assertNotNull(NodeCategory.EVENT);
        assertNotNull(NodeCategory.CONDITION);
        assertNotNull(NodeCategory.MATH);
        assertNotNull(NodeCategory.LOGIC);
        assertNotNull(NodeCategory.DATA);
        assertNotNull(NodeCategory.FLOW_CONTROL);
        assertNotNull(NodeCategory.MISC);

        assertEquals("nodeforge:action", NodeCategory.ACTION.id());
        assertEquals("Action", NodeCategory.ACTION.displayName());
        assertTrue(NodeCategory.ACTION.order() < NodeCategory.MISC.order());
        assertTrue(NodeCategory.ACTION.parent().isEmpty());
    }

    @Test
    @DisplayName("creates custom top-level and nested categories")
    void customAndNestedCategories() {
        NodeCategory parent = NodeCategory.of("mod:skills", "Skills", 15);
        assertEquals("mod:skills", parent.id());
        assertEquals("Skills", parent.displayName());
        assertEquals(15, parent.order());
        assertTrue(parent.parent().isEmpty());

        NodeCategory combat = NodeCategory.nested("mod:skills_combat", "Combat Skills", 5, parent);
        assertEquals("mod:skills_combat", combat.id());
        assertEquals("Combat Skills", combat.displayName());
        assertEquals(5, combat.order());
        assertTrue(combat.parent().isPresent());
        assertSame(parent, combat.parent().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank ID and displayName")
    void rejectsBlank(String blank) {
        assertThrows(IllegalArgumentException.class, () -> NodeCategory.of(blank, "Name"));
        assertThrows(IllegalArgumentException.class, () -> NodeCategory.of("id", blank));
    }

    @Test
    @DisplayName("equality and toString")
    void equalsAndToString() {
        NodeCategory c1 = NodeCategory.of("cat:a", "First");
        NodeCategory c2 = NodeCategory.of("cat:a", "Second");
        NodeCategory c3 = NodeCategory.of("cat:b", "First");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
        assertEquals("NodeCategory[cat:a ('First')]", c1.toString());
    }
}
