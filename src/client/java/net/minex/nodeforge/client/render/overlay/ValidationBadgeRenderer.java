package net.minex.nodeforge.client.render.overlay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.core.validation.ValidationSeverity;

import java.util.Objects;

/**
 * Renders status and validation badge overlays on node card headers.
 */
public final class ValidationBadgeRenderer {

    public static final int DEFAULT_BADGE_SIZE = 12;

    private ValidationBadgeRenderer() {}

    /**
     * Renders a validation badge based on severity level.
     *
     * @param context      drawing context
     * @param textRenderer font text renderer
     * @param severity     validation severity (ERROR, WARNING, INFO)
     * @param x            badge top-left X coordinate
     * @param y            badge top-left Y coordinate
     * @param size         badge dimensions in pixels
     */
    public static void renderBadge(
            DrawContext context,
            TextRenderer textRenderer,
            ValidationSeverity severity,
            int x,
            int y,
            int size
    ) {
        Objects.requireNonNull(severity, "severity must not be null");

        int bg;
        String symbol;
        int fg = 0xFFFFFFFF;

        switch (severity) {
            case ERROR -> {
                bg = 0xFFD32F2F; // Red
                symbol = "!";
            }
            case WARNING -> {
                bg = 0xFFF57C00; // Orange/Yellow
                symbol = "▲";
                fg = 0xFF181A20;
            }
            case INFO -> {
                bg = 0xFF1976D2; // Blue
                symbol = "i";
            }
            default -> {
                bg = 0xFF388E3C; // Green
                symbol = "✓";
            }
        }

        // Circular/Square badge background
        context.fill(x, y, x + size, y + size, bg);
        context.drawStrokedRectangle(x, y, size, size, 0xFFFFFFFF);

        // Center symbol text
        int textWidth = textRenderer.getWidth(symbol);
        int textX = x + (size - textWidth) / 2;
        int textY = y + (size - textRenderer.fontHeight) / 2 + 1;
        context.drawText(textRenderer, symbol, textX, textY, fg, false);
    }

    /**
     * Returns {@code true} if the mouse cursor is hovering over the validation badge.
     */
    public static boolean isHovered(int mouseX, int mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
    }
}
