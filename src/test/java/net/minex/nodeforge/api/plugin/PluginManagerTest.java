package net.minex.nodeforge.api.plugin;

import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PluginManager")
class PluginManagerTest {

    private PluginManager manager;

    @BeforeEach
    void setUp() {
        manager = new PluginManager();
    }

    @Test
    @DisplayName("executes plugin registration in strict dependency order")
    void registrationOrder() {
        List<String> order = new ArrayList<>();

        NodeForgePlugin plugin = new NodeForgePlugin() {
            @Override
            public void registerPortTypes(PortTypeRegistry registry) {
                order.add("port_types");
            }

            @Override
            public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
                order.add("node_definitions");
            }

            @Override
            public void registerExecutors(NodeExecutorRegistry registry) {
                order.add("executors");
            }

            @Override
            public void onInitialize(NodeForgeContext context) {
                order.add("on_initialize");
            }
        };

        manager.registerPlugin(plugin);
        manager.initializePlugins();

        assertEquals(List.of("port_types", "node_definitions", "executors", "on_initialize"), order);
        assertTrue(manager.isInitialized());
    }

    @Test
    @DisplayName("isolates plugin errors so failing plugins do not halt other plugins")
    void faultIsolation() {
        AtomicBoolean secondPluginInitialized = new AtomicBoolean(false);

        NodeForgePlugin failingPlugin = new NodeForgePlugin() {
            @Override
            public void registerPortTypes(PortTypeRegistry registry) {
                throw new RuntimeException("Simulated failure in port types");
            }

            @Override
            public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
                throw new RuntimeException("Simulated failure in node defs");
            }
        };

        NodeForgePlugin healthyPlugin = new NodeForgePlugin() {
            @Override
            public void onInitialize(NodeForgeContext context) {
                secondPluginInitialized.set(true);
            }
        };

        manager.registerPlugin(failingPlugin);
        manager.registerPlugin(healthyPlugin);

        assertDoesNotThrow(() -> manager.initializePlugins());
        assertTrue(secondPluginInitialized.get(), "Healthy plugin should still execute onInitialize");
    }

    @Test
    @DisplayName("prevent double-initialization on subsequent calls")
    void idempotentInitialization() {
        List<String> events = new ArrayList<>();

        manager.registerPlugin(new NodeForgePlugin() {
            @Override
            public void onInitialize(NodeForgeContext context) {
                events.add("init");
            }
        });

        manager.initializePlugins();
        manager.initializePlugins();

        assertEquals(1, events.size(), "onInitialize should only be called once");
    }

    @Test
    @DisplayName("late registered plugins after initialization run immediately")
    void lateRegistration() {
        manager.initializePlugins();
        assertTrue(manager.isInitialized());

        AtomicBoolean lateInit = new AtomicBoolean(false);
        manager.registerPlugin(new NodeForgePlugin() {
            @Override
            public void onInitialize(NodeForgeContext context) {
                lateInit.set(true);
            }
        });

        assertTrue(lateInit.get(), "Late registered plugin should initialize immediately");
    }

    @Test
    @DisplayName("anonymous plugins generate non-empty fallback id")
    void anonymousPluginGeneratesValidId() {
        NodeForgePlugin anonymous = new NodeForgePlugin() {};
        assertNotNull(anonymous.id());
        assertFalse(anonymous.id().isEmpty());
        assertTrue(anonymous.id().startsWith("anonymous@"));
    }
}
