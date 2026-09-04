package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;

/**
 * Command for connecting two ports in the graph.
 */
public class ConnectCommand implements EditorCommand {

    private final Graph graph;
    private final NodeId fromNode;
    private final PortId fromPort;
    private final NodeId toNode;
    private final PortId toPort;
    private ConnectionId createdConnectionId;

    public ConnectCommand(Graph graph, NodeId fromNode, PortId fromPort, NodeId toNode, PortId toPort) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.fromNode = Objects.requireNonNull(fromNode, "fromNode must not be null");
        this.fromPort = Objects.requireNonNull(fromPort, "fromPort must not be null");
        this.toNode = Objects.requireNonNull(toNode, "toNode must not be null");
        this.toPort = Objects.requireNonNull(toPort, "toPort must not be null");
    }

    @Override
    public void execute() {
        ConnectionResult result = graph.connect(fromNode, fromPort, toNode, toPort);
        if (result instanceof ConnectionResult.Success success) {
            this.createdConnectionId = success.connection().id();
        } else if (result instanceof ConnectionResult.Failure failure) {
            throw new IllegalStateException("ConnectCommand failed: " + failure.reason());
        }
    }

    @Override
    public void undo() {
        if (createdConnectionId != null) {
            graph.disconnect(createdConnectionId);
        }
    }

    @Override
    public String description() {
        return "Connect " + fromNode.value() + ":" + fromPort.value() + " -> " + toNode.value() + ":" + toPort.value();
    }

    public ConnectionId getCreatedConnectionId() {
        return createdConnectionId;
    }
}
