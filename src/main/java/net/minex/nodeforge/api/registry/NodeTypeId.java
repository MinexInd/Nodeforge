package net.minex.nodeforge.api.registry;

import java.util.Objects;

/**
 * A strongly-typed identifier for a {@link NodeDefinition}.
 *
 * <p>Node type identifiers follow the namespaced format {@code "namespace:path"}
 * (e.g. {@code "nodeforge:math_add"}, {@code "mymod:custom_node"}).
 *
 * @param value the string value of this identifier, never {@code null} or blank
 */
public record NodeTypeId(String value) {

    /**
     * Creates a new {@code NodeTypeId} with the given value.
     *
     * @param value the identifier string, must not be {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public NodeTypeId {
        Objects.requireNonNull(value, "NodeTypeId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("NodeTypeId value must not be blank");
        }
    }

    /**
     * Creates a new {@code NodeTypeId} with the given string value.
     *
     * @param value the identifier string
     * @return a new {@code NodeTypeId}
     */
    public static NodeTypeId of(String value) {
        return new NodeTypeId(value);
    }

    /**
     * Creates a namespaced {@code NodeTypeId} (e.g. {@code "namespace:path"}).
     *
     * @param namespace the namespace, must not be null or blank
     * @param path      the path, must not be null or blank
     * @return a new namespaced {@code NodeTypeId}
     */
    public static NodeTypeId of(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return new NodeTypeId(namespace + ":" + path);
    }

    @Override
    public String toString() {
        return "NodeTypeId[" + value + "]";
    }
}
