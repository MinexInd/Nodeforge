package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;

/**
 * A connection point on a {@link Node}.
 *
 * <p>Ports are immutable and defined at node construction time. Each port has:
 * <ul>
 *   <li>A {@link PortId} unique within its containing node</li>
 *   <li>A human-readable display name (non-blank)</li>
 *   <li>A {@link PortDirection} (input or output)</li>
 *   <li>A type key string identifying the data type (non-blank, e.g. {@code "nodeforge:float"})</li>
 * </ul>
 */
public final class Port {

    private final PortId id;
    private final String name;
    private final PortDirection direction;
    private final String typeKey;

    /**
     * Creates a new port with a string type key.
     *
     * @param id        the port identifier, unique within its node
     * @param name      the human-readable display name
     * @param direction the port direction (input or output)
     * @param typeKey   the type key for connection compatibility (e.g. {@code "nodeforge:float"})
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if {@code name} or {@code typeKey} is blank
     */
    public Port(PortId id, String name, PortDirection direction, String typeKey) {
        this.id = Objects.requireNonNull(id, "Port id must not be null");
        this.name = Objects.requireNonNull(name, "Port name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Port name must not be blank");
        }
        this.direction = Objects.requireNonNull(direction, "Port direction must not be null");
        this.typeKey = Objects.requireNonNull(typeKey, "Port typeKey must not be null");
        if (typeKey.isBlank()) {
            throw new IllegalArgumentException("Port typeKey must not be blank");
        }
    }

    /**
     * Creates a new port with a strongly-typed {@link PortType}.
     *
     * @param id        the port identifier
     * @param name      the display name
     * @param direction the port direction
     * @param portType  the strongly-typed port type
     */
    public Port(PortId id, String name, PortDirection direction, PortType<?> portType) {
        this(id, name, direction, Objects.requireNonNull(portType, "PortType must not be null").id().value());
    }

    // ========== Factories (with PortType) ==========

    /**
     * Creates an input port with a typed {@link PortType}.
     */
    public static Port input(PortId id, String name, PortType<?> portType) {
        return new Port(id, name, PortDirection.INPUT, portType);
    }

    /**
     * Creates an input port with a typed {@link PortType} using string ID.
     */
    public static Port input(String id, String name, PortType<?> portType) {
        return new Port(PortId.of(id), name, PortDirection.INPUT, portType);
    }

    /**
     * Creates an output port with a typed {@link PortType}.
     */
    public static Port output(PortId id, String name, PortType<?> portType) {
        return new Port(id, name, PortDirection.OUTPUT, portType);
    }

    /**
     * Creates an output port with a typed {@link PortType} using string ID.
     */
    public static Port output(String id, String name, PortType<?> portType) {
        return new Port(PortId.of(id), name, PortDirection.OUTPUT, portType);
    }

    // ========== Factories (with string typeKey) ==========

    /**
     * Convenience factory for creating an input port.
     *
     * @param id      the port identifier
     * @param name    the display name
     * @param typeKey the type key
     * @return a new input port
     */
    public static Port input(PortId id, String name, String typeKey) {
        return new Port(id, name, PortDirection.INPUT, typeKey);
    }

    /**
     * Convenience factory for creating an input port using string IDs.
     *
     * @param id      the port identifier string
     * @param name    the display name
     * @param typeKey the type key
     * @return a new input port
     */
    public static Port input(String id, String name, String typeKey) {
        return new Port(PortId.of(id), name, PortDirection.INPUT, typeKey);
    }

    /**
     * Convenience factory for creating an output port.
     *
     * @param id      the port identifier
     * @param name    the display name
     * @param typeKey the type key
     * @return a new output port
     */
    public static Port output(PortId id, String name, String typeKey) {
        return new Port(id, name, PortDirection.OUTPUT, typeKey);
    }

    /**
     * Convenience factory for creating an output port using string IDs.
     *
     * @param id      the port identifier string
     * @param name    the display name
     * @param typeKey the type key
     * @return a new output port
     */
    public static Port output(String id, String name, String typeKey) {
        return new Port(PortId.of(id), name, PortDirection.OUTPUT, typeKey);
    }

    /** Returns the port identifier. */
    public PortId id() {
        return id;
    }

    /** Returns the human-readable display name. */
    public String name() {
        return name;
    }

    /** Returns the port direction. */
    public PortDirection direction() {
        return direction;
    }

    /** Returns the type key used for connection compatibility. */
    public String typeKey() {
        return typeKey;
    }

    /**
     * Resolves the {@link PortType} for this port from the global {@link PortTypeRegistry}.
     *
     * @return the resolved {@link PortType}, or {@code null} if unregistered
     */
    public PortType<?> portType() {
        return PortTypeRegistry.getInstance().get(typeKey);
    }

    /** Returns {@code true} if this is an input port. */
    public boolean isInput() {
        return direction == PortDirection.INPUT;
    }

    /** Returns {@code true} if this is an output port. */
    public boolean isOutput() {
        return direction == PortDirection.OUTPUT;
    }

    /** Returns {@code true} if this port is an execution control-flow port. */
    public boolean isExecution() {
        PortType<?> pt = portType();
        if (pt != null) {
            return pt.isExecution();
        }
        return "nodeforge:execution".equals(typeKey);
    }

    /** Returns {@code true} if this port carries data values. */
    public boolean isData() {
        return !isExecution();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Port port)) return false;
        return direction == port.direction && id.equals(port.id);
    }

    @Override
    public int hashCode() {
        return 31 * id.hashCode() + direction.hashCode();
    }

    @Override
    public String toString() {
        return "Port[" + id.value() + " " + direction + " " + typeKey + "]";
    }
}
