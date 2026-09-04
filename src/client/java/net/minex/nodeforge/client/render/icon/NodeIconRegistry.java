package net.minex.nodeforge.client.render.icon;

import net.minex.nodeforge.api.registry.NodeTypeId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe client registry mapping node type keys to visual {@link NodeIcon} badges.
 */
public class NodeIconRegistry {

    private static final NodeIconRegistry INSTANCE = new NodeIconRegistry();

    private final Map<String, NodeIcon> icons = new ConcurrentHashMap<>();

    public NodeIconRegistry() {}

    /** Returns the shared global singleton instance. */
    public static NodeIconRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers an icon for the given node type key string.
     *
     * @param typeKey unique node type key (e.g. "math:add")
     * @param icon    visual node icon
     * @return previous icon registered for this type key, if any
     */
    public NodeIcon register(String typeKey, NodeIcon icon) {
        Objects.requireNonNull(typeKey, "typeKey must not be null");
        Objects.requireNonNull(icon, "icon must not be null");
        if (typeKey.isBlank()) {
            throw new IllegalArgumentException("typeKey must not be blank");
        }
        return icons.put(typeKey, icon);
    }

    /**
     * Registers an icon for the given namespaced {@link NodeTypeId}.
     */
    public NodeIcon register(NodeTypeId typeId, NodeIcon icon) {
        Objects.requireNonNull(typeId, "typeId must not be null");
        return register(typeId.value(), icon);
    }

    /** Retrieves the icon registered for the given type key, or {@code null}. */
    public NodeIcon get(String typeKey) {
        if (typeKey == null) return null;
        return icons.get(typeKey);
    }

    /** Retrieves the icon registered for the given {@link NodeTypeId}, or {@code null}. */
    public NodeIcon get(NodeTypeId typeId) {
        if (typeId == null) return null;
        return get(typeId.value());
    }

    /** Returns {@code true} if an icon is registered for the given type key. */
    public boolean has(String typeKey) {
        if (typeKey == null) return false;
        return icons.containsKey(typeKey);
    }

    /** Unregisters the icon for the given type key. */
    public NodeIcon unregister(String typeKey) {
        if (typeKey == null) return null;
        return icons.remove(typeKey);
    }

    /** Returns the total number of registered icons. */
    public int size() {
        return icons.size();
    }

    /** Clears all registered icons. */
    public void clear() {
        icons.clear();
    }
}
