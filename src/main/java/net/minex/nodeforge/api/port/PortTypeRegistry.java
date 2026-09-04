package net.minex.nodeforge.api.port;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for registering and looking up {@link PortType} definitions.
 *
 * <p>NodeForge maintains a global singleton instance accessible via {@link #getInstance()},
 * pre-populated with {@link BuiltinPortTypes}. Consumers and custom mods can register
 * their own port types here during initialization.
 */
public class PortTypeRegistry {

    private static final PortTypeRegistry INSTANCE = new PortTypeRegistry();

    static {
        BuiltinPortTypes.registerAll(INSTANCE);
    }

    private final Map<PortTypeId, PortType<?>> types = new ConcurrentHashMap<>();

    /**
     * Creates an empty port type registry without pre-registered types.
     */
    public PortTypeRegistry() {
    }

    /**
     * Returns the global default port type registry instance.
     *
     * @return the singleton registry instance
     */
    public static PortTypeRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a new port type.
     *
     * @param type the port type to register, must not be {@code null}
     * @param <T>  the payload type
     * @throws NullPointerException     if {@code type} is {@code null}
     * @throws IllegalArgumentException if a different port type is already registered under the same ID
     */
    public <T> void register(PortType<T> type) {
        Objects.requireNonNull(type, "PortType must not be null");
        PortType<?> existing = types.putIfAbsent(type.id(), type);
        if (existing != null && !existing.equals(type)) {
            throw new IllegalArgumentException(
                    "PortType already registered for ID '" + type.id().value() + "': " + existing);
        }
    }

    /**
     * Returns the port type registered with the given ID, or {@code null} if not found.
     *
     * @param id the port type ID
     * @return the registered port type, or {@code null}
     */
    public PortType<?> get(PortTypeId id) {
        if (id == null) return null;
        return types.get(id);
    }

    /**
     * Returns the port type registered with the given ID string, or {@code null} if not found.
     *
     * @param idString the port type ID string
     * @return the registered port type, or {@code null}
     */
    public PortType<?> get(String idString) {
        if (idString == null || idString.isBlank()) return null;
        return types.get(PortTypeId.of(idString));
    }

    /**
     * Returns {@code true} if a port type is registered with the given ID.
     *
     * @param id the port type ID
     * @return {@code true} if registered
     */
    public boolean has(PortTypeId id) {
        if (id == null) return false;
        return types.containsKey(id);
    }

    /**
     * Returns {@code true} if a port type is registered with the given ID string.
     *
     * @param idString the port type ID string
     * @return {@code true} if registered
     */
    public boolean has(String idString) {
        if (idString == null || idString.isBlank()) return false;
        return types.containsKey(PortTypeId.of(idString));
    }

    /**
     * Returns an unmodifiable collection of all currently registered port types.
     *
     * @return all registered port types
     */
    public Collection<PortType<?>> allTypes() {
        return Collections.unmodifiableCollection(types.values());
    }

    /**
     * Unregisters a port type by its ID.
     *
     * @param id the port type ID to remove
     * @return {@code true} if a type was removed
     */
    public boolean unregister(PortTypeId id) {
        if (id == null) return false;
        return types.remove(id) != null;
    }

    /**
     * Returns the number of registered port types.
     *
     * @return count of registered types
     */
    public int size() {
        return types.size();
    }
}
