package net.minex.nodeforge.stress;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.execution.GraphCycleException;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.runner.GraphRunner;
import net.minex.nodeforge.core.execution.TopologicalSorter;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests execution resilience under deep recursion chains, cycles, cancellations,
 * step limits, and null input boundaries.
 */
class DeepChainAndResilienceTest {

    @Test
    @DisplayName("2,000-node deep linear data chain evaluates correctly without StackOverflowError")
    void testDeepLinearDataChain() {
        Graph graph = new Graph("deep_chain_graph");
        int chainLength = 2_000;

        // Node 0 is constant 1.0
        Node n0 = Node.builder(NodeId.of("n_0"), "data:constant")
                .metadata("value", "1.0")
                .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                .build();
        graph.addNode(n0);

        // Nodes 1..1999 add 1.0 to the previous node's output
        for (int i = 1; i < chainLength; i++) {
            Node n = Node.builder(NodeId.of("n_" + i), "math:add")
                    .inputPort("a", "A", BuiltinPortTypes.FLOAT)
                    .inputPort("b", "B", BuiltinPortTypes.FLOAT)
                    .outputPort("sum", "Sum", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(n);

            // Connect previous node output to 'a'
            graph.connect(
                    NodeId.of("n_" + (i - 1)), i == 1 ? PortId.of("out") : PortId.of("sum"),
                    n.id(), PortId.of("a"));
        }

        ExecutionContext context = new ExecutionContext(50_000); // Allow sufficient steps for 2,000 nodes

        // Pre-populate 'b' port of all add nodes with 1.0
        for (int i = 1; i < chainLength; i++) {
            context.setInputValue(NodeId.of("n_" + i), PortId.of("b"), 1.0);
        }

        ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);
        assertTrue(summary.isSuccess(), "Evaluation failed: " + summary.errorMessage());
        assertEquals(chainLength, summary.stepsExecuted());

        // Value of n_1999 should be 1.0 + 1999 * 1.0 = 2000.0
        Object finalVal = context.getOutputValue(NodeId.of("n_" + (chainLength - 1)), PortId.of("sum"));
        assertNotNull(finalVal);
        assertEquals(2000.0, ((Number) finalVal).doubleValue(), 1e-6);
    }

    @Test
    @DisplayName("Procedural execution on 1,500-node upstream chain resolves iteratively without StackOverflowError")
    void testProceduralDeepUpstreamChain() {
        Graph graph = new Graph("procedural_deep_upstream");
        int chainLength = 1_500;

        // Upstream data chain
        Node n0 = Node.builder(NodeId.of("d_0"), "data:constant")
                .metadata("value", "5.0")
                .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                .build();
        graph.addNode(n0);

        for (int i = 1; i < chainLength; i++) {
            Node n = Node.builder(NodeId.of("d_" + i), "math:add")
                    .inputPort("a", "A", BuiltinPortTypes.FLOAT)
                    .inputPort("b", "B", BuiltinPortTypes.FLOAT)
                    .outputPort("sum", "Sum", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(n);
            graph.connect(
                    NodeId.of("d_" + (i - 1)), i == 1 ? PortId.of("out") : PortId.of("sum"),
                    n.id(), PortId.of("a"));
        }

        // Procedural entrypoint node that reads from the end of the upstream chain
        Node entry = Node.builder(NodeId.of("proc_entry"), "data:set_variable")
                .metadata("var_name", "final_result")
                .inputPort("exec_in", "In", BuiltinPortTypes.EXECUTION)
                .inputPort("value", "Value", BuiltinPortTypes.FLOAT)
                .outputPort("exec_out", "Out", BuiltinPortTypes.EXECUTION)
                .build();
        graph.addNode(entry);

        // Connect the end of data chain to the procedural node's data input
        graph.connect(
                NodeId.of("d_" + (chainLength - 1)), PortId.of("sum"),
                entry.id(), PortId.of("value"));

        ExecutionContext context = new ExecutionContext(50_000);
        for (int i = 1; i < chainLength; i++) {
            context.setInputValue(NodeId.of("d_" + i), PortId.of("b"), 2.0);
        }

        ExecutionSummary summary = GraphRunner.executeProcedural(graph, entry.id(), context);
        assertTrue(summary.isSuccess(), "Procedural execution failed: " + summary.errorMessage());
        // 5.0 + 1499 * 2.0 = 5 + 2998 = 3003.0
        assertEquals(3003.0, context.getVariable("final_result", Double.class, 0.0), 1e-6);
    }

    @Test
    @DisplayName("Multi-node cross cycle detection under load throws GraphCycleException")
    void testComplexCycleDetection() {
        Graph graph = new Graph("cyclic_graph");
        // Create 10 nodes: n0 -> n1 -> n2 -> n3 -> n4 -> n5 -> n6 -> n7 -> n8 -> n9
        for (int i = 0; i < 10; i++) {
            Node n = Node.builder(NodeId.of("c_" + i), "math:add")
                    .inputPort("in", "In", BuiltinPortTypes.FLOAT)
                    .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(n);
            if (i > 0) {
                graph.connect(NodeId.of("c_" + (i - 1)), PortId.of("out"), n.id(), PortId.of("in"));
            }
        }

        // Now introduce a feedback cycle: c_9 -> c_3
        graph.connect(NodeId.of("c_9"), PortId.of("out"), NodeId.of("c_3"), PortId.of("in"));

        assertThrows(GraphCycleException.class, () -> TopologicalSorter.sort(graph));

        // GraphRunner evaluation also fails cleanly
        ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph);
        assertFalse(summary.isSuccess());
        String err = summary.errorMessage().orElse("").toLowerCase();
        assertTrue(err.contains("circular") || err.contains("cycle"));
    }

    @Test
    @DisplayName("Mid-execution cancellation halts immediately and returns cancelled summary")
    void testCancellationUnderLoad() {
        Graph graph = new Graph("cancellation_graph");
        for (int i = 0; i < 500; i++) {
            Node n = Node.builder(NodeId.of("cancel_" + i), "math:add")
                    .inputPort("in", "In", BuiltinPortTypes.FLOAT)
                    .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(n);
            if (i > 0) {
                graph.connect(NodeId.of("cancel_" + (i - 1)), PortId.of("out"), n.id(), PortId.of("in"));
            }
        }

        ExecutionContext context = new ExecutionContext();
        // Cancel before start
        context.cancel();
        assertTrue(context.isCancelled());

        ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);
        assertTrue(summary.isCancelled());
        assertFalse(summary.isSuccess());
        assertEquals(0, summary.stepsExecuted());
    }

    @Test
    @DisplayName("Step limit enforcement terminates execution cleanly when exceeded")
    void testStepLimitExceeded() {
        Graph graph = new Graph("step_limit_graph");
        for (int i = 0; i < 200; i++) {
            Node n = Node.builder(NodeId.of("step_" + i), "math:add")
                    .inputPort("in", "In", BuiltinPortTypes.FLOAT)
                    .outputPort("out", "Out", BuiltinPortTypes.FLOAT)
                    .build();
            graph.addNode(n);
            if (i > 0) {
                graph.connect(NodeId.of("step_" + (i - 1)), PortId.of("out"), n.id(), PortId.of("in"));
            }
        }

        // Limit to 50 steps
        ExecutionContext context = new ExecutionContext(50);
        ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);

        assertFalse(summary.isSuccess());
        assertTrue(summary.errorMessage().isPresent());
        assertTrue(summary.errorMessage().get().contains("maximum allowed step limit"));
    }

    @Test
    @DisplayName("ExecutionContext handles null inputs and outputs safely without NullPointerException")
    void testExecutionContextNullHandling() {
        ExecutionContext context = new ExecutionContext();
        NodeId node = NodeId.of("null_test_node");
        PortId port = PortId.of("null_port");

        // Setting a non-null input then clearing it with null
        context.setInputValue(node, port, "hello");
        assertEquals("hello", context.getInputValue(node, port));

        assertDoesNotThrow(() -> context.setInputValue(node, port, null));
        assertNull(context.getInputValue(node, port));

        // Setting a non-null output then clearing it with null
        context.setOutputValue(node, port, 42);
        assertEquals(42, context.getOutputValue(node, port));

        assertDoesNotThrow(() -> context.setOutputValue(node, port, null));
        assertNull(context.getOutputValue(node, port));

        // Setting null variable
        context.setVariable("my_var", "present");
        assertEquals("present", context.getVariable("my_var"));

        assertDoesNotThrow(() -> context.setVariable("my_var", null));
        assertNull(context.getVariable("my_var"));
        assertFalse(context.hasVariable("my_var"));
    }
}
