package net.minex.nodeforge.client.render;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import net.minex.nodeforge.client.editor.grid.GridConfig;
import net.minex.nodeforge.client.editor.grid.GridStyle;

import java.util.Objects;

/**
 * Renders the adaptive infinite background grid for the node editor canvas with configurable styles.
 */
public class GridRenderer {

    private final GridConfig config;

    public GridRenderer(GridConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public GridRenderer(double gridSize) {
        this(new GridConfig(GridStyle.LINES, gridSize, true));
    }

    public GridRenderer() {
        this(new GridConfig());
    }

    /** Returns the active grid configuration. */
    public GridConfig getConfig() {
        return config;
    }

    /**
     * Renders background grid clipped to the visible world bounds.
     *
     * @param context the Minecraft DrawContext
     * @param camera  the active camera
     * @param theme   the active color theme
     * @param vw      viewport width
     * @param vh      viewport height
     */
    public void render(DrawContext context, Camera camera, NodeTheme theme, double vw, double vh) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(camera, "camera must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        double gridSize = config.getSize();
        double majorInterval = gridSize * config.getMajorInterval();
        BoundingBox visible = camera.getVisibleWorldBounds(vw, vh);

        double startX = Math.floor(visible.minX() / gridSize) * gridSize;
        double endX = Math.ceil(visible.maxX() / gridSize) * gridSize;
        double startY = Math.floor(visible.minY() / gridSize) * gridSize;
        double endY = Math.ceil(visible.maxY() / gridSize) * gridSize;

        GridStyle style = config.getStyle();

        if (style == GridStyle.LINES) {
            // Render Vertical Grid Lines
            for (double x = startX; x <= endX; x += gridSize) {
                boolean isMajor = Math.abs(x % majorInterval) < 0.001 || Math.abs((x % majorInterval) - majorInterval) < 0.001;
                int color = isMajor ? theme.gridMajorColor() : theme.gridMinorColor();
                context.fill((int) Math.round(x), (int) Math.round(visible.minY()),
                        (int) Math.round(x + 1.0), (int) Math.round(visible.maxY()), color);
            }

            // Render Horizontal Grid Lines
            for (double y = startY; y <= endY; y += gridSize) {
                boolean isMajor = Math.abs(y % majorInterval) < 0.001 || Math.abs((y % majorInterval) - majorInterval) < 0.001;
                int color = isMajor ? theme.gridMajorColor() : theme.gridMinorColor();
                context.fill((int) Math.round(visible.minX()), (int) Math.round(y),
                        (int) Math.round(visible.maxX()), (int) Math.round(y + 1.0), color);
            }
        } else if (style == GridStyle.DOTS) {
            // Render Dots at intersections
            for (double x = startX; x <= endX; x += gridSize) {
                boolean isMajorX = Math.abs(x % majorInterval) < 0.001 || Math.abs((x % majorInterval) - majorInterval) < 0.001;
                for (double y = startY; y <= endY; y += gridSize) {
                    boolean isMajorY = Math.abs(y % majorInterval) < 0.001 || Math.abs((y % majorInterval) - majorInterval) < 0.001;
                    int color = (isMajorX && isMajorY) ? theme.gridMajorColor() : theme.gridMinorColor();
                    int dotSize = (isMajorX && isMajorY) ? 2 : 1;
                    int px = (int) Math.round(x);
                    int py = (int) Math.round(y);
                    context.fill(px, py, px + dotSize, py + dotSize, color);
                }
            }
        } else if (style == GridStyle.CROSSES) {
            // Render Crosses at intersections
            int arm = 3;
            for (double x = startX; x <= endX; x += gridSize) {
                boolean isMajorX = Math.abs(x % majorInterval) < 0.001 || Math.abs((x % majorInterval) - majorInterval) < 0.001;
                for (double y = startY; y <= endY; y += gridSize) {
                    boolean isMajorY = Math.abs(y % majorInterval) < 0.001 || Math.abs((y % majorInterval) - majorInterval) < 0.001;
                    int color = (isMajorX && isMajorY) ? theme.gridMajorColor() : theme.gridMinorColor();
                    int px = (int) Math.round(x);
                    int py = (int) Math.round(y);
                    context.fill(px - arm, py, px + arm + 1, py + 1, color);
                    context.fill(px, py - arm, px + 1, py + arm + 1, color);
                }
            }
        }
    }
}
