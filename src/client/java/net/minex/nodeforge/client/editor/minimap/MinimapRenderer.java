package net.minex.nodeforge.client.editor.minimap;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.CommentBox;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Locale;
import java.util.Objects;

/**
 * Interactive bird's-eye minimap viewport for rapid canvas navigation and overview.
 */
public class MinimapRenderer {

    public static final int DEFAULT_WIDTH = 140;
    public static final int DEFAULT_HEIGHT = 90;
    public static final int MARGIN = 8;

    private boolean visible = true;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private boolean dragging = false;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean toggleVisible() {
        this.visible = !this.visible;
        return this.visible;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = Math.max(80, width);
        this.height = Math.max(60, height);
    }

    /** Returns the top-left screen X coordinate of the minimap. */
    public int getScreenX(int screenW) {
        return screenW - width - MARGIN;
    }

    /** Returns the top-left screen Y coordinate of the minimap. */
    public int getScreenY(int screenH) {
        return screenH - height - 26; // Above status bar
    }

    /** Renders the minimap overview overlay. */
    public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                       NodeTheme theme, int screenW, int screenH, int mouseX, int mouseY) {
        if (!visible) return;

        int mapX = getScreenX(screenW);
        int mapY = getScreenY(screenH);

        // 1. Draw minimap frame
        context.fill(mapX, mapY, mapX + width, mapY + height, 0xEE14161C);
        boolean hovered = isHovered(mouseX, mouseY, screenW, screenH);
        context.drawStrokedRectangle(mapX, mapY, width, height, hovered ? theme.nodeHoverBorderColor() : 0xFF3A3E48);

        // Label
        if (textRenderer != null) {
            int nodeCount = state.graph().nodeCount();
            int commentCount = state.graph().commentBoxCount();
            String label = commentCount > 0
                    ? String.format(Locale.ROOT, "MAP (%dN, %dC)", nodeCount, commentCount)
                    : "MAP (" + nodeCount + "N)";
            context.drawText(textRenderer, label, mapX + 4, mapY + 3, 0xFF666677, false);
        }

        // 2. Compute combined world bounds (nodes + comments + camera visible viewport)
        Camera camera = state.camera();
        BoundingBox viewBounds = camera.getVisibleWorldBounds(screenW, screenH);

        double minX = viewBounds.minX();
        double minY = viewBounds.minY();
        double maxX = viewBounds.maxX();
        double maxY = viewBounds.maxY();

        for (Node node : state.graph().getNodes()) {
            minX = Math.min(minX, node.position().x());
            minY = Math.min(minY, node.position().y());
            maxX = Math.max(maxX, node.position().x() + node.size().width());
            maxY = Math.max(maxY, node.position().y() + node.size().height());
        }

        for (CommentBox box : state.graph().getCommentBoxes()) {
            minX = Math.min(minX, box.minX());
            minY = Math.min(minY, box.minY());
            maxX = Math.max(maxX, box.maxX());
            maxY = Math.max(maxY, box.maxY());
        }

        double spanX = Math.max(200.0, maxX - minX);
        double spanY = Math.max(150.0, maxY - minY);

        // Scale factors
        int innerPad = 4;
        int innerW = width - innerPad * 2;
        int innerH = height - innerPad * 2 - 10;
        int innerX = mapX + innerPad;
        int innerY = mapY + innerPad + 10;

        double scale = Math.min(innerW / spanX, innerH / spanY);

        // 2.5. Render miniature comment boxes
        for (CommentBox box : state.graph().getCommentBoxes()) {
            int bx = innerX + (int) Math.round((box.minX() - minX) * scale);
            int by = innerY + (int) Math.round((box.minY() - minY) * scale);
            int bw = Math.max(4, (int) Math.round(box.size().width() * scale));
            int bh = Math.max(4, (int) Math.round(box.size().height() * scale));

            boolean isSelected = state.selection().isCommentBoxSelected(box.id());
            int boxBorder = isSelected ? theme.nodeSelectedBorderColor() : (box.color() | 0xFF000000);
            context.fill(bx, by, bx + bw, by + bh, 0x1A404550);
            context.drawStrokedRectangle(bx, by, bw, bh, boxBorder);
        }

        // 3. Render miniature node rectangles
        for (Node node : state.graph().getNodes()) {
            int nx = innerX + (int) Math.round((node.position().x() - minX) * scale);
            int ny = innerY + (int) Math.round((node.position().y() - minY) * scale);
            int nw = Math.max(2, (int) Math.round(node.size().width() * scale));
            int nh = Math.max(2, (int) Math.round(node.size().height() * scale));

            boolean isSelected = state.selection().isSelected(node.id());
            int nodeColor = isSelected ? theme.nodeSelectedBorderColor() : 0xFF6A7080;
            context.fill(nx, ny, nx + nw, ny + nh, nodeColor);
        }

        // 4. Render viewport frustum camera rectangle
        int vx = innerX + (int) Math.round((viewBounds.minX() - minX) * scale);
        int vy = innerY + (int) Math.round((viewBounds.minY() - minY) * scale);
        int vw = Math.max(4, (int) Math.round(viewBounds.width() * scale));
        int vh = Math.max(4, (int) Math.round(viewBounds.height() * scale));

        context.fill(vx, vy, vx + vw, vy + vh, 0x224A90E2);
        context.drawStrokedRectangle(vx, vy, vw, vh, 0xFF4A90E2);
    }

    /** Returns {@code true} if the screen mouse coordinates are within the minimap. */
    public boolean isHovered(int mouseX, int mouseY, int screenW, int screenH) {
        if (!visible) return false;
        int mapX = getScreenX(screenW);
        int mapY = getScreenY(screenH);
        return mouseX >= mapX && mouseX <= mapX + width
                && mouseY >= mapY && mouseY <= mapY + height;
    }

    /** Handles clicking inside the minimap to pan the camera. */
    public boolean mouseClicked(Click click, EditorState state, int screenW, int screenH) {
        if (!visible || !isHovered((int) click.x(), (int) click.y(), screenW, screenH)) {
            return false;
        }

        this.dragging = true;
        panCameraToMinimapPoint(click.x(), click.y(), state, screenW, screenH);
        return true;
    }

    public boolean mouseDragged(Click click, EditorState state, int screenW, int screenH) {
        if (!visible || !dragging) return false;
        panCameraToMinimapPoint(click.x(), click.y(), state, screenW, screenH);
        return true;
    }

    public void mouseReleased(Click click) {
        this.dragging = false;
    }

    private void panCameraToMinimapPoint(double mx, double my, EditorState state, int screenW, int screenH) {
        int innerPad = 4;
        int innerW = width - innerPad * 2;
        int innerH = height - innerPad * 2 - 10;
        int innerX = getScreenX(screenW) + innerPad;
        int innerY = getScreenY(screenH) + innerPad + 10;

        Camera camera = state.camera();
        BoundingBox viewBounds = camera.getVisibleWorldBounds(screenW, screenH);

        double minX = viewBounds.minX();
        double minY = viewBounds.minY();
        double maxX = viewBounds.maxX();
        double maxY = viewBounds.maxY();

        for (Node node : state.graph().getNodes()) {
            minX = Math.min(minX, node.position().x());
            minY = Math.min(minY, node.position().y());
            maxX = Math.max(maxX, node.position().x() + node.size().width());
            maxY = Math.max(maxY, node.position().y() + node.size().height());
        }

        for (CommentBox box : state.graph().getCommentBoxes()) {
            minX = Math.min(minX, box.minX());
            minY = Math.min(minY, box.minY());
            maxX = Math.max(maxX, box.maxX());
            maxY = Math.max(maxY, box.maxY());
        }

        double spanX = Math.max(200.0, maxX - minX);
        double spanY = Math.max(150.0, maxY - minY);
        double scale = Math.min(innerW / spanX, innerH / spanY);

        double targetWorldX = minX + (mx - innerX) / scale;
        double targetWorldY = minY + (my - innerY) / scale;

        camera.setPan(new Position(targetWorldX, targetWorldY));
    }
}
