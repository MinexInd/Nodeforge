package net.minex.nodeforge.qa;

import net.minecraft.util.Identifier;
import net.minex.nodeforge.api.execution.*;
import net.minex.nodeforge.api.graph.*;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.GraphRegistry;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.serialization.GraphSerializationException;
import net.minex.nodeforge.api.serialization.GraphSerializer;
import net.minex.nodeforge.core.execution.GraphEvaluator;
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.resource.GraphResourceReloader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive adversarial QA audit test suite simulating an external third-party consumer mod.
 *
 * <p>Validates:
 * <ul>
 *   <li>Domain-agnostic custom node, port, and executor registration without internal coupling</li>
 *   <li>Hybrid execution semantics (interleaved control-flow pulse with iterative DFS upstream data evaluation)</li>
 *   <li>Concurrency isolation across multiple threads evaluating a shared graph structure</li>
 *   <li>Immediate propagation of fatal JVM errors (VirtualMachineError) without suppression</li>
 *   <li>Extreme numeric coordinate preservation and JSON serialization round-trips</li>
 *   <li>Robustness against malformed JSON, duplicate identifiers, self-connections, and cyclic dependencies</li>
 *   <li>Consumer-configured Minecraft Resource Graph Persistence reloading</li>
 * </ul>
 */
class AdversarialConsumerQATest {

    private PortTypeRegistry portTypeRegistry;
    private NodeDefinitionRegistry nodeDefRegistry;
    private NodeExecutorRegistry executorRegistry;
    private GraphEvaluator evaluator;
    private GraphSerializer serializer;

    @BeforeEach
    void setUp() {
        portTypeRegistry = new PortTypeRegistry();
        nodeDefRegistry = new NodeDefinitionRegistry();
        executorRegistry = new NodeExecutorRegistry();
        evaluator = new GraphEvaluator(executorRegistry);
        serializer = new GraphSerializer();
    }

    // ========== 1. External Consumer Custom Node & Executor Lifecycle ==========

    @Test
    @DisplayName("External consumer registers custom domain ports, nodes, and executors for pure data-flow")
    void testExternalConsumerDataFlowPipeline() {
        // Consumer registers custom port type: "skill:mana"
        PortType<Integer> manaType = PortType.builder("skill:mana", Integer.class)
                .displayName("Mana")
                .color(0x3366FF)
                .build();
        portTypeRegistry.register(manaType);
        assertTrue(portTypeRegistry.has(manaType.id()));

        // Consumer registers "skill:mana_source" node
        NodeDefinition sourceDef = NodeDefinition.builder("skill:mana_source")
                .displayName("Mana Font")
                .outputPort("mana_out", "Mana", manaType)
                .build();
        nodeDefRegistry.register(sourceDef);

        // Consumer registers "skill:spell_amplifier" node
        NodeDefinition ampDef = NodeDefinition.builder("skill:spell_amplifier")
                .displayName("Spell Amplifier")
                .inputPort("mana_in", "Mana Input", manaType)
                .outputPort("mana_out", "Amplified Mana", manaType)
                .build();
        nodeDefRegistry.register(ampDef);

        // Consumer binds executors
        executorRegistry.register("skill:mana_source", (node, ctx) -> {
            ctx.setOutputValue(node.id(), PortId.of("mana_out"), 50);
            return ExecutionResult.Success.of();
        });

        executorRegistry.register("skill:spell_amplifier", (node, ctx) -> {
            Integer inMana = (Integer) ctx.getInputValue(node.id(), PortId.of("mana_in"));
            int multiplier = ctx.getVariable("multiplier", 2);
            int result = (inMana != null ? inMana : 0) * multiplier;
            ctx.setOutputValue(node.id(), PortId.of("mana_out"), result);
            return ExecutionResult.Success.of();
        });

        // Assemble graph
        Graph graph = new Graph("skill_tree_pipeline");
        Node font = Node.builder(NodeId.of("font_1"), "skill:mana_source")
                .outputPort("mana_out", "Mana", manaType)
                .build();
        Node amp = Node.builder(NodeId.of("amp_1"), "skill:spell_amplifier")
                .inputPort("mana_in", "Mana Input", manaType)
                .outputPort("mana_out", "Amplified Mana", manaType)
                .build();

        graph.addNode(font);
        graph.addNode(amp);
        ConnectionResult connRes = graph.connect(font.id(), PortId.of("mana_out"), amp.id(), PortId.of("mana_in"));
        assertTrue(connRes.isSuccess());

        // Evaluate graph with context variables
        ExecutionContext context = new ExecutionContext();
        context.setVariable("multiplier", 3);

        ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);
        assertTrue(summary.isSuccess(), "Data-flow evaluation must succeed: " + summary.errorMessage().orElse(""));
        assertEquals(2, summary.stepsExecuted());
        assertEquals(150, context.getOutputValue(amp.id(), PortId.of("mana_out")));
    }

    // ========== 2. Hybrid Execution Model (Control Flow + Upstream Data Resolution) ==========

    @Test
    @DisplayName("Hybrid execution model evaluates upstream data dependencies on demand during control pulses")
    void testHybridExecutionModel() {
        PortType<Integer> numType = PortType.builder("rpg:number", Integer.class)
                .color(0x00FF88)
                .build();
        PortType<?> execType = BuiltinPortTypes.EXECUTION;

        // Upstream pure data node: provides player character level
        executorRegistry.register("rpg:level_sensor", (node, ctx) -> {
            int level = ctx.getVariable("player_level", 1);
            ctx.setOutputValue(node.id(), PortId.of("level_out"), level);
            return ExecutionResult.Success.of();
        });

        // Control flow node: branch based on level
        executorRegistry.register("rpg:level_gate", (node, ctx) -> {
            Integer lvl = (Integer) ctx.getInputValue(node.id(), PortId.of("level_in"));
            if (lvl != null && lvl >= 10) {
                return ExecutionResult.Success.of(PortId.of("pass_flow"));
            } else {
                return ExecutionResult.Success.of(PortId.of("fail_flow"));
            }
        });

        // Terminal action nodes
        AtomicInteger rewardCounter = new AtomicInteger(0);
        executorRegistry.register("rpg:grant_mastery", (node, ctx) -> {
            rewardCounter.incrementAndGet();
            return ExecutionResult.Success.of();
        });

        Graph graph = new Graph("hybrid_quest_graph");

        // 1. Data provider node
        Node sensor = Node.builder(NodeId.of("sensor"), "rpg:level_sensor")
                .outputPort("level_out", "Level", numType)
                .build();

        // 2. Control gate node
        Node gate = Node.builder(NodeId.of("gate"), "rpg:level_gate")
                .inputPort("exec_in", "Exec", execType)
                .inputPort("level_in", "Required Level", numType)
                .outputPort("pass_flow", "Pass", execType)
                .outputPort("fail_flow", "Fail", execType)
                .build();

        // 3. Action node
        Node reward = Node.builder(NodeId.of("reward"), "rpg:grant_mastery")
                .inputPort("exec_in", "Exec", execType)
                .build();

        graph.addNode(sensor);
        graph.addNode(gate);
        graph.addNode(reward);

        // Connect upstream data wire: sensor -> gate
        graph.connect(sensor.id(), PortId.of("level_out"), gate.id(), PortId.of("level_in"));
        // Connect control flow wire: gate.pass_flow -> reward.exec_in
        graph.connect(gate.id(), PortId.of("pass_flow"), reward.id(), PortId.of("exec_in"));

        // Case A: Player level 15 -> passes gate and executes reward
        ExecutionContext ctxPass = new ExecutionContext();
        ctxPass.setVariable("player_level", 15);
        ExecutionSummary summaryPass = evaluator.executeControlFlow(graph, gate.id(), ctxPass);

        assertTrue(summaryPass.isSuccess());
        assertEquals(1, rewardCounter.get(), "Reward executor must be invoked once");

        // Case B: Player level 5 -> fails gate, reward is never reached
        ExecutionContext ctxFail = new ExecutionContext();
        ctxFail.setVariable("player_level", 5);
        ExecutionSummary summaryFail = evaluator.executeControlFlow(graph, gate.id(), ctxFail);

        assertTrue(summaryFail.isSuccess());
        assertEquals(1, rewardCounter.get(), "Reward counter must remain 1 because level 5 branched to fail_flow");
    }

    // ========== 3. Thread Safety: Concurrent Evaluation of Shared Graph ==========

    @Test
    @DisplayName("Multiple concurrent threads evaluate the same shared Graph instance with isolated contexts")
    void testConcurrentEvaluationOnSharedGraph() throws InterruptedException, ExecutionException {
        PortType<Double> numType = PortType.builder("math:number", Double.class).build();

        executorRegistry.register("math:multiply_constant", (node, ctx) -> {
            Double val = ctx.getVariable("input_value", 0.0);
            ctx.setOutputValue(node.id(), PortId.of("out"), val * 2.5);
            return ExecutionResult.Success.of();
        });

        Graph sharedGraph = new Graph("shared_concurrency_graph");
        Node calcNode = Node.builder(NodeId.of("calc"), "math:multiply_constant")
                .outputPort("out", "Result", numType)
                .build();
        sharedGraph.addNode(calcNode);

        int threadCount = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        List<Callable<Double>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final double factor = i + 1;
            tasks.add(() -> {
                ExecutionContext ctx = new ExecutionContext();
                ctx.setVariable("input_value", factor);
                ExecutionSummary summary = evaluator.evaluateDataFlow(sharedGraph, ctx);
                if (!summary.isSuccess()) {
                    throw new IllegalStateException("Execution failed: " + summary.errorMessage());
                }
                return (Double) ctx.getOutputValue(calcNode.id(), PortId.of("out"));
            });
        }

        List<Future<Double>> futures = threadPool.invokeAll(tasks);
        threadPool.shutdown();
        assertTrue(threadPool.awaitTermination(5, TimeUnit.SECONDS));

        for (int i = 0; i < threadCount; i++) {
            double expected = (i + 1) * 2.5;
            assertEquals(expected, futures.get(i).get(), 1e-6);
        }
    }

    // ========== 4. Fatal JVM Error Propagation (VirtualMachineError) ==========

    @Test
    @DisplayName("GraphEvaluator propagates fatal JVM errors (VirtualMachineError) immediately without suppression")
    void testFatalJvmErrorsPropagatedImmediately() {
        executorRegistry.register("fatal:oom_node", (node, ctx) -> {
            throw new OutOfMemoryError("Simulated heap exhaustion in adversarial test");
        });

        Graph graph = new Graph("fatal_test_graph");
        Node node = Node.builder(NodeId.of("fatal_node"), "fatal:oom_node").build();
        graph.addNode(node);

        ExecutionContext ctx = new ExecutionContext();

        // Must throw OutOfMemoryError directly to caller, NOT wrap in ExecutionSummary.failure
        assertThrows(OutOfMemoryError.class, () -> evaluator.evaluateDataFlow(graph, ctx));
        assertThrows(OutOfMemoryError.class, () -> evaluator.executeControlFlow(graph, node.id(), ctx));
    }

    // ========== 5. Extreme Numeric Bounds & IEEE 754 Preservation ==========

    @Test
    @DisplayName("GraphSerializer preserves extreme numeric coordinates without overflow or loss of precision")
    void testExtremeCoordinatesSerialization() {
        Graph graph = new Graph("extreme_coords_graph");
        double extremeX = 1.0e14;
        double extremeY = -1.0e14;

        Node node = Node.builder(NodeId.of("far_node"), "nodeforge:default")
                .position(new Position(extremeX, extremeY))
                .build();
        graph.addNode(node);

        String json = serializer.toJson(graph, false);
        assertNotNull(json);

        Graph restored = serializer.fromJson(json);
        Node restoredNode = restored.getNode(NodeId.of("far_node"));
        assertNotNull(restoredNode);
        assertEquals(extremeX, restoredNode.position().x(), 1.0);
        assertEquals(extremeY, restoredNode.position().y(), 1.0);
    }

    // ========== 6. Graph Misuse, Cyclic Boundaries & Malformed JSON Resilience ==========

    @Test
    @DisplayName("Graph API rejects duplicate IDs, self-connections, and cyclic dependencies")
    void testGraphBoundaryDefenses() {
        Graph graph = new Graph("boundary_graph");
        Node n1 = Node.builder(NodeId.of("n1"), "math:add")
                .inputPort("in", "In", BuiltinPortTypes.EXECUTION)
                .outputPort("out", "Out", BuiltinPortTypes.EXECUTION)
                .build();
        graph.addNode(n1);

        // 1. Duplicate node ID rejection
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(n1));

        // 2. Self-connection rejection
        ConnectionResult selfRes = graph.connect(n1.id(), PortId.of("out"), n1.id(), PortId.of("in"));
        assertFalse(selfRes.isSuccess(), "Self-connection must be rejected");

        // 3. Cyclic dependency detection in data-flow
        PortType<String> dataPort = PortType.builder("test:data", String.class).build();
        Node a = Node.builder(NodeId.of("a"), "test:node")
                .inputPort("in", "In", dataPort)
                .outputPort("out", "Out", dataPort)
                .build();
        Node b = Node.builder(NodeId.of("b"), "test:node")
                .inputPort("in", "In", dataPort)
                .outputPort("out", "Out", dataPort)
                .build();

        Graph cyclicGraph = new Graph("cycle_graph");
        cyclicGraph.addNode(a);
        cyclicGraph.addNode(b);
        cyclicGraph.connect(a.id(), PortId.of("out"), b.id(), PortId.of("in"));
        cyclicGraph.connect(b.id(), PortId.of("out"), a.id(), PortId.of("in"));
        // TopologicalSorter throws GraphCycleException directly
        assertThrows(net.minex.nodeforge.api.execution.GraphCycleException.class, () -> net.minex.nodeforge.core.execution.TopologicalSorter.sort(cyclicGraph));

        // GraphEvaluator safely captures cycle error into ExecutionSummary.failure
        ExecutionSummary cycleSummary = evaluator.evaluateDataFlow(cyclicGraph, new ExecutionContext());
        assertFalse(cycleSummary.isSuccess());
        assertTrue(cycleSummary.errorMessage().orElse("").toLowerCase().contains("circular"));
    }

    @Test
    @DisplayName("GraphSerializer throws descriptive GraphSerializationException on malformed JSON")
    void testMalformedJsonResilience() {
        // Missing "nodes" array
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"id\":\"test\"}"));

        // Syntax error
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson("{\"id\": }"));

        // Duplicate node ID in serialized array
        String duplicateNodesJson = """
                {
                  "schema_version": 1,
                  "id": "dup_test",
                  "nodes": [
                    {"id": "same_id", "type": "nodeforge:default", "position": {"x": 0, "y": 0}, "ports": []},
                    {"id": "same_id", "type": "nodeforge:default", "position": {"x": 10, "y": 10}, "ports": []}
                  ],
                  "connections": []
                }
                """;
        assertThrows(GraphSerializationException.class, () -> serializer.fromJson(duplicateNodesJson));
    }

    // ========== 7. Minecraft Resource Graph Persistence Configuration ==========

    @Test
    @DisplayName("GraphResourceReloader supports custom consumer resource directories and listener IDs")
    void testConsumerPersistenceConfiguration() {
        GraphRegistry registry = new GraphRegistry();
        String customPath = "unbound/skill_trees";
        Identifier customId = Identifier.of("unbound", "skill_reloader");

        GraphResourceReloader reloader = new GraphResourceReloader(registry, customPath, customId);

        assertEquals(customId, reloader.getFabricId());
        assertEquals(customPath, reloader.getDirectoryPath());
    }
}
