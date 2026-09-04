package net.minex.nodeforge.demo.client;

import net.minex.nodeforge.client.plugin.NodeForgeClientContext;
import net.minex.nodeforge.client.plugin.NodeForgeClientPlugin;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.client.render.theme.ThemeId;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Living reference implementation of a {@link NodeForgeClientPlugin}.
 *
 * <p>Demonstrates registering custom color themes and canvas rendering layers.
 */
public class DemoClientPlugin implements NodeForgeClientPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("DemoClientPlugin");

    public static final ThemeId NEON_MATRIX_THEME = ThemeId.of("demo:neon_matrix");

    @Override
    public String id() {
        return "DemoClientPlugin";
    }

    @Override
    public void registerThemes(ThemeRegistry registry) {
        NodeTheme neonMatrix = NodeTheme.builder()
                .backgroundColor(0xFF030A06)
                .gridMinorColor(0x2000FF66)
                .gridMajorColor(0x4000FF66)
                .nodeBackgroundColor(0xFF05150C)
                .nodeHeaderColor(0xFF0A2B19)
                .nodeBorderColor(0xFF14532D)
                .nodeSelectedBorderColor(0xFF00FF66)
                .textColor(0xFFE6FFFA)
                .textSecondaryColor(0xFF6EE7B7)
                .cableDefaultColor(0xFF00E5FF)
                .cableExecutionColor(0xFF00FF66)
                .categoryMathColor(0xFF00FF66)
                .build();

        registry.register(NEON_MATRIX_THEME, neonMatrix);
    }

    @Override
    public void registerCanvasLayers(CanvasLayerRegistry registry) {
        registry.register(new WatermarkCanvasLayer());
    }

    @Override
    public void onInitializeClient(NodeForgeClientContext context) {
        LOGGER.info("DemoClientPlugin initialized successfully with NEON_MATRIX theme and watermark layer");
    }
}
