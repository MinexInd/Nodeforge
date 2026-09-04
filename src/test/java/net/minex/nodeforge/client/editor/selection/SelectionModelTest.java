package net.minex.nodeforge.client.editor.selection;

import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SelectionModel")
class SelectionModelTest {

    private SelectionModel selection;

    @BeforeEach
    void setUp() {
        selection = new SelectionModel();
    }

    @Test
    @DisplayName("manages single and multi node selection")
    void nodeSelection() {
        NodeId n1 = NodeId.of("node_1");
        NodeId n2 = NodeId.of("node_2");

        assertTrue(selection.isEmpty());

        assertTrue(selection.selectNode(n1));
        assertFalse(selection.selectNode(n1), "Duplicate select returns false");
        assertEquals(1, selection.selectedNodeCount());
        assertTrue(selection.isSelected(n1));

        assertTrue(selection.selectNode(n2));
        assertEquals(2, selection.selectedNodeCount());

        assertTrue(selection.deselectNode(n1));
        assertFalse(selection.isSelected(n1));
        assertTrue(selection.isSelected(n2));
    }

    @Test
    @DisplayName("toggles node selection state")
    void toggleNode() {
        NodeId n1 = NodeId.of("node_1");

        selection.toggleNode(n1);
        assertTrue(selection.isSelected(n1));

        selection.toggleNode(n1);
        assertFalse(selection.isSelected(n1));
    }

    @Test
    @DisplayName("manages connection selection")
    void connectionSelection() {
        ConnectionId c1 = ConnectionId.of("conn_1");
        ConnectionId c2 = ConnectionId.of("conn_2");

        selection.selectConnection(c1);
        assertEquals(1, selection.selectedConnectionCount());
        assertTrue(selection.isSelected(c1));

        selection.toggleConnection(c2);
        assertTrue(selection.isSelected(c2));

        selection.clearSelection();
        assertTrue(selection.isEmpty());
    }

    @Test
    @DisplayName("setSingleNode replaces all selections")
    void setSingleNode() {
        selection.selectNode(NodeId.of("n1"));
        selection.selectConnection(ConnectionId.of("c1"));

        NodeId target = NodeId.of("target");
        selection.setSingleNode(target);

        assertEquals(1, selection.selectedNodeCount());
        assertEquals(0, selection.selectedConnectionCount());
        assertTrue(selection.isSelected(target));
    }

    @Test
    @DisplayName("dispatches selection change events to listeners")
    void changeListeners() {
        AtomicInteger eventCount = new AtomicInteger(0);
        selection.addSelectionListener(m -> eventCount.incrementAndGet());

        selection.selectNode(NodeId.of("n1"));
        assertEquals(1, eventCount.get());

        selection.selectAllNodes(List.of(NodeId.of("n2"), NodeId.of("n3")));
        assertEquals(2, eventCount.get());

        selection.clearSelection();
        assertEquals(3, eventCount.get());
    }

    @Test
    @DisplayName("manages comment box selection")
    void commentBoxSelection() {
        selection.selectCommentBox("c1");
        assertEquals(1, selection.selectedCommentBoxCount());
        assertTrue(selection.isCommentBoxSelected("c1"));

        selection.toggleCommentBox("c2");
        assertTrue(selection.isCommentBoxSelected("c2"));
        assertEquals(2, selection.selectedCommentBoxCount());

        selection.toggleCommentBox("c1");
        assertFalse(selection.isCommentBoxSelected("c1"));

        selection.setSingleCommentBox("c3");
        assertEquals(1, selection.selectedCommentBoxCount());
        assertTrue(selection.isCommentBoxSelected("c3"));
        assertFalse(selection.isCommentBoxSelected("c2"));

        selection.clearSelection();
        assertTrue(selection.isEmpty());
        assertEquals(0, selection.selectedCommentBoxCount());
    }
}
