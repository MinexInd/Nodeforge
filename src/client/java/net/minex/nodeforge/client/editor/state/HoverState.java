package net.minex.nodeforge.client.editor.state;

import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.Optional;

/**
 * Encapsulates the current element under the mouse pointer in the editor canvas.
 *
 * @param hoveredNode       the node currently under the cursor, or {@code null}
 * @param hoveredPort       the port currently under the cursor, or {@code null}
 * @param hoveredConnection the connection cable currently under the cursor, or {@code null}
 */
public record HoverState(
        NodeId hoveredNode,
        PortId hoveredPort,
        ConnectionId hoveredConnection,
        String hoveredCommentBox
) {

    /** An empty hover state representing no hovered elements. */
    public static final HoverState NONE = new HoverState(null, null, null, null);

    public HoverState(NodeId hoveredNode, PortId hoveredPort, ConnectionId hoveredConnection) {
        this(hoveredNode, hoveredPort, hoveredConnection, null);
    }

    /** Creates a hover state pointing at a node. */
    public static HoverState node(NodeId nodeId) {
        return new HoverState(nodeId, null, null, null);
    }

    /** Creates a hover state pointing at a port on a node. */
    public static HoverState port(NodeId nodeId, PortId portId) {
        return new HoverState(nodeId, portId, null, null);
    }

    /** Creates a hover state pointing at a connection cable. */
    public static HoverState connection(ConnectionId connectionId) {
        return new HoverState(null, null, connectionId, null);
    }

    /** Creates a hover state pointing at a comment box. */
    public static HoverState commentBox(String commentBoxId) {
        return new HoverState(null, null, null, commentBoxId);
    }

    /** Returns {@code true} if anything is currently hovered. */
    public boolean hasHover() {
        return hoveredNode != null || hoveredPort != null || hoveredConnection != null || hoveredCommentBox != null;
    }

    /** Returns {@code true} if the given node is hovered. */
    public boolean isNodeHovered(NodeId nodeId) {
        return nodeId != null && nodeId.equals(hoveredNode);
    }

    /** Returns {@code true} if the given port on the given node is hovered. */
    public boolean isPortHovered(NodeId nodeId, PortId portId) {
        return nodeId != null && portId != null
                && nodeId.equals(hoveredNode) && portId.equals(hoveredPort);
    }

    /** Returns {@code true} if the given connection is hovered. */
    public boolean isConnectionHovered(ConnectionId connectionId) {
        return connectionId != null && connectionId.equals(hoveredConnection);
    }

    /** Returns {@code true} if the given comment box is hovered. */
    public boolean isCommentBoxHovered(String boxId) {
        return boxId != null && boxId.equals(hoveredCommentBox);
    }

    public Optional<NodeId> getNode() {
        return Optional.ofNullable(hoveredNode);
    }

    public Optional<PortId> getPort() {
        return Optional.ofNullable(hoveredPort);
    }

    public Optional<String> getCommentBox() {
        return Optional.ofNullable(hoveredCommentBox);
    }

    public Optional<ConnectionId> getConnection() {
        return Optional.ofNullable(hoveredConnection);
    }
}
