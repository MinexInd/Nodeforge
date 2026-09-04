package net.minex.nodeforge.client.editor.command;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;

import java.util.Map;
import java.util.Objects;

/**
 * Command for batch translating multiple nodes across the canvas.
 */
public class MoveNodesCommand implements EditorCommand {

    private final Graph graph;
    private final Map<NodeId, Position> initialPositions;
    private final Map<NodeId, Position> finalPositions;

    public MoveNodesCommand(Graph graph, Map<NodeId, Position> initialPositions, Map<NodeId, Position> finalPositions) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.initialPositions = Map.copyOf(Objects.requireNonNull(initialPositions, "initialPositions must not be null"));
        this.finalPositions = Map.copyOf(Objects.requireNonNull(finalPositions, "finalPositions must not be null"));
    }

    @Override
    public void execute() {
        for (Map.Entry<NodeId, Position> entry : finalPositions.entrySet()) {
            Node node = graph.getNode(entry.getKey());
            if (node != null) {
                node.setPosition(entry.getValue());
            }
        }
    }

    @Override
    public void undo() {
        for (Map.Entry<NodeId, Position> entry : initialPositions.entrySet()) {
            Node node = graph.getNode(entry.getKey());
            if (node != null) {
                node.setPosition(entry.getValue());
            }
        }
    }

    @Override
    public String description() {
        return "Move " + finalPositions.size() + " Node" + (finalPositions.size() == 1 ? "" : "s");
    }
}
