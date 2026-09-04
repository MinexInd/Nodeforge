package net.minex.nodeforge.client.render.layer;

/**
 * Execution phases during graph canvas rendering where custom {@link CanvasLayer} instances can draw.
 */
public enum CanvasLayerPhase {

    /**
     * Rendered in world coordinates before the background grid (deep world background).
     * Scales and pans with camera zoom and translation. For a fixed screen-space background,
     * use {@link #SCREEN_OVERLAY}.
     */
    PRE_GRID,

    /** Rendered in world coordinates after the grid, but beneath comment boxes and connection cables. */
    POST_GRID,

    /** Rendered in world coordinates after connection cables, but beneath node cards. */
    POST_CABLES,

    /** Rendered in world coordinates after node cards and VFX (scales and pans with camera zoom). */
    POST_NODES,

    /**
     * Rendered as a screen-space overlay in viewport pixel coordinates after world-space transforms are popped.
     */
    SCREEN_OVERLAY
}
