package net.minex.nodeforge.api.runner;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.core.execution.GraphEvaluator;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * High-level ergonomic evaluation facade for running NodeForge graphs.
 *
 * <p>External mod authors can evaluate graphs with a single method call:
 * <pre>{@code
 * ExecutionContext context = new ExecutionContext();
 * context.setVariable("player_level", 42);
 * ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);
 * if (summary.isSuccess()) {
 *     System.out.println("Result: " + context.getVariable("output"));
 * }
 * }</pre>
 */
public final class GraphRunner {

    private GraphRunner() {}

    /**
     * Evaluates a pure data-flow dependency graph in topological order using a fresh context.
     *
     * @param graph the graph to evaluate, must not be null
     * @return execution summary
     */
    public static ExecutionSummary evaluateDataFlow(Graph graph) {
        return evaluateDataFlow(graph, new ExecutionContext());
    }

    /**
     * Evaluates a pure data-flow dependency graph in topological order using the provided context.
     *
     * @param graph   the graph to evaluate, must not be null
     * @param context the runtime execution context, must not be null
     * @return execution summary
     */
    public static ExecutionSummary evaluateDataFlow(Graph graph, ExecutionContext context) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(context, "context must not be null");
        return new GraphEvaluator().evaluateDataFlow(graph, context);
    }

    /**
     * Executes a procedural control-flow graph starting from the specified entry node.
     *
     * @param graph     the graph to execute, must not be null
     * @param startNode the ID of the starting entry node, must not be null
     * @return execution summary
     */
    public static ExecutionSummary executeProcedural(Graph graph, NodeId startNode) {
        return executeProcedural(graph, startNode, new ExecutionContext());
    }

    /**
     * Executes a procedural control-flow graph starting from the specified entry node with a custom context.
     *
     * @param graph     the graph to execute, must not be null
     * @param startNode the ID of the starting entry node, must not be null
     * @param context   the runtime execution context, must not be null
     * @return execution summary
     */
    public static ExecutionSummary executeProcedural(Graph graph, NodeId startNode, ExecutionContext context) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(startNode, "startNode must not be null");
        Objects.requireNonNull(context, "context must not be null");
        return new GraphEvaluator().executeControlFlow(graph, startNode, context);
    }

    /**
     * Evaluates a data-flow graph asynchronously on the default ForkJoin thread pool.
     *
     * @param graph   the graph to evaluate, must not be null
     * @param context the runtime execution context, must not be null
     * @return a future completing with the execution summary
     */
    public static CompletableFuture<ExecutionSummary> evaluateAsync(Graph graph, ExecutionContext context) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(context, "context must not be null");
        return new GraphEvaluator().evaluateDataFlowAsync(graph, context, null);
    }
}
