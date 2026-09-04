package net.minex.nodeforge.core.id;

import java.util.Objects;
import java.util.UUID;

/**
 * A strongly-typed, globally unique identifier for a connection within a graph.
 *
 * @param value the string value of this identifier, never {@code null} or blank
 */
public record ConnectionId(String value) {

    /**
     * Creates a new {@code ConnectionId} with the given value.
     *
     * @param value the identifier string, must not be {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public ConnectionId {
        Objects.requireNonNull(value, "ConnectionId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ConnectionId value must not be blank");
        }
    }

    /**
     * Creates a new {@code ConnectionId} with a random UUID value.
     *
     * @return a new unique {@code ConnectionId}
     */
    public static ConnectionId random() {
        return new ConnectionId(UUID.randomUUID().toString());
    }

    /**
     * Creates a new {@code ConnectionId} with the given string value.
     * Convenience factory method.
     *
     * @param value the identifier string
     * @return a new {@code ConnectionId}
     */
    public static ConnectionId of(String value) {
        return new ConnectionId(value);
    }

    @Override
    public String toString() {
        return "ConnectionId[" + value + "]";
    }
}
