package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.validation.ValidationError;

import java.util.List;

/**
 * Functional interface for custom domain validation rules evaluated during graph validation.
 *
 * <p>Custom validation rules can enforce domain constraints such as:
 * <ul>
 *   <li>Required input connections must be connected</li>
 *   <li>Property values must satisfy cross-field constraints</li>
 *   <li>Forbidden node combinations or topological restrictions</li>
 * </ul>
 */
@FunctionalInterface
public interface NodeValidationRule {

    /**
     * Validates the given node within the context of its containing graph.
     *
     * @param node  the node to validate
     * @param graph the graph containing the node
     * @return a list of validation errors (or an empty list if valid)
     */
    List<ValidationError> validate(Node node, Graph graph);
}
