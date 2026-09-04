package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;

import java.util.*;

/**
 * Command for deleting selected nodes and connections, preserving all severed connections
 * for complete restoration upon undo.
 */
public class DeleteSelectionCommand implements EditorCommand {

    private final Graph graph;
    private final List<Node> deletedNodes;
    private final List<Connection> deletedConnections;

    public DeleteSelectionCommand(Graph graph, Collection<NodeId> nodeIds, Collection<ConnectionId> connectionIds) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");

        // 1. Collect all nodes to be deleted
        List<Node> nodes = new ArrayList<>();
        Set<NodeId> targetNodeIds = new HashSet<>();
        if (nodeIds != null) {
            for (NodeId id : nodeIds) {
                Node node = graph.getNode(id);
                if (node != null) {
                    nodes.add(node);
                    targetNodeIds.add(id);
                }
            }
        }
        this.deletedNodes = Collections.unmodifiableList(nodes);

        // 2. Collect all connections involving deleted nodes + explicitly deleted connections
        Set<ConnectionId> targetConnIds = new HashSet<>();
        if (connectionIds != null) {
            targetConnIds.addAll(connectionIds);
        }
        for (NodeId nodeId : targetNodeIds) {
            for (Connection conn : graph.getConnectionsForNode(nodeId)) {
                targetConnIds.add(conn.id());
            }
        }

        List<Connection> conns = new ArrayList<>();
        for (ConnectionId connId : targetConnIds) {
            Connection conn = graph.getConnection(connId);
            if (conn != null) {
                conns.add(conn);
            }
        }
        this.deletedConnections = Collections.unmodifiableList(conns);
    }

    @Override
    public void execute() {
        // Remove connections first
        for (Connection conn : deletedConnections) {
            graph.disconnect(conn.id());
        }
        // Remove nodes
        for (Node node : deletedNodes) {
            graph.removeNode(node.id());
        }
    }

    @Override
    public void undo() {
        // Restore nodes first
        for (Node node : deletedNodes) {
            graph.addNode(node);
        }
        // Restore all connections
        for (Connection conn : deletedConnections) {
            graph.connect(conn.fromNode(), conn.fromPort(), conn.toNode(), conn.toPort());
        }
    }

    @Override
    public String description() {
        return "Delete Selection (" + deletedNodes.size() + " nodes, " + deletedConnections.size() + " connections)";
    }

    public List<Node> getDeletedNodes() {
        return deletedNodes;
    }

    public List<Connection> getDeletedConnections() {
        return deletedConnections;
    }
}
