package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;

/**
 * A blueprint specification defining a port on a {@link NodeDefinition}.
 *
 * <p>When a node is instantiated via {@link NodeDefinition#createNode},
 * each {@code PortTemplate} produces an active {@link Port} on the new node.
 *
 * @param id          the port identifier, unique within the node definition
 * @param displayName the human-readable display name
 * @param direction   the port direction (input or output)
 * @param portType    the strongly-typed data or execution type
 * @param required    whether this port must be connected for the node to be valid
 * @param description optional documentation description for tooltips
 */
public record PortTemplate(
        PortId id,
        String displayName,
        PortDirection direction,
        PortType<?> portType,
        boolean required,
        String description
) {

    /**
     * Creates a new {@code PortTemplate}.
     *
     * @throws NullPointerException     if id, displayName, direction, or portType is null
     * @throws IllegalArgumentException if displayName is blank
     */
    public PortTemplate {
        Objects.requireNonNull(id, "PortId must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(portType, "portType must not be null");
        description = description == null ? "" : description;
    }

    /** Creates an input port template. */
    public static PortTemplate input(String id, String displayName, PortType<?> portType) {
        return new PortTemplate(PortId.of(id), displayName, PortDirection.INPUT, portType, false, "");
    }

    /** Creates an input port template with required flag. */
    public static PortTemplate input(String id, String displayName, PortType<?> portType, boolean required) {
        return new PortTemplate(PortId.of(id), displayName, PortDirection.INPUT, portType, required, "");
    }

    /** Creates an input port template with full parameters. */
    public static PortTemplate input(PortId id, String displayName, PortType<?> portType, boolean required, String description) {
        return new PortTemplate(id, displayName, PortDirection.INPUT, portType, required, description);
    }

    /** Creates an output port template. */
    public static PortTemplate output(String id, String displayName, PortType<?> portType) {
        return new PortTemplate(PortId.of(id), displayName, PortDirection.OUTPUT, portType, false, "");
    }

    /** Creates an output port template with full parameters. */
    public static PortTemplate output(PortId id, String displayName, PortType<?> portType, String description) {
        return new PortTemplate(id, displayName, PortDirection.OUTPUT, portType, false, description);
    }

    /**
     * Instantiates an active {@link Port} based on this template.
     *
     * @return a new active {@link Port}
     */
    public Port instantiate() {
        return new Port(id, displayName, direction, portType);
    }

    /** Returns {@code true} if this template defines an input port. */
    public boolean isInput() {
        return direction == PortDirection.INPUT;
    }

    /** Returns {@code true} if this template defines an output port. */
    public boolean isOutput() {
        return direction == PortDirection.OUTPUT;
    }
}
