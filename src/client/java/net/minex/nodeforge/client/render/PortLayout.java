package net.minex.nodeforge.client.render;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.core.id.PortId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Calculates geometric positions and hit-testing zones for node port sockets.
 */
public final class PortLayout {

    public static final double HEADER_HEIGHT = 24.0;
    public static final double SOCKET_RADIUS = 5.0;
    public static final double DEFAULT_HIT_RADIUS = 8.0;

    private PortLayout() {}

    /**
     * Computes the world position of the center of a port socket on a node.
     *
     * @param node the node containing the port
     * @param port the port to locate
     * @return the world position of the socket center
     */
    public static Position getPortPosition(Node node, Port port) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(port, "port must not be null");

        double nodeX = node.position().x();
        double nodeY = node.position().y();
        double nodeW = node.size().width();
        double nodeH = node.size().height();

        double bodyHeight = Math.max(20.0, nodeH - HEADER_HEIGHT);

        if (port.isInput()) {
            List<Port> inputs = node.inputPorts();
            int index = inputs.indexOf(port);
            if (index < 0) index = 0;
            int count = Math.max(1, inputs.size());
            double spacing = bodyHeight / (count + 1);
            double y = nodeY + HEADER_HEIGHT + spacing * (index + 1);
            return new Position(nodeX, y);
        } else {
            List<Port> outputs = node.outputPorts();
            int index = outputs.indexOf(port);
            if (index < 0) index = 0;
            int count = Math.max(1, outputs.size());
            double spacing = bodyHeight / (count + 1);
            double y = nodeY + HEADER_HEIGHT + spacing * (index + 1);
            return new Position(nodeX + nodeW, y);
        }
    }

    /**
     * Finds which port socket on a node is under the given world coordinate within a hit radius.
     *
     * @param node      the candidate node
     * @param worldPos  the query position in world space
     * @param hitRadius maximum distance to register a hit
     * @return the hit port, if any
     */
    public static Optional<Port> getPortAt(Node node, Position worldPos, double hitRadius) {
        if (node == null || worldPos == null) return Optional.empty();

        double hitRadiusSq = hitRadius * hitRadius;

        for (Port port : node.ports().values()) {
            Position socketPos = getPortPosition(node, port);
            if (worldPos.distanceSquaredTo(socketPos) <= hitRadiusSq) {
                return Optional.of(port);
            }
        }

        return Optional.empty();
    }

    /**
     * Finds which port socket on a node is under the given world coordinate within default hit radius.
     */
    public static Optional<Port> getPortAt(Node node, Position worldPos) {
        return getPortAt(node, worldPos, DEFAULT_HIT_RADIUS);
    }
}
