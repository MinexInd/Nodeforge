package net.minex.nodeforge.client.render.layer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

/**
 * Extension point for rendering custom visual content onto the NodeForge canvas.
 */
public interface CanvasLayer {

    /** Unique identifier for this canvas layer (defaults to simple class name or anonymous fallback). */
    default String id() {
        String name = getClass().getSimpleName();
        return name.isEmpty() ? "anonymous@" + Integer.toHexString(System.identityHashCode(this)) : name;
    }

    /** Display sort order (lower values render first). */
    default int order() {
        return 100;
    }

    /** Returns {@code true} if this layer should render during the specified phase. */
    boolean shouldRender(CanvasLayerPhase phase);

    /**
     * Renders custom content during the specified phase.
     *
     * <p>For {@link CanvasLayerPhase#PRE_GRID}, {@link CanvasLayerPhase#POST_GRID},
     * {@link CanvasLayerPhase#POST_CABLES}, and {@link CanvasLayerPhase#POST_NODES},
     * coordinates are in world space (camera zoom and pan already applied).
     *
     * <p>For {@link CanvasLayerPhase#SCREEN_OVERLAY}, coordinates are in screen pixels.
     *
     * @param context      the Minecraft draw context
     * @param textRenderer the Minecraft text renderer
     * @param state        the active editor state
     * @param theme        the active color theme
     * @param phase        the current render phase
     * @param screenW      total viewport width in pixels
     * @param screenH      total viewport height in pixels
     */
    void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH);
}
