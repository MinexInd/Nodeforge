package net.minex.nodeforge.client.editor.menu;

import net.minex.nodeforge.api.graph.CommentBox;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.editor.command.AddNodeCommand;
import net.minex.nodeforge.client.editor.command.CommandStack;
import net.minex.nodeforge.client.editor.command.DeleteSelectionCommand;
import net.minex.nodeforge.client.editor.interaction.NodeCreationPalette;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory creating contextual menus for right-click canvas, node, and connection actions.
 */
public final class ContextMenuFactory {

    private ContextMenuFactory() {}

    /** Builds the context menu for empty canvas right-clicks. */
    public static List<MenuItem> createCanvasMenu(
            EditorState state,
            CommandStack stack,
            NodeCreationPalette palette,
            double screenX,
            double screenY,
            Position worldPos,
            int screenW,
            int screenH
    ) {
        List<MenuItem> items = new ArrayList<>();

        items.add(MenuItem.action("Add Node...", "Space", () -> palette.open(screenX, screenY, worldPos)));
        items.add(MenuItem.separator());

        boolean hasSelection = !state.selection().isEmpty();
        items.add(MenuItem.action("Wrap in Comment", "C", () -> hasSelection, () -> {
            List<Node> selectedNodes = state.selection().selectedNodes().stream()
                    .map(id -> state.graph().getNode(id))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            String id = "comment_" + UUID.randomUUID().toString().substring(0, 8);
            CommentBox box = CommentBox.aroundNodes(id, "Group", selectedNodes, 20.0);
            state.graph().addCommentBox(box);
        }));

        items.add(MenuItem.action("Select All", "Ctrl+A", () -> {
            List<NodeId> all = state.graph().getNodes().stream().map(Node::id).toList();
            state.selection().selectAllNodes(all);
        }));

        items.add(MenuItem.action("Clear Selection", () -> hasSelection, () -> state.selection().clearSelection()));
        items.add(MenuItem.separator());

        items.add(MenuItem.action("Frame All", "F", () -> state.frameAll(screenW, screenH, 50.0)));
        items.add(MenuItem.action("Reset Zoom (100%)", () -> state.camera().setZoom(1.0)));

        return items;
    }

    /** Builds the context menu for empty canvas right-clicks with theme selection support. */
    public static List<MenuItem> createCanvasMenu(
            EditorState state,
            CommandStack stack,
            NodeCreationPalette palette,
            double screenX,
            double screenY,
            Position worldPos,
            int screenW,
            int screenH,
            java.util.function.Consumer<net.minex.nodeforge.client.render.theme.ThemeId> onSelectTheme
    ) {
        List<MenuItem> items = createCanvasMenu(state, stack, palette, screenX, screenY, worldPos, screenW, screenH);
        if (onSelectTheme != null) {
            items.add(MenuItem.separator());
            for (net.minex.nodeforge.client.render.theme.ThemeId tid : net.minex.nodeforge.client.render.theme.ThemeRegistry.getInstance().registeredIds()) {
                items.add(MenuItem.action("Theme: " + tid.value(), () -> onSelectTheme.accept(tid)));
            }
        }
        return items;
    }

    /** Builds the context menu when right-clicking a node. */
    public static List<MenuItem> createNodeMenu(
            EditorState state,
            CommandStack stack,
            NodeId nodeId
    ) {
        List<MenuItem> items = new ArrayList<>();
        Node node = state.graph().getNode(nodeId);
        if (node == null) return items;

        items.add(MenuItem.action("Duplicate", "Ctrl+D", () -> {
            Node clone = node.copy(NodeId.random(), node.position().offset(30.0, 30.0));
            stack.execute(new AddNodeCommand(state.graph(), clone));
            state.selection().setSingleNode(clone.id());
        }));

        items.add(MenuItem.action("Delete Node", "Del", () -> {
            stack.execute(new DeleteSelectionCommand(state.graph(), List.of(nodeId), List.of()));
            state.selection().deselectNode(nodeId);
        }));

        items.add(MenuItem.separator());

        items.add(MenuItem.action("Wrap in Comment", "C", () -> {
            String id = "comment_" + UUID.randomUUID().toString().substring(0, 8);
            CommentBox box = CommentBox.aroundNodes(id, node.displayName() + " Group", List.of(node), 20.0);
            state.graph().addCommentBox(box);
        }));

        items.add(MenuItem.action("Disconnect All Cables", () -> {
            List<ConnectionId> conns = state.graph().getConnectionsForNode(nodeId).stream()
                    .map(Connection::id)
                    .toList();
            stack.execute(new DeleteSelectionCommand(state.graph(), List.of(), conns));
        }));

        return items;
    }

    /** Builds the context menu when right-clicking a connection cable. */
    public static List<MenuItem> createConnectionMenu(
            EditorState state,
            CommandStack stack,
            ConnectionId connId
    ) {
        List<MenuItem> items = new ArrayList<>();
        Connection conn = state.graph().getConnection(connId);
        if (conn == null) return items;

        items.add(MenuItem.action("Delete Connection", "Del", () -> {
            stack.execute(new DeleteSelectionCommand(state.graph(), List.of(), List.of(connId)));
        }));

        items.add(MenuItem.action("Select Endpoints", () -> {
            state.selection().clearSelection();
            state.selection().selectNode(conn.fromNode());
            state.selection().selectNode(conn.toNode());
        }));

        return items;
    }
}
