package net.minex.nodeforge.client.render.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeTheme & Palettes")
class NodeThemeTest {

    @Test
    @DisplayName("built-in themes have valid non-zero ARGB colors")
    void builtInThemes() {
        assertNotNull(NodeTheme.DARK);
        assertNotNull(NodeTheme.LIGHT);
        assertNotNull(NodeTheme.HIGH_CONTRAST);
        assertNotNull(NodeTheme.MINECRAFT_DARK);

        assertNotEquals(0, NodeTheme.DARK.backgroundColor());
        assertNotEquals(0, NodeTheme.DARK.nodeBackgroundColor());
        assertNotEquals(0, NodeTheme.DARK.nodeSelectedBorderColor());
        assertNotEquals(0, NodeTheme.DARK.cableDefaultColor());
    }

    @Test
    @DisplayName("builder customizes individual color tokens")
    void themeBuilder() {
        NodeTheme custom = NodeTheme.builder()
                .backgroundColor(0xFF001122)
                .nodeSelectedBorderColor(0xFF00FF00)
                .build();

        assertEquals(0xFF001122, custom.backgroundColor());
        assertEquals(0xFF00FF00, custom.nodeSelectedBorderColor());
        assertEquals(NodeTheme.DARK.nodeBackgroundColor(), custom.nodeBackgroundColor());
    }
}
