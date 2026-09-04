package net.minex.nodeforge.client.render.layer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CanvasLayerRegistry")
class CanvasLayerRegistryTest {

    private CanvasLayerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CanvasLayerRegistry();
    }

    @Test
    @DisplayName("sorts registered layers according to order")
    void sortsByOrder() {
        List<String> rendered = new ArrayList<>();

        CanvasLayer layer1 = new CanvasLayer() {
            @Override
            public int order() { return 200; }
            @Override
            public boolean shouldRender(CanvasLayerPhase phase) { return true; }
            @Override
            public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                               NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
                rendered.add("layer_200");
            }
        };

        CanvasLayer layer2 = new CanvasLayer() {
            @Override
            public int order() { return 50; }
            @Override
            public boolean shouldRender(CanvasLayerPhase phase) { return true; }
            @Override
            public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                               NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
                rendered.add("layer_50");
            }
        };

        registry.register(layer1);
        registry.register(layer2);

        // Verify layer list is properly sorted by order ascending: layer2 (50) before layer1 (200)
        List<CanvasLayer> layers = registry.getLayers();
        assertEquals(2, layers.size());
        assertSame(layer2, layers.get(0), "Layer with order 50 should come first");
        assertSame(layer1, layers.get(1), "Layer with order 200 should come second");

        // Verify P13-1 anonymous ID generation
        assertNotNull(layer1.id());
        assertFalse(layer1.id().isEmpty());
        assertTrue(layer1.id().startsWith("anonymous@"));
    }

    @Test
    @DisplayName("filters layers by phase")
    void phaseFiltering() {
        List<CanvasLayerPhase> phasesSeen = new ArrayList<>();

        CanvasLayer layer = new CanvasLayer() {
            @Override
            public boolean shouldRender(CanvasLayerPhase phase) {
                return phase == CanvasLayerPhase.POST_NODES;
            }
            @Override
            public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                               NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
                phasesSeen.add(phase);
            }
        };

        registry.register(layer);
        assertEquals(1, registry.layerCount());
        assertTrue(layer.shouldRender(CanvasLayerPhase.POST_NODES));
        assertFalse(layer.shouldRender(CanvasLayerPhase.PRE_GRID));

        registry.unregister(layer);
        assertEquals(0, registry.layerCount());
    }
}
