package net.minex.nodeforge.demo;

import net.minex.nodeforge.api.plugin.PluginManager;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.client.plugin.ClientPluginManager;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;
import net.minex.nodeforge.demo.client.DemoClientPlugin;
import net.minex.nodeforge.demo.client.WatermarkCanvasLayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DemoPlugin")
class DemoPluginTest {

    @org.junit.jupiter.api.BeforeEach
    @org.junit.jupiter.api.AfterEach
    void resetSingletons() {
        PluginManager.getInstance().resetForTesting();
        ClientPluginManager.getInstance().resetForTesting();
        ThemeRegistry.getInstance().unregister(DemoClientPlugin.NEON_MATRIX_THEME);
        CanvasLayerRegistry.getInstance().clear();
    }

    @Test
    @DisplayName("registers demo math plugin through PluginManager")
    void registerDemoMathPlugin() {
        DemoMathPlugin mathPlugin = new DemoMathPlugin();
        PluginManager.getInstance().registerPlugin(mathPlugin);
        PluginManager.getInstance().initializePlugins();

        assertTrue(PortTypeRegistry.getInstance().has(DemoMathPlugin.FLOAT_TYPE_ID));
        assertTrue(PortTypeRegistry.getInstance().has(DemoMathPlugin.VEC2_TYPE_ID));

        assertTrue(NodeDefinitionRegistry.getInstance().has(DemoMathPlugin.ADD_NODE));
        assertTrue(NodeDefinitionRegistry.getInstance().has(DemoMathPlugin.MULTIPLY_NODE));
        assertTrue(NodeDefinitionRegistry.getInstance().has(DemoMathPlugin.CLAMP_NODE));
        assertTrue(NodeDefinitionRegistry.getInstance().has(DemoMathPlugin.VEC2_MAKE_NODE));
    }

    @Test
    @DisplayName("registers demo client plugin through ClientPluginManager")
    void registerDemoClientPlugin() {
        DemoClientPlugin clientPlugin = new DemoClientPlugin();
        ClientPluginManager.getInstance().registerPlugin(clientPlugin);
        ClientPluginManager.getInstance().initializePlugins();

        NodeTheme theme = ThemeRegistry.getInstance().get(DemoClientPlugin.NEON_MATRIX_THEME);
        assertNotNull(theme, "Neon Matrix theme should be registered");

        boolean hasWatermark = CanvasLayerRegistry.getInstance().getLayers().stream()
                .anyMatch(l -> l instanceof WatermarkCanvasLayer);
        assertTrue(hasWatermark, "WatermarkCanvasLayer should be registered in CanvasLayerRegistry");
    }
}
