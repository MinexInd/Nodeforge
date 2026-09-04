package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.api.serialization.GraphSerializer;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphCommentSerializationTest {

    @Test
    @DisplayName("Graph manages CommentBoxes correctly")
    void testGraphCommentManagement() {
        Graph graph = new Graph("test_graph");
        assertEquals(0, graph.commentBoxCount());

        CommentBox box1 = new CommentBox("c1", "Box 1", new Position(10.0, 20.0), new Size(100.0, 80.0));
        graph.addCommentBox(box1);
        assertEquals(1, graph.commentBoxCount());
        assertSame(box1, graph.getCommentBox("c1"));

        // Reject duplicate ID
        assertThrows(IllegalArgumentException.class, () -> graph.addCommentBox(new CommentBox("c1", "Dup", Position.ZERO, new Size(50.0, 50.0))));

        // Remove
        CommentBox removed = graph.removeCommentBox("c1");
        assertSame(box1, removed);
        assertEquals(0, graph.commentBoxCount());
        assertNull(graph.getCommentBox("c1"));
    }

    @Test
    @DisplayName("GraphSerializer persists and restores CommentBoxes")
    void testSerializationRoundTrip() {
        Graph original = new Graph("persisted_graph");
        Node node = Node.builder(NodeId.of("n1"), "test:node").displayName("Node 1")
                .position(new Position(50.0, 50.0)).size(new Size(100.0, 50.0)).build();
        original.addNode(node);

        CommentBox box = new CommentBox("c_box", "Visual Group", new Position(30.0, 20.0), new Size(200.0, 150.0), 0x55AABBCC);
        original.addCommentBox(box);

        GraphSerializer serializer = new GraphSerializer();
        String json = serializer.toJson(original);

        assertTrue(json.contains("\"comments\""));
        assertTrue(json.contains("\"Visual Group\""));
        assertTrue(json.contains("\"c_box\""));

        Graph restored = serializer.fromJson(json);
        assertEquals(1, restored.commentBoxCount());

        CommentBox restoredBox = restored.getCommentBox("c_box");
        assertNotNull(restoredBox);
        assertEquals("Visual Group", restoredBox.title());
        assertEquals(30.0, restoredBox.position().x());
        assertEquals(20.0, restoredBox.position().y());
        assertEquals(200.0, restoredBox.size().width());
        assertEquals(150.0, restoredBox.size().height());
        assertEquals(0x55AABBCC, restoredBox.color());
    }
}
