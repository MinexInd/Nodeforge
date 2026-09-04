package net.minex.nodeforge.client.render.node;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

/**
 * Custom rendering provider interface allowing consumer mods to override the visual representation
 * of specific node types in the editor canvas.
 */
@FunctionalInterface
public interface CustomNodeRenderer {

    /**
     * Renders an individual node card on the canvas.
     *
     * @param context      Minecraft 2D drawing context
     * @param textRenderer font text renderer
     * @param node         the node being rendered
     * @param state        active editor state and hover/selection model
     * @param theme        current visual color theme tokens
     * @param isSelected   {@code true} if this node is currently selected
     * @param isHovered    {@code true} if the cursor is currently hovering over this node
     */
    void renderNode(
            DrawContext context,
            TextRenderer textRenderer,
            Node node,
            EditorState state,
            NodeTheme theme,
            boolean isSelected,
            boolean isHovered
    );
}
