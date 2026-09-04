package net.minex.nodeforge.client.editor.camera;

import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;

import java.util.Objects;

/**
 * An immutable 2D axis-aligned bounding box in world graph coordinates.
 *
 * @param minX minimum horizontal coordinate (left)
 * @param minY minimum vertical coordinate (top)
 * @param maxX maximum horizontal coordinate (right)
 * @param maxY maximum vertical coordinate (bottom)
 */
public record BoundingBox(double minX, double minY, double maxX, double maxY) {

    /**
     * Creates a new bounding box, ensuring coordinates are finite and min <= max.
     */
    public BoundingBox {
        if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            throw new IllegalArgumentException("BoundingBox coordinates must be finite: [" + minX + ", " + minY + ", " + maxX + ", " + maxY + "]");
        }
        if (minX > maxX) {
            double temp = minX;
            minX = maxX;
            maxX = temp;
        }
        if (minY > maxY) {
            double temp = minY;
            minY = maxY;
            maxY = temp;
        }
    }

    /**
     * Creates a bounding box from a top-left position and size.
     */
    public static BoundingBox fromPositionAndSize(Position position, Size size) {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(size, "size must not be null");
        return new BoundingBox(position.x(), position.y(), position.x() + size.width(), position.y() + size.height());
    }

    /**
     * Creates a bounding box enclosing two arbitrary corner positions.
     */
    public static BoundingBox fromCorners(Position p1, Position p2) {
        Objects.requireNonNull(p1, "p1 must not be null");
        Objects.requireNonNull(p2, "p2 must not be null");
        return new BoundingBox(
                Math.min(p1.x(), p2.x()),
                Math.min(p1.y(), p2.y()),
                Math.max(p1.x(), p2.x()),
                Math.max(p1.y(), p2.y())
        );
    }

    /** Returns the width of this bounding box. */
    public double width() {
        return maxX - minX;
    }

    /** Returns the height of this bounding box. */
    public double height() {
        return maxY - minY;
    }

    /** Returns the center position of this bounding box. */
    public Position center() {
        return new Position(minX + width() / 2.0, minY + height() / 2.0);
    }

    /**
     * Checks if the given world position lies inside this bounding box.
     */
    public boolean contains(Position position) {
        if (position == null) return false;
        return position.x() >= minX && position.x() <= maxX
                && position.y() >= minY && position.y() <= maxY;
    }

    /**
     * Checks if this bounding box intersects with another bounding box.
     */
    public boolean intersects(BoundingBox other) {
        if (other == null) return false;
        return this.minX <= other.maxX && this.maxX >= other.minX
                && this.minY <= other.maxY && this.maxY >= other.minY;
    }

    /**
     * Expands this bounding box by the given padding on all sides.
     */
    public BoundingBox expand(double padding) {
        return new BoundingBox(minX - padding, minY - padding, maxX + padding, maxY + padding);
    }
}
