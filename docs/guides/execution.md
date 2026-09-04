# Execution Engine

The NodeForge execution engine evaluates data-flow expressions, executes procedural control pulses, and orchestrates hybrid visual workflows.

---

## 1. The Execution Pipeline

```text
Graph + NodeExecutors
          │
          ▼
   Topological Sorter ───► Kahn's Algorithm O(V+E)
          │
          ▼
   ExecutionContext   ───► Variable Scope & Input/Output Value Buffers
          │
          ▼
   Node Execution     ───► NodeExecutor.execute(node, context)
          │
          ▼
   Value Propagation  ───► Outputs pushed downstream along wires
          │
          ▼
   ExecutionSummary   ───► Result metrics & diagnostics report
```

---

## 2. Managing the `ExecutionContext`

The `ExecutionContext` (`net.minex.nodeforge.api.execution.ExecutionContext`) encapsulates all runtime state for an evaluation run.

### 2.1 Context Variables
Variables provide global data available to all executing nodes:

```java
import net.minex.nodeforge.api.execution.ExecutionContext;

ExecutionContext context = new ExecutionContext();

// Set global variables
context.setVariable("player_level", 42);
context.setVariable("difficulty", "HARD");

// Retrieve variables with fallback defaults
int level = context.getVariable("player_level", Integer.class, 1);
```

### 2.2 Feeding Input Values
Before evaluating, seed input ports with initial data:

```java
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

// Inject initial values directly into a node's input socket
context.setInputValue(NodeId.of("add_node"), PortId.of("a"), 10.0);
context.setInputValue(NodeId.of("add_node"), PortId.of("b"), 25.0);
```

### 2.3 Step Limits & Cancellation
To guard against runaway execution or infinite loops:

```java
// Create a context with an explicit step limit (default is 10,000 steps)
ExecutionContext context = new ExecutionContext(500);

// Request mid-flight cancellation from another thread
context.cancel();

if (context.isCancelled()) {
    System.out.println("Execution was cancelled.");
}
```

---

## 3. Pure Data-Flow Evaluation

Use `evaluateDataFlow` when nodes compute expressions based on incoming wire values:

```java
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.core.execution.GraphEvaluator;
import net.minex.nodeforge.core.graph.Graph;

GraphEvaluator evaluator = new GraphEvaluator();
ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);

if (summary.isSuccess()) {
    System.out.println("Duration: " + summary.durationMillis() + " ms");
    System.out.println("Steps executed: " + summary.stepsExecuted());
    
    // Read the output value produced by the terminal node
    Double finalResult = (Double) context.getOutputValue(NodeId.of("mult_node"), PortId.of("result"));
    System.out.println("Result: " + finalResult);
} else {
    System.err.println("Execution failed: " + summary.errorMessage().orElse("Unknown error"));
}
```

### 3.1 Asynchronous Data-Flow Evaluation
Run heavy calculations on a background worker thread:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

CompletableFuture<ExecutionSummary> future = evaluator.evaluateDataFlowAsync(
        graph, context, ForkJoinPool.commonPool()
);

future.thenAccept(result -> {
    System.out.println("Async evaluation finished. Success: " + result.isSuccess());
});
```

---

## 4. Procedural Control-Flow Execution

Use `executeControlFlow` when graphs represent sequential instructions governed by execution ports:

```java
// Execute starting from an explicit entrypoint node ID
ExecutionSummary summary = evaluator.executeControlFlow(
        graph, NodeId.of("dialogue_start"), context
);
```

### 4.1 Hybrid Evaluation (On-Demand Upstream Resolution)
When a procedural node executes, it may require inputs connected to upstream mathematical or sensory query nodes.

NodeForge automatically performs **iterative post-order depth-first traversal** of all uncomputed upstream data dependencies before running the control node. Upstream data nodes are evaluated on demand without risking JVM call-stack recursion, supporting deep chains ($>2{,}000$ nodes) with zero `StackOverflowError`.

---

## 5. Ergonomic Facade: `GraphRunner`

For simple evaluations, `GraphRunner` provides static helper methods:

```java
import net.minex.nodeforge.api.runner.GraphRunner;

// Single-line synchronous evaluation
ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);

// Single-line asynchronous evaluation
CompletableFuture<ExecutionSummary> asyncSummary = GraphRunner.evaluateDataFlowAsync(graph, context);
```

---

## 6. Next Steps

- Learn how to bind node values to editor controls in the [Properties Guide](properties.md).
- Learn how to create custom node execution logic in the [Custom Nodes Guide](../customization/custom-nodes.md).
