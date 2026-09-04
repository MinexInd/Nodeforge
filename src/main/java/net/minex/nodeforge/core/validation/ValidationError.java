package net.minex.nodeforge.core.validation;

import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

/**
 * A validation error or warning discovered during graph validation.
 *
 * @param severity the severity level
 * @param message  a human-readable description of the problem
 * @param nodeId   the related node, or {@code null} for graph-level issues
 * @param portId   the related port, or {@code null} for node-level or graph-level issues
 */
public record ValidationError(
        ValidationSeverity severity,
        String message,
        NodeId nodeId,
        PortId portId
) {

    /**
     * Creates a graph-level error (no node or port context).
     */
    public static ValidationError graphError(ValidationSeverity severity, String message) {
        return new ValidationError(severity, message, null, null);
    }

    /**
     * Creates a node-level error.
     */
    public static ValidationError nodeError(ValidationSeverity severity, String message, NodeId nodeId) {
        return new ValidationError(severity, message, nodeId, null);
    }

    /**
     * Creates a port-level error.
     */
    public static ValidationError portError(ValidationSeverity severity, String message,
                                            NodeId nodeId, PortId portId) {
        return new ValidationError(severity, message, nodeId, portId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(severity).append("] ").append(message);
        if (nodeId != null) {
            sb.append(" (node: ").append(nodeId.value());
            if (portId != null) {
                sb.append(", port: ").append(portId.value());
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
