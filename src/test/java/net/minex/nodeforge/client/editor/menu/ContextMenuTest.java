package net.minex.nodeforge.client.editor.menu;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.client.editor.command.CommandStack;
import net.minex.nodeforge.client.editor.interaction.NodeCreationPalette;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ContextMenuTest {

    @Test
    @DisplayName("MenuItem executes action and respects enabled condition")
    void testMenuItem() {
        AtomicBoolean actionExecuted = new AtomicBoolean(false);
        MenuItem item = MenuItem.action("Test Action", "Ctrl+T", () -> actionExecuted.set(true));

        assertEquals("Test Action", item.label());
        assertEquals("Ctrl+T", item.shortcut());
        assertFalse(item.isSeparator());
        assertTrue(item.isEnabled());

        item.action().run();
        assertTrue(actionExecuted.get());

        MenuItem disabled = MenuItem.action("Disabled", () -> false, () -> {});
        assertFalse(disabled.isEnabled());

        MenuItem sep = MenuItem.separator();
        assertTrue(sep.isSeparator());
        assertFalse(sep.isEnabled());
    }

    @Test
    @DisplayName("ContextMenu opens, calculates height, and closes")
    void testContextMenuLifecycle() {
        ContextMenu menu = new ContextMenu();
        assertFalse(menu.isOpen());

        List<MenuItem> items = List.of(
                MenuItem.action("Action 1", () -> {}),
                MenuItem.separator(),
                MenuItem.action("Action 2", "Ctrl+X", () -> {})
        );

        menu.open(50.0, 100.0, items);
        assertTrue(menu.isOpen());
        assertEquals(50.0, menu.screenX());
        assertEquals(100.0, menu.screenY());
        assertEquals(3, menu.items().size());
        assertTrue(menu.calculateTotalHeight() > 0);

        menu.close();
        assertFalse(menu.isOpen());
        assertTrue(menu.items().isEmpty());
    }

    @Test
    @DisplayName("ContextMenuFactory builds canvas and node menus")
    void testContextMenuFactory() {
        Graph graph = new Graph("test_graph");
        Node node = Node.builder(NodeId.of("n1"), "math:add").displayName("Add")
                .position(Position.ZERO).size(new Size(100.0, 50.0)).build();
        graph.addNode(node);

        EditorState state = new EditorState(graph);
        CommandStack stack = new CommandStack();
        NodeCreationPalette palette = new NodeCreationPalette();

        List<MenuItem> canvasItems = ContextMenuFactory.createCanvasMenu(
                state, stack, palette, 10.0, 10.0, Position.ZERO, 800, 600
        );
        assertFalse(canvasItems.isEmpty());
        assertTrue(canvasItems.stream().anyMatch(i -> i.label().contains("Add Node")));

        List<MenuItem> nodeItems = ContextMenuFactory.createNodeMenu(state, stack, node.id());
        assertFalse(nodeItems.isEmpty());
        assertTrue(nodeItems.stream().anyMatch(i -> i.label().contains("Duplicate")));
        assertTrue(nodeItems.stream().anyMatch(i -> i.label().contains("Delete Node")));
    }
}
