package net.minex.nodeforge.client.plugin;

import net.minex.nodeforge.client.render.icon.NodeIconRegistry;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;

/**
 * Client-side entrypoint interface for external mods extending NodeForge's rendering and UI systems.
 *
 * <p>Third-party mods can declare client plugins in their {@code fabric.mod.json}:
 * <pre>{@code
 * "entrypoints": {
 *   "nodeforge:client_plugin": [
 *     "com.example.mymod.MyNodeForgeClientPlugin"
 *   ]
 * }
 * }</pre>
 * Alternatively, client plugins can be registered programmatically via
 * {@link ClientPluginManager#registerPlugin(NodeForgeClientPlugin)}.
 */
public interface NodeForgeClientPlugin {

    /** Unique identifier for this client plugin (defaults to simple class name or anonymous fallback). */
    default String id() {
        String name = getClass().getSimpleName();
        return name.isEmpty() ? "anonymous@" + Integer.toHexString(System.identityHashCode(this)) : name;
    }

    /** Registers custom color and styling themes. */
    default void registerThemes(ThemeRegistry registry) {
    }

    /** Registers custom node card renderers for specialized node types. */
    default void registerCustomNodeRenderers(NodeRendererRegistry registry) {
    }

    /** Registers custom node title bar icons. */
    default void registerNodeIcons(NodeIconRegistry registry) {
    }

    /** Registers custom canvas rendering layers. */
    default void registerCanvasLayers(CanvasLayerRegistry registry) {
    }

    /** General client lifecycle hook invoked after all registrations are complete. */
    default void onInitializeClient(NodeForgeClientContext context) {
    }
}
