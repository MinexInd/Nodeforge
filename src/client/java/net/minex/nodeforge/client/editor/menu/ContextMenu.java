package net.minex.nodeforge.client.editor.menu;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Floating popup context menu for right-click interaction on the canvas, nodes, and connections.
 */
public class ContextMenu {

    public static final int MENU_WIDTH = 160;
    public static final int ITEM_HEIGHT = 18;
    public static final int SEPARATOR_HEIGHT = 5;
    public static final int PADDING = 4;

    private boolean open = false;
    private double screenX = 0.0;
    private double screenY = 0.0;
    private List<MenuItem> items = Collections.emptyList();

    public void open(double screenX, double screenY, List<MenuItem> items) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.items = items != null ? List.copyOf(items) : Collections.emptyList();
        this.open = !this.items.isEmpty();
    }

    public void close() {
        this.open = false;
        this.items = Collections.emptyList();
    }

    public boolean isOpen() {
        return open;
    }

    public double screenX() {
        return screenX;
    }

    public double screenY() {
        return screenY;
    }

    public List<MenuItem> items() {
        return items;
    }

    /** Returns total height required to render all items. */
    public int calculateTotalHeight() {
        int h = PADDING * 2;
        for (MenuItem item : items) {
            h += item.isSeparator() ? SEPARATOR_HEIGHT : ITEM_HEIGHT;
        }
        return h;
    }

    /** Renders the context menu card and items. */
    public void render(DrawContext context, TextRenderer textRenderer, NodeTheme theme,
                       int mouseX, int mouseY, int screenW, int screenH) {
        if (!open || items.isEmpty()) return;

        int totalH = calculateTotalHeight();

        // Clamp inside screen boundaries
        int x = (int) Math.round(screenX);
        int y = (int) Math.round(screenY);
        if (x + MENU_WIDTH > screenW) {
            x = screenW - MENU_WIDTH - 4;
        }
        if (y + totalH > screenH) {
            y = screenH - totalH - 4;
        }

        // Draw background box and border
        context.fill(x, y, x + MENU_WIDTH, y + totalH, theme.menuBackgroundColor());
        context.drawStrokedRectangle(x, y, MENU_WIDTH, totalH, theme.menuBorderColor());

        int currentY = y + PADDING;

        for (MenuItem item : items) {
            if (item.isSeparator()) {
                int lineY = currentY + SEPARATOR_HEIGHT / 2;
                context.fill(x + 4, lineY, x + MENU_WIDTH - 4, lineY + 1, theme.menuBorderColor());
                currentY += SEPARATOR_HEIGHT;
                continue;
            }

            boolean hovered = mouseX >= x && mouseX <= x + MENU_WIDTH
                    && mouseY >= currentY && mouseY < currentY + ITEM_HEIGHT;
            boolean enabled = item.isEnabled();

            if (hovered && enabled) {
                context.fill(x + 2, currentY, x + MENU_WIDTH - 2, currentY + ITEM_HEIGHT, theme.menuHoverColor());
            }

            // Draw label
            int textY = currentY + (ITEM_HEIGHT - textRenderer.fontHeight) / 2 + 1;
            int textColor = enabled ? (hovered ? theme.textColor() : theme.menuTextColor()) : 0xFF666666;
            context.drawText(textRenderer, item.label(), x + 8, textY, textColor, false);

            // Draw shortcut hint right-aligned
            if (item.shortcut() != null && !item.shortcut().isBlank()) {
                int scWidth = textRenderer.getWidth(item.shortcut());
                int scColor = enabled ? theme.textSecondaryColor() : 0xFF555555;
                context.drawText(textRenderer, item.shortcut(), x + MENU_WIDTH - scWidth - 8, textY, scColor, false);
            }

            currentY += ITEM_HEIGHT;
        }
    }

    /**
     * Handles mouse click on the menu.
     *
     * @param click the mouse click event
     * @param screenW screen width for bounds checking
     * @param screenH screen height for bounds checking
     * @return {@code true} if click was handled or dismissed
     */
    public boolean mouseClicked(Click click, int screenW, int screenH) {
        if (!open) return false;

        double mx = click.x();
        double my = click.y();

        int totalH = calculateTotalHeight();
        int x = (int) Math.round(screenX);
        int y = (int) Math.round(screenY);
        if (x + MENU_WIDTH > screenW) x = screenW - MENU_WIDTH - 4;
        if (y + totalH > screenH) y = screenH - totalH - 4;

        if (mx < x || mx > x + MENU_WIDTH || my < y || my > y + totalH) {
            close();
            return false;
        }

        int currentY = y + PADDING;
        for (MenuItem item : items) {
            if (item.isSeparator()) {
                currentY += SEPARATOR_HEIGHT;
                continue;
            }

            if (mx >= x && mx <= x + MENU_WIDTH && my >= currentY && my < currentY + ITEM_HEIGHT) {
                if (item.isEnabled() && item.action() != null) {
                    close();
                    item.action().run();
                    return true;
                }
            }
            currentY += ITEM_HEIGHT;
        }

        close();
        return true;
    }
}
