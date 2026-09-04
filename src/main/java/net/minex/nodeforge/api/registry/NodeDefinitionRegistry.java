package net.minex.nodeforge.api.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe registry for managing and looking up {@link NodeDefinition} instances.
 *
 * <p>NodeForge maintains a global singleton registry accessible via {@link #getInstance()},
 * and custom isolated registries can be instantiated for specific graphs or sandboxes.
 */
public class NodeDefinitionRegistry {

    private static final NodeDefinitionRegistry INSTANCE = new NodeDefinitionRegistry();

    private final Map<NodeTypeId, NodeDefinition> definitions = new ConcurrentHashMap<>();

    /** Creates a new empty node definition registry. */
    public NodeDefinitionRegistry() {
    }

    /**
     * Returns the global default node definition registry.
     *
     * @return the singleton registry instance
     */
    public static NodeDefinitionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a node definition into this registry.
     *
     * @param definition the node definition to register
     * @throws NullPointerException     if {@code definition} is {@code null}
     * @throws IllegalArgumentException if a different definition is already registered under the same ID
     */
    public void register(NodeDefinition definition) {
        Objects.requireNonNull(definition, "NodeDefinition must not be null");
        NodeDefinition existing = definitions.putIfAbsent(definition.id(), definition);
        if (existing != null && !existing.equals(definition)) {
            throw new IllegalArgumentException(
                    "Different NodeDefinition already registered for ID '" + definition.id().value() + "': " + existing);
        }
    }

    /**
     * Returns the node definition registered with the given ID, or {@code null} if not found.
     *
     * @param id the node type ID
     * @return the registered definition, or {@code null}
     */
    public NodeDefinition get(NodeTypeId id) {
        if (id == null) return null;
        return definitions.get(id);
    }

    /**
     * Returns the node definition registered with the given ID string, or {@code null} if not found.
     *
     * @param idString the node type ID string
     * @return the registered definition, or {@code null}
     */
    public NodeDefinition get(String idString) {
        if (idString == null || idString.isBlank()) return null;
        return definitions.get(NodeTypeId.of(idString));
    }

    /**
     * Returns {@code true} if a node definition is registered with the given ID.
     *
     * @param id the node type ID
     * @return {@code true} if registered
     */
    public boolean has(NodeTypeId id) {
        if (id == null) return false;
        return definitions.containsKey(id);
    }

    /**
     * Returns {@code true} if a node definition is registered with the given ID string.
     *
     * @param idString the node type ID string
     * @return {@code true} if registered
     */
    public boolean has(String idString) {
        if (idString == null || idString.isBlank()) return false;
        return definitions.containsKey(NodeTypeId.of(idString));
    }

    /**
     * Returns an unmodifiable collection of all currently registered node definitions.
     *
     * @return all registered definitions
     */
    public Collection<NodeDefinition> allDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    /**
     * Returns all registered node definitions belonging to the specified category.
     *
     * @param category the category filter
     * @return list of matching definitions
     */
    public List<NodeDefinition> byCategory(NodeCategory category) {
        if (category == null) return Collections.emptyList();
        return definitions.values().stream()
                .filter(def -> category.equals(def.category()))
                .sorted(Comparator.comparing(NodeDefinition::displayName))
                .collect(Collectors.toList());
    }

    /**
     * Unregisters a node definition by its ID.
     *
     * @param id the node type ID
     * @return {@code true} if a definition was removed
     */
    public boolean unregister(NodeTypeId id) {
        if (id == null) return false;
        return definitions.remove(id) != null;
    }

    /**
     * Returns the number of registered node definitions.
     *
     * @return count of definitions
     */
    public int size() {
        return definitions.size();
    }

    /**
     * Clears all registered definitions.
     */
    public void clear() {
        definitions.clear();
    }
}
