package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.GraphCycleException;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;

import java.util.*;

/**
 * Computes topological evaluation order for data dependencies in a {@link Graph} using Kahn's algorithm.
 */
public final class TopologicalSorter {

    private TopologicalSorter() {}

    /**
     * Computes a linear topological ordering of nodes based on their data dependencies.
     *
     * <p>Excludes pure execution flow connections to allow procedural loops in control-flow graphs.
     *
     * @param graph the graph to sort
     * @return list of node IDs in topological evaluation order
     * @throws GraphCycleException if an illegal cycle is detected among data dependencies
     */
    public static List<NodeId> sort(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        // 1. Build adjacency list and compute in-degrees for data connections only
        Map<NodeId, Set<NodeId>> downstream = new HashMap<>();
        Map<NodeId, Integer> inDegree = new HashMap<>();

        for (Node node : graph.getNodes()) {
            downstream.put(node.id(), new HashSet<>());
            inDegree.put(node.id(), 0);
        }

        for (Connection conn : graph.getConnections()) {
            Node src = graph.getNode(conn.fromNode());
            if (src == null) continue;
            Port srcPort = src.getPort(conn.fromPort());
            if (srcPort != null && srcPort.isExecution()) {
                // Skip execution flow connections
                continue;
            }

            NodeId from = conn.fromNode();
            NodeId to = conn.toNode();

            if (!from.equals(to)) {
                if (downstream.get(from).add(to)) {
                    inDegree.put(to, inDegree.get(to) + 1);
                }
            }
        }

        // 2. Enqueue all nodes with in-degree == 0
        Queue<NodeId> queue = new ArrayDeque<>();
        for (Map.Entry<NodeId, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<NodeId> order = new ArrayList<>();

        while (!queue.isEmpty()) {
            NodeId current = queue.poll();
            order.add(current);

            for (NodeId neighbor : downstream.get(current)) {
                int newInDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newInDegree);
                if (newInDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // 3. Verify all nodes were sorted (no circular data dependencies)
        if (order.size() < graph.nodeCount()) {
            List<NodeId> cycleNodes = new ArrayList<>();
            for (Map.Entry<NodeId, Integer> entry : inDegree.entrySet()) {
                if (entry.getValue() > 0) {
                    cycleNodes.add(entry.getKey());
                }
            }
            throw new GraphCycleException("Circular data dependency detected involving nodes: " + cycleNodes);
        }

        return Collections.unmodifiableList(order);
    }
}
