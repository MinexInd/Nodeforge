package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.*;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Core engine for evaluating data-flow expressions, executing procedural logic graphs,
 * and running asynchronous graph workflows.
 */
public class GraphEvaluator {

    private final NodeExecutorRegistry registry;

    public GraphEvaluator(NodeExecutorRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public GraphEvaluator() {
        this(NodeExecutorRegistry.getInstance());
        BuiltinExecutors.registerAll(this.registry);
    }

    // ========== Data-Flow Evaluation ==========

    /**
     * Evaluates a pure data-flow dependency graph in topological order.
     *
     * @param graph   the graph to evaluate
     * @param context the runtime execution context
     * @return execution summary report
     */
    public ExecutionSummary evaluateDataFlow(Graph graph, ExecutionContext context) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(context, "context must not be null");

        long startTime = System.nanoTime();

        try {
            List<NodeId> order = TopologicalSorter.sort(graph);

            for (NodeId nodeId : order) {
                if (context.isCancelled()) {
                    return ExecutionSummary.cancelled(context.stepCount(), System.nanoTime() - startTime, context.variables());
                }

                context.incrementAndCheckSteps();
                Node node = graph.getNode(nodeId);
                if (node == null) continue;

                NodeExecutor executor = registry.get(node.typeKey());
                if (executor != null) {
                    ExecutionResult result = executor.execute(node, context);
                    if (result instanceof ExecutionResult.Failure failure) {
                        return ExecutionSummary.failure(failure.errorMessage(), context.stepCount(),
                                System.nanoTime() - startTime, context.variables());
                    } else if (result instanceof ExecutionResult.Halt) {
                        break;
                    }
                }

                // Propagate output port values along outgoing data connections
                propagateOutputs(graph, node, context);
            }

            return ExecutionSummary.success(context.stepCount(), System.nanoTime() - startTime, context.variables());

        } catch (VirtualMachineError e) {
            throw e;
        } catch (Throwable e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ExecutionSummary.failure(msg, context.stepCount(),
                    System.nanoTime() - startTime, context.variables());
        }
    }

    // ========== Control-Flow Pulse Execution ==========

    /**
     * Executes procedural control flow starting from an entrypoint node.
     *
     * @param graph       the graph to execute
     * @param entryNodeId ID of the starting node
     * @param context     the runtime execution context
     * @return execution summary report
     */
    public ExecutionSummary executeControlFlow(Graph graph, NodeId entryNodeId, ExecutionContext context) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(entryNodeId, "entryNodeId must not be null");
        Objects.requireNonNull(context, "context must not be null");

        long startTime = System.nanoTime();
        NodeId currentNodeId = entryNodeId;

        try {
            while (currentNodeId != null) {
                if (context.isCancelled()) {
                    return ExecutionSummary.cancelled(context.stepCount(), System.nanoTime() - startTime, context.variables());
                }

                context.incrementAndCheckSteps();
                Node node = graph.getNode(currentNodeId);
                if (node == null) {
                    return ExecutionSummary.failure("Node '" + currentNodeId.value() + "' not found in graph",
                            context.stepCount(), System.nanoTime() - startTime, context.variables());
                }

                // Evaluate upstream data dependencies for this node before executing
                evaluateUpstreamData(graph, node, context);

                NodeExecutor executor = registry.get(node.typeKey());
                ExecutionResult result = executor != null ? executor.execute(node, context) : ExecutionResult.Success.of();

                if (result instanceof ExecutionResult.Failure failure) {
                    return ExecutionSummary.failure(failure.errorMessage(), context.stepCount(),
                            System.nanoTime() - startTime, context.variables());
                } else if (result instanceof ExecutionResult.Halt) {
                    break;
                }

                // Propagate emitted data outputs
                propagateOutputs(graph, node, context);

                // Determine next control-flow node
                if (result instanceof ExecutionResult.Success success && success.nextFlowPort().isPresent()) {
                    PortId flowPort = success.nextFlowPort().get();
                    currentNodeId = findNextFlowNode(graph, node.id(), flowPort);
                } else {
                    // Look for default "exec_out" or "out" execution port connection
                    currentNodeId = findNextFlowNode(graph, node.id(), PortId.of("exec_out"));
                    if (currentNodeId == null) {
                        currentNodeId = findNextFlowNode(graph, node.id(), PortId.of("out"));
                    }
                }
            }

            return ExecutionSummary.success(context.stepCount(), System.nanoTime() - startTime, context.variables());

        } catch (VirtualMachineError e) {
            throw e;
        } catch (Throwable e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ExecutionSummary.failure(msg, context.stepCount(),
                    System.nanoTime() - startTime, context.variables());
        }
    }

    // ========== Async Execution ==========

    /**
     * Executes data-flow asynchronously on the specified executor.
     */
    public CompletableFuture<ExecutionSummary> evaluateDataFlowAsync(Graph graph, ExecutionContext context, Executor executor) {
        return CompletableFuture.supplyAsync(() -> evaluateDataFlow(graph, context), executor != null ? executor : ForkJoinPool.commonPool());
    }

    /**
     * Executes control-flow asynchronously on the specified executor.
     */
    public CompletableFuture<ExecutionSummary> executeControlFlowAsync(Graph graph, NodeId entryNodeId, ExecutionContext context, Executor executor) {
        return CompletableFuture.supplyAsync(() -> executeControlFlow(graph, entryNodeId, context), executor != null ? executor : ForkJoinPool.commonPool());
    }

    // ========== Internal Helpers ==========

    private void propagateOutputs(Graph graph, Node node, ExecutionContext context) {
        for (Port outPort : node.outputPorts()) {
            if (outPort.isExecution()) continue; // Skip execution connections
            Object outVal = context.getOutputValue(node.id(), outPort.id());
            if (outVal != null) {
                for (Connection conn : graph.getConnectionsForPort(node.id(), outPort.id())) {
                    if (conn.fromNode().equals(node.id()) && conn.fromPort().equals(outPort.id())) {
                        context.setInputValue(conn.toNode(), conn.toPort(), outVal);
                    }
                }
            }
        }
    }

    private void evaluateUpstreamData(Graph graph, Node targetNode, ExecutionContext context) {
        // Collect evaluation order using iterative DFS post-order traversal to eliminate recursion stack limits
        List<NodeId> order = new ArrayList<>();
        Set<NodeId> visited = new HashSet<>();
        Set<NodeId> inStack = new HashSet<>();
        Deque<Iterator<NodeId>> stack = new ArrayDeque<>();
        Deque<NodeId> path = new ArrayDeque<>();

        path.push(targetNode.id());
        inStack.add(targetNode.id());
        stack.push(getUpstreamDataNeighbors(graph, targetNode, context).iterator());

        while (!stack.isEmpty()) {
            Iterator<NodeId> currentNeighbors = stack.peek();
            if (currentNeighbors.hasNext()) {
                NodeId next = currentNeighbors.next();
                if (inStack.contains(next)) {
                    throw new GraphCycleException("Cyclic upstream data dependency detected involving node '" + next.value() + "'");
                }
                if (visited.add(next)) {
                    Node nextNode = graph.getNode(next);
                    if (nextNode != null) {
                        path.push(next);
                        inStack.add(next);
                        stack.push(getUpstreamDataNeighbors(graph, nextNode, context).iterator());
                    }
                }
            } else {
                stack.pop();
                NodeId finished = path.pop();
                inStack.remove(finished);
                if (!finished.equals(targetNode.id())) {
                    order.add(finished);
                }
            }
        }

        // Now evaluate each uncomputed upstream node in topological dependency order
        for (NodeId nodeId : order) {
            Node upstreamNode = graph.getNode(nodeId);
            if (upstreamNode != null) {
                NodeExecutor srcExec = registry.get(upstreamNode.typeKey());
                if (srcExec != null) {
                    srcExec.execute(upstreamNode, context);
                }
                propagateOutputs(graph, upstreamNode, context);
            }
        }
    }

    private List<NodeId> getUpstreamDataNeighbors(Graph graph, Node node, ExecutionContext context) {
        List<NodeId> upstream = new ArrayList<>();
        for (Port inPort : node.inputPorts()) {
            if (inPort.isExecution()) continue;
            for (Connection conn : graph.getConnectionsForPort(node.id(), inPort.id())) {
                if (conn.toNode().equals(node.id()) && conn.toPort().equals(inPort.id())) {
                    Node srcNode = graph.getNode(conn.fromNode());
                    if (srcNode != null && context.getOutputValue(srcNode.id(), conn.fromPort()) == null) {
                        upstream.add(srcNode.id());
                    }
                }
            }
        }
        return upstream;
    }

    private NodeId findNextFlowNode(Graph graph, NodeId fromNode, PortId fromPort) {
        for (Connection conn : graph.getConnectionsForPort(fromNode, fromPort)) {
            if (conn.fromNode().equals(fromNode) && conn.fromPort().equals(fromPort)) {
                return conn.toNode();
            }
        }
        return null;
    }
}
