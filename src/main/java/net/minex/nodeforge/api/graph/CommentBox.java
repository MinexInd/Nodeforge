package net.minex.nodeforge.api.graph;

import java.util.Collection;
import java.util.Objects;

/**
 * A visual grouping container and comment frame in a node graph.
 *
 * <p>Comment boxes group related nodes, provide descriptive headers,
 * and allow moving all enclosed nodes as a single logical unit.
 */
public final class CommentBox {

    public static final int DEFAULT_COLOR = 0x4031353D; // Translucent dark gray
    public static final double DEFAULT_PADDING = 20.0;
    public static final double HEADER_HEIGHT = 22.0;

    private final String id;
    private String title;
    private Position position;
    private Size size;
    private int color;

    public CommentBox(String id, String title, Position position, Size size, int color) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = title == null ? "Comment" : title;
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.size = Objects.requireNonNull(size, "size must not be null");
        this.color = color;
    }

    public CommentBox(String id, String title, Position position, Size size) {
        this(id, title, position, size, DEFAULT_COLOR);
    }

    /** Creates a comment box sized to enclose a collection of nodes with padding. */
    public static CommentBox aroundNodes(String id, String title, Collection<Node> nodes, double padding) {
        Objects.requireNonNull(nodes, "nodes must not be null");
        if (nodes.isEmpty()) {
            return new CommentBox(id, title, Position.ZERO, new Size(200.0, 150.0));
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Node node : nodes) {
            minX = Math.min(minX, node.position().x());
            minY = Math.min(minY, node.position().y());
            maxX = Math.max(maxX, node.position().x() + node.size().width());
            maxY = Math.max(maxY, node.position().y() + node.size().height());
        }

        double x = minX - padding;
        double y = minY - padding - HEADER_HEIGHT;
        double w = Math.max(120.0, (maxX - minX) + padding * 2.0);
        double h = Math.max(80.0, (maxY - minY) + padding * 2.0 + HEADER_HEIGHT);

        return new CommentBox(id, title, new Position(x, y), new Size(w, h));
    }

    // ========== Getters and Mutators ==========

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public Position position() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    public Size size() {
        return size;
    }

    public void setSize(Size size) {
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    // ========== Spatial Boundaries ==========

    public double minX() {
        return position.x();
    }

    public double minY() {
        return position.y();
    }

    public double maxX() {
        return position.x() + size.width();
    }

    public double maxY() {
        return position.y() + size.height();
    }

    /** Returns {@code true} if the given world position is contained within this box. */
    public boolean contains(Position pos) {
        if (pos == null) return false;
        return pos.x() >= minX() && pos.x() <= maxX()
                && pos.y() >= minY() && pos.y() <= maxY();
    }

    /** Returns {@code true} if the given point is inside the top header bar of this comment box. */
    public boolean isHeaderHit(Position pos) {
        if (pos == null) return false;
        return pos.x() >= minX() && pos.x() <= maxX()
                && pos.y() >= minY() && pos.y() <= minY() + HEADER_HEIGHT;
    }

    /** Returns {@code true} if the given node is completely enclosed by this comment box. */
    public boolean encloses(Node node) {
        if (node == null) return false;
        double nMinX = node.position().x();
        double nMinY = node.position().y();
        double nMaxX = nMinX + node.size().width();
        double nMaxY = nMinY + node.size().height();

        return nMinX >= minX() && nMaxX <= maxX()
                && nMinY >= minY() && nMaxY <= maxY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentBox that)) return false;
        return color == that.color
                && id.equals(that.id)
                && title.equals(that.title)
                && position.equals(that.position)
                && size.equals(that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, position, size, color);
    }

    @Override
    public String toString() {
        return "CommentBox{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", position=" + position +
                ", size=" + size +
                '}';
    }
}
