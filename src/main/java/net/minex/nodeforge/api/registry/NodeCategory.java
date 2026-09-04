package net.minex.nodeforge.api.registry;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a category grouping for node types in node creation palettes, menus, and search filters.
 *
 * <p>Categories support hierarchical nesting via {@link #parent()} and ordering via {@link #order()}.
 */
public final class NodeCategory {

    // ========== Built-in Standard Categories ==========

    public static final NodeCategory ACTION = new NodeCategory("nodeforge:action", "Action", 10, null);
    public static final NodeCategory EVENT = new NodeCategory("nodeforge:event", "Event", 20, null);
    public static final NodeCategory CONDITION = new NodeCategory("nodeforge:condition", "Condition", 30, null);
    public static final NodeCategory MATH = new NodeCategory("nodeforge:math", "Math", 40, null);
    public static final NodeCategory LOGIC = new NodeCategory("nodeforge:logic", "Logic", 50, null);
    public static final NodeCategory DATA = new NodeCategory("nodeforge:data", "Data", 60, null);
    public static final NodeCategory FLOW_CONTROL = new NodeCategory("nodeforge:flow_control", "Flow Control", 70, null);
    public static final NodeCategory MISC = new NodeCategory("nodeforge:misc", "Miscellaneous", 999, null);

    private final String id;
    private final String displayName;
    private final int order;
    private final NodeCategory parent;

    /**
     * Creates a new category.
     *
     * @param id          the unique category ID, must not be null or blank
     * @param displayName the human-readable display name, must not be null or blank
     * @param order       display sort order index (lower values appear first)
     * @param parent      optional parent category for nesting, or {@code null}
     */
    public NodeCategory(String id, String displayName, int order, NodeCategory parent) {
        this.id = Objects.requireNonNull(id, "category id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("category id must not be blank");
        }
        this.displayName = Objects.requireNonNull(displayName, "category displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("category displayName must not be blank");
        }
        this.order = order;
        this.parent = parent;
    }

    /**
     * Creates a top-level category with default order (100).
     */
    public static NodeCategory of(String id, String displayName) {
        return new NodeCategory(id, displayName, 100, null);
    }

    /**
     * Creates a top-level category with a specific sort order.
     */
    public static NodeCategory of(String id, String displayName, int order) {
        return new NodeCategory(id, displayName, order, null);
    }

    /**
     * Creates a nested child category under a parent category.
     */
    public static NodeCategory nested(String id, String displayName, NodeCategory parent) {
        return new NodeCategory(id, displayName, 100, Objects.requireNonNull(parent, "parent must not be null"));
    }

    /**
     * Creates a nested child category under a parent category with a specific sort order.
     */
    public static NodeCategory nested(String id, String displayName, int order, NodeCategory parent) {
        return new NodeCategory(id, displayName, order, Objects.requireNonNull(parent, "parent must not be null"));
    }

    /** Returns the unique identifier of this category. */
    public String id() {
        return id;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the sort order index. */
    public int order() {
        return order;
    }

    /** Returns the optional parent category. */
    public Optional<NodeCategory> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeCategory that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "NodeCategory[" + id + " ('" + displayName + "')]";
    }
}
