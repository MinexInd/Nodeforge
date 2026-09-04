package net.minex.nodeforge.api.execution;

import net.minex.nodeforge.api.graph.Node;

/**
 * Functional handler responsible for evaluating an individual {@link Node} during graph execution.
 */
@FunctionalInterface
public interface NodeExecutor {

    /**
     * Executes logic for the given node within the provided execution context.
     *
     * @param node    the node being executed
     * @param context the runtime execution context
     * @return the outcome of execution
     */
    ExecutionResult execute(Node node, ExecutionContext context);
}
