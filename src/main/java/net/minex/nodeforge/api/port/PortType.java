package net.minex.nodeforge.api.port;

import java.util.Objects;

/**
 * Represents the typed data contract of a node port.
 *
 * <p>A {@code PortType} defines the runtime Java type associated with a port,
 * a display name, an ARGB visual color for socket and wire rendering, and
 * whether the port represents an execution control flow rather than data.
 *
 * @param <T> the runtime Java payload type flowing through this port (or {@link Void} for execution flow)
 */
public final class PortType<T> {

    private final PortTypeId id;
    private final Class<T> typeClass;
    private final String displayName;
    private final int color;
    private final boolean execution;

    private PortType(Builder<T> builder) {
        this.id = builder.id;
        this.typeClass = builder.typeClass;
        this.displayName = builder.displayName;
        this.color = builder.color;
        this.execution = builder.execution;
    }

    /** Returns the unique identifier of this port type. */
    public PortTypeId id() {
        return id;
    }

    /** Returns the Java class associated with this port type. */
    public Class<T> typeClass() {
        return typeClass;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the ARGB color integer for visual rendering of this port type. */
    public int color() {
        return color;
    }

    /** Returns {@code true} if this port type represents an execution control flow (e.g. impulse / signal). */
    public boolean isExecution() {
        return execution;
    }

    /**
     * Checks if a value of this type can be assigned to a port of {@code other} type based on class hierarchy.
     *
     * @param other the target port type to check against
     * @return {@code true} if this type's class is assignable to the target type's class
     */
    public boolean isAssignableTo(PortType<?> other) {
        if (other == null) return false;
        if (this.equals(other)) return true;
        if (this.execution || other.execution) {
            return this.execution == other.execution;
        }
        return other.typeClass.isAssignableFrom(this.typeClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortType<?> portType)) return false;
        return color == portType.color &&
                execution == portType.execution &&
                id.equals(portType.id) &&
                typeClass.equals(portType.typeClass) &&
                displayName.equals(portType.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeClass, displayName, color, execution);
    }

    @Override
    public String toString() {
        return "PortType[" + id.value() + " (" + displayName + ")]";
    }

    // ========== Builder ==========

    /**
     * Creates a builder for a data port type.
     *
     * @param id        the port type ID
     * @param typeClass the Java class representing the payload
     * @param <T>       the payload type
     * @return a new builder
     */
    public static <T> Builder<T> builder(PortTypeId id, Class<T> typeClass) {
        return new Builder<>(id, typeClass);
    }

    /**
     * Creates a builder for a data port type using string ID.
     *
     * @param idString  the port type ID string
     * @param typeClass the Java class representing the payload
     * @param <T>       the payload type
     * @return a new builder
     */
    public static <T> Builder<T> builder(String idString, Class<T> typeClass) {
        return new Builder<>(PortTypeId.of(idString), typeClass);
    }

    /**
     * Creates an execution control flow port type.
     *
     * @param id          the port type ID
     * @param displayName the display name
     * @param color       the ARGB color
     * @return an execution port type
     */
    public static PortType<Void> execution(PortTypeId id, String displayName, int color) {
        return new Builder<>(id, Void.class)
                .displayName(displayName)
                .color(color)
                .execution(true)
                .build();
    }

    /**
     * Builder for constructing {@link PortType} instances.
     *
     * @param <T> the payload type
     */
    public static final class Builder<T> {
        private final PortTypeId id;
        private final Class<T> typeClass;
        private String displayName;
        private int color = 0xFFFFFFFF; // default white
        private boolean execution = false;

        private Builder(PortTypeId id, Class<T> typeClass) {
            this.id = Objects.requireNonNull(id, "PortTypeId must not be null");
            this.typeClass = Objects.requireNonNull(typeClass, "typeClass must not be null");
            this.displayName = id.value();
        }

        /** Sets the human-readable display name. */
        public Builder<T> displayName(String displayName) {
            Objects.requireNonNull(displayName, "displayName must not be null");
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("displayName must not be blank");
            }
            this.displayName = displayName;
            return this;
        }

        /** Sets the ARGB color. */
        public Builder<T> color(int argb) {
            this.color = argb;
            return this;
        }

        /** Sets whether this port type represents execution flow. */
        public Builder<T> execution(boolean execution) {
            this.execution = execution;
            return this;
        }

        /** Builds the {@link PortType}. */
        public PortType<T> build() {
            return new PortType<>(this);
        }
    }
}
