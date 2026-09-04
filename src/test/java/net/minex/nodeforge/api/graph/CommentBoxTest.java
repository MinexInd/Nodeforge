package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentBoxTest {

    @Test
    @DisplayName("CommentBox basic properties and boundaries")
    void testBasicProperties() {
        CommentBox box = new CommentBox("c1", "My Group", new Position(100.0, 200.0), new Size(300.0, 250.0), 0xFF123456);

        assertEquals("c1", box.id());
        assertEquals("My Group", box.title());
        assertEquals(100.0, box.minX());
        assertEquals(200.0, box.minY());
        assertEquals(400.0, box.maxX());
        assertEquals(450.0, box.maxY());
        assertEquals(0xFF123456, box.color());

        assertTrue(box.contains(new Position(150.0, 250.0)));
        assertFalse(box.contains(new Position(50.0, 50.0)));

        assertTrue(box.isHeaderHit(new Position(150.0, 210.0)));
        assertFalse(box.isHeaderHit(new Position(150.0, 240.0)));
    }

    @Test
    @DisplayName("CommentBox.aroundNodes computes enclosing bounds correctly")
    void testAroundNodes() {
        Node n1 = Node.builder(NodeId.of("n1"), "math:add").displayName("Add")
                .position(new Position(100.0, 100.0)).size(new Size(120.0, 80.0)).build();
        Node n2 = Node.builder(NodeId.of("n2"), "math:sub").displayName("Sub")
                .position(new Position(300.0, 250.0)).size(new Size(100.0, 60.0)).build();

        CommentBox box = CommentBox.aroundNodes("group1", "Math Operations", List.of(n1, n2), 20.0);

        assertEquals("group1", box.id());
        assertEquals("Math Operations", box.title());

        assertTrue(box.encloses(n1));
        assertTrue(box.encloses(n2));

        Node n3 = Node.builder(NodeId.of("n3"), "math:mul").displayName("Mul")
                .position(new Position(0.0, 0.0)).size(new Size(50.0, 50.0)).build();
        assertFalse(box.encloses(n3));
    }

    @Test
    @DisplayName("CommentBox equals and hashCode compare all fields")
    void testEqualsAndHashCode() {
        CommentBox b1 = new CommentBox("c1", "Title", new Position(10.0, 10.0), new Size(100.0, 80.0), 0xFF112233);
        CommentBox b2 = new CommentBox("c1", "Title", new Position(10.0, 10.0), new Size(100.0, 80.0), 0xFF112233);
        CommentBox bDiffTitle = new CommentBox("c1", "Other", new Position(10.0, 10.0), new Size(100.0, 80.0), 0xFF112233);
        CommentBox bDiffPos = new CommentBox("c1", "Title", new Position(20.0, 10.0), new Size(100.0, 80.0), 0xFF112233);
        CommentBox bDiffSize = new CommentBox("c1", "Title", new Position(10.0, 10.0), new Size(200.0, 80.0), 0xFF112233);
        CommentBox bDiffColor = new CommentBox("c1", "Title", new Position(10.0, 10.0), new Size(100.0, 80.0), 0xFF445566);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());

        assertNotEquals(b1, bDiffTitle);
        assertNotEquals(b1, bDiffPos);
        assertNotEquals(b1, bDiffSize);
        assertNotEquals(b1, bDiffColor);
    }
}
