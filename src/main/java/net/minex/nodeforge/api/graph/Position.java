package net.minex.nodeforge.api.graph;

/**
 * A 2D position in graph coordinate space.
 *
 * <p>Graph coordinates are continuous (double-precision) and may be negative.
 * Both {@code x} and {@code y} must be finite numbers (cannot be NaN or Infinite).
 * Negative zero ({@code -0.0}) is normalized to {@code 0.0}.
 * The origin {@code (0, 0)} is typically the center of the canvas.
 *
 * @param x the horizontal coordinate (finite)
 * @param y the vertical coordinate (finite)
 */
public record Position(double x, double y) {

    /** The origin position {@code (0, 0)}. */
    public static final Position ZERO = new Position(0.0, 0.0);

    /**
     * Creates a new {@code Position}.
     *
     * @param x horizontal coordinate, must be finite
     * @param y vertical coordinate, must be finite
     * @throws IllegalArgumentException if x or y is NaN or infinite
     */
    public Position {
        if (!Double.isFinite(x)) {
            throw new IllegalArgumentException("Position x coordinate must be finite: " + x);
        }
        if (!Double.isFinite(y)) {
            throw new IllegalArgumentException("Position y coordinate must be finite: " + y);
        }
        // Normalize -0.0 to 0.0
        x = x == 0.0 ? 0.0 : x;
        y = y == 0.0 ? 0.0 : y;
    }

    /**
     * Returns a new position offset by the given deltas.
     *
     * @param dx horizontal offset
     * @param dy vertical offset
     * @return a new offset position
     */
    public Position offset(double dx, double dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Computes the Euclidean distance to another position.
     *
     * @param other the other position
     * @return the distance between the two positions
     */
    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the squared distance to another position.
     * Useful for distance comparisons without the sqrt cost.
     *
     * @param other the other position
     * @return the squared distance
     */
    public double distanceSquaredTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
