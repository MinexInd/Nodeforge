package net.minex.nodeforge.client.render;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.editor.state.InteractionState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Objects;

/**
 * Renders the marquee selection box overlay during box selection gestures.
 */
public class MarqueeRenderer {

    /**
     * Renders the marquee box in screen coordinate space.
     */
    public void render(DrawContext context, EditorState editorState, NodeTheme theme) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(editorState, "editorState must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        if (editorState.interactionState() instanceof InteractionState.BoxSelecting box) {
            int x1 = (int) Math.round(Math.min(box.startScreenX(), box.currentScreenX()));
            int x2 = (int) Math.round(Math.max(box.startScreenX(), box.currentScreenX()));
            int y1 = (int) Math.round(Math.min(box.startScreenY(), box.currentScreenY()));
            int y2 = (int) Math.round(Math.max(box.startScreenY(), box.currentScreenY()));

            // Semi-transparent box fill
            context.fill(x1, y1, x2, y2, theme.selectionBoxFillColor());

            // Outline borders
            context.fill(x1, y1, x2, y1 + 1, theme.selectionBoxBorderColor()); // Top
            context.fill(x1, y2 - 1, x2, y2, theme.selectionBoxBorderColor()); // Bottom
            context.fill(x1, y1, x1 + 1, y2, theme.selectionBoxBorderColor()); // Left
            context.fill(x2 - 1, y1, x2, y2, theme.selectionBoxBorderColor()); // Right
        }
    }
}
