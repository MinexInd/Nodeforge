package net.minex.nodeforge.client.editor.grid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridConfigTest {

    @Test
    @DisplayName("GridStyle cycles sequentially through LINES, DOTS, CROSSES")
    void testGridStyleCycling() {
        GridStyle style = GridStyle.LINES;
        assertEquals(GridStyle.DOTS, style.next());
        assertEquals(GridStyle.CROSSES, style.next().next());
        assertEquals(GridStyle.LINES, style.next().next().next());
    }

    @Test
    @DisplayName("GridConfig toggles snapping and validates parameters")
    void testGridConfig() {
        GridConfig config = new GridConfig();
        assertEquals(GridStyle.LINES, config.getStyle());
        assertEquals(16.0, config.getSize());
        assertTrue(config.isSnapEnabled());

        assertFalse(config.toggleSnap());
        assertFalse(config.isSnapEnabled());
        assertTrue(config.toggleSnap());
        assertTrue(config.isSnapEnabled());

        config.cycleStyle();
        assertEquals(GridStyle.DOTS, config.getStyle());

        config.setSize(32.0);
        assertEquals(32.0, config.getSize());

        config.setSize(-5.0); // should fallback to default
        assertEquals(GridConfig.DEFAULT_SIZE, config.getSize());
    }

    @Test
    @DisplayName("GridConfig notifies onChange listener on mutations")
    void testGridConfigOnChange() {
        GridConfig config = new GridConfig();
        java.util.concurrent.atomic.AtomicInteger changeCount = new java.util.concurrent.atomic.AtomicInteger(0);
        config.setOnChange(cfg -> changeCount.incrementAndGet());

        config.setSize(24.0);
        assertEquals(1, changeCount.get());

        config.toggleSnap();
        assertEquals(2, changeCount.get());

        config.cycleStyle();
        assertEquals(3, changeCount.get());

        config.setMajorInterval(10.0);
        assertEquals(4, changeCount.get());
    }
}
