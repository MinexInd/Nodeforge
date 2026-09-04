package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;

/**
 * A node in a graph.
 *
 * <p>Each node has a unique {@link NodeId}, a type key identifying what kind of
 * node it is (linked to the node registry in Phase 3), a set of immutable
 * {@link Port}s, a mutable position and size, and optional metadata.
 *
 * <p>Nodes are created using the {@link Builder}.
 *
 * <h2>Mutability</h2>
 * <ul>
 *   <li>{@code position} and {@code size} are mutable (editor needs to move/resize)</li>
 *   <li>{@code displayName} is mutable (users may rename nodes)</li>
 *   <li>{@code ports} are fixed at construction (defined by the node type)</li>
 *   <li>{@code metadata} is mutable (arbitrary key-value storage)</li>
 * </ul>
 */
public final class Node {

    private final NodeId id;
    private final String typeKey;
    private String displayName;
    private Position position;
    private Size size;
    private final Map<PortId, Port> ports;
    private final List<Port> inputPorts;
    private final List<Port> outputPorts;
    private final Map<String, String> metadata;

    private Node(Builder builder) {
        this.id = builder.id;
        this.typeKey = builder.typeKey;
        this.displayName = builder.displayName;
        this.position = builder.position;
        this.size = builder.size;
        this.metadata = new LinkedHashMap<>(builder.metadata);

        // Build port maps
        Map<PortId, Port> portMap = new LinkedHashMap<>();
        List<Port> inputs = new ArrayList<>();
        List<Port> outputs = new ArrayList<>();
        for (Port port : builder.ports) {
            if (portMap.containsKey(port.id())) {
                throw new IllegalArgumentException(
                        "Duplicate port ID '" + port.id().value() + "' in node '" + id.value() + "'");
            }
            portMap.put(port.id(), port);
            if (port.isInput()) {
                inputs.add(port);
            } else {
                outputs.add(port);
            }
        }
        this.ports = Collections.unmodifiableMap(portMap);
        this.inputPorts = Collections.unmodifiableList(inputs);
        this.outputPorts = Collections.unmodifiableList(outputs);
    }

    // ========== Getters ==========

    /** Returns the unique identifier of this node. */
    public NodeId id() {
        return id;
    }

    /** Returns the type key identifying this node's type (e.g. {@code "nodeforge:heal_player"}). */
    public String typeKey() {
        return typeKey;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the position of this node in graph coordinates. */
    public Position position() {
        return position;
    }

    /** Returns the size of this node. */
    public Size size() {
        return size;
    }

    /**
     * Returns an unmodifiable view of all ports on this node, keyed by {@link PortId}.
     *
     * @return unmodifiable port map
     */
    public Map<PortId, Port> ports() {
        return ports;
    }

    /**
     * Returns the port with the given ID, or {@code null} if not found.
     *
     * @param portId the port identifier
     * @return the port, or {@code null}
     */
    public Port getPort(PortId portId) {
        if (portId == null) return null;
        return ports.get(portId);
    }

    /**
     * Returns {@code true} if this node has a port with the given ID.
     *
     * @param portId the port identifier
     * @return {@code true} if the port exists
     */
    public boolean hasPort(PortId portId) {
        if (portId == null) return false;
        return ports.containsKey(portId);
    }

    /** Returns an unmodifiable list of input ports, in definition order. */
    public List<Port> inputPorts() {
        return inputPorts;
    }

    /** Returns an unmodifiable list of output ports, in definition order. */
    public List<Port> outputPorts() {
        return outputPorts;
    }

    /** Returns the total number of ports on this node. */
    public int portCount() {
        return ports.size();
    }

    /**
     * Returns an unmodifiable view of the metadata map.
     *
     * @return the metadata
     */
    public Map<String, String> metadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Returns the metadata value for the given key, or {@code null}.
     *
     * @param key the metadata key, must not be {@code null}
     * @return the value, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public String getMetadata(String key) {
        Objects.requireNonNull(key, "metadata key must not be null");
        return metadata.get(key);
    }

    // ========== Mutators ==========

    /**
     * Sets the display name.
     *
     * @param displayName the new display name, must not be null or blank
     * @throws NullPointerException     if {@code displayName} is {@code null}
     * @throws IllegalArgumentException if {@code displayName} is blank
     */
    public void setDisplayName(String displayName) {
        Objects.requireNonNull(displayName, "displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.displayName = displayName;
    }

    /**
     * Sets the position of this node.
     *
     * @param position the new position
     * @throws NullPointerException if {@code position} is {@code null}
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    /**
     * Sets the size of this node.
     *
     * @param size the new size
     * @throws NullPointerException if {@code size} is {@code null}
     */
    public void setSize(Size size) {
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    /**
     * Sets a metadata value.
     *
     * @param key   the key, must not be {@code null}
     * @param value the value, must not be {@code null}
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     */
    public void setMetadata(String key, String value) {
        metadata.put(
                Objects.requireNonNull(key, "metadata key must not be null"),
                Objects.requireNonNull(value, "metadata value must not be null")
        );
    }

    /**
     * Removes a metadata entry.
     *
     * @param key the key to remove, must not be {@code null}
     * @return the previous value, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public String removeMetadata(String key) {
        Objects.requireNonNull(key, "metadata key must not be null");
        return metadata.remove(key);
    }

    /**
     * Creates a duplicate of this node with a new identifier and position,
     * preserving its type, display name, size, ports, and metadata.
     *
     * @param newId       the identifier for the new node, must not be null
     * @param newPosition the position for the new node, must not be null
     * @return the cloned node
     */
    public Node copy(NodeId newId, Position newPosition) {
        Objects.requireNonNull(newId, "newId must not be null");
        Objects.requireNonNull(newPosition, "newPosition must not be null");
        Builder b = Node.builder(newId, this.typeKey)
                .displayName(this.displayName)
                .position(newPosition)
                .size(this.size);
        for (Port p : this.ports.values()) {
            b.port(p);
        }
        for (Map.Entry<String, String> entry : this.metadata.entrySet()) {
            b.metadata(entry.getKey(), entry.getValue());
        }
        return b.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Node[" + id.value() + " '" + displayName + "' " + typeKey + "]";
    }

    // ========== Builder ==========

    /**
     * Creates a new builder for constructing a {@link Node}.
     *
     * @param id      the unique node identifier
     * @param typeKey the node type key
     * @return a new builder
     * @throws NullPointerException     if {@code id} or {@code typeKey} is {@code null}
     * @throws IllegalArgumentException if {@code typeKey} is blank
     */
    public static Builder builder(NodeId id, String typeKey) {
        return new Builder(id, typeKey);
    }

    /**
     * Builder for constructing {@link Node} instances.
     *
     * <p>A builder instance can only be built once. Calling {@link #build()} more than
     * once on the same builder will throw {@link IllegalStateException}.
     */
    public static final class Builder {
        private final NodeId id;
        private final String typeKey;
        private String displayName;
        private Position position = Position.ZERO;
        private Size size = Size.DEFAULT;
        private final Set<PortId> portIds = new HashSet<>();
        private final List<Port> ports = new ArrayList<>();
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private boolean built = false;

        private Builder(NodeId id, String typeKey) {
            this.id = Objects.requireNonNull(id, "NodeId must not be null");
            this.typeKey = Objects.requireNonNull(typeKey, "typeKey must not be null");
            if (typeKey.isBlank()) {
                throw new IllegalArgumentException("typeKey must not be blank");
            }
            this.displayName = typeKey; // default display name is the type key
        }

        private void checkNotBuilt() {
            if (built) {
                throw new IllegalStateException("Node.Builder cannot be reused after build()");
            }
        }

        /**
         * Sets the display name.
         *
         * @param displayName the display name
         * @return this builder
         */
        public Builder displayName(String displayName) {
            checkNotBuilt();
            Objects.requireNonNull(displayName, "displayName must not be null");
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("displayName must not be blank");
            }
            this.displayName = displayName;
            return this;
        }

        /** Sets the position. */
        public Builder position(Position position) {
            checkNotBuilt();
            this.position = Objects.requireNonNull(position, "position must not be null");
            return this;
        }

        /** Sets the position using raw coordinates. */
        public Builder position(double x, double y) {
            checkNotBuilt();
            this.position = new Position(x, y);
            return this;
        }

        /** Sets the size. */
        public Builder size(Size size) {
            checkNotBuilt();
            this.size = Objects.requireNonNull(size, "size must not be null");
            return this;
        }

        /** Sets the size using raw dimensions. */
        public Builder size(double width, double height) {
            checkNotBuilt();
            this.size = new Size(width, height);
            return this;
        }

        /**
         * Adds a port to this node.
         *
         * @param port the port to add
         * @return this builder
         * @throws NullPointerException     if {@code port} is {@code null}
         * @throws IllegalArgumentException if a port with the same {@link PortId} has already been added
         */
        public Builder port(Port port) {
            checkNotBuilt();
            Objects.requireNonNull(port, "port must not be null");
            if (!portIds.add(port.id())) {
                throw new IllegalArgumentException(
                        "Duplicate port ID '" + port.id().value() + "' in builder for node '" + id.value() + "'");
            }
            this.ports.add(port);
            return this;
        }

        /** Adds an input port with a string typeKey. */
        public Builder inputPort(String id, String name, String typeKey) {
            return port(Port.input(id, name, typeKey));
        }

        /** Adds an input port with a typed {@link PortType}. */
        public Builder inputPort(String id, String name, PortType<?> portType) {
            return port(Port.input(id, name, portType));
        }

        /** Adds an output port with a string typeKey. */
        public Builder outputPort(String id, String name, String typeKey) {
            return port(Port.output(id, name, typeKey));
        }

        /** Adds an output port with a typed {@link PortType}. */
        public Builder outputPort(String id, String name, PortType<?> portType) {
            return port(Port.output(id, name, portType));
        }

        /** Sets a metadata value. */
        public Builder metadata(String key, String value) {
            checkNotBuilt();
            this.metadata.put(
                    Objects.requireNonNull(key, "metadata key must not be null"),
                    Objects.requireNonNull(value, "metadata value must not be null")
            );
            return this;
        }

        /**
         * Builds the node.
         *
         * @return a new {@link Node}
         * @throws IllegalStateException if this builder has already been used to build a node
         */
        public Node build() {
            checkNotBuilt();
            built = true;
            return new Node(this);
        }
    }
}
