# Minimal Graph Example

This guide walks through creating, validating, and evaluating a minimal data-flow arithmetic graph using NodeForge 1.0.0. The code in this example runs entirely in common code, requires zero Minecraft client dependencies, and executes synchronously in any Java 21 environment.

---

## What We Are Building

We will construct a simple arithmetic graph that sums two constant numbers ($15.0 + 25.0$) and verifies the evaluated result ($40.0$):

```text
[ Constant A (15.0) ] ──(value)──► [a] 
                                        [ Math Add ] ──(result)──► 40.0
[ Constant B (25.0) ] ──(value)──► [b] 
```

### Key Components

1. **`Graph`**: The root container holding nodes and connections.
2. **`Node`**: Discrete calculation units with typed input and output ports.
3. **`GraphValidator`**: Verifies graph integrity, cycle-free topology, and port compatibility before evaluation.
4. **`GraphEvaluator`**: The execution engine that resolves topological order and passes values across connections.
5. **`ExecutionContext`**: Transient runtime storage for intermediate port values, variables, and step counters.

---

## Complete Standalone Code

The following class is a self-contained test or demonstration runner that can be placed in your mod's test source set (`src/test/java`) or server/common logic:

```java
package com.example.mymod;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.graph.ConnectionResult;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.type.BuiltinPortTypes;
import net.minex.nodeforge.core.execution.GraphEvaluator;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.validation.GraphValidationResult;
import net.minex.nodeforge.core.validation.GraphValidator;

public class MinimalGraphDemo {

    public static void main(String[] args) {
        // 1. Initialize empty graph container
        Graph graph = new Graph("arithmetic_minimal_demo");

        // 2. Define Node A (Constant 15.0)
        Node constA = Node.builder(NodeId.of("const_a"), "data:constant")
                .name("Input A")
                .metadata("value", "15.0")
                .addPort(Port.output(PortId.of("value"), "Value", BuiltinPortTypes.FLOAT))
                .position(50.0, 100.0)
                .build();

        // 3. Define Node B (Constant 25.0)
        Node constB = Node.builder(NodeId.of("const_b"), "data:constant")
                .name("Input B")
                .metadata("value", "25.0")
                .addPort(Port.output(PortId.of("value"), "Value", BuiltinPortTypes.FLOAT))
                .position(50.0, 250.0)
                .build();

        // 4. Define Node C (Math Add: a + b -> result)
        Node addNode = Node.builder(NodeId.of("math_add"), "math:add")
                .name("Add")
                .addPort(Port.input(PortId.of("a"), "A", BuiltinPortTypes.FLOAT))
                .addPort(Port.input(PortId.of("b"), "B", BuiltinPortTypes.FLOAT))
                .addPort(Port.output(PortId.of("result"), "Result", BuiltinPortTypes.FLOAT))
                .position(300.0, 175.0)
                .build();

        // 5. Register nodes into the graph
        graph.addNode(constA);
        graph.addNode(constB);
        graph.addNode(addNode);

        // 6. Connect ports: constA.value -> addNode.a, constB.value -> addNode.b
        ConnectionResult resA = graph.connect(
                constA.id(), PortId.of("value"),
                addNode.id(), PortId.of("a")
        );
        if (!resA.isSuccess()) {
            throw new IllegalStateException("Connection A failed: " + resA.errorMessage());
        }

        ConnectionResult resB = graph.connect(
                constB.id(), PortId.of("value"),
                addNode.id(), PortId.of("b")
        );
        if (!resB.isSuccess()) {
            throw new IllegalStateException("Connection B failed: " + resB.errorMessage());
        }

        // 7. Validate graph topology and type compatibility
        GraphValidationResult validation = GraphValidator.validate(graph);
        if (!validation.isValid()) {
            throw new IllegalStateException("Validation errors: " + validation.errors());
        }

        // 8. Create runtime context and evaluator
        ExecutionContext context = ExecutionContext.create();
        GraphEvaluator evaluator = new GraphEvaluator();

        // 9. Execute data-flow evaluation in topological order
        ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);

        if (!summary.isSuccess()) {
            throw new RuntimeException("Evaluation failed: " + summary.errorMessage());
        }

        // 10. Extract output value from math_add node's 'result' port
        Object rawResult = context.getOutputValue(addNode.id(), PortId.of("result"));
        double finalValue = ((Number) rawResult).doubleValue();

        System.out.println("Graph evaluation succeeded!");
        System.out.println("Result: " + finalValue);
        System.out.println("Steps executed: " + summary.stepCount());
        System.out.println("Duration (ns): " + summary.durationNanos());

        // Verify mathematical assertion: 15.0 + 25.0 = 40.0
        if (Math.abs(finalValue - 40.0) > 1e-6) {
            throw new AssertionError("Expected 40.0 but received: " + finalValue);
        }
    }
}
```

---

## Step-by-Step Breakdown

### 1. Graph Instantiation

```java
Graph graph = new Graph("arithmetic_minimal_demo");
```

The identifier `"arithmetic_minimal_demo"` labels the graph instance for diagnostics, logging, and serialization. `Graph` is a stateful container managing nodes, connections, and metadata.

### 2. Building Typed Nodes

```java
Node constA = Node.builder(NodeId.of("const_a"), "data:constant")
        .name("Input A")
        .metadata("value", "15.0")
        .addPort(Port.output(PortId.of("value"), "Value", BuiltinPortTypes.FLOAT))
        .position(50.0, 100.0)
        .build();
```

* **`NodeId.of("const_a")`**: Unique identifier within this graph. Duplicate IDs cause `IllegalArgumentException` upon `graph.addNode()`.
* **`"data:constant"`**: Type key matching a registered `NodeExecutor`. Built-in executors parse the `"value"` metadata string into a typed number or boolean.
* **`Port.output(...)`**: Defines an output socket named `"value"` carrying `BuiltinPortTypes.FLOAT`.
* **`position(50.0, 100.0)`**: Canvas coordinates $(x, y)$ in world space. While not required for evaluation, positions ensure the graph renders cleanly if loaded into `NodeEditorScreen`.

### 3. Wiring Connections

```java
ConnectionResult resA = graph.connect(
        constA.id(), PortId.of("value"),
        addNode.id(), PortId.of("a")
);
```

Connecting two ports verifies multiple invariants:
1. Source port must be an `OUTPUT`.
2. Target port must be an `INPUT`.
3. Port types must be compatible according to the `TypeSystem` (e.g. `FLOAT` to `FLOAT`, or widening numeric conversions).
4. No cycle is formed if the connection would introduce a circular dependency.

Always inspect `resA.isSuccess()` before proceeding to execution.

### 4. Graph Validation

```java
GraphValidationResult validation = GraphValidator.validate(graph);
if (!validation.isValid()) {
    throw new IllegalStateException("Validation errors: " + validation.errors());
}
```

`GraphValidator.validate(graph)` runs the full suite of structural integrity checks:
* Disconnected mandatory inputs.
* Cycles in pure data-flow connections.
* Dangling connection endpoints pointing to non-existent nodes.

### 5. Evaluation & Context Extraction

```java
ExecutionContext context = ExecutionContext.create();
GraphEvaluator evaluator = new GraphEvaluator();
ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);
```

* **Topological Sort**: `GraphEvaluator` computes dependency order (`const_a` and `const_b` first, followed by `math_add`).
* **Step Count**: Each node execution increments `context.stepCount()`. If an infinite loop or runaway chain exceeds `context.maxSteps()`, evaluation terminates with `ExecutionSummary.failure()`.
* **Output Retrieval**: `context.getOutputValue(NodeId, PortId)` retrieves the calculated value emitted by the executor.

---

## Next Steps

* Learn how to define custom computation nodes in [Custom Nodes Guide](../customization/custom-nodes.md).
* Explore control-flow execution (conditional branching and triggers) in [Execution Model Reference](../reference/execution-model.md).
* Open this graph in the visual editor via [Custom Editor Example](custom-editor.md).
