package net.minex.nodeforge.client.plugin;

import net.minex.nodeforge.client.render.icon.NodeIconRegistry;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClientPluginManager")
class ClientPluginManagerTest {

    private ClientPluginManager manager;

    @BeforeEach
    void setUp() {
        manager = new ClientPluginManager();
    }

    @Test
    @DisplayName("executes client plugin registrations in proper order")
    void registrationOrder() {
        List<String> order = new ArrayList<>();

        NodeForgeClientPlugin plugin = new NodeForgeClientPlugin() {
            @Override
            public void registerThemes(ThemeRegistry registry) {
                order.add("themes");
            }

            @Override
            public void registerCustomNodeRenderers(NodeRendererRegistry registry) {
                order.add("renderers");
            }

            @Override
            public void registerNodeIcons(NodeIconRegistry registry) {
                order.add("icons");
            }

            @Override
            public void registerCanvasLayers(CanvasLayerRegistry registry) {
                order.add("layers");
            }

            @Override
            public void onInitializeClient(NodeForgeClientContext context) {
                order.add("init_client");
            }
        };

        manager.registerPlugin(plugin);
        manager.initializePlugins();

        assertEquals(List.of("themes", "renderers", "icons", "layers", "init_client"), order);
        assertTrue(manager.isInitialized());
    }

    @Test
    @DisplayName("isolates client plugin exceptions without crashing other plugins")
    void faultIsolation() {
        AtomicBoolean healthyCalled = new AtomicBoolean(false);

        NodeForgeClientPlugin failing = new NodeForgeClientPlugin() {
            @Override
            public void registerThemes(ThemeRegistry registry) {
                throw new RuntimeException("Theme registration crash");
            }
        };

        NodeForgeClientPlugin healthy = new NodeForgeClientPlugin() {
            @Override
            public void onInitializeClient(NodeForgeClientContext context) {
                healthyCalled.set(true);
            }
        };

        manager.registerPlugin(failing);
        manager.registerPlugin(healthy);

        assertDoesNotThrow(() -> manager.initializePlugins());
        assertTrue(healthyCalled.get(), "Healthy client plugin should still run onInitializeClient");
    }
}
