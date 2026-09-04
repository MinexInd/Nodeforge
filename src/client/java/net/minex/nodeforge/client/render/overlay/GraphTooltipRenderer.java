package net.minex.nodeforge.client.render.overlay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Objects;

/**
 * Generic floating tooltip renderer for ports, widgets, and validation badges.
 */
public final class GraphTooltipRenderer {

    private GraphTooltipRenderer() {}

    /**
     * Renders a floating multi-line tooltip card at the cursor position.
     *
     * @param context      drawing context
     * @param textRenderer font text renderer
     * @param lines        tooltip text lines
     * @param mouseX       cursor X
     * @param mouseY       cursor Y
     * @param screenWidth  screen width for clamping
     * @param screenHeight screen height for clamping
     */
    public static void renderTooltip(
            DrawContext context,
            TextRenderer textRenderer,
            List<String> lines,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight
    ) {
        if (lines == null || lines.isEmpty()) return;

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(line));
        }

        int padding = 4;
        int boxWidth = maxWidth + padding * 2;
        int lineHeight = textRenderer.fontHeight + 2;
        int boxHeight = lines.size() * lineHeight + padding * 2;

        int boxX = mouseX + 12;
        int boxY = mouseY - 12;

        // Clamp inside screen bounds
        if (boxX + boxWidth > screenWidth) {
            boxX = mouseX - boxWidth - 4;
        }
        if (boxY + boxHeight > screenHeight) {
            boxY = screenHeight - boxHeight - 4;
        }
        if (boxY < 4) {
            boxY = 4;
        }

        // Draw background box
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0101216);
        context.drawStrokedRectangle(boxX, boxY, boxWidth, boxHeight, 0xFF4A90E2);

        // Render lines
        for (int i = 0; i < lines.size(); i++) {
            int lineY = boxY + padding + i * lineHeight + 1;
            int color = (i == 0) ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawText(textRenderer, lines.get(i), boxX + padding, lineY, color, false);
        }
    }
}
