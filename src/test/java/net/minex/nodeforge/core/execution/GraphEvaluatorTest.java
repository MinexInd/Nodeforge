package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphEvaluator & Execution Engine")
class GraphEvaluatorTest {

    private GraphEvaluator evaluator;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        evaluator = new GraphEvaluator();
        context = new ExecutionContext();
    }

    @Test
    @DisplayName("evaluates data-flow graph and propagates computed values")
    void evaluateDataFlow() {
        Graph graph = new Graph("calc");

        // c1: constant 15.0
        Node c1 = Node.builder(NodeId.of("c1"), "data:constant")
                .metadata("value", "15.0")
                .outputPort("val", "Val", BuiltinPortTypes.DOUBLE)
                .build();

        // c2: constant 5.0
        Node c2 = Node.builder(NodeId.of("c2"), "data:constant")
                .metadata("value", "5.0")
                .outputPort("val", "Val", BuiltinPortTypes.DOUBLE)
                .build();

        // add: adds c1 and c2 -> 20.0
        Node add = Node.builder(NodeId.of("add"), "math:add")
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("sum", "Sum", BuiltinPortTypes.DOUBLE)
                .build();

        // mul: multiplies sum by c2 -> 100.0
        Node mul = Node.builder(NodeId.of("mul"), "math:multiply")
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("prod", "Prod", BuiltinPortTypes.DOUBLE)
                .build();

        graph.addNode(c1);
        graph.addNode(c2);
        graph.addNode(add);
        graph.addNode(mul);

        graph.connect(NodeId.of("c1"), PortId.of("val"), NodeId.of("add"), PortId.of("a"));
        graph.connect(NodeId.of("c2"), PortId.of("val"), NodeId.of("add"), PortId.of("b"));
        graph.connect(NodeId.of("add"), PortId.of("sum"), NodeId.of("mul"), PortId.of("a"));
        graph.connect(NodeId.of("c2"), PortId.of("val"), NodeId.of("mul"), PortId.of("b"));

        ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);

        assertTrue(summary.isSuccess());
        assertEquals(4, summary.stepsExecuted());
        assertEquals(20.0, ((Number) context.getOutputValue(NodeId.of("add"), "sum")).doubleValue(), 1e-6);
        assertEquals(100.0, ((Number) context.getOutputValue(NodeId.of("mul"), "prod")).doubleValue(), 1e-6);
    }

    @Test
    @DisplayName("executes control-flow pulses along execution connections")
    void executeControlFlow() {
        Graph graph = new Graph("flow");

        Node start = Node.builder(NodeId.of("start"), "data:constant")
                .outputPort("exec_out", "Out", BuiltinPortTypes.EXECUTION)
                .build();

        Node setVar = Node.builder(NodeId.of("set"), "data:set_variable")
                .metadata("var_name", "score")
                .metadata("value", "99")
                .inputPort("exec_in", "In", BuiltinPortTypes.EXECUTION)
                .build();

        graph.addNode(start);
        graph.addNode(setVar);

        graph.connect(NodeId.of("start"), PortId.of("exec_out"), NodeId.of("set"), PortId.of("exec_in"));

        ExecutionSummary summary = evaluator.executeControlFlow(graph, NodeId.of("start"), context);

        assertTrue(summary.isSuccess());
        assertEquals(2, summary.stepsExecuted());
    }

    @Test
    @DisplayName("executes data-flow asynchronously via CompletableFuture")
    void executeAsync() throws ExecutionException, InterruptedException {
        Graph graph = new Graph("async");
        Node c1 = Node.builder(NodeId.of("c1"), "data:constant")
                .metadata("value", "42")
                .outputPort("out", "Out", BuiltinPortTypes.DOUBLE)
                .build();
        graph.addNode(c1);

        CompletableFuture<ExecutionSummary> future = evaluator.evaluateDataFlowAsync(graph, context, null);
        ExecutionSummary summary = future.get();

        assertTrue(summary.isSuccess());
        assertEquals(1, summary.stepsExecuted());
    }
}
