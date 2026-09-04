package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;

import java.util.Objects;

/**
 * Command for removing a connection from the graph with reversible restoration.
 */
public class DisconnectCommand implements EditorCommand {

    private final Graph graph;
    private final Connection connection;

    public DisconnectCommand(Graph graph, Connection connection) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
    }

    public DisconnectCommand(Graph graph, ConnectionId connectionId) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(connectionId, "connectionId must not be null");
        this.connection = Objects.requireNonNull(graph.getConnection(connectionId),
                "Connection '" + connectionId.value() + "' not found in graph");
    }

    @Override
    public void execute() {
        graph.disconnect(connection.id());
    }

    @Override
    public void undo() {
        graph.connect(connection.fromNode(), connection.fromPort(), connection.toNode(), connection.toPort());
    }

    @Override
    public String description() {
        return "Disconnect " + connection.fromNode().value() + " -> " + connection.toNode().value();
    }

    public Connection getConnection() {
        return connection;
    }
}
