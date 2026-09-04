package net.minex.nodeforge.core.graph;

import net.minex.nodeforge.api.graph.*;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.port.TypeCompatibilityEngine;
import net.minex.nodeforge.core.validation.GraphValidator;
import net.minex.nodeforge.core.validation.ValidationError;

import java.util.*;

/**
 * The central graph container. Owns all nodes and connections.
 *
 * <p>All mutation and query operations are synchronized to ensure atomic consistency
 * across concurrent threads (such as background resource compilers or network handlers).
 * The graph enforces:
 * <ul>
 *   <li>Node ID uniqueness</li>
 *   <li>Connection validity (correct nodes, ports, directions, and type compatibility)</li>
 *   <li>Cascading removal (deleting a node removes its connections)</li>
 *   <li>No duplicate connections (indexed for O(1) duplicate checks)</li>
 *   <li>No self-connections</li>
 * </ul>
 */
public class Graph {

    private final String id;
    private final Map<NodeId, Node> nodes = new LinkedHashMap<>();
    private final Map<ConnectionId, Connection> connections = new LinkedHashMap<>();

    // O(1) Spatial and Lookup Indices
    private final Map<ConnectionEndpoints, ConnectionId> connectionEndpointsIndex = new HashMap<>();
    private final Map<NodeId, Set<ConnectionId>> nodeConnectionIndex = new HashMap<>();
    private final Map<PortEndpoint, Set<ConnectionId>> portConnectionIndex = new HashMap<>();

    private final Map<String, String> metadata = new LinkedHashMap<>();
    private final Map<String, net.minex.nodeforge.api.graph.CommentBox> commentBoxes = new LinkedHashMap<>();

    private record ConnectionEndpoints(NodeId fromNode, PortId fromPort, NodeId toNode, PortId toPort) {}
    private record PortEndpoint(NodeId nodeId, PortId portId) {}

    /**
     * Creates a new empty graph with the given identifier.
     *
     * @param id the graph identifier, must not be {@code null} or blank
     * @throws NullPointerException     if {@code id} is {@code null}
     * @throws IllegalArgumentException if {@code id} is blank
     */
    public Graph(String id) {
        Objects.requireNonNull(id, "Graph id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Graph id must not be blank");
        }
        this.id = id;
    }

    // ========== Graph Identity ==========

    /** Returns the graph identifier. */
    public String id() {
        return id;
    }

    // ========== Node Operations ==========

    /**
     * Adds a node to the graph.
     *
     * @param node the node to add
     * @throws NullPointerException     if {@code node} is {@code null}
     * @throws IllegalArgumentException if a node with the same ID already exists
     */
    public synchronized void addNode(Node node) {
        Objects.requireNonNull(node, "Node must not be null");
        if (nodes.containsKey(node.id())) {
            throw new IllegalArgumentException(
                    "Node with ID '" + node.id().value() + "' already exists in graph '" + id + "'");
        }
        nodes.put(node.id(), node);
    }

    /**
     * Removes a node and all its connections from the graph.
     *
     * @param nodeId the ID of the node to remove
     * @return {@code true} if the node was found and removed
     */
    public synchronized boolean removeNode(NodeId nodeId) {
        if (nodeId == null) return false;
        Node removed = nodes.remove(nodeId);
        if (removed == null) {
            return false;
        }

        // Cascade: remove all connections involving this node using the node index
        Set<ConnectionId> associatedConns = nodeConnectionIndex.get(nodeId);
        if (associatedConns != null) {
            List<ConnectionId> toRemove = new ArrayList<>(associatedConns);
            for (ConnectionId connId : toRemove) {
                disconnectInternal(connId);
            }
        }
        nodeConnectionIndex.remove(nodeId);
        return true;
    }

    /**
     * Moves a node to a new position.
     *
     * @param nodeId   the node to move
     * @param position the new position
     * @throws NullPointerException     if {@code nodeId} or {@code position} is {@code null}
     * @throws IllegalArgumentException if the node does not exist
     */
    public synchronized void moveNode(NodeId nodeId, Position position) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(position, "position must not be null");
        Node node = requireNode(nodeId);
        node.setPosition(position);
    }

    /**
     * Returns the node with the given ID, or {@code null} if not found.
     *
     * @param nodeId the node ID
     * @return the node, or {@code null}
     */
    public synchronized Node getNode(NodeId nodeId) {
        if (nodeId == null) return null;
        return nodes.get(nodeId);
    }

    /**
     * Returns {@code true} if the graph contains a node with the given ID.
     *
     * @param nodeId the node ID
     * @return {@code true} if the node exists
     */
    public synchronized boolean hasNode(NodeId nodeId) {
        if (nodeId == null) return false;
        return nodes.containsKey(nodeId);
    }

    /**
     * Returns an unmodifiable snapshot view of all nodes in the graph.
     *
     * @return all nodes
     */
    public synchronized Collection<Node> getNodes() {
        return List.copyOf(nodes.values());
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the node count
     */
    public synchronized int nodeCount() {
        return nodes.size();
    }

    // ========== Connection Operations ==========

    /**
     * Attempts to create a connection between two ports.
     *
     * <p>Validates that:
     * <ul>
     *   <li>Both nodes exist</li>
     *   <li>Both ports exist on their respective nodes</li>
     *   <li>The source port is an output and the target port is an input</li>
     *   <li>The connection is not a self-connection</li>
     *   <li>Port types are compatible according to {@link TypeCompatibilityEngine}</li>
     *   <li>No duplicate connection exists</li>
     * </ul>
     *
     * @param fromNode the source node ID, must not be {@code null}
     * @param fromPort the output port ID on the source node, must not be {@code null}
     * @param toNode   the destination node ID, must not be {@code null}
     * @param toPort   the input port ID on the destination node, must not be {@code null}
     * @return a {@link ConnectionResult} indicating success or the reason for failure
     * @throws NullPointerException if any argument is {@code null}
     */
    public synchronized ConnectionResult connect(NodeId fromNode, PortId fromPort,
                                                 NodeId toNode, PortId toPort) {
        Objects.requireNonNull(fromNode, "fromNode must not be null");
        Objects.requireNonNull(fromPort, "fromPort must not be null");
        Objects.requireNonNull(toNode, "toNode must not be null");
        Objects.requireNonNull(toPort, "toPort must not be null");

        // Validate source node
        Node source = nodes.get(fromNode);
        if (source == null) {
            return new ConnectionResult.Failure(
                    "Source node '" + fromNode.value() + "' not found",
                    ConnectionResult.FailureType.NODE_NOT_FOUND);
        }

        // Validate target node
        Node target = nodes.get(toNode);
        if (target == null) {
            return new ConnectionResult.Failure(
                    "Target node '" + toNode.value() + "' not found",
                    ConnectionResult.FailureType.NODE_NOT_FOUND);
        }

        // Self-connection check
        if (fromNode.equals(toNode)) {
            return new ConnectionResult.Failure(
                    "Self-connections are not allowed",
                    ConnectionResult.FailureType.SELF_CONNECTION);
        }

        // Validate source port
        Port sourcePort = source.getPort(fromPort);
        if (sourcePort == null) {
            return new ConnectionResult.Failure(
                    "Source port '" + fromPort.value() + "' not found on node '" + fromNode.value() + "'",
                    ConnectionResult.FailureType.PORT_NOT_FOUND);
        }

        // Validate target port
        Port targetPort = target.getPort(toPort);
        if (targetPort == null) {
            return new ConnectionResult.Failure(
                    "Target port '" + toPort.value() + "' not found on node '" + toNode.value() + "'",
                    ConnectionResult.FailureType.PORT_NOT_FOUND);
        }

        // Direction check
        if (sourcePort.direction() != PortDirection.OUTPUT) {
            return new ConnectionResult.Failure(
                    "Source port '" + fromPort.value() + "' is not an output port",
                    ConnectionResult.FailureType.WRONG_DIRECTION);
        }
        if (targetPort.direction() != PortDirection.INPUT) {
            return new ConnectionResult.Failure(
                    "Target port '" + toPort.value() + "' is not an input port",
                    ConnectionResult.FailureType.WRONG_DIRECTION);
        }

        // Type compatibility check
        TypeCompatibilityEngine.TypeCheckResult typeCheck =
                TypeCompatibilityEngine.checkCompatibility(sourcePort, targetPort);
        if (!typeCheck.isCompatible()) {
            return new ConnectionResult.Failure(
                    typeCheck.reason(),
                    ConnectionResult.FailureType.INCOMPATIBLE_TYPES);
        }

        // Duplicate check using O(1) endpoints index
        ConnectionEndpoints endpoints = new ConnectionEndpoints(fromNode, fromPort, toNode, toPort);
        if (connectionEndpointsIndex.containsKey(endpoints)) {
            return new ConnectionResult.Failure(
                    "Connection already exists",
                    ConnectionResult.FailureType.DUPLICATE_CONNECTION);
        }

        // All checks passed — create and index connection atomically
        Connection connection = Connection.create(fromNode, fromPort, toNode, toPort);
        addConnectionToIndices(connection);
        return new ConnectionResult.Success(connection);
    }

    private void addConnectionToIndices(Connection connection) {
        ConnectionId id = connection.id();
        connections.put(id, connection);
        connectionEndpointsIndex.put(new ConnectionEndpoints(
                connection.fromNode(), connection.fromPort(), connection.toNode(), connection.toPort()), id);

        nodeConnectionIndex.computeIfAbsent(connection.fromNode(), k -> new HashSet<>()).add(id);
        nodeConnectionIndex.computeIfAbsent(connection.toNode(), k -> new HashSet<>()).add(id);

        portConnectionIndex.computeIfAbsent(new PortEndpoint(connection.fromNode(), connection.fromPort()), k -> new HashSet<>()).add(id);
        portConnectionIndex.computeIfAbsent(new PortEndpoint(connection.toNode(), connection.toPort()), k -> new HashSet<>()).add(id);
    }

    private void removeConnectionFromIndices(Connection connection) {
        ConnectionId id = connection.id();
        connectionEndpointsIndex.remove(new ConnectionEndpoints(
                connection.fromNode(), connection.fromPort(), connection.toNode(), connection.toPort()));

        Set<ConnectionId> fromNodeConns = nodeConnectionIndex.get(connection.fromNode());
        if (fromNodeConns != null) fromNodeConns.remove(id);

        Set<ConnectionId> toNodeConns = nodeConnectionIndex.get(connection.toNode());
        if (toNodeConns != null) toNodeConns.remove(id);

        Set<ConnectionId> fromPortConns = portConnectionIndex.get(new PortEndpoint(connection.fromNode(), connection.fromPort()));
        if (fromPortConns != null) fromPortConns.remove(id);

        Set<ConnectionId> toPortConns = portConnectionIndex.get(new PortEndpoint(connection.toNode(), connection.toPort()));
        if (toPortConns != null) toPortConns.remove(id);
    }

    /**
     * Removes a connection by its ID.
     *
     * @param connectionId the connection ID
     * @return {@code true} if the connection was found and removed
     */
    public synchronized boolean disconnect(ConnectionId connectionId) {
        return disconnectInternal(connectionId);
    }

    private boolean disconnectInternal(ConnectionId connectionId) {
        if (connectionId == null) return false;
        Connection removed = connections.remove(connectionId);
        if (removed != null) {
            removeConnectionFromIndices(removed);
            return true;
        }
        return false;
    }

    /**
     * Removes all connections involving the given node.
     *
     * @param nodeId the node ID
     * @return the number of connections removed
     */
    public synchronized int disconnectAll(NodeId nodeId) {
        if (nodeId == null) return 0;
        Set<ConnectionId> connIds = nodeConnectionIndex.get(nodeId);
        if (connIds == null || connIds.isEmpty()) {
            return 0;
        }
        List<ConnectionId> toRemove = new ArrayList<>(connIds);
        int removedCount = 0;
        for (ConnectionId id : toRemove) {
            if (disconnectInternal(id)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    /**
     * Returns the connection with the given ID, or {@code null} if not found.
     *
     * @param connectionId the connection ID
     * @return the connection, or {@code null}
     */
    public synchronized Connection getConnection(ConnectionId connectionId) {
        if (connectionId == null) return null;
        return connections.get(connectionId);
    }

    /**
     * Returns an unmodifiable snapshot view of all connections in the graph.
     *
     * @return all connections
     */
    public synchronized Collection<Connection> getConnections() {
        return List.copyOf(connections.values());
    }

    /**
     * Returns all connections involving the given node (as source or target).
     *
     * @param nodeId the node ID
     * @return list of connections (may be empty)
     */
    public synchronized List<Connection> getConnectionsForNode(NodeId nodeId) {
        if (nodeId == null) return Collections.emptyList();
        Set<ConnectionId> connIds = nodeConnectionIndex.get(nodeId);
        if (connIds == null || connIds.isEmpty()) return Collections.emptyList();

        List<Connection> result = new ArrayList<>(connIds.size());
        for (ConnectionId id : connIds) {
            Connection conn = connections.get(id);
            if (conn != null) result.add(conn);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all connections involving the given port on the given node.
     *
     * @param nodeId the node ID
     * @param portId the port ID
     * @return list of connections (may be empty)
     */
    public synchronized List<Connection> getConnectionsForPort(NodeId nodeId, PortId portId) {
        if (nodeId == null || portId == null) return Collections.emptyList();
        Set<ConnectionId> connIds = portConnectionIndex.get(new PortEndpoint(nodeId, portId));
        if (connIds == null || connIds.isEmpty()) return Collections.emptyList();

        List<Connection> result = new ArrayList<>(connIds.size());
        for (ConnectionId id : connIds) {
            Connection conn = connections.get(id);
            if (conn != null) result.add(conn);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the number of connections in the graph.
     *
     * @return the connection count
     */
    public synchronized int connectionCount() {
        return connections.size();
    }

    // ========== Metadata ==========

    /**
     * Returns an unmodifiable snapshot view of the graph metadata.
     */
    public synchronized Map<String, String> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the metadata value for the given key, or {@code null}.
     *
     * @param key the metadata key, must not be {@code null}
     * @return the metadata value, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public synchronized String getMetadata(String key) {
        Objects.requireNonNull(key, "metadata key must not be null");
        return metadata.get(key);
    }

    /**
     * Sets a metadata value.
     *
     * @param key   the metadata key, must not be {@code null}
     * @param value the metadata value, must not be {@code null}
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     */
    public synchronized void setMetadata(String key, String value) {
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
    public synchronized String removeMetadata(String key) {
        Objects.requireNonNull(key, "metadata key must not be null");
        return metadata.remove(key);
    }

    // ========== Comment Boxes ==========

    /**
     * Adds a comment box to the graph.
     *
     * @param box the comment box to add, must not be {@code null}
     * @throws NullPointerException     if {@code box} is {@code null}
     * @throws IllegalArgumentException if a comment box with the same ID already exists
     */
    public synchronized void addCommentBox(net.minex.nodeforge.api.graph.CommentBox box) {
        Objects.requireNonNull(box, "comment box must not be null");
        if (commentBoxes.containsKey(box.id())) {
            throw new IllegalArgumentException("Comment box '" + box.id() + "' already exists in graph '" + id + "'");
        }
        commentBoxes.put(box.id(), box);
    }

    /** Retrieves a comment box by its identifier, or {@code null} if not found. */
    public synchronized net.minex.nodeforge.api.graph.CommentBox getCommentBox(String id) {
        return commentBoxes.get(id);
    }

    /**
     * Removes a comment box by its identifier.
     *
     * @param id the identifier of the comment box to remove
     * @return the removed comment box, or {@code null} if not found
     */
    public synchronized net.minex.nodeforge.api.graph.CommentBox removeCommentBox(String id) {
        if (id == null) return null;
        return commentBoxes.remove(id);
    }

    /** Returns an unmodifiable view of all comment boxes currently in the graph. */
    public synchronized List<net.minex.nodeforge.api.graph.CommentBox> getCommentBoxes() {
        return List.copyOf(commentBoxes.values());
    }

    /** Returns the total number of comment boxes in this graph. */
    public synchronized int commentBoxCount() {
        return commentBoxes.size();
    }

    /** Clears all comment boxes from the graph. */
    public synchronized void clearCommentBoxes() {
        commentBoxes.clear();
    }

    // ========== Validation ==========

    /**
     * Validates the graph and returns all discovered issues.
     *
     * @return a list of validation errors (empty if the graph is valid)
     * @see GraphValidator
     */
    public List<ValidationError> validate() {
        return GraphValidator.validate(this);
    }

    // ========== Internal ==========

    private Node requireNode(NodeId nodeId) {
        Node node = nodes.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException(
                    "Node '" + nodeId.value() + "' not found in graph '" + id + "'");
        }
        return node;
    }

    @Override
    public synchronized String toString() {
        return "Graph[" + id + " nodes=" + nodes.size() + " connections=" + connections.size() + "]";
    }
}
