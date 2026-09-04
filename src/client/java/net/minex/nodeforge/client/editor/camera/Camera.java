package net.minex.nodeforge.client.editor.camera;

import net.minex.nodeforge.api.graph.Position;

import java.util.Objects;

/**
 * 2D camera model managing pan, zoom, and continuous coordinate projections
 * between screen pixels and world graph coordinates.
 *
 * <p>All transformations assume the origin {@code (0, 0)} of the graph aligns
 * with the center of the viewport when pan is {@code (0, 0)}.
 */
public final class Camera {

    public static final double DEFAULT_MIN_ZOOM = 0.1;
    public static final double DEFAULT_MAX_ZOOM = 3.0;
    public static final double DEFAULT_ZOOM = 1.0;

    private Position pan = Position.ZERO;
    private double zoom = DEFAULT_ZOOM;
    private double minZoom = DEFAULT_MIN_ZOOM;
    private double maxZoom = DEFAULT_MAX_ZOOM;

    /**
     * Creates a camera at default position and zoom.
     */
    public Camera() {
    }

    /**
     * Creates a camera with initial pan and zoom.
     */
    public Camera(Position pan, double zoom) {
        setPan(pan);
        setZoom(zoom);
    }

    // ========== Pan & Zoom Accessors ==========

    /** Returns the current world pan position centered in the viewport. */
    public Position pan() {
        return pan;
    }

    /** Sets the pan position. */
    public void setPan(Position pan) {
        this.pan = Objects.requireNonNull(pan, "pan must not be null");
    }

    /** Sets the pan position from coordinates. */
    public void setPan(double x, double y) {
        this.pan = new Position(x, y);
    }

    /** Translates the pan position by delta coordinates in world space. */
    public void movePan(double dx, double dy) {
        this.pan = this.pan.offset(dx, dy);
    }

    /** Returns the current zoom scale factor (1.0 = 100%). */
    public double zoom() {
        return zoom;
    }

    /**
     * Sets the zoom level, clamped between {@link #minZoom()} and {@link #maxZoom()}.
     */
    public void setZoom(double newZoom) {
        if (!Double.isFinite(newZoom) || newZoom <= 0.0) {
            throw new IllegalArgumentException("Zoom must be a positive finite number: " + newZoom);
        }
        this.zoom = Math.clamp(newZoom, minZoom, maxZoom);
    }

    /** Returns the minimum allowable zoom level. */
    public double minZoom() {
        return minZoom;
    }

    /** Returns the maximum allowable zoom level. */
    public double maxZoom() {
        return maxZoom;
    }

    /** Sets the minimum and maximum allowable zoom bounds. */
    public void setZoomLimits(double min, double max) {
        if (!Double.isFinite(min) || min <= 0.0) {
            throw new IllegalArgumentException("minZoom must be positive and finite: " + min);
        }
        if (!Double.isFinite(max) || max <= 0.0) {
            throw new IllegalArgumentException("maxZoom must be positive and finite: " + max);
        }
        if (min > max) {
            throw new IllegalArgumentException("minZoom (" + min + ") must not exceed maxZoom (" + max + ")");
        }
        this.minZoom = min;
        this.maxZoom = max;
        // Re-clamp current zoom
        this.zoom = Math.clamp(zoom, minZoom, maxZoom);
    }

    /** Resets the camera to the default pan {@code (0, 0)} and zoom {@code 1.0}. */
    public void reset() {
        this.pan = Position.ZERO;
        this.zoom = DEFAULT_ZOOM;
    }

    // ========== Coordinate Transformations ==========

    /**
     * Converts screen pixel coordinates to world graph coordinates.
     *
     * @param screenX        horizontal screen pixel coordinate
     * @param screenY        vertical screen pixel coordinate
     * @param viewportWidth  width of the viewport in screen pixels
     * @param viewportHeight height of the viewport in screen pixels
     * @return the corresponding world position
     */
    public Position screenToWorld(double screenX, double screenY, double viewportWidth, double viewportHeight) {
        validateDimensions(viewportWidth, viewportHeight);
        double centerX = viewportWidth / 2.0;
        double centerY = viewportHeight / 2.0;

        double worldX = pan.x() + (screenX - centerX) / zoom;
        double worldY = pan.y() + (screenY - centerY) / zoom;
        return new Position(worldX, worldY);
    }

    /**
     * Converts world graph coordinates to screen pixel coordinates.
     *
     * @param worldX         horizontal world coordinate
     * @param worldY         vertical world coordinate
     * @param viewportWidth  width of the viewport in screen pixels
     * @param viewportHeight height of the viewport in screen pixels
     * @return the corresponding screen position
     */
    public Position worldToScreen(double worldX, double worldY, double viewportWidth, double viewportHeight) {
        validateDimensions(viewportWidth, viewportHeight);
        double centerX = viewportWidth / 2.0;
        double centerY = viewportHeight / 2.0;

        double screenX = centerX + (worldX - pan.x()) * zoom;
        double screenY = centerY + (worldY - pan.y()) * zoom;
        return new Position(screenX, screenY);
    }

    /**
     * Adjusts the zoom level while keeping the world point under the mouse cursor stationary.
     *
     * @param screenX        cursor horizontal pixel coordinate
     * @param screenY        cursor vertical pixel coordinate
     * @param zoomDelta      multiplicative or additive zoom factor (e.g. 1.15 to zoom in, 0.85 to zoom out)
     * @param viewportWidth  width of the viewport
     * @param viewportHeight height of the viewport
     */
    public void zoomAt(double screenX, double screenY, double zoomDelta, double viewportWidth, double viewportHeight) {
        validateDimensions(viewportWidth, viewportHeight);
        if (zoomDelta <= 0.0 || !Double.isFinite(zoomDelta)) return;

        // 1. Determine the world coordinate under the cursor before zoom
        Position worldPosBefore = screenToWorld(screenX, screenY, viewportWidth, viewportHeight);

        // 2. Compute new clamped zoom
        double targetZoom = Math.clamp(this.zoom * zoomDelta, minZoom, maxZoom);
        this.zoom = targetZoom;

        // 3. Re-adjust pan so the same world coordinate remains under (screenX, screenY)
        double centerX = viewportWidth / 2.0;
        double centerY = viewportHeight / 2.0;
        double newPanX = worldPosBefore.x() - (screenX - centerX) / this.zoom;
        double newPanY = worldPosBefore.y() - (screenY - centerY) / this.zoom;
        this.pan = new Position(newPanX, newPanY);
    }

    /**
     * Computes the visible bounding box in world space currently displayed in the viewport.
     * Useful for frustum culling during rendering.
     *
     * @param viewportWidth  width of the viewport in screen pixels
     * @param viewportHeight height of the viewport in screen pixels
     * @return the world-space bounding box
     */
    public BoundingBox getVisibleWorldBounds(double viewportWidth, double viewportHeight) {
        Position topLeft = screenToWorld(0, 0, viewportWidth, viewportHeight);
        Position bottomRight = screenToWorld(viewportWidth, viewportHeight, viewportWidth, viewportHeight);
        return BoundingBox.fromCorners(topLeft, bottomRight);
    }

    private void validateDimensions(double w, double h) {
        if (!Double.isFinite(w) || w <= 0.0 || !Double.isFinite(h) || h <= 0.0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive finite numbers: " + w + "x" + h);
        }
    }

    @Override
    public String toString() {
        return "Camera[pan=" + pan + ", zoom=" + zoom + "]";
    }
}
