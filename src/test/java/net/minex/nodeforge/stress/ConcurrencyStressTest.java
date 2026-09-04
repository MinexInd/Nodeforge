package net.minex.nodeforge.stress;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.registry.NodeCategory;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.registry.NodeTypeId;
import net.minex.nodeforge.api.runner.GraphRunner;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.validation.GraphValidator;
import net.minex.nodeforge.core.validation.ValidationError;
import net.minex.nodeforge.core.validation.ValidationSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency stress tests and third-party plugin fault isolation tests.
 */
class ConcurrencyStressTest {

    @Test
    @DisplayName("16 concurrent threads evaluate the same shared Graph with independent contexts cleanly")
    void testConcurrentGraphEvaluation() throws Exception {
        // Build a shared computation graph: (a + b) * 2
        Graph sharedGraph = new Graph("shared_concurrency_graph");

        Node addNode = Node.builder(NodeId.of("add"), "math:add")
                .inputPort("a", "A", BuiltinPortTypes.FLOAT)
                .inputPort("b", "B", BuiltinPortTypes.FLOAT)
                .outputPort("sum", "Sum", BuiltinPortTypes.FLOAT)
                .build();
        sharedGraph.addNode(addNode);

        Node mulNode = Node.builder(NodeId.of("mul"), "math:multiply")
                .inputPort("a", "A", BuiltinPortTypes.FLOAT)
                .inputPort("b", "B", BuiltinPortTypes.FLOAT)
                .outputPort("prod", "Prod", BuiltinPortTypes.FLOAT)
                .build();
        sharedGraph.addNode(mulNode);

        sharedGraph.connect(addNode.id(), PortId.of("sum"), mulNode.id(), PortId.of("a"));

        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Double>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final double valA = i * 2.0;
            final double valB = i * 3.0;
            tasks.add(() -> {
                ExecutionContext ctx = new ExecutionContext();
                ctx.setInputValue(addNode.id(), PortId.of("a"), valA);
                ctx.setInputValue(addNode.id(), PortId.of("b"), valB);
                ctx.setInputValue(mulNode.id(), PortId.of("b"), 2.0);

                ExecutionSummary summary = GraphRunner.evaluateDataFlow(sharedGraph, ctx);
                if (!summary.isSuccess()) {
                    throw new RuntimeException("Evaluation failed: " + summary.errorMessage());
                }

                Object result = ctx.getOutputValue(mulNode.id(), PortId.of("prod"));
                return ((Number) result).doubleValue();
            });
        }

        List<Future<Double>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        for (int i = 0; i < threadCount; i++) {
            double expected = ((i * 2.0) + (i * 3.0)) * 2.0; // (5i) * 2 = 10i
            assertEquals(expected, futures.get(i).get(), 1e-5);
        }
    }

    @Test
    @DisplayName("GraphValidator isolates throwing third-party validation rules without crashing")
    void testValidatorFaultIsolation() {
        NodeDefinitionRegistry registry = new NodeDefinitionRegistry();
        NodeTypeId buggyType = NodeTypeId.of("test:buggy_validator");

        NodeDefinition def = NodeDefinition.builder(buggyType)
                .displayName("Buggy Validator Node")
                .category(NodeCategory.of("test", "Test"))
                .validationRule((node, g) -> {
                    throw new RuntimeException("Simulated malicious or buggy third-party exception!");
                })
                .build();
        registry.register(def);

        Graph graph = new Graph("fault_isolation_graph");
        graph.addNode(Node.builder(NodeId.of("buggy_node"), buggyType.value()).build());

        List<ValidationError> errors = assertDoesNotThrow(() -> GraphValidator.validate(graph, registry));
        assertFalse(errors.isEmpty(), "Expected validation error from failing rule");

        ValidationError err = errors.getFirst();
        assertEquals(ValidationSeverity.ERROR, err.severity());
        assertTrue(err.message().contains("Simulated malicious or buggy third-party exception!"),
                "Error message should report isolated exception: " + err.message());
    }
}
