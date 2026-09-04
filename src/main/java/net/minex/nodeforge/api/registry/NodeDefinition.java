package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;

/**
 * An immutable blueprint and archetype definition for a node type.
 *
 * <p>A {@code NodeDefinition} specifies the ports, configuration properties,
 * category, display name, validation rules, and lifecycle behavior of a node type.
 * Live {@link Node} instances in a graph are instantiated using {@link #createNode}.
 */
public final class NodeDefinition {

    private final NodeTypeId id;
    private final String displayName;
    private final String description;
    private final NodeCategory category;
    private final String iconPath;
    private final List<PortTemplate> inputPorts;
    private final List<PortTemplate> outputPorts;
    private final Map<String, PropertyDefinition<?>> properties;
    private final List<NodeValidationRule> validationRules;
    private final NodeLifecycleHooks lifecycleHooks;
    private final Size defaultSize;

    private NodeDefinition(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.category = builder.category;
        this.iconPath = builder.iconPath;
        this.inputPorts = List.copyOf(builder.inputPorts);
        this.outputPorts = List.copyOf(builder.outputPorts);
        this.properties = Map.copyOf(builder.properties);
        this.validationRules = List.copyOf(builder.validationRules);
        this.lifecycleHooks = builder.lifecycleHooks != null ? builder.lifecycleHooks : NodeLifecycleHooks.EMPTY;
        this.defaultSize = builder.defaultSize;
    }

    // ========== Getters ==========

    /** Returns the unique identifier of this node definition. */
    public NodeTypeId id() {
        return id;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the node description. */
    public String description() {
        return description;
    }

    /** Returns the category grouping for this node definition. */
    public NodeCategory category() {
        return category;
    }

    /** Returns the optional icon resource path, or empty string. */
    public String iconPath() {
        return iconPath;
    }

    /** Returns the unmodifiable list of input port templates. */
    public List<PortTemplate> inputPorts() {
        return inputPorts;
    }

    /** Returns the unmodifiable list of output port templates. */
    public List<PortTemplate> outputPorts() {
        return outputPorts;
    }

    /** Returns the unmodifiable map of property definitions. */
    public Map<String, PropertyDefinition<?>> properties() {
        return properties;
    }

    /** Returns the unmodifiable list of custom validation rules. */
    public List<NodeValidationRule> validationRules() {
        return validationRules;
    }

    /** Returns the lifecycle callback hooks for this node definition. */
    public NodeLifecycleHooks lifecycleHooks() {
        return lifecycleHooks;
    }

    /** Returns the default dimensions for new node instances. */
    public Size defaultSize() {
        return defaultSize;
    }

    // ========== Node Instantiation ==========

    /**
     * Instantiates an active {@link Node} pre-populated with ports, default property metadata,
     * and default size.
     *
     * @param nodeId   the unique identifier for the new node instance
     * @param position the initial canvas position
     * @return a new configured {@link Node} instance
     */
    public Node createNode(NodeId nodeId, Position position) {
        Objects.requireNonNull(nodeId, "NodeId must not be null");
        Objects.requireNonNull(position, "Position must not be null");

        Node.Builder builder = Node.builder(nodeId, id.value())
                .displayName(displayName)
                .position(position)
                .size(defaultSize);

        // Instantiate input and output ports
        for (PortTemplate inTemplate : inputPorts) {
            builder.port(inTemplate.instantiate());
        }
        for (PortTemplate outTemplate : outputPorts) {
            builder.port(outTemplate.instantiate());
        }

        // Initialize default property metadata
        for (PropertyDefinition<?> prop : properties.values()) {
            builder.metadata(prop.key(), String.valueOf(prop.defaultValue()));
        }

        return builder.build();
    }

    /** Instantiates a new node instance at {@link Position#ZERO}. */
    public Node createNode(NodeId nodeId) {
        return createNode(nodeId, Position.ZERO);
    }

    /** Instantiates a new node instance with a random {@link NodeId} at the given position. */
    public Node createNode(Position position) {
        return createNode(NodeId.random(), position);
    }

    /** Instantiates a new node instance with a random {@link NodeId} at {@link Position#ZERO}. */
    public Node createNode() {
        return createNode(NodeId.random(), Position.ZERO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeDefinition that)) return false;
        return id.equals(that.id) &&
                displayName.equals(that.displayName) &&
                description.equals(that.description) &&
                category.equals(that.category) &&
                iconPath.equals(that.iconPath) &&
                inputPorts.equals(that.inputPorts) &&
                outputPorts.equals(that.outputPorts) &&
                properties.equals(that.properties) &&
                defaultSize.equals(that.defaultSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, category, iconPath, inputPorts, outputPorts, properties, defaultSize);
    }

    @Override
    public String toString() {
        return "NodeDefinition[" + id.value() + " ('" + displayName + "')]";
    }

    // ========== Builder ==========

    /**
     * Creates a builder for a {@link NodeDefinition}.
     *
     * @param id the unique node type identifier
     * @return a new builder
     */
    public static Builder builder(NodeTypeId id) {
        return new Builder(id);
    }

    /**
     * Creates a builder for a {@link NodeDefinition} using string ID.
     *
     * @param idString the node type ID string
     * @return a new builder
     */
    public static Builder builder(String idString) {
        return new Builder(NodeTypeId.of(idString));
    }

    /**
     * Builder for constructing {@link NodeDefinition} instances.
     *
     * <p>A builder instance can only be built once. Calling {@link #build()} more than
     * once on the same builder will throw {@link IllegalStateException}.
     */
    public static final class Builder {
        private final NodeTypeId id;
        private String displayName;
        private String description = "";
        private NodeCategory category = NodeCategory.MISC;
        private String iconPath = "";
        private final Set<PortId> portIds = new HashSet<>();
        private final List<PortTemplate> inputPorts = new ArrayList<>();
        private final List<PortTemplate> outputPorts = new ArrayList<>();
        private final Map<String, PropertyDefinition<?>> properties = new LinkedHashMap<>();
        private final List<NodeValidationRule> validationRules = new ArrayList<>();
        private NodeLifecycleHooks lifecycleHooks = NodeLifecycleHooks.EMPTY;
        private Size defaultSize = Size.DEFAULT;
        private boolean built = false;

        private Builder(NodeTypeId id) {
            this.id = Objects.requireNonNull(id, "NodeTypeId must not be null");
            this.displayName = id.value();
        }

        private void checkNotBuilt() {
            if (built) {
                throw new IllegalStateException("NodeDefinition.Builder cannot be reused after build()");
            }
        }

        /** Sets the human-readable display name. */
        public Builder displayName(String displayName) {
            checkNotBuilt();
            Objects.requireNonNull(displayName, "displayName must not be null");
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("displayName must not be blank");
            }
            this.displayName = displayName;
            return this;
        }

        /** Sets the node description for documentation and tooltips. */
        public Builder description(String description) {
            checkNotBuilt();
            this.description = description == null ? "" : description;
            return this;
        }

        /** Sets the category grouping. */
        public Builder category(NodeCategory category) {
            checkNotBuilt();
            this.category = Objects.requireNonNull(category, "category must not be null");
            return this;
        }

        /** Sets the icon resource path. */
        public Builder icon(String iconPath) {
            checkNotBuilt();
            this.iconPath = iconPath == null ? "" : iconPath;
            return this;
        }

        /** Adds an input port template. */
        public Builder inputPort(PortTemplate template) {
            checkNotBuilt();
            Objects.requireNonNull(template, "PortTemplate must not be null");
            if (!template.isInput()) {
                throw new IllegalArgumentException("PortTemplate is not an input port: " + template);
            }
            if (!portIds.add(template.id())) {
                throw new IllegalArgumentException("Duplicate port ID '" + template.id().value() + "' in node definition '" + id.value() + "'");
            }
            this.inputPorts.add(template);
            return this;
        }

        /** Adds an input port template. */
        public Builder inputPort(String id, String displayName, PortType<?> portType) {
            return inputPort(PortTemplate.input(id, displayName, portType));
        }

        /** Adds a required input port template. */
        public Builder inputPort(String id, String displayName, PortType<?> portType, boolean required) {
            return inputPort(PortTemplate.input(id, displayName, portType, required));
        }

        /** Adds an output port template. */
        public Builder outputPort(PortTemplate template) {
            checkNotBuilt();
            Objects.requireNonNull(template, "PortTemplate must not be null");
            if (!template.isOutput()) {
                throw new IllegalArgumentException("PortTemplate is not an output port: " + template);
            }
            if (!portIds.add(template.id())) {
                throw new IllegalArgumentException("Duplicate port ID '" + template.id().value() + "' in node definition '" + id.value() + "'");
            }
            this.outputPorts.add(template);
            return this;
        }

        /** Adds an output port template. */
        public Builder outputPort(String id, String displayName, PortType<?> portType) {
            return outputPort(PortTemplate.output(id, displayName, portType));
        }

        /** Adds a property definition. */
        public Builder property(PropertyDefinition<?> property) {
            checkNotBuilt();
            Objects.requireNonNull(property, "PropertyDefinition must not be null");
            if (properties.containsKey(property.key())) {
                throw new IllegalArgumentException("Duplicate property key '" + property.key() + "' in node definition '" + id.value() + "'");
            }
            this.properties.put(property.key(), property);
            return this;
        }

        /** Adds a custom validation rule. */
        public Builder validationRule(NodeValidationRule rule) {
            checkNotBuilt();
            this.validationRules.add(Objects.requireNonNull(rule, "NodeValidationRule must not be null"));
            return this;
        }

        /** Sets the lifecycle callback hooks. */
        public Builder lifecycleHooks(NodeLifecycleHooks hooks) {
            checkNotBuilt();
            this.lifecycleHooks = Objects.requireNonNull(hooks, "NodeLifecycleHooks must not be null");
            return this;
        }

        /** Sets the default size for newly created node instances. */
        public Builder defaultSize(Size defaultSize) {
            checkNotBuilt();
            this.defaultSize = Objects.requireNonNull(defaultSize, "defaultSize must not be null");
            return this;
        }

        /** Sets the default size using width and height. */
        public Builder defaultSize(double width, double height) {
            return defaultSize(new Size(width, height));
        }

        /**
         * Builds the immutable {@link NodeDefinition}.
         *
         * @return a new {@link NodeDefinition}
         * @throws IllegalStateException if this builder has already been used
         */
        public NodeDefinition build() {
            checkNotBuilt();
            built = true;
            return new NodeDefinition(this);
        }
    }
}
