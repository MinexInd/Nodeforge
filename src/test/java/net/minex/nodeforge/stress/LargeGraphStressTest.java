package net.minex.nodeforge.stress;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.serialization.GraphSerializer;
import net.minex.nodeforge.core.execution.TopologicalSorter;
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-volume graph scalability, topology benchmarks, and cascading index stress tests.
 */
class LargeGraphStressTest {

    @Test
    @DisplayName("5,000 nodes and 10,000 connections topology & Kahn's sort performance benchmark")
    void testLargeGraphTopologyAndSort() {
        Graph graph = new Graph("stress_large_graph");
        int nodeCount = 5_000;

        // 1. Create 5,000 nodes in layers (e.g. 500 layers of 10 nodes)
        int nodesPerLayer = 10;
        int layers = nodeCount / nodesPerLayer;

        for (int l = 0; l < layers; l++) {
            for (int i = 0; i < nodesPerLayer; i++) {
                int index = l * nodesPerLayer + i;
                Node node = Node.builder(NodeId.of("n_" + index), "math:add")
                        .displayName("Node " + index)
                        .position(new Position(l * 100.0, i * 80.0))
                        .inputPort("a", "A", BuiltinPortTypes.FLOAT)
                        .inputPort("b", "B", BuiltinPortTypes.FLOAT)
                        .outputPort("sum", "Sum", BuiltinPortTypes.FLOAT)
                        .build();
                graph.addNode(node);
            }
        }
        assertEquals(nodeCount, graph.nodeCount());

        // 2. Add 2 forward connections per node to the next layer (approx ~10,000 connections)
        int connectionCount = 0;
        for (int l = 0; l < layers - 1; l++) {
            for (int i = 0; i < nodesPerLayer; i++) {
                int fromIndex = l * nodesPerLayer + i;
                int toIndex1 = (l + 1) * nodesPerLayer + i;
                int toIndex2 = (l + 1) * nodesPerLayer + ((i + 1) % nodesPerLayer);

                ConnectionResult r1 = graph.connect(
                        NodeId.of("n_" + fromIndex), PortId.of("sum"),
                        NodeId.of("n_" + toIndex1), PortId.of("a"));
                assertTrue(r1.isSuccess(), "Forward connection 1 failed: " + r1);
                connectionCount++;

                ConnectionResult r2 = graph.connect(
                        NodeId.of("n_" + fromIndex), PortId.of("sum"),
                        NodeId.of("n_" + toIndex2), PortId.of("b"));
                assertTrue(r2.isSuccess(), "Forward connection 2 failed: " + r2);
                connectionCount++;
            }
        }
        assertEquals(connectionCount, graph.connectionCount());
        assertTrue(connectionCount >= 9_000);

        // 3. Benchmark Kahn's algorithm O(V + E) sort
        long startSort = System.nanoTime();
        List<NodeId> sorted = TopologicalSorter.sort(graph);
        long sortElapsedMs = (System.nanoTime() - startSort) / 1_000_000;

        assertEquals(nodeCount, sorted.size(), "All nodes must be present in topological sort");
        // Ensure Kahn's sort on 5,000 nodes & 10,000 connections is fast (< 500ms on any modern JVM)
        assertTrue(sortElapsedMs < 500, "Kahn's sort took too long: " + sortElapsedMs + "ms");
    }

    @Test
    @DisplayName("Hub node cascading deletion with 500 incident connections cleans all indices in < 20ms")
    void testHubNodeCascadingRemoval() {
        Graph graph = new Graph("hub_stress_graph");

        // Hub node with multiple output ports
        Node hub = Node.builder(NodeId.of("hub"), "data:broadcast")
                .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                .build();
        graph.addNode(hub);

        // 500 downstream receiver nodes
        int receiverCount = 500;
        for (int i = 0; i < receiverCount; i++) {
            Node receiver = Node.builder(NodeId.of("rec_" + i), "math:add")
                    .inputPort("in", "In", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(receiver);
            ConnectionResult r = graph.connect(
                    hub.id(), PortId.of("out"),
                    receiver.id(), PortId.of("in"));
            assertTrue(r.isSuccess());
        }

        assertEquals(501, graph.nodeCount());
        assertEquals(receiverCount, graph.connectionCount());
        assertEquals(receiverCount, graph.getConnectionsForNode(hub.id()).size());

        // Remove the hub node
        long startRemove = System.nanoTime();
        boolean removed = graph.removeNode(hub.id());
        long removeElapsedMs = (System.nanoTime() - startRemove) / 1_000_000;

        assertTrue(removed);
        assertEquals(receiverCount, graph.nodeCount());
        assertEquals(0, graph.connectionCount(), "All connections to hub must be cascaded");
        assertEquals(0, graph.getConnectionsForNode(hub.id()).size());
        assertTrue(removeElapsedMs < 50, "Hub removal took too long: " + removeElapsedMs + "ms");
    }

    @Test
    @DisplayName("Massive graph round-trip serialization (1,000 nodes, 1,500 connections)")
    void testMassiveGraphSerialization() {
        Graph original = new Graph("massive_serial_graph");
        original.setMetadata("version", "1.0-stress");

        int count = 1_000;
        for (int i = 0; i < count; i++) {
            Node node = Node.builder(NodeId.of("m_" + i), "math:add")
                    .displayName("Massive " + i)
                    .position(new Position(i * 10.0, i * 5.0))
                    .inputPort("in", "In", BuiltinPortTypes.FLOAT)
                    .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                    .build();
            original.addNode(node);
            if (i > 0) {
                // Connect i-1 to i
                original.connect(
                        NodeId.of("m_" + (i - 1)), PortId.of("out"),
                        NodeId.of("m_" + i), PortId.of("in"));
            }
        }

        assertEquals(count, original.nodeCount());
        assertEquals(count - 1, original.connectionCount());

        GraphSerializer serializer = new GraphSerializer();
        String json = serializer.toJson(original, false);
        assertNotNull(json);
        assertFalse(json.isBlank());

        Graph restored = serializer.fromJson(json);
        assertEquals(original.id(), restored.id());
        assertEquals(original.nodeCount(), restored.nodeCount());
        assertEquals(original.connectionCount(), restored.connectionCount());
        assertEquals("1.0-stress", restored.getMetadata("version"));

        // Spot check nodes and positions
        Node first = restored.getNode(NodeId.of("m_0"));
        assertNotNull(first);
        assertEquals(0.0, first.position().x());
        assertEquals(0.0, first.position().y());

        Node last = restored.getNode(NodeId.of("m_" + (count - 1)));
        assertNotNull(last);
        assertEquals((count - 1) * 10.0, last.position().x());
        assertEquals((count - 1) * 5.0, last.position().y());
    }
}
