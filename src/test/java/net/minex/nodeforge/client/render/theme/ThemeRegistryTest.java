package net.minex.nodeforge.client.render.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThemeRegistry")
class ThemeRegistryTest {

    @Test
    @DisplayName("initializes with all standard built-in themes")
    void builtinsPresent() {
        ThemeRegistry registry = ThemeRegistry.getInstance();
        assertTrue(registry.themeCount() >= 6);

        assertTrue(registry.contains(ThemeId.DARK));
        assertTrue(registry.contains(ThemeId.LIGHT));
        assertTrue(registry.contains(ThemeId.MIDNIGHT));
        assertTrue(registry.contains(ThemeId.CYBERPUNK));
        assertTrue(registry.contains(ThemeId.HIGH_CONTRAST));
        assertTrue(registry.contains(ThemeId.MINECRAFT_DARK));

        assertSame(NodeTheme.DARK, registry.get(ThemeId.DARK));
        assertSame(NodeTheme.LIGHT, registry.get(ThemeId.LIGHT));
        assertSame(NodeTheme.MIDNIGHT, registry.get(ThemeId.MIDNIGHT));
        assertSame(NodeTheme.CYBERPUNK, registry.get(ThemeId.CYBERPUNK));
        assertSame(NodeTheme.HIGH_CONTRAST, registry.get(ThemeId.HIGH_CONTRAST));
        assertSame(NodeTheme.MINECRAFT_DARK, registry.get(ThemeId.MINECRAFT_DARK));
    }

    @Test
    @DisplayName("registers custom theme and allows lookup")
    void registerCustomTheme() {
        ThemeRegistry registry = new ThemeRegistry();
        ThemeId customId = ThemeId.of("mod", "solarized");
        NodeTheme customTheme = NodeTheme.builder().backgroundColor(0xFF002B36).build();

        assertFalse(registry.contains(customId));
        registry.register(customId, customTheme);
        assertTrue(registry.contains(customId));
        assertSame(customTheme, registry.get(customId));
    }

    @Test
    @DisplayName("cycles themes in registered order")
    void cycleThemes() {
        ThemeRegistry registry = new ThemeRegistry();
        List<ThemeId> ids = registry.registeredIds();
        assertFalse(ids.isEmpty());

        ThemeId current = ids.get(0);
        for (int i = 1; i < ids.size(); i++) {
            current = registry.cycleNext(current);
            assertEquals(ids.get(i), current);
        }

        // Cycling past last returns first
        ThemeId wrap = registry.cycleNext(current);
        assertEquals(ids.get(0), wrap);
    }

    @Test
    @DisplayName("getOrDefault returns fallback on unknown theme")
    void getOrDefault() {
        ThemeRegistry registry = new ThemeRegistry();
        ThemeId unknown = ThemeId.of("test", "unknown");
        assertSame(NodeTheme.DARK, registry.getOrDefault(unknown, NodeTheme.DARK));
    }

    @Test
    @DisplayName("rejects null registration parameters")
    void rejectsNull() {
        ThemeRegistry registry = new ThemeRegistry();
        assertThrows(NullPointerException.class, () -> registry.register(null, NodeTheme.DARK));
        assertThrows(NullPointerException.class, () -> registry.register(ThemeId.DARK, null));
    }
}
