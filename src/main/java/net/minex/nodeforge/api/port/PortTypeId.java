package net.minex.nodeforge.api.port;

import java.util.Objects;

/**
 * A strongly-typed identifier for a {@link PortType}.
 *
 * <p>Identifiers typically follow the namespaced format {@code "namespace:path"}
 * (e.g. {@code "nodeforge:execution"}, {@code "nodeforge:string"}, {@code "mymod:custom_port"}),
 * but simple names are also accepted.
 *
 * @param value the string value of this identifier, never {@code null} or blank
 */
public record PortTypeId(String value) {

    /**
     * Creates a new {@code PortTypeId} with the given value.
     *
     * @param value the identifier string, must not be {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public PortTypeId {
        Objects.requireNonNull(value, "PortTypeId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PortTypeId value must not be blank");
        }
    }

    /**
     * Creates a new {@code PortTypeId} with the given string value.
     *
     * @param value the identifier string
     * @return a new {@code PortTypeId}
     */
    public static PortTypeId of(String value) {
        return new PortTypeId(value);
    }

    /**
     * Creates a namespaced {@code PortTypeId} (e.g. {@code "namespace:path"}).
     *
     * @param namespace the namespace, must not be null or blank
     * @param path      the path, must not be null or blank
     * @return a new namespaced {@code PortTypeId}
     */
    public static PortTypeId of(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return new PortTypeId(namespace + ":" + path);
    }

    @Override
    public String toString() {
        return "PortTypeId[" + value + "]";
    }
}
