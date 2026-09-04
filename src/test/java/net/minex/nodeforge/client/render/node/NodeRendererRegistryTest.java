package net.minex.nodeforge.client.render.node;

import net.minex.nodeforge.api.registry.NodeTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeRendererRegistry & Custom Renderers")
class NodeRendererRegistryTest {

    private NodeRendererRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NodeRendererRegistry();
    }

    @Test
    @DisplayName("registers, retrieves, and unregisters custom node renderers")
    void registerAndQuery() {
        CustomNodeRenderer renderer = (context, textRenderer, node, state, theme, isSelected, isHovered) -> {};

        assertNull(registry.get("custom:node"));
        assertFalse(registry.has("custom:node"));

        registry.register("custom:node", renderer);
        assertTrue(registry.has("custom:node"));
        assertSame(renderer, registry.get("custom:node"));
        assertSame(renderer, registry.get(NodeTypeId.of("custom:node")));

        assertEquals(1, registry.size());

        assertSame(renderer, registry.unregister("custom:node"));
        assertEquals(0, registry.size());
        assertFalse(registry.has("custom:node"));
    }

    @Test
    @DisplayName("rejects null and blank type keys")
    void invalidKeys() {
        CustomNodeRenderer renderer = (context, textRenderer, node, state, theme, isSelected, isHovered) -> {};
        assertThrows(NullPointerException.class, () -> registry.register((String) null, renderer));
        assertThrows(NullPointerException.class, () -> registry.register((NodeTypeId) null, renderer));
        assertThrows(IllegalArgumentException.class, () -> registry.register("   ", renderer));
    }
}
