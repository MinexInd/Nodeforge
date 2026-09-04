package net.minex.nodeforge.client.render.comment;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.CommentBox;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Objects;

/**
 * Renders comment frames and grouping containers on the graph canvas in world coordinates.
 */
public class CommentBoxRenderer {

    public static final double RESIZE_HANDLE_SIZE = 12.0;

    /**
     * Renders a comment box in world space.
     *
     * @param context      Minecraft DrawContext
     * @param textRenderer Minecraft TextRenderer
     * @param box          comment box to render
     * @param theme        active color theme
     * @param isSelected   whether this comment box is selected
     * @param isHovered    whether this comment box is hovered
     */
    public void render(DrawContext context, TextRenderer textRenderer, CommentBox box,
                       NodeTheme theme, boolean isSelected, boolean isHovered) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(box, "box must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        int x = (int) Math.round(box.position().x());
        int y = (int) Math.round(box.position().y());
        int w = (int) Math.round(box.size().width());
        int h = (int) Math.round(box.size().height());
        int headerH = (int) Math.round(CommentBox.HEADER_HEIGHT);

        // 1. Semi-translucent body fill
        int bodyColor = (box.color() != 0) ? box.color() : theme.commentBodyColor();
        context.fill(x, y, x + w, y + h, bodyColor);

        // 2. Header bar fill
        int headerColor = theme.commentHeaderColor();
        context.fill(x, y, x + w, y + headerH, headerColor);

        // 3. Border (highlighted when selected or hovered)
        int borderColor = isSelected ? theme.nodeSelectedBorderColor() :
                (isHovered ? theme.nodeHoverBorderColor() : theme.commentBorderColor());
        context.drawStrokedRectangle(x, y, w, h, borderColor);

        // 4. Header separator line
        context.fill(x, y + headerH, x + w, y + headerH + 1, borderColor);

        // 5. Title text
        if (textRenderer != null && box.title() != null && !box.title().isBlank()) {
            context.drawText(textRenderer, box.title(), x + 8, y + 6, theme.textColor(), false);
        }

        // 6. Resize handle glyph at bottom-right corner
        int handleX = x + w - (int) RESIZE_HANDLE_SIZE;
        int handleY = y + h - (int) RESIZE_HANDLE_SIZE;
        context.fill(handleX + 2, handleY + 8, x + w - 2, y + h - 2, borderColor);
        context.fill(handleX + 6, handleY + 4, x + w - 2, y + h - 2, borderColor);
    }

    /** Returns {@code true} if the position hits the bottom-right resize handle of the comment box. */
    public static boolean isResizeHandleHit(Position pos, CommentBox box) {
        if (pos == null || box == null) return false;
        double handleMinX = box.position().x() + box.size().width() - RESIZE_HANDLE_SIZE;
        double handleMinY = box.position().y() + box.size().height() - RESIZE_HANDLE_SIZE;
        double handleMaxX = box.position().x() + box.size().width();
        double handleMaxY = box.position().y() + box.size().height();

        return pos.x() >= handleMinX && pos.x() <= handleMaxX
                && pos.y() >= handleMinY && pos.y() <= handleMaxY;
    }
}
