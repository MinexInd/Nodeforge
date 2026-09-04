package net.minex.nodeforge.api.graph;

/**
 * The dimensions of a node in graph coordinate space.
 *
 * <p>Width and height must be finite non-negative numbers.
 * Negative zero ({@code -0.0}) is normalized to {@code 0.0}.
 *
 * @param width  the horizontal extent, must be finite and {@code >= 0}
 * @param height the vertical extent, must be finite and {@code >= 0}
 */
public record Size(double width, double height) {

    /** A zero-sized dimension. */
    public static final Size ZERO = new Size(0.0, 0.0);

    /**
     * Default size for newly created nodes.
     * Consumers can override this per node type.
     */
    public static final Size DEFAULT = new Size(160.0, 80.0);

    /**
     * Creates a new {@code Size}.
     *
     * @param width  the horizontal extent, must be finite and {@code >= 0}
     * @param height the vertical extent, must be finite and {@code >= 0}
     * @throws IllegalArgumentException if width or height is negative, NaN, or infinite
     */
    public Size {
        if (!Double.isFinite(width)) {
            throw new IllegalArgumentException("Width must be finite: " + width);
        }
        if (!Double.isFinite(height)) {
            throw new IllegalArgumentException("Height must be finite: " + height);
        }
        if (width < 0.0) {
            throw new IllegalArgumentException("Width must not be negative: " + width);
        }
        if (height < 0.0) {
            throw new IllegalArgumentException("Height must not be negative: " + height);
        }
        // Normalize -0.0 to 0.0
        width = width == 0.0 ? 0.0 : width;
        height = height == 0.0 ? 0.0 : height;
    }

    /**
     * Returns a new {@code Size} scaled by the given factor.
     *
     * @param factor the scale factor, must be finite and non-negative
     * @return a new scaled size
     * @throws IllegalArgumentException if factor is negative, NaN, or infinite
     */
    public Size scale(double factor) {
        if (!Double.isFinite(factor) || factor < 0.0) {
            throw new IllegalArgumentException("Scale factor must be a finite non-negative number: " + factor);
        }
        return new Size(width * factor, height * factor);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
