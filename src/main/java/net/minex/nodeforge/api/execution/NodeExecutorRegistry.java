package net.minex.nodeforge.api.execution;

import net.minex.nodeforge.api.registry.NodeTypeId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry mapping node type keys to {@link NodeExecutor} logic handlers.
 */
public class NodeExecutorRegistry {

    private static final NodeExecutorRegistry INSTANCE = new NodeExecutorRegistry();

    private final Map<String, NodeExecutor> executors = new ConcurrentHashMap<>();

    public NodeExecutorRegistry() {}

    /** Returns the shared global singleton instance. */
    public static NodeExecutorRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a node executor handler for the given type key string.
     *
     * @param typeKey  unique node type key (e.g. "math:add")
     * @param executor the logic executor handler
     * @return the previous executor registered for this type key, if any
     */
    public NodeExecutor register(String typeKey, NodeExecutor executor) {
        Objects.requireNonNull(typeKey, "typeKey must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        if (typeKey.isBlank()) {
            throw new IllegalArgumentException("typeKey must not be blank");
        }
        return executors.put(typeKey, executor);
    }

    /**
     * Registers a node executor handler for the given namespaced {@link NodeTypeId}.
     */
    public NodeExecutor register(NodeTypeId typeId, NodeExecutor executor) {
        Objects.requireNonNull(typeId, "typeId must not be null");
        return register(typeId.value(), executor);
    }

    /**
     * Retrieves the executor registered for the given type key, or {@code null}.
     */
    public NodeExecutor get(String typeKey) {
        if (typeKey == null) return null;
        return executors.get(typeKey);
    }

    /**
     * Retrieves the executor registered for the given {@link NodeTypeId}, or {@code null}.
     */
    public NodeExecutor get(NodeTypeId typeId) {
        if (typeId == null) return null;
        return get(typeId.value());
    }

    /** Returns {@code true} if an executor is registered for the given type key. */
    public boolean has(String typeKey) {
        if (typeKey == null) return false;
        return executors.containsKey(typeKey);
    }

    /** Unregisters the executor for the given type key. */
    public NodeExecutor unregister(String typeKey) {
        if (typeKey == null) return null;
        return executors.remove(typeKey);
    }

    /** Returns the number of registered executors. */
    public int size() {
        return executors.size();
    }

    /** Clears all registered executors. */
    public void clear() {
        executors.clear();
    }
}
