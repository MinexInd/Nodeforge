package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.port.BuiltinPortTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeDefinitionRegistry")
class NodeDefinitionRegistryTest {

    @Test
    @DisplayName("registers, queries, and unregisters definitions in isolated registry")
    void registryOperations() {
        NodeDefinitionRegistry registry = new NodeDefinitionRegistry();
        assertEquals(0, registry.size());

        NodeDefinition def1 = NodeDefinition.builder("math:add")
                .displayName("Add")
                .category(NodeCategory.MATH)
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("sum", "Sum", BuiltinPortTypes.DOUBLE)
                .build();

        NodeDefinition def2 = NodeDefinition.builder("math:multiply")
                .displayName("Multiply")
                .category(NodeCategory.MATH)
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("prod", "Product", BuiltinPortTypes.DOUBLE)
                .build();

        NodeDefinition def3 = NodeDefinition.builder("logic:if")
                .displayName("Branch")
                .category(NodeCategory.LOGIC)
                .inputPort("cond", "Condition", BuiltinPortTypes.BOOLEAN)
                .build();

        registry.register(def1);
        registry.register(def2);
        registry.register(def3);

        assertEquals(3, registry.size());
        assertTrue(registry.has(def1.id()));
        assertTrue(registry.has("math:multiply"));
        assertFalse(registry.has("unknown:node"));

        assertSame(def1, registry.get(def1.id()));
        assertSame(def2, registry.get("math:multiply"));
        assertNull(registry.get("unknown:node"));

        // byCategory filter
        List<NodeDefinition> mathNodes = registry.byCategory(NodeCategory.MATH);
        assertEquals(2, mathNodes.size());
        assertTrue(mathNodes.contains(def1));
        assertTrue(mathNodes.contains(def2));

        List<NodeDefinition> actionNodes = registry.byCategory(NodeCategory.ACTION);
        assertTrue(actionNodes.isEmpty());

        // Idempotent re-registration
        assertDoesNotThrow(() -> registry.register(def1));

        // Conflicting registration
        NodeDefinition conflict = NodeDefinition.builder("math:add")
                .displayName("Different Add")
                .build();
        assertThrows(IllegalArgumentException.class, () -> registry.register(conflict));

        // Unregister
        assertTrue(registry.unregister(def1.id()));
        assertEquals(2, registry.size());
        assertFalse(registry.has(def1.id()));

        registry.clear();
        assertEquals(0, registry.size());
    }
}
