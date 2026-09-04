package net.minex.nodeforge.client.render.theme;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing editor {@link NodeTheme} configurations.
 *
 * <p>Allows NodeForge extensions and third-party mods to register custom themes,
 * query available themes, and cycle through active visual profiles.
 */
public class ThemeRegistry {

    private static final ThemeRegistry INSTANCE = new ThemeRegistry();

    private final Map<ThemeId, NodeTheme> themes = new LinkedHashMap<>();
    private final List<ThemeId> order = new ArrayList<>();

    public ThemeRegistry() {
        registerBuiltins();
    }

    /** Returns the global singleton theme registry. */
    public static ThemeRegistry getInstance() {
        return INSTANCE;
    }

    private synchronized void registerBuiltins() {
        register(ThemeId.DARK, NodeTheme.DARK);
        register(ThemeId.LIGHT, NodeTheme.LIGHT);
        register(ThemeId.MIDNIGHT, NodeTheme.MIDNIGHT);
        register(ThemeId.CYBERPUNK, NodeTheme.CYBERPUNK);
        register(ThemeId.HIGH_CONTRAST, NodeTheme.HIGH_CONTRAST);
        register(ThemeId.MINECRAFT_DARK, NodeTheme.MINECRAFT_DARK);
    }

    /**
     * Registers a new theme under the specified identifier.
     *
     * @param id    the unique theme identifier, must not be null
     * @param theme the theme configuration, must not be null
     * @throws NullPointerException if {@code id} or {@code theme} is null
     */
    public synchronized void register(ThemeId id, NodeTheme theme) {
        Objects.requireNonNull(id, "ThemeId must not be null");
        Objects.requireNonNull(theme, "NodeTheme must not be null");
        if (!themes.containsKey(id)) {
            order.add(id);
        }
        themes.put(id, theme);
    }

    /** Unregisters a theme by identifier. */
    public synchronized void unregister(ThemeId id) {
        if (id != null) {
            themes.remove(id);
            order.remove(id);
        }
    }

    /**
     * Retrieves the theme registered with the given identifier, or {@code null} if not found.
     */
    public synchronized NodeTheme get(ThemeId id) {
        if (id == null) return null;
        return themes.get(id);
    }

    /**
     * Retrieves the theme registered with the given identifier, or a fallback theme if absent.
     */
    public synchronized NodeTheme getOrDefault(ThemeId id, NodeTheme fallback) {
        NodeTheme theme = get(id);
        return theme != null ? theme : fallback;
    }

    /**
     * Returns {@code true} if a theme is registered under the given identifier.
     */
    public synchronized boolean contains(ThemeId id) {
        return id != null && themes.containsKey(id);
    }

    /**
     * Returns an unmodifiable snapshot of all registered themes in registration order.
     */
    public synchronized Map<ThemeId, NodeTheme> allThemes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(themes));
    }

    /**
     * Returns the list of registered theme identifiers in order.
     */
    public synchronized List<ThemeId> registeredIds() {
        return Collections.unmodifiableList(new ArrayList<>(order));
    }

    /**
     * Cycles to the next registered theme after {@code current}.
     *
     * @param current the currently active theme identifier
     * @return the next theme identifier in order
     */
    public synchronized ThemeId cycleNext(ThemeId current) {
        if (order.isEmpty()) return ThemeId.DARK;
        int idx = order.indexOf(current);
        if (idx == -1 || idx + 1 >= order.size()) {
            return order.get(0);
        }
        return order.get(idx + 1);
    }

    /**
     * Returns the total count of registered themes.
     */
    public synchronized int themeCount() {
        return themes.size();
    }
}
