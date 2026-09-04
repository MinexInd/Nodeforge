package net.minex.nodeforge.core.graph;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Graph")
class GraphTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph("test_graph");
    }

    private Node createTestNode(String idStr, String inPort, String outPort) {
        Node.Builder builder = Node.builder(NodeId.of(idStr), "test:node");
        if (inPort != null) {
            builder.inputPort(inPort, "In", "data");
        }
        if (outPort != null) {
            builder.outputPort(outPort, "Out", "data");
        }
        return builder.build();
    }

    @Nested
    @DisplayName("Node Management")
    class NodeManagement {
        @Test
        @DisplayName("adds and retrieves nodes")
        void addAndGetNode() {
            Node node = createTestNode("node_1", "in", "out");
            graph.addNode(node);

            assertEquals(1, graph.nodeCount());
            assertTrue(graph.hasNode(NodeId.of("node_1")));
            assertSame(node, graph.getNode(NodeId.of("node_1")));
            assertNull(graph.getNode(NodeId.of("missing")));
            assertFalse(graph.hasNode(NodeId.of("missing")));
            assertNull(graph.getNode(null));
            assertFalse(graph.hasNode(null));
        }

        @Test
        @DisplayName("rejects duplicate node IDs")
        void rejectsDuplicateNode() {
            Node n1 = createTestNode("dup", "in", "out");
            Node n2 = createTestNode("dup", "in2", "out2");

            graph.addNode(n1);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> graph.addNode(n2));
            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("removes existing and nonexistent nodes")
        void removeNode() {
            Node node = createTestNode("n1", "in", "out");
            graph.addNode(node);

            assertTrue(graph.removeNode(NodeId.of("n1")));
            assertEquals(0, graph.nodeCount());
            assertFalse(graph.hasNode(NodeId.of("n1")));

            assertFalse(graph.removeNode(NodeId.of("n1")), "Removing again should return false");
            assertFalse(graph.removeNode(NodeId.of("unknown")));
            assertFalse(graph.removeNode(null));
        }

        @Test
        @DisplayName("moves existing node and throws for missing node or nulls")
        void moveNode() {
            Node node = createTestNode("movable", "in", "out");
            graph.addNode(node);

            graph.moveNode(NodeId.of("movable"), new Position(42, 84));
            assertEquals(new Position(42, 84), node.position());

            assertThrows(IllegalArgumentException.class, () ->
                    graph.moveNode(NodeId.of("nonexistent"), new Position(0, 0)));
            assertThrows(NullPointerException.class, () ->
                    graph.moveNode(null, new Position(0, 0)));
            assertThrows(NullPointerException.class, () ->
                    graph.moveNode(NodeId.of("movable"), null));
        }

        @Test
        @DisplayName("getNodes returns unmodifiable view")
        void unmodifiableNodesView() {
            Node node = createTestNode("n1", "in", "out");
            graph.addNode(node);

            assertThrows(UnsupportedOperationException.class, () ->
                    graph.getNodes().clear());
        }
    }

    @Nested
    @DisplayName("Connection Management")
    class ConnectionManagement {
        private Node srcNode;
        private Node dstNode;

        @BeforeEach
        void setupNodes() {
            srcNode = createTestNode("src", "in1", "out1");
            dstNode = createTestNode("dst", "in2", "out2");
            graph.addNode(srcNode);
            graph.addNode(dstNode);
        }

        @Test
        @DisplayName("connects valid output to input ports")
        void successfulConnect() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("in2")
            );

            assertInstanceOf(ConnectionResult.Success.class, result);
            assertTrue(result.isSuccess());

            Connection connection = ((ConnectionResult.Success) result).connection();
            assertNotNull(connection);
            assertEquals(NodeId.of("src"), connection.fromNode());
            assertEquals(PortId.of("out1"), connection.fromPort());
            assertEquals(NodeId.of("dst"), connection.toNode());
            assertEquals(PortId.of("in2"), connection.toPort());

            assertEquals(1, graph.connectionCount());
            assertSame(connection, graph.getConnection(connection.id()));
        }

        @Test
        @DisplayName("rejects null arguments with descriptive NPE in connect")
        void nullArgumentsConnect() {
            NullPointerException ex1 = assertThrows(NullPointerException.class, () ->
                    graph.connect(null, PortId.of("out1"), NodeId.of("dst"), PortId.of("in2")));
            assertTrue(ex1.getMessage().contains("fromNode"));

            NullPointerException ex2 = assertThrows(NullPointerException.class, () ->
                    graph.connect(NodeId.of("src"), null, NodeId.of("dst"), PortId.of("in2")));
            assertTrue(ex2.getMessage().contains("fromPort"));

            NullPointerException ex3 = assertThrows(NullPointerException.class, () ->
                    graph.connect(NodeId.of("src"), PortId.of("out1"), null, PortId.of("in2")));
            assertTrue(ex3.getMessage().contains("toNode"));

            NullPointerException ex4 = assertThrows(NullPointerException.class, () ->
                    graph.connect(NodeId.of("src"), PortId.of("out1"), NodeId.of("dst"), null));
            assertTrue(ex4.getMessage().contains("toPort"));
        }

        @Test
        @DisplayName("fails when source node missing")
        void missingSourceNode() {
            ConnectionResult result = graph.connect(
                    NodeId.of("missing_src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("in2")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            assertFalse(result.isSuccess());
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.NODE_NOT_FOUND, failure.type());
            assertTrue(failure.reason().contains("Source node"));
        }

        @Test
        @DisplayName("fails when target node missing")
        void missingTargetNode() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("missing_dst"), PortId.of("in2")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.NODE_NOT_FOUND, failure.type());
            assertTrue(failure.reason().contains("Target node"));
        }

        @Test
        @DisplayName("fails on self-connection")
        void selfConnection() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("src"), PortId.of("in1")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.SELF_CONNECTION, failure.type());
        }

        @Test
        @DisplayName("fails when source port missing")
        void missingSourcePort() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("no_such_port"),
                    NodeId.of("dst"), PortId.of("in2")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.PORT_NOT_FOUND, failure.type());
        }

        @Test
        @DisplayName("fails when target port missing")
        void missingTargetPort() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("no_such_port")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.PORT_NOT_FOUND, failure.type());
        }

        @Test
        @DisplayName("fails when source port is an INPUT")
        void wrongDirectionSource() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("in1"),
                    NodeId.of("dst"), PortId.of("in2")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.WRONG_DIRECTION, failure.type());
        }

        @Test
        @DisplayName("fails when target port is an OUTPUT")
        void wrongDirectionTarget() {
            ConnectionResult result = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("out2")
            );

            assertInstanceOf(ConnectionResult.Failure.class, result);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) result;
            assertEquals(ConnectionResult.FailureType.WRONG_DIRECTION, failure.type());
        }

        @Test
        @DisplayName("fails on duplicate connection")
        void duplicateConnection() {
            ConnectionResult r1 = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("in2")
            );
            assertTrue(r1.isSuccess());

            ConnectionResult r2 = graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("in2")
            );
            assertInstanceOf(ConnectionResult.Failure.class, r2);
            ConnectionResult.Failure failure = (ConnectionResult.Failure) r2;
            assertEquals(ConnectionResult.FailureType.DUPLICATE_CONNECTION, failure.type());
            assertEquals(1, graph.connectionCount());
        }

        @Test
        @DisplayName("disconnect by ConnectionId")
        void disconnectById() {
            ConnectionResult.Success success = (ConnectionResult.Success) graph.connect(
                    NodeId.of("src"), PortId.of("out1"),
                    NodeId.of("dst"), PortId.of("in2")
            );

            assertTrue(graph.disconnect(success.connection().id()));
            assertEquals(0, graph.connectionCount());
            assertNull(graph.getConnection(success.connection().id()));

            assertFalse(graph.disconnect(success.connection().id()));
            assertFalse(graph.disconnect(ConnectionId.of("fake")));
            assertFalse(graph.disconnect(null));
        }

        @Test
        @DisplayName("disconnectAll removes all connections for a node")
        void disconnectAll() {
            Node third = createTestNode("third", "in3", "out3");
            graph.addNode(third);

            graph.connect(NodeId.of("src"), PortId.of("out1"), NodeId.of("dst"), PortId.of("in2"));
            graph.connect(NodeId.of("third"), PortId.of("out3"), NodeId.of("dst"), PortId.of("in2"));
            assertEquals(2, graph.connectionCount());

            int removed = graph.disconnectAll(NodeId.of("dst"));
            assertEquals(2, removed);
            assertEquals(0, graph.connectionCount());
            assertEquals(0, graph.disconnectAll(null));
        }

        @Test
        @DisplayName("cascading removal: deleting a node removes its connections")
        void cascadingNodeRemoval() {
            Node n3 = createTestNode("n3", "in3", "out3");
            graph.addNode(n3);

            graph.connect(NodeId.of("src"), PortId.of("out1"), NodeId.of("dst"), PortId.of("in2"));
            graph.connect(NodeId.of("dst"), PortId.of("out2"), NodeId.of("n3"), PortId.of("in3"));
            assertEquals(2, graph.connectionCount());

            // Remove dst node (has 1 incoming and 1 outgoing)
            graph.removeNode(NodeId.of("dst"));
            assertEquals(0, graph.connectionCount());
        }
    }

    @Nested
    @DisplayName("Type System Integration")
    class TypeSystemIntegration {
        @Test
        @DisplayName("connects compatible typed ports")
        void compatibleTypedPorts() {
            Node n1 = Node.builder(NodeId.of("typed_src"), "nodeforge:math")
                    .outputPort("int_out", "Int Out", BuiltinPortTypes.INTEGER)
                    .outputPort("exec_out", "Exec Out", BuiltinPortTypes.EXECUTION)
                    .build();

            Node n2 = Node.builder(NodeId.of("typed_dst"), "nodeforge:math")
                    .inputPort("float_in", "Float In", BuiltinPortTypes.FLOAT)
                    .inputPort("exec_in", "Exec In", BuiltinPortTypes.EXECUTION)
                    .build();

            graph.addNode(n1);
            graph.addNode(n2);

            // Integer -> Float widening
            ConnectionResult r1 = graph.connect(
                    NodeId.of("typed_src"), PortId.of("int_out"),
                    NodeId.of("typed_dst"), PortId.of("float_in"));
            assertTrue(r1.isSuccess());

            // Execution -> Execution
            ConnectionResult r2 = graph.connect(
                    NodeId.of("typed_src"), PortId.of("exec_out"),
                    NodeId.of("typed_dst"), PortId.of("exec_in"));
            assertTrue(r2.isSuccess());
        }

        @Test
        @DisplayName("fails when connecting incompatible port types")
        void incompatibleTypedPorts() {
            Node n1 = Node.builder(NodeId.of("src_incomp"), "nodeforge:test")
                    .outputPort("str_out", "String Out", BuiltinPortTypes.STRING)
                    .outputPort("exec_out", "Exec Out", BuiltinPortTypes.EXECUTION)
                    .build();

            Node n2 = Node.builder(NodeId.of("dst_incomp"), "nodeforge:test")
                    .inputPort("int_in", "Int In", BuiltinPortTypes.INTEGER)
                    .build();

            graph.addNode(n1);
            graph.addNode(n2);

            // String -> Integer: Incompatible
            ConnectionResult r1 = graph.connect(
                    NodeId.of("src_incomp"), PortId.of("str_out"),
                    NodeId.of("dst_incomp"), PortId.of("int_in"));
            assertInstanceOf(ConnectionResult.Failure.class, r1);
            ConnectionResult.Failure f1 = (ConnectionResult.Failure) r1;
            assertEquals(ConnectionResult.FailureType.INCOMPATIBLE_TYPES, f1.type());
            assertTrue(f1.reason().contains("incompatible"));

            // Exec -> Integer: Incompatible
            ConnectionResult r2 = graph.connect(
                    NodeId.of("src_incomp"), PortId.of("exec_out"),
                    NodeId.of("dst_incomp"), PortId.of("int_in"));
            assertInstanceOf(ConnectionResult.Failure.class, r2);
            ConnectionResult.Failure f2 = (ConnectionResult.Failure) r2;
            assertEquals(ConnectionResult.FailureType.INCOMPATIBLE_TYPES, f2.type());
            assertTrue(f2.reason().contains("execution flow port"));
        }
    }

    @Nested
    @DisplayName("Connection Queries")
    class ConnectionQueries {
        @Test
        @DisplayName("queries connections by node and port")
        void queryByNodeAndPort() {
            Node n1 = createTestNode("n1", null, "out");
            Node n2 = createTestNode("n2", "in", "out");
            Node n3 = createTestNode("n3", "in", null);

            graph.addNode(n1);
            graph.addNode(n2);
            graph.addNode(n3);

            graph.connect(NodeId.of("n1"), PortId.of("out"), NodeId.of("n2"), PortId.of("in"));
            graph.connect(NodeId.of("n2"), PortId.of("out"), NodeId.of("n3"), PortId.of("in"));

            // Node queries
            List<Connection> n1Conns = graph.getConnectionsForNode(NodeId.of("n1"));
            assertEquals(1, n1Conns.size());

            List<Connection> n2Conns = graph.getConnectionsForNode(NodeId.of("n2"));
            assertEquals(2, n2Conns.size());

            List<Connection> n3Conns = graph.getConnectionsForNode(NodeId.of("n3"));
            assertEquals(1, n3Conns.size());

            assertTrue(graph.getConnectionsForNode(null).isEmpty());

            // Port queries
            List<Connection> n2InConns = graph.getConnectionsForPort(NodeId.of("n2"), PortId.of("in"));
            assertEquals(1, n2InConns.size());
            assertEquals(NodeId.of("n1"), n2InConns.get(0).fromNode());

            List<Connection> n2OutConns = graph.getConnectionsForPort(NodeId.of("n2"), PortId.of("out"));
            assertEquals(1, n2OutConns.size());
            assertEquals(NodeId.of("n3"), n2OutConns.get(0).toNode());

            assertTrue(graph.getConnectionsForPort(null, PortId.of("in")).isEmpty());
            assertTrue(graph.getConnectionsForPort(NodeId.of("n2"), null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Metadata and Identity")
    class MetadataAndIdentity {
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("rejects blank graph id")
        void blankGraphId(String blank) {
            assertThrows(IllegalArgumentException.class, () -> new Graph(blank));
        }

        @Test
        @DisplayName("graph identifier and metadata management")
        void metadata() {
            assertEquals("test_graph", graph.id());
            assertTrue(graph.metadata().isEmpty());

            graph.setMetadata("version", "1.0.0");
            graph.setMetadata("title", "Main Workflow");

            assertEquals("1.0.0", graph.getMetadata("version"));
            assertEquals("Main Workflow", graph.getMetadata("title"));
            assertEquals(2, graph.metadata().size());

            assertThrows(NullPointerException.class, () -> graph.getMetadata(null));
            assertThrows(NullPointerException.class, () -> graph.setMetadata(null, "v"));
            assertThrows(NullPointerException.class, () -> graph.setMetadata("k", null));
            assertThrows(NullPointerException.class, () -> graph.removeMetadata(null));
        }

        @Test
        @DisplayName("toString representation")
        void toStringRep() {
            graph.addNode(createTestNode("n1", "in", "out"));
            assertEquals("Graph[test_graph nodes=1 connections=0]", graph.toString());
        }
    }

    @Nested
    @DisplayName("Performance / Scale Sanity")
    class PerformanceScale {
        @Test
        @DisplayName("handles 1000 nodes and linear chain of connections")
        void largeGraphSanity() {
            int count = 1000;
            for (int i = 0; i < count; i++) {
                graph.addNode(createTestNode("node_" + i, "in", "out"));
            }
            assertEquals(count, graph.nodeCount());

            for (int i = 0; i < count - 1; i++) {
                ConnectionResult result = graph.connect(
                        NodeId.of("node_" + i), PortId.of("out"),
                        NodeId.of("node_" + (i + 1)), PortId.of("in")
                );
                assertTrue(result.isSuccess());
            }
            assertEquals(count - 1, graph.connectionCount());

            // Validate graph
            List<ValidationError> errors = graph.validate();
            assertTrue(errors.isEmpty(), "Valid chain graph should have no validation errors");
        }
    }

    @Nested
    @DisplayName("Concurrency & Thread Safety")
    class ConcurrencySafety {
        @Test
        @DisplayName("concurrent addNode prevents duplicate insertions")
        void concurrentAddNode() throws InterruptedException {
            int threadCount = 50;
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        latch.await();
                        graph.addNode(createTestNode("concurrent_node", "in", "out"));
                        successCount.incrementAndGet();
                    } catch (IllegalArgumentException ignored) {
                        // Expected on duplicate ID
                    } catch (Exception e) {
                        fail("Unexpected exception: " + e.getMessage());
                    }
                });
            }

            latch.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));

            assertEquals(1, successCount.get(), "Only exactly 1 thread should succeed in adding the node");
            assertEquals(1, graph.nodeCount(), "Graph must have exactly 1 node");
        }

        @Test
        @DisplayName("concurrent connect prevents duplicate connections")
        void concurrentConnect() throws InterruptedException {
            graph.addNode(createTestNode("src", "in1", "out1"));
            graph.addNode(createTestNode("dst", "in2", "out2"));

            int threadCount = 50;
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        latch.await();
                        ConnectionResult result = graph.connect(
                                NodeId.of("src"), PortId.of("out1"),
                                NodeId.of("dst"), PortId.of("in2")
                        );
                        if (result.isSuccess()) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        fail("Unexpected exception during connect: " + e.getMessage());
                    }
                });
            }

            latch.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));

            assertEquals(1, successCount.get(), "Only exactly 1 thread should succeed in creating the connection");
            assertEquals(1, graph.connectionCount(), "Graph must contain exactly 1 connection");
        }
    }
}
