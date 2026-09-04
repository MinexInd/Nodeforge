package net.minex.nodeforge.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.api.graph.CommentBox;
import net.minex.nodeforge.client.render.comment.CommentBoxRenderer;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Objects;

/**
 * Master rendering pipeline for the NodeForge graph editor canvas.
 *
 * <p>Handles camera viewport transformations, frustum culling, layer ordering,
 * and screen-space overlay elements.
 */
public class GraphCanvasRenderer {

    private final GridRenderer gridRenderer;
    private final CommentBoxRenderer commentBoxRenderer;
    private final CableRenderer cableRenderer;
    private final NodeRenderer nodeRenderer;
    private final MarqueeRenderer marqueeRenderer;

    public GraphCanvasRenderer(
            GridRenderer gridRenderer,
            CommentBoxRenderer commentBoxRenderer,
            CableRenderer cableRenderer,
            NodeRenderer nodeRenderer,
            MarqueeRenderer marqueeRenderer
    ) {
        this.gridRenderer = Objects.requireNonNull(gridRenderer, "gridRenderer must not be null");
        this.commentBoxRenderer = Objects.requireNonNull(commentBoxRenderer, "commentBoxRenderer must not be null");
        this.cableRenderer = Objects.requireNonNull(cableRenderer, "cableRenderer must not be null");
        this.nodeRenderer = Objects.requireNonNull(nodeRenderer, "nodeRenderer must not be null");
        this.marqueeRenderer = Objects.requireNonNull(marqueeRenderer, "marqueeRenderer must not be null");
    }

    public GraphCanvasRenderer(
            GridRenderer gridRenderer,
            CableRenderer cableRenderer,
            NodeRenderer nodeRenderer,
            MarqueeRenderer marqueeRenderer
    ) {
        this(gridRenderer, new CommentBoxRenderer(), cableRenderer, nodeRenderer, marqueeRenderer);
    }

    public GraphCanvasRenderer() {
        this(new GridRenderer(), new CommentBoxRenderer(), new CableRenderer(), new NodeRenderer(), new MarqueeRenderer());
    }

    public GridRenderer getGridRenderer() {
        return gridRenderer;
    }

    public CommentBoxRenderer getCommentBoxRenderer() {
        return commentBoxRenderer;
    }

    /**
     * Renders the entire graph editor canvas.
     *
     * @param context      the Minecraft DrawContext
     * @param textRenderer the Minecraft TextRenderer (optional)
     * @param editorState  the active editor state
     * @param theme        the visual styling theme
     * @param vw           viewport width in screen pixels
     * @param vh           viewport height in screen pixels
     */
    public void render(DrawContext context, TextRenderer textRenderer, EditorState editorState,
                       NodeTheme theme, double vw, double vh) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(editorState, "editorState must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        // 1. Draw canvas background color
        context.fill(0, 0, (int) Math.round(vw), (int) Math.round(vh), theme.backgroundColor());

        Camera camera = editorState.camera();
        double centerX = vw / 2.0;
        double centerY = vh / 2.0;
        float zoom = (float) camera.zoom();
        Position pan = camera.pan();

        // 2. Begin World-Space Transformations
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) centerX, (float) centerY);
        context.getMatrices().scale(zoom, zoom);
        context.getMatrices().translate((float) -pan.x(), (float) -pan.y());

        int screenW = (int) Math.round(vw);
        int screenH = (int) Math.round(vh);
        net.minex.nodeforge.client.render.layer.CanvasLayerRegistry layerRegistry =
                net.minex.nodeforge.client.render.layer.CanvasLayerRegistry.getInstance();

        // 3. Render Pre-Grid Custom Layers & Background Grid
        layerRegistry.renderLayers(context, textRenderer, editorState, theme,
                net.minex.nodeforge.client.render.layer.CanvasLayerPhase.PRE_GRID, screenW, screenH);
        gridRenderer.render(context, camera, theme, vw, vh);

        // 3.2. Render Post-Grid Custom Layers
        layerRegistry.renderLayers(context, textRenderer, editorState, theme,
                net.minex.nodeforge.client.render.layer.CanvasLayerPhase.POST_GRID, screenW, screenH);

        // 3.5. Render Comment Boxes (behind cables and nodes)
        for (CommentBox box : editorState.graph().getCommentBoxes()) {
            boolean isSelected = editorState.selection().isCommentBoxSelected(box.id());
            boolean isHovered = editorState.hoverState().isCommentBoxHovered(box.id());
            commentBoxRenderer.render(context, textRenderer, box, theme, isSelected, isHovered);
        }

        // 4. Render Connection Cables
        cableRenderer.render(context, editorState, theme);

        // 4.5. Render Post-Cables Custom Layers
        layerRegistry.renderLayers(context, textRenderer, editorState, theme,
                net.minex.nodeforge.client.render.layer.CanvasLayerPhase.POST_CABLES, screenW, screenH);

        // 5. Render Nodes (with Frustum Culling)
        BoundingBox visibleBounds = camera.getVisibleWorldBounds(vw, vh);
        for (Node node : editorState.graph().getNodes()) {
            BoundingBox nodeBox = BoundingBox.fromPositionAndSize(node.position(), node.size());
            if (visibleBounds.intersects(nodeBox)) {
                nodeRenderer.renderNode(context, node, textRenderer, editorState, theme);
            }
        }

        // 5.5. Render World-Space Visual Effects (pulses, impulses, particles)
        net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().render(context, editorState, theme);

        // 5.8. Render Post-Nodes Custom Layers (World-Space)
        layerRegistry.renderLayers(context, textRenderer, editorState, theme,
                net.minex.nodeforge.client.render.layer.CanvasLayerPhase.POST_NODES, screenW, screenH);

        // 6. End World-Space Transformations
        context.getMatrices().popMatrix();

        // 7. Render Screen-Space Overlays (Marquee Selection Box & Screen-Space Custom Layers)
        marqueeRenderer.render(context, editorState, theme);
        layerRegistry.renderLayers(context, textRenderer, editorState, theme,
                net.minex.nodeforge.client.render.layer.CanvasLayerPhase.SCREEN_OVERLAY, screenW, screenH);
    }
}
