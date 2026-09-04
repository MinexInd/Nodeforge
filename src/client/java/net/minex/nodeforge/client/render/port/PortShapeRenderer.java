package net.minex.nodeforge.client.render.port;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.PortDirection;

/**
 * High-performance raster renderer for geometric port sockets.
 */
public final class PortShapeRenderer {

    private PortShapeRenderer() {}

    /**
     * Renders a styled geometric port socket on canvas.
     *
     * @param context         the Minecraft draw context
     * @param shape           the geometric shape
     * @param direction       the direction of the port (INPUT / OUTPUT)
     * @param centerX         center X coordinate in canvas space
     * @param centerY         center Y coordinate in canvas space
     * @param radius          radius size of the socket
     * @param fillColor       primary type color (ARGB)
     * @param borderColor     outer border color (ARGB)
     * @param backgroundColor inner hollow color when unconnected (ARGB)
     * @param isConnected     whether the port has active cable connections
     * @param isHovered       whether the port is currently hovered by cursor
     */
    public static void renderPortSocket(
            DrawContext context, PortShape shape, PortDirection direction,
            int centerX, int centerY, int radius, int fillColor, int borderColor,
            int backgroundColor, boolean isConnected, boolean isHovered
    ) {
        if (context == null || radius <= 0) return;
        if (shape == null) shape = PortShape.CIRCLE;

        switch (shape) {
            case TRIANGLE -> renderTriangle(context, direction, centerX, centerY, radius, fillColor, borderColor, backgroundColor, isConnected, isHovered);
            case SQUARE -> renderSquare(context, centerX, centerY, radius, fillColor, borderColor, backgroundColor, isConnected, isHovered);
            case DIAMOND -> renderDiamond(context, centerX, centerY, radius, fillColor, borderColor, backgroundColor, isConnected, isHovered);
            case CAPSULE -> renderCapsule(context, direction, centerX, centerY, radius, fillColor, borderColor, backgroundColor, isConnected, isHovered);
            case CIRCLE -> renderCircle(context, centerX, centerY, radius, fillColor, borderColor, backgroundColor, isConnected, isHovered);
        }
    }

    private static void renderCircle(
            DrawContext context, int cx, int cy, int r, int fill, int border,
            int bg, boolean connected, boolean hovered
    ) {
        int pad = hovered ? 2 : 1;
        // Outer border
        context.fill(cx - r - pad, cy - r, cx + r + pad, cy + r, border);
        context.fill(cx - r, cy - r - pad, cx + r, cy + r + pad, border);

        // Inner disc
        context.fill(cx - r, cy - r + 1, cx + r, cy + r - 1, fill);
        context.fill(cx - r + 1, cy - r, cx + r - 1, cy + r, fill);

        // Hollow core if disconnected
        if (!connected) {
            int hr = Math.max(1, r - 2);
            context.fill(cx - hr, cy - hr, cx + hr, cy + hr, bg);
        }
    }

    private static void renderSquare(
            DrawContext context, int cx, int cy, int r, int fill, int border,
            int bg, boolean connected, boolean hovered
    ) {
        int pad = hovered ? 2 : 1;
        // Border
        context.fill(cx - r - pad, cy - r - pad, cx + r + pad, cy + r + pad, border);
        // Fill
        context.fill(cx - r, cy - r, cx + r, cy + r, fill);
        // Hollow core if disconnected
        if (!connected) {
            int hr = Math.max(1, r - 2);
            context.fill(cx - hr, cy - hr, cx + hr, cy + hr, bg);
        }
    }

    private static void renderTriangle(
            DrawContext context, PortDirection dir, int cx, int cy, int r, int fill, int border,
            int bg, boolean connected, boolean hovered
    ) {
        int pad = hovered ? 2 : 1;
        // Directional execution arrow: points towards right (+X) for flow
        int startX = cx - r;
        int endX = cx + r + 1;

        // Border pass (slightly expanded)
        for (int dy = -r - pad; dy <= r + pad; dy++) {
            double frac = 1.0 - (double) Math.abs(dy) / (r + pad);
            int width = (int) Math.round(frac * (2 * r + pad * 2));
            int scanX1 = startX - pad;
            int scanX2 = scanX1 + width;
            if (scanX2 > scanX1) {
                context.fill(scanX1, cy + dy, scanX2, cy + dy + 1, border);
            }
        }

        // Inner fill pass
        for (int dy = -r; dy <= r; dy++) {
            double frac = 1.0 - (double) Math.abs(dy) / r;
            int width = (int) Math.round(frac * (2 * r));
            int scanX1 = startX;
            int scanX2 = scanX1 + width;
            if (scanX2 > scanX1) {
                context.fill(scanX1, cy + dy, scanX2, cy + dy + 1, fill);
            }
        }

        // Hollow cutout if not connected
        if (!connected) {
            int hr = Math.max(1, r - 2);
            for (int dy = -hr; dy <= hr; dy++) {
                double frac = 1.0 - (double) Math.abs(dy) / hr;
                int width = (int) Math.round(frac * (2 * hr));
                int scanX1 = startX + 1;
                int scanX2 = scanX1 + width;
                if (scanX2 > scanX1) {
                    context.fill(scanX1, cy + dy, scanX2, cy + dy + 1, bg);
                }
            }
        }
    }

    private static void renderDiamond(
            DrawContext context, int cx, int cy, int r, int fill, int border,
            int bg, boolean connected, boolean hovered
    ) {
        int pad = hovered ? 2 : 1;
        // Border
        for (int dy = -r - pad; dy <= r + pad; dy++) {
            int span = (r + pad) - Math.abs(dy);
            context.fill(cx - span, cy + dy, cx + span + 1, cy + dy + 1, border);
        }
        // Fill
        for (int dy = -r; dy <= r; dy++) {
            int span = r - Math.abs(dy);
            context.fill(cx - span, cy + dy, cx + span + 1, cy + dy + 1, fill);
        }
        // Hollow cutout if not connected
        if (!connected) {
            int hr = Math.max(1, r - 2);
            for (int dy = -hr; dy <= hr; dy++) {
                int span = hr - Math.abs(dy);
                context.fill(cx - span, cy + dy, cx + span + 1, cy + dy + 1, bg);
            }
        }
    }

    private static void renderCapsule(
            DrawContext context, PortDirection dir, int cx, int cy, int r, int fill, int border,
            int bg, boolean connected, boolean hovered
    ) {
        int pad = hovered ? 2 : 1;
        int halfW = r + 2;
        context.fill(cx - halfW - pad, cy - r - pad, cx + halfW + pad, cy + r + pad, border);
        context.fill(cx - halfW, cy - r, cx + halfW, cy + r, fill);
        if (!connected) {
            int hr = Math.max(1, r - 2);
            context.fill(cx - halfW + 2, cy - hr, cx + halfW - 2, cy + hr, bg);
        }
    }
}
