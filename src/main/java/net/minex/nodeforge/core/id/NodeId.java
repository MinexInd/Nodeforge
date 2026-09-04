package net.minex.nodeforge.core.id;

import java.util.Objects;
import java.util.UUID;

/**
 * A strongly-typed, globally unique identifier for a node within a graph.
 *
 * <p>{@code NodeId} values are unique within a single graph.
 * Use {@link #random()} to generate collision-resistant IDs.
 *
 * @param value the string value of this identifier, never {@code null} or blank
 */
public record NodeId(String value) {

    /**
     * Creates a new {@code NodeId} with the given value.
     *
     * @param value the identifier string, must not be {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public NodeId {
        Objects.requireNonNull(value, "NodeId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("NodeId value must not be blank");
        }
    }

    /**
     * Creates a new {@code NodeId} with a random UUID value.
     *
     * @return a new unique {@code NodeId}
     */
    public static NodeId random() {
        return new NodeId(UUID.randomUUID().toString());
    }

    /**
     * Creates a new {@code NodeId} with the given string value.
     * Convenience factory method.
     *
     * @param value the identifier string
     * @return a new {@code NodeId}
     */
    public static NodeId of(String value) {
        return new NodeId(value);
    }

    @Override
    public String toString() {
        return "NodeId[" + value + "]";
    }
}
