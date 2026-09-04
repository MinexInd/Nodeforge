package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.core.graph.Graph;

/**
 * Lifecycle callback hooks invoked during graph operations involving a node of this definition.
 */
public interface NodeLifecycleHooks {

    /** Default no-op lifecycle hooks instance. */
    NodeLifecycleHooks EMPTY = new NodeLifecycleHooks() {};

    /**
     * Called when a new instance of this node is created and added to a graph.
     *
     * @param node  the newly added node
     * @param graph the containing graph
     */
    default void onNodeCreated(Node node, Graph graph) {}

    /**
     * Called when this node is removed from a graph.
     *
     * @param node  the removed node
     * @param graph the containing graph
     */
    default void onNodeRemoved(Node node, Graph graph) {}

    /**
     * Called when a connection is formed involving one of this node's ports.
     *
     * @param node       this node
     * @param port       the port that was connected
     * @param connection the new connection
     * @param graph      the containing graph
     */
    default void onConnected(Node node, Port port, Connection connection, Graph graph) {}

    /**
     * Called when a connection involving one of this node's ports is severed.
     *
     * @param node       this node
     * @param port       the port that was disconnected
     * @param connection the removed connection
     * @param graph      the containing graph
     */
    default void onDisconnected(Node node, Port port, Connection connection, Graph graph) {}
}
