package net.minex.nodeforge.demo.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.layer.CanvasLayer;
import net.minex.nodeforge.client.render.layer.CanvasLayerPhase;
import net.minex.nodeforge.client.render.theme.NodeTheme;

/**
 * Reference implementation of a custom {@link CanvasLayer}.
 *
 * <p>Renders a watermark overlay indicating demo mode in screen coordinates.
 */
public class WatermarkCanvasLayer implements CanvasLayer {

    private boolean enabled = true;

    @Override
    public String id() {
        return "WatermarkCanvasLayer";
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public boolean shouldRender(CanvasLayerPhase phase) {
        return enabled && phase == CanvasLayerPhase.SCREEN_OVERLAY;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                       NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
        if (!enabled || context == null || textRenderer == null) return;

        String label = "NODEFORGE DEMO EXTENSION";
        int width = textRenderer.getWidth(label);
        int x = screenW - width - 12;
        int y = 8;

        // Subtle semi-transparent tag
        context.fill(x - 4, y - 2, x + width + 4, y + 11, 0x55000000);
        context.drawText(textRenderer, label, x, y, 0x8800E5FF, false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
