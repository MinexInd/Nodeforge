package net.minex.nodeforge.core.id;

import java.util.Objects;

/**
 * A strongly-typed identifier for a port within a node.
 *
 * <p>{@code PortId} values are scoped to their containing node — they are
 * NOT globally unique. A port is uniquely identified within a graph by the
 * pair {@code (NodeId, PortId)}.
 *
 * @param value the string value of this identifier, never {@code null} or blank
 */
public record PortId(String value) {

    /**
     * Creates a new {@code PortId} with the given value.
     *
     * @param value the identifier string, must not be {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public PortId {
        Objects.requireNonNull(value, "PortId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PortId value must not be blank");
        }
    }

    /**
     * Creates a new {@code PortId} with the given string value.
     * Convenience factory method.
     *
     * @param value the identifier string
     * @return a new {@code PortId}
     */
    public static PortId of(String value) {
        return new PortId(value);
    }

    @Override
    public String toString() {
        return "PortId[" + value + "]";
    }
}
