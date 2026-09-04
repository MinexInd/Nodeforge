package net.minex.nodeforge.api.registry;

import net.minecraft.util.Identifier;
import net.minex.nodeforge.core.graph.Graph;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe global repository for loaded and registered {@link Graph} instances.
 */
public class GraphRegistry {

    private static final GraphRegistry INSTANCE = new GraphRegistry();

    private final Map<String, Graph> graphs = new ConcurrentHashMap<>();

    public GraphRegistry() {}

    /** Returns the global shared {@link GraphRegistry} singleton instance. */
    public static GraphRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a graph under the specified string identifier.
     *
     * @param id    unique identifier string
     * @param graph the graph to register
     * @return the previous graph registered under this ID, if any
     */
    public Graph register(String id, Graph graph) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(graph, "graph must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return graphs.put(id, graph);
    }

    /**
     * Registers a graph under a Minecraft {@link Identifier}.
     */
    public Graph register(Identifier id, Graph graph) {
        Objects.requireNonNull(id, "id must not be null");
        return register(id.toString(), graph);
    }

    /**
     * Retrieves the graph registered under the given string identifier.
     */
    public Graph get(String id) {
        if (id == null) return null;
        return graphs.get(id);
    }

    /**
     * Retrieves the graph registered under the given Minecraft {@link Identifier}.
     */
    public Graph get(Identifier id) {
        if (id == null) return null;
        return get(id.toString());
    }

    /**
     * Returns {@code true} if a graph is registered under the given identifier.
     */
    public boolean has(String id) {
        if (id == null) return false;
        return graphs.containsKey(id);
    }

    /**
     * Returns {@code true} if a graph is registered under the given identifier.
     */
    public boolean has(Identifier id) {
        if (id == null) return false;
        return has(id.toString());
    }

    /**
     * Unregisters the graph under the given identifier.
     */
    public Graph unregister(String id) {
        if (id == null) return null;
        return graphs.remove(id);
    }

    /**
     * Unregisters the graph under the given identifier.
     */
    public Graph unregister(Identifier id) {
        if (id == null) return null;
        return unregister(id.toString());
    }

    /** Returns an unmodifiable collection of all currently registered graphs. */
    public Collection<Graph> allGraphs() {
        return Collections.unmodifiableCollection(graphs.values());
    }

    /** Returns an unmodifiable set of all registered graph identifiers. */
    public Set<String> allIds() {
        return Collections.unmodifiableSet(graphs.keySet());
    }

    /** Returns the total number of registered graphs. */
    public int size() {
        return graphs.size();
    }

    /** Clears all registered graphs. */
    public void clear() {
        graphs.clear();
    }
}
