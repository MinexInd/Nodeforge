package net.minex.nodeforge.client.render.theme;

import java.util.Objects;

/**
 * Strongly-typed identifier for a {@link NodeTheme}.
 *
 * <p>Identifies themes registered in {@link ThemeRegistry}.
 */
public record ThemeId(String value) {

    public static final ThemeId DARK = ThemeId.of("nodeforge", "dark");
    public static final ThemeId LIGHT = ThemeId.of("nodeforge", "light");
    public static final ThemeId MIDNIGHT = ThemeId.of("nodeforge", "midnight");
    public static final ThemeId CYBERPUNK = ThemeId.of("nodeforge", "cyberpunk");
    public static final ThemeId HIGH_CONTRAST = ThemeId.of("nodeforge", "high_contrast");
    public static final ThemeId MINECRAFT_DARK = ThemeId.of("nodeforge", "minecraft_dark");

    public ThemeId {
        Objects.requireNonNull(value, "ThemeId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ThemeId value must not be blank");
        }
    }

    /**
     * Creates a ThemeId from a raw string value.
     */
    public static ThemeId of(String value) {
        return new ThemeId(value);
    }

    /**
     * Creates a ThemeId from a namespace and path pair.
     */
    public static ThemeId of(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(path, "path must not be null");
        return new ThemeId(namespace + ":" + path);
    }

    @Override
    public String toString() {
        return "ThemeId[" + value + "]";
    }
}
