package net.minex.nodeforge.core.validation;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.registry.NodeValidationRule;
import net.minex.nodeforge.api.registry.PortTemplate;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.port.TypeCompatibilityEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates graph integrity, structural correctness, type safety, and node definition rules.
 *
 * <p>Checks performed:
 * <ul>
 *   <li>No connections reference missing nodes or ports</li>
 *   <li>Connection direction is correct (output → input)</li>
 *   <li>Port data types are compatible according to {@link TypeCompatibilityEngine}</li>
 *   <li>No duplicate connections or self-connections</li>
 *   <li>Required input ports defined on {@link NodeDefinition}s have connections</li>
 *   <li>Custom {@link NodeValidationRule}s registered on {@link NodeDefinition}s pass</li>
 * </ul>
 */
public final class GraphValidator {

    private GraphValidator() {
        // static utility
    }

    /**
     * Validates the given graph and returns all discovered issues using the global definition registry.
     *
     * @param graph the graph to validate
     * @return a list of validation errors (empty if the graph is valid)
     */
    public static List<ValidationError> validate(Graph graph) {
        return validate(graph, NodeDefinitionRegistry.getInstance());
    }

    /**
     * Validates the given graph using a specific {@link NodeDefinitionRegistry}.
     *
     * @param graph    the graph to validate
     * @param registry the node definition registry
     * @return a list of validation errors
     */
    public static List<ValidationError> validate(Graph graph, NodeDefinitionRegistry registry) {
        List<ValidationError> errors = new ArrayList<>();

        // 1. Connection-level validation
        for (Connection conn : graph.getConnections()) {
            validateConnection(graph, conn, errors);
        }

        // 2. Node-level & Definition validation
        for (Node node : graph.getNodes()) {
            validateNode(graph, node, registry, errors);
        }

        return errors;
    }

    private static void validateConnection(Graph graph, Connection conn, List<ValidationError> errors) {
        // Check source node exists
        Node fromNode = graph.getNode(conn.fromNode());
        if (fromNode == null) {
            errors.add(ValidationError.graphError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' references missing source node '" +
                            conn.fromNode().value() + "'"));
            return;
        }

        // Check target node exists
        Node toNode = graph.getNode(conn.toNode());
        if (toNode == null) {
            errors.add(ValidationError.graphError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' references missing target node '" +
                            conn.toNode().value() + "'"));
            return;
        }

        // Check self-connection
        if (conn.fromNode().equals(conn.toNode())) {
            errors.add(ValidationError.nodeError(
                    ValidationSeverity.WARNING,
                    "Connection '" + conn.id().value() + "' is a self-connection",
                    conn.fromNode()));
        }

        // Check source port exists
        Port fromPort = fromNode.getPort(conn.fromPort());
        if (fromPort == null) {
            errors.add(ValidationError.portError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' references missing source port '" +
                            conn.fromPort().value() + "'",
                    conn.fromNode(), conn.fromPort()));
            return;
        }

        // Check target port exists
        Port toPort = toNode.getPort(conn.toPort());
        if (toPort == null) {
            errors.add(ValidationError.portError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' references missing target port '" +
                            conn.toPort().value() + "'",
                    conn.toNode(), conn.toPort()));
            return;
        }

        // Check direction: from should be OUTPUT, to should be INPUT
        if (fromPort.direction() != PortDirection.OUTPUT) {
            errors.add(ValidationError.portError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' source port '" +
                            fromPort.id().value() + "' is not an output port",
                    conn.fromNode(), conn.fromPort()));
        }

        if (toPort.direction() != PortDirection.INPUT) {
            errors.add(ValidationError.portError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' target port '" +
                            toPort.id().value() + "' is not an input port",
                    conn.toNode(), conn.toPort()));
        }

        // Check type compatibility
        TypeCompatibilityEngine.TypeCheckResult typeCheck = TypeCompatibilityEngine.checkCompatibility(fromPort, toPort);
        if (!typeCheck.isCompatible()) {
            errors.add(ValidationError.portError(
                    ValidationSeverity.ERROR,
                    "Connection '" + conn.id().value() + "' has incompatible types: " + typeCheck.reason(),
                    conn.toNode(), conn.toPort()));
        }
    }

    private static void validateNode(Graph graph, Node node, NodeDefinitionRegistry registry, List<ValidationError> errors) {
        if (registry == null) return;
        NodeDefinition def = registry.get(node.typeKey());
        if (def == null) return;

        // Check required input ports
        for (PortTemplate template : def.inputPorts()) {
            if (template.required()) {
                List<Connection> incoming = graph.getConnectionsForPort(node.id(), template.id());
                if (incoming.isEmpty()) {
                    errors.add(ValidationError.portError(
                            ValidationSeverity.ERROR,
                            "Required input port '" + template.id().value() + "' has no incoming connection",
                            node.id(), template.id()));
                }
            }
        }

        // Execute custom node validation rules with fault isolation
        for (NodeValidationRule rule : def.validationRules()) {
            try {
                List<ValidationError> customErrors = rule.validate(node, graph);
                if (customErrors != null && !customErrors.isEmpty()) {
                    errors.addAll(customErrors);
                }
            } catch (VirtualMachineError t) {
                throw t;
            } catch (Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                errors.add(ValidationError.nodeError(
                        ValidationSeverity.ERROR,
                        "Validation rule threw unexpected exception on node '" + node.id().value() + "': " + msg,
                        node.id()));
            }
        }
    }
}
