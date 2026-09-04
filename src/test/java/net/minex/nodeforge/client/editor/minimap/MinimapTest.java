package net.minex.nodeforge.client.editor.minimap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinimapTest {

    @Test
    @DisplayName("MinimapRenderer toggles visibility and computes screen bounds")
    void testMinimapBasics() {
        MinimapRenderer minimap = new MinimapRenderer();
        assertTrue(minimap.isVisible());

        minimap.toggleVisible();
        assertFalse(minimap.isVisible());

        minimap.setVisible(true);
        assertTrue(minimap.isVisible());

        int screenW = 800;
        int screenH = 600;
        int mapX = minimap.getScreenX(screenW);
        int mapY = minimap.getScreenY(screenH);

        assertTrue(mapX > 0 && mapX < screenW);
        assertTrue(mapY > 0 && mapY < screenH);

        assertTrue(minimap.isHovered(mapX + 10, mapY + 10, screenW, screenH));
        assertFalse(minimap.isHovered(10, 10, screenW, screenH));
    }
}
