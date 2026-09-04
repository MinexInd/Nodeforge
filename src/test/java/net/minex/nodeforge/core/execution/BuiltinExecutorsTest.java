package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BuiltinExecutors & Logic Handlers")
class BuiltinExecutorsTest {

    private NodeExecutorRegistry registry;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        registry = new NodeExecutorRegistry();
        BuiltinExecutors.registerAll(registry);
        context = new ExecutionContext();
    }

    @Test
    @DisplayName("executes math add, subtract, multiply, divide")
    void mathExecutors() {
        Node addNode = Node.builder(NodeId.of("add"), "math:add").build();
        context.setInputValue(addNode.id(), PortId.of("a"), 10.0);
        context.setInputValue(addNode.id(), PortId.of("b"), 25.0);

        ExecutionResult addRes = registry.get("math:add").execute(addNode, context);
        assertTrue(addRes instanceof ExecutionResult.Success);
        assertEquals(35.0, ((Number) context.getOutputValue(addNode.id(), "result")).doubleValue(), 1e-6);

        Node mulNode = Node.builder(NodeId.of("mul"), "math:multiply").build();
        context.setInputValue(mulNode.id(), PortId.of("a"), 4.0);
        context.setInputValue(mulNode.id(), PortId.of("b"), 7.0);

        registry.get("math:multiply").execute(mulNode, context);
        assertEquals(28.0, ((Number) context.getOutputValue(mulNode.id(), "result")).doubleValue(), 1e-6);
    }

    @Test
    @DisplayName("executes logic operations and comparisons")
    void logicExecutors() {
        Node andNode = Node.builder(NodeId.of("and"), "logic:and").build();
        context.setInputValue(andNode.id(), PortId.of("a"), true);
        context.setInputValue(andNode.id(), PortId.of("b"), false);

        registry.get("logic:and").execute(andNode, context);
        assertEquals(false, context.getOutputValue(andNode.id(), "result"));

        Node compNode = Node.builder(NodeId.of("comp"), "logic:compare")
                .metadata("op", ">")
                .build();
        context.setInputValue(compNode.id(), PortId.of("a"), 50.0);
        context.setInputValue(compNode.id(), PortId.of("b"), 20.0);

        registry.get("logic:compare").execute(compNode, context);
        assertEquals(true, context.getOutputValue(compNode.id(), "result"));
    }

    @Test
    @DisplayName("executes flow branch routing")
    void flowBranch() {
        Node branch = Node.builder(NodeId.of("branch"), "flow:branch")
                .outputPort("true_exec", "True", BuiltinPortTypes.EXECUTION)
                .outputPort("false_exec", "False", BuiltinPortTypes.EXECUTION)
                .build();

        context.setInputValue(branch.id(), PortId.of("condition"), true);
        ExecutionResult trueRes = registry.get("flow:branch").execute(branch, context);
        assertTrue(trueRes instanceof ExecutionResult.Success);
        assertEquals(PortId.of("true_exec"), ((ExecutionResult.Success) trueRes).nextFlowPort().orElse(null));

        context.setInputValue(branch.id(), PortId.of("condition"), false);
        ExecutionResult falseRes = registry.get("flow:branch").execute(branch, context);
        assertTrue(falseRes instanceof ExecutionResult.Success);
        assertEquals(PortId.of("false_exec"), ((ExecutionResult.Success) falseRes).nextFlowPort().orElse(null));
    }
}
