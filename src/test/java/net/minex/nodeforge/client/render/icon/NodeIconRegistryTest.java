package net.minex.nodeforge.client.render.icon;

import net.minex.nodeforge.api.registry.NodeTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeIconRegistry")
class NodeIconRegistryTest {

    private NodeIconRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NodeIconRegistry();
    }

    @Test
    @DisplayName("registers, queries, and unregisters node icons")
    void registerAndQuery() {
        NodeIcon icon = new NodeIcon.ColorSwatch(0xFF00FF00);

        assertNull(registry.get("math:add"));
        assertFalse(registry.has("math:add"));

        registry.register("math:add", icon);
        assertTrue(registry.has("math:add"));
        assertSame(icon, registry.get("math:add"));
        assertSame(icon, registry.get(NodeTypeId.of("math:add")));
        assertEquals(1, registry.size());

        assertSame(icon, registry.unregister("math:add"));
        assertEquals(0, registry.size());
        assertFalse(registry.has("math:add"));
    }

    @Test
    @DisplayName("rejects null and blank keys")
    void validation() {
        NodeIcon icon = new NodeIcon.Text("A", 0xFFFFFFFF);
        assertThrows(NullPointerException.class, () -> registry.register((String) null, icon));
        assertThrows(NullPointerException.class, () -> registry.register((NodeTypeId) null, icon));
        assertThrows(IllegalArgumentException.class, () -> registry.register("   ", icon));
    }
}
