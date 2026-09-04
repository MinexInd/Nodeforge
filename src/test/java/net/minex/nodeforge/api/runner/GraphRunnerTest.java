package net.minex.nodeforge.api.runner;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.demo.DemoMathPlugin;
import net.minex.nodeforge.demo.Vector2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphRunner")
class GraphRunnerTest {

    private static DemoMathPlugin plugin;

    @BeforeAll
    static void initPlugin() {
        plugin = new DemoMathPlugin();
        plugin.registerPortTypes(PortTypeRegistry.getInstance());
        plugin.registerNodeDefinitions(NodeDefinitionRegistry.getInstance());
        plugin.registerExecutors(NodeExecutorRegistry.getInstance());
    }

    @Test
    @DisplayName("evaluates chained mathematical graph with GraphRunner")
    void evaluateChainedMathGraph() {
        Graph graph = new Graph("math_eval_graph");

        // Node 1: Add (15 + 25 = 40)
        Node addNode = NodeDefinitionRegistry.getInstance().get(DemoMathPlugin.ADD_NODE)
                .createNode(NodeId.of("add_1"), new Position(0, 0));
        graph.addNode(addNode);

        // Node 2: Multiply (Result * 2 = 80)
        Node mulNode = NodeDefinitionRegistry.getInstance().get(DemoMathPlugin.MULTIPLY_NODE)
                .createNode(NodeId.of("mul_1"), new Position(100, 0));
        graph.addNode(mulNode);

        // Node 3: Clamp (val=80, min=0, max=50 -> 50)
        Node clampNode = NodeDefinitionRegistry.getInstance().get(DemoMathPlugin.CLAMP_NODE)
                .createNode(NodeId.of("clamp_1"), new Position(200, 0));
        graph.addNode(clampNode);

        // Node 4: Make Vector2 (x=50, y=10 -> Vector2(50, 10))
        Node vecNode = NodeDefinitionRegistry.getInstance().get(DemoMathPlugin.VEC2_MAKE_NODE)
                .createNode(NodeId.of("vec_1"), new Position(300, 0));
        graph.addNode(vecNode);

        // Connections:
        // add_1.result -> mul_1.a
        graph.connect(addNode.id(), PortId.of("result"), mulNode.id(), PortId.of("a"));
        // mul_1.result -> clamp_1.val
        graph.connect(mulNode.id(), PortId.of("result"), clampNode.id(), PortId.of("val"));
        // clamp_1.result -> vec_1.x
        graph.connect(clampNode.id(), PortId.of("result"), vecNode.id(), PortId.of("x"));

        ExecutionContext context = new ExecutionContext();
        // Set initial inputs
        context.setInputValue(addNode.id(), PortId.of("a"), 15f);
        context.setInputValue(addNode.id(), PortId.of("b"), 25f);
        context.setInputValue(mulNode.id(), PortId.of("b"), 2f);
        context.setInputValue(clampNode.id(), PortId.of("min"), 0f);
        context.setInputValue(clampNode.id(), PortId.of("max"), 50f);
        context.setInputValue(vecNode.id(), PortId.of("y"), 10f);

        ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);

        assertTrue(summary.isSuccess());
        assertEquals(4, summary.stepsExecuted());

        // Verify intermediary outputs
        assertEquals(40f, context.getOutputValue(addNode.id(), PortId.of("result")));
        assertEquals(80f, context.getOutputValue(mulNode.id(), PortId.of("result")));
        assertEquals(50f, context.getOutputValue(clampNode.id(), PortId.of("result")));

        // Verify final vector output
        Object rawVec = context.getOutputValue(vecNode.id(), PortId.of("vec"));
        assertInstanceOf(Vector2.class, rawVec);
        Vector2 finalVec = (Vector2) rawVec;
        assertEquals(50f, finalVec.x());
        assertEquals(10f, finalVec.y());
    }

    @Test
    @DisplayName("evaluates graph asynchronously via GraphRunner")
    void evaluateAsyncGraph() throws Exception {
        Graph graph = new Graph("async_math_graph");
        Node addNode = NodeDefinitionRegistry.getInstance().get(DemoMathPlugin.ADD_NODE)
                .createNode(NodeId.of("async_add"), new Position(0, 0));
        graph.addNode(addNode);

        ExecutionContext context = new ExecutionContext();
        context.setInputValue(addNode.id(), PortId.of("a"), 7f);
        context.setInputValue(addNode.id(), PortId.of("b"), 8f);

        CompletableFuture<ExecutionSummary> future = GraphRunner.evaluateAsync(graph, context);
        ExecutionSummary summary = future.get();

        assertTrue(summary.isSuccess());
        assertEquals(15f, context.getOutputValue(addNode.id(), PortId.of("result")));
    }
}
