package net.minex.nodeforge.client.render.layer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry and dispatcher for custom {@link CanvasLayer} instances.
 */
public class CanvasLayerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("NodeForge/CanvasLayers");
    private static final CanvasLayerRegistry INSTANCE = new CanvasLayerRegistry();

    private final List<CanvasLayer> layers = new CopyOnWriteArrayList<>();

    public CanvasLayerRegistry() {}

    public static CanvasLayerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a custom canvas layer.
     *
     * @param layer the layer to register, must not be null
     */
    public synchronized void register(CanvasLayer layer) {
        Objects.requireNonNull(layer, "layer must not be null");
        if (layers.contains(layer)) return;
        layers.add(layer);
        layers.sort(Comparator.comparingInt(CanvasLayer::order));
    }

    /** Unregisters a canvas layer. */
    public synchronized void unregister(CanvasLayer layer) {
        if (layer != null) {
            layers.remove(layer);
        }
    }

    /**
     * Renders all registered layers that participate in the given phase.
     */
    public void renderLayers(DrawContext context, TextRenderer textRenderer, EditorState state,
                             NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
        if (layers.isEmpty() || context == null) return;

        for (CanvasLayer layer : layers) {
            try {
                if (layer.shouldRender(phase)) {
                    layer.render(context, textRenderer, state, theme, phase, screenW, screenH);
                }
            } catch (Exception e) {
                LOGGER.error("Error rendering canvas layer '{}' during phase {}", layer.id(), phase, e);
            }
        }
    }

    public int layerCount() {
        return layers.size();
    }

    public List<CanvasLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public void clear() {
        layers.clear();
    }
}
