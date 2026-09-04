package net.minex.nodeforge.client.editor.state;

import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.Map;
import java.util.Objects;

/**
 * Models the current user interaction gesture in the node editor canvas.
 */
public sealed interface InteractionState {

    /** Default idle state when no active gesture is being performed. */
    record Idle() implements InteractionState {
        public static final Idle INSTANCE = new Idle();
    }

    /** User is panning the background canvas. */
    record Panning(Position startPan, double startScreenX, double startScreenY) implements InteractionState {
        public Panning {
            Objects.requireNonNull(startPan, "startPan must not be null");
        }
    }

    /** User is dragging one or more selected nodes. */
    record DraggingNodes(
            Map<NodeId, Position> initialPositions,
            double startScreenX,
            double startScreenY
    ) implements InteractionState {
        public DraggingNodes {
            Objects.requireNonNull(initialPositions, "initialPositions must not be null");
            initialPositions = Map.copyOf(initialPositions);
        }
    }

    /** User is dragging a connection cable from a source port. */
    record ConnectingCable(
            NodeId sourceNode,
            PortId sourcePort,
            PortDirection sourceDirection,
            PortType<?> portType,
            Position currentWorldPos
    ) implements InteractionState {
        public ConnectingCable {
            Objects.requireNonNull(sourceNode, "sourceNode must not be null");
            Objects.requireNonNull(sourcePort, "sourcePort must not be null");
            Objects.requireNonNull(sourceDirection, "sourceDirection must not be null");
            Objects.requireNonNull(currentWorldPos, "currentWorldPos must not be null");
        }
    }

    /** User is dragging a marquee box selection rectangle. */
    record BoxSelecting(
            double startScreenX,
            double startScreenY,
            double currentScreenX,
            double currentScreenY
    ) implements InteractionState {}
}
