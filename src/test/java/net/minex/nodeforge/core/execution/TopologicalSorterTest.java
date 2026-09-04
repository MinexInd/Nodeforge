package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.GraphCycleException;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TopologicalSorter & Dependency Ordering")
class TopologicalSorterTest {

    private Node createDataNode(String id) {
        return Node.builder(NodeId.of(id), "math:add")
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("res", "Res", BuiltinPortTypes.DOUBLE)
                .build();
    }

    @Test
    @DisplayName("sorts linear data dependency chain in topological order")
    void linearChain() {
        Graph graph = new Graph("linear");
        Node n1 = createDataNode("n1");
        Node n2 = createDataNode("n2");
        Node n3 = createDataNode("n3");

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        graph.connect(NodeId.of("n1"), PortId.of("res"), NodeId.of("n2"), PortId.of("a"));
        graph.connect(NodeId.of("n2"), PortId.of("res"), NodeId.of("n3"), PortId.of("a"));

        List<NodeId> order = TopologicalSorter.sort(graph);
        assertEquals(List.of(NodeId.of("n1"), NodeId.of("n2"), NodeId.of("n3")), order);
    }

    @Test
    @DisplayName("sorts diamond dependency graph correctly")
    void diamondGraph() {
        Graph graph = new Graph("diamond");
        Node root = createDataNode("root");
        Node left = createDataNode("left");
        Node right = createDataNode("right");
        Node join = createDataNode("join");

        graph.addNode(root);
        graph.addNode(left);
        graph.addNode(right);
        graph.addNode(join);

        graph.connect(NodeId.of("root"), PortId.of("res"), NodeId.of("left"), PortId.of("a"));
        graph.connect(NodeId.of("root"), PortId.of("res"), NodeId.of("right"), PortId.of("a"));
        graph.connect(NodeId.of("left"), PortId.of("res"), NodeId.of("join"), PortId.of("a"));
        graph.connect(NodeId.of("right"), PortId.of("res"), NodeId.of("join"), PortId.of("b"));

        List<NodeId> order = TopologicalSorter.sort(graph);
        assertEquals(4, order.size());
        assertEquals(NodeId.of("root"), order.get(0));
        assertEquals(NodeId.of("join"), order.get(3));
    }

    @Test
    @DisplayName("detects cycle in data dependencies and throws GraphCycleException")
    void cycleDetection() {
        Graph graph = new Graph("cycle");
        Node a = createDataNode("a");
        Node b = createDataNode("b");

        graph.addNode(a);
        graph.addNode(b);

        graph.connect(NodeId.of("a"), PortId.of("res"), NodeId.of("b"), PortId.of("a"));
        graph.connect(NodeId.of("b"), PortId.of("res"), NodeId.of("a"), PortId.of("a"));

        assertThrows(GraphCycleException.class, () -> TopologicalSorter.sort(graph));
    }
}
