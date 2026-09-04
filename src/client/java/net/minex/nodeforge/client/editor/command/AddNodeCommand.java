package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.graph.Graph;

import java.util.Objects;

/**
 * Command for adding a node to a graph.
 */
public class AddNodeCommand implements EditorCommand {

    private final Graph graph;
    private final Node node;

    public AddNodeCommand(Graph graph, Node node) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.node = Objects.requireNonNull(node, "node must not be null");
    }

    @Override
    public void execute() {
        graph.addNode(node);
    }

    @Override
    public void undo() {
        graph.removeNode(node.id());
    }

    @Override
    public String description() {
        return "Add Node '" + node.displayName() + "'";
    }

    public Node getNode() {
        return node;
    }
}
