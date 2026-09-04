package net.minex.nodeforge.api.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortTypeRegistry")
class PortTypeRegistryTest {

    @Test
    @DisplayName("global instance contains all built-in port types")
    void globalInstanceHasBuiltins() {
        PortTypeRegistry registry = PortTypeRegistry.getInstance();

        assertNotNull(registry.get(BuiltinPortTypes.EXECUTION.id()));
        assertNotNull(registry.get("nodeforge:execution"));
        assertNotNull(registry.get(BuiltinPortTypes.BOOLEAN.id()));
        assertNotNull(registry.get(BuiltinPortTypes.INTEGER.id()));
        assertNotNull(registry.get(BuiltinPortTypes.LONG.id()));
        assertNotNull(registry.get(BuiltinPortTypes.FLOAT.id()));
        assertNotNull(registry.get(BuiltinPortTypes.DOUBLE.id()));
        assertNotNull(registry.get(BuiltinPortTypes.STRING.id()));
        assertNotNull(registry.get(BuiltinPortTypes.ANY.id()));

        assertTrue(registry.has(BuiltinPortTypes.INTEGER.id()));
        assertTrue(registry.has("nodeforge:string"));
        assertFalse(registry.has("unknown:type"));
        assertNull(registry.get("unknown:type"));
        assertNull(registry.get((PortTypeId) null));
        assertNull(registry.get((String) null));
    }

    @Test
    @DisplayName("registers custom port types in isolated registry")
    void customRegistration() {
        PortTypeRegistry registry = new PortTypeRegistry();
        assertEquals(0, registry.size());

        PortType<Double> custom = PortType.builder("custom:energy", Double.class).build();
        registry.register(custom);

        assertEquals(1, registry.size());
        assertTrue(registry.has(custom.id()));
        assertSame(custom, registry.get(custom.id()));

        // Idempotent re-registration of identical type is permitted
        assertDoesNotThrow(() -> registry.register(custom));

        // Re-registration with different properties/class throws
        PortType<String> conflicting = PortType.builder("custom:energy", String.class).build();
        assertThrows(IllegalArgumentException.class, () -> registry.register(conflicting));

        // Unregister
        assertTrue(registry.unregister(custom.id()));
        assertEquals(0, registry.size());
        assertFalse(registry.has(custom.id()));
        assertFalse(registry.unregister(custom.id()));
    }
}
