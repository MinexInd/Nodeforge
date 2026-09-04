package net.minex.nodeforge.client.editor.camera;

import net.minex.nodeforge.api.graph.Position;

import java.util.Objects;

/**
 * Handles grid snapping calculations and coordinate quantization for the node editor canvas.
 *
 * @param gridSize the distance between grid snap lines (must be > 0)
 * @param enabled  whether snapping is currently active
 */
public record GridSnap(double gridSize, boolean enabled) {

    /** Default grid configuration with 16.0 unit spacing and snapping enabled. */
    public static final GridSnap DEFAULT = new GridSnap(16.0, true);

    /** A disabled grid snap instance. */
    public static final GridSnap DISABLED = new GridSnap(16.0, false);

    /**
     * Creates a new {@code GridSnap}.
     *
     * @param gridSize the grid spacing, must be positive and finite
     * @param enabled  whether snapping is enabled
     */
    public GridSnap {
        if (!Double.isFinite(gridSize) || gridSize <= 0.0) {
            throw new IllegalArgumentException("Grid size must be a positive finite number: " + gridSize);
        }
    }

    /** Creates an enabled grid snap configuration with the given size. */
    public static GridSnap of(double gridSize) {
        return new GridSnap(gridSize, true);
    }

    /**
     * Snaps a 1D scalar value to the nearest grid line if snapping is enabled.
     *
     * @param value the raw coordinate
     * @return the snapped coordinate (or original value if disabled)
     */
    public double snap(double value) {
        if (!enabled || !Double.isFinite(value)) return value;
        double snapped = Math.floor((value / gridSize) + 0.5) * gridSize;
        return snapped == 0.0 ? 0.0 : snapped;
    }

    /**
     * Snaps a 2D {@link Position} to the nearest grid point if snapping is enabled.
     *
     * @param position the raw position, must not be null
     * @return the snapped position
     */
    public Position snap(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        if (!enabled) return position;
        return new Position(snap(position.x()), snap(position.y()));
    }

    /**
     * Snaps a 2D {@link Position} to the grid only if within the specified tolerance distance.
     *
     * @param position  the raw position
     * @param tolerance maximum distance from a grid line to trigger snapping
     * @return the snapped position if within tolerance, otherwise the original position
     */
    public Position snap(Position position, double tolerance) {
        Objects.requireNonNull(position, "position must not be null");
        if (!enabled || tolerance <= 0.0) return position;

        double nearestX = snap(position.x());
        double nearestY = snap(position.y());

        double finalX = Math.abs(position.x() - nearestX) <= tolerance ? nearestX : position.x();
        double finalY = Math.abs(position.y() - nearestY) <= tolerance ? nearestY : position.y();

        return new Position(finalX, finalY);
    }

    /** Returns a new {@code GridSnap} with the enabled state toggled. */
    public GridSnap withEnabled(boolean enabled) {
        return new GridSnap(gridSize, enabled);
    }

    /** Returns a new {@code GridSnap} with a different grid size. */
    public GridSnap withGridSize(double newSize) {
        return new GridSnap(newSize, enabled);
    }
}
