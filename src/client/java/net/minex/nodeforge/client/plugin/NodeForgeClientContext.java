package net.minex.nodeforge.client.plugin;

import net.minex.nodeforge.client.render.icon.NodeIconRegistry;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;
import net.minex.nodeforge.client.render.vfx.VfxManager;

import java.util.Objects;

/**
 * Context provided to {@link NodeForgeClientPlugin} instances during client-side initialization.
 */
public record NodeForgeClientContext(
        ThemeRegistry themeRegistry,
        NodeRendererRegistry nodeRendererRegistry,
        NodeIconRegistry iconRegistry,
        CanvasLayerRegistry canvasLayerRegistry,
        VfxManager vfxManager
) {

    public NodeForgeClientContext {
        Objects.requireNonNull(themeRegistry, "themeRegistry must not be null");
        Objects.requireNonNull(nodeRendererRegistry, "nodeRendererRegistry must not be null");
        Objects.requireNonNull(iconRegistry, "iconRegistry must not be null");
        Objects.requireNonNull(canvasLayerRegistry, "canvasLayerRegistry must not be null");
        Objects.requireNonNull(vfxManager, "vfxManager must not be null");
    }

    /** Creates a default client context using the global singleton registries. */
    public static NodeForgeClientContext createDefault() {
        return new NodeForgeClientContext(
                ThemeRegistry.getInstance(),
                NodeRendererRegistry.getInstance(),
                NodeIconRegistry.getInstance(),
                CanvasLayerRegistry.getInstance(),
                VfxManager.getInstance()
        );
    }
}
