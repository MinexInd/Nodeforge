package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;

/**
 * A directed connection between two ports in a graph.
 *
 * <p>Connections flow from an {@link PortDirection#OUTPUT output} port on one node
 * to an {@link PortDirection#INPUT input} port on another node (or, in some cases,
 * the same node if explicitly allowed).
 *
 * <p>Connections are immutable. To "modify" a connection, delete and recreate it.
 *
 * @param id       the unique connection identifier within the graph
 * @param fromNode the source node
 * @param fromPort the output port on the source node
 * @param toNode   the destination node
 * @param toPort   the input port on the destination node
 */
public record Connection(
        ConnectionId id,
        NodeId fromNode,
        PortId fromPort,
        NodeId toNode,
        PortId toPort
) {

    /**
     * Creates a new connection.
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public Connection {
        Objects.requireNonNull(id, "Connection id must not be null");
        Objects.requireNonNull(fromNode, "fromNode must not be null");
        Objects.requireNonNull(fromPort, "fromPort must not be null");
        Objects.requireNonNull(toNode, "toNode must not be null");
        Objects.requireNonNull(toPort, "toPort must not be null");
    }

    /**
     * Creates a connection with an auto-generated ID.
     *
     * @param fromNode the source node
     * @param fromPort the output port
     * @param toNode   the destination node
     * @param toPort   the input port
     * @return a new connection with a random ID
     */
    public static Connection create(NodeId fromNode, PortId fromPort, NodeId toNode, PortId toPort) {
        return new Connection(ConnectionId.random(), fromNode, fromPort, toNode, toPort);
    }

    @Override
    public String toString() {
        return "Connection[" + id.value() + " " +
                fromNode.value() + ":" + fromPort.value() + " → " +
                toNode.value() + ":" + toPort.value() + "]";
    }
}
