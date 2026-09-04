package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Connection")
class ConnectionTest {

    @Test
    @DisplayName("creates connection with all fields")
    void createsConnection() {
        ConnectionId id = ConnectionId.of("c1");
        NodeId n1 = NodeId.of("node1");
        PortId p1 = PortId.of("out");
        NodeId n2 = NodeId.of("node2");
        PortId p2 = PortId.of("in");

        Connection connection = new Connection(id, n1, p1, n2, p2);

        assertEquals(id, connection.id());
        assertEquals(n1, connection.fromNode());
        assertEquals(p1, connection.fromPort());
        assertEquals(n2, connection.toNode());
        assertEquals(p2, connection.toPort());
        assertEquals("Connection[c1 node1:out → node2:in]", connection.toString());
    }

    @Test
    @DisplayName("factory creates random ID")
    void factoryRandomId() {
        Connection c = Connection.create(NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), PortId.of("p2"));
        assertNotNull(c.id());
        assertNotNull(c.id().value());
    }

    @Test
    @DisplayName("rejects null fields")
    void rejectsNull() {
        assertThrows(NullPointerException.class, () ->
                new Connection(null, NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), PortId.of("p2")));
        assertThrows(NullPointerException.class, () ->
                new Connection(ConnectionId.of("c"), null, PortId.of("p1"), NodeId.of("n2"), PortId.of("p2")));
        assertThrows(NullPointerException.class, () ->
                new Connection(ConnectionId.of("c"), NodeId.of("n1"), null, NodeId.of("n2"), PortId.of("p2")));
        assertThrows(NullPointerException.class, () ->
                new Connection(ConnectionId.of("c"), NodeId.of("n1"), PortId.of("p1"), null, PortId.of("p2")));
        assertThrows(NullPointerException.class, () ->
                new Connection(ConnectionId.of("c"), NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), null));
    }

    @Test
    @DisplayName("record equality and hashCode")
    void equalsAndHashCode() {
        Connection c1 = new Connection(ConnectionId.of("c1"), NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), PortId.of("p2"));
        Connection c2 = new Connection(ConnectionId.of("c1"), NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), PortId.of("p2"));
        Connection c3 = new Connection(ConnectionId.of("c2"), NodeId.of("n1"), PortId.of("p1"), NodeId.of("n2"), PortId.of("p2"));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}
