package net.minex.nodeforge.client.render.node;

import net.minex.nodeforge.api.registry.NodeTypeId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry mapping node type keys to custom {@link CustomNodeRenderer} instances.
 */
public class NodeRendererRegistry {

    private static final NodeRendererRegistry INSTANCE = new NodeRendererRegistry();

    private final Map<String, CustomNodeRenderer> renderers = new ConcurrentHashMap<>();

    public NodeRendererRegistry() {}

    /** Returns the shared global singleton instance. */
    public static NodeRendererRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a custom node renderer for the given node type key string.
     *
     * @param typeKey  node type key (e.g. "math:add" or "mymod:custom")
     * @param renderer custom node renderer
     * @return previous renderer, if any
     */
    public CustomNodeRenderer register(String typeKey, CustomNodeRenderer renderer) {
        Objects.requireNonNull(typeKey, "typeKey must not be null");
        Objects.requireNonNull(renderer, "renderer must not be null");
        if (typeKey.isBlank()) {
            throw new IllegalArgumentException("typeKey must not be blank");
        }
        return renderers.put(typeKey, renderer);
    }

    /**
     * Registers a custom node renderer for a namespaced {@link NodeTypeId}.
     */
    public CustomNodeRenderer register(NodeTypeId typeId, CustomNodeRenderer renderer) {
        Objects.requireNonNull(typeId, "typeId must not be null");
        return register(typeId.value(), renderer);
    }

    /** Retrieves the custom renderer for the given type key, or {@code null}. */
    public CustomNodeRenderer get(String typeKey) {
        if (typeKey == null) return null;
        return renderers.get(typeKey);
    }

    /** Retrieves the custom renderer for the given {@link NodeTypeId}, or {@code null}. */
    public CustomNodeRenderer get(NodeTypeId typeId) {
        if (typeId == null) return null;
        return get(typeId.value());
    }

    /** Returns {@code true} if a custom renderer is registered for the given type key. */
    public boolean has(String typeKey) {
        if (typeKey == null) return false;
        return renderers.containsKey(typeKey);
    }

    /** Unregisters a custom node renderer. */
    public CustomNodeRenderer unregister(String typeKey) {
        if (typeKey == null) return null;
        return renderers.remove(typeKey);
    }

    /** Returns total number of registered custom renderers. */
    public int size() {
        return renderers.size();
    }

    /** Clears all registered custom renderers. */
    public void clear() {
        renderers.clear();
    }
}
