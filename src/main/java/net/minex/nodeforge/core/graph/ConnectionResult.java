package net.minex.nodeforge.core.graph;

import net.minex.nodeforge.api.graph.Connection;

/**
 * The result of attempting to create a connection in a graph.
 *
 * <p>This is a sealed interface — callers can exhaustively match on
 * {@link Success} and {@link Failure} using pattern matching (Java 21+).
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * switch (graph.connect(fromNode, fromPort, toNode, toPort)) {
 *     case ConnectionResult.Success s -> System.out.println("Connected: " + s.connection());
 *     case ConnectionResult.Failure f -> System.out.println("Failed: " + f.reason());
 * }
 * }</pre>
 */
public sealed interface ConnectionResult {

    /**
     * A successful connection.
     *
     * @param connection the newly created connection
     */
    record Success(Connection connection) implements ConnectionResult {}

    /**
     * A failed connection attempt.
     *
     * @param reason a human-readable explanation
     * @param type   the category of failure
     */
    record Failure(String reason, FailureType type) implements ConnectionResult {}

    /**
     * Returns {@code true} if this result is a success.
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Categories of connection failure.
     */
    enum FailureType {
        /** One of the referenced nodes does not exist in the graph. */
        NODE_NOT_FOUND,

        /** One of the referenced ports does not exist on the node. */
        PORT_NOT_FOUND,

        /** Both endpoints refer to the same node (self-connection). */
        SELF_CONNECTION,

        /** The connection direction is wrong (e.g. input→input). */
        WRONG_DIRECTION,

        /** An identical connection already exists. */
        DUPLICATE_CONNECTION,

        /** The port types are incompatible (used in Phase 2+). */
        INCOMPATIBLE_TYPES
    }
}
