# Error Handling & Exception Boundaries

NodeForge implements a strict error handling hierarchy separating recoverable domain errors from fatal JVM faults.

---

## 1. Error Taxonomy

NodeForge categorizes errors across five distinct operational boundaries:

```text
Error Category               Reported As                     Resolution Action
-----------------------------------------------------------------------------------------
Connection Invariant         ConnectionResult.failure(...)   Connection rejected; state intact.
Validation Finding           ValidationError                 Reported in inspector / logs.
Serialization Failure        GraphSerializationException     Malformed JSON rejected.
Cyclic Data Dependency       GraphCycleException             Topological sort aborted.
Node Execution Failure       ExecutionSummary.failure(...)   Individual evaluation halted.
Fatal System Error           VirtualMachineError (rethrown)  Crash reporting / JVM shutdown.
```

---

## 2. Fatal JVM Error Boundary

A critical invariant in NodeForge 1.0.0 is that fatal JVM errors are never intercepted or suppressed:

```text
                           java.lang.Throwable
                                    │
          ┌─────────────────────────┴─────────────────────────┐
          ▼                                                   ▼
 java.lang.Exception                                  java.lang.Error
          │                                                   │
   [DOMAIN FAILURE]                                   [FATAL SYSTEM FAULT]
   - Evaluator captures exception                     - OutOfMemoryError
   - Records in ExecutionSummary.Failure              - StackOverflowError
   - Host thread continues safely                     - VirtualMachineError
                                                              │
                                                      [RETHROWN IMMEDIATELY]
                                                      Never caught or masked
```

Catch blocks in `GraphEvaluator`, `GraphValidator`, `PluginManager`, and `SelectionModel` explicitly rethrow `VirtualMachineError`:

```java
try {
    // Execute node logic
} catch (VirtualMachineError e) {
    throw e; // Fatal JVM condition: propagate immediately to crash handler
} catch (Throwable e) {
    // Recoverable domain error: isolate and return Failure record
    return ExecutionSummary.failure(e.getMessage(), ...);
}
```

This prevents faulty user code or out-of-memory states from causing silent deadlocks or corrupted game state.

---

## 3. Handling Execution Failures

When invoking `GraphEvaluator`, always inspect the returned `ExecutionSummary`:

```java
ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);

if (!summary.isSuccess()) {
    if (summary.isCancelled()) {
        System.out.println("Evaluation was cancelled by user.");
    } else {
        String errorMsg = summary.errorMessage().orElse("Unknown executor error");
        System.err.println("Execution failed at step " + summary.stepsExecuted() + ": " + errorMsg);
    }
}
```

Because failure is encapsulated within `ExecutionSummary`, a single failing node does not crash the server tick loop.

---

## 4. Handling Cyclic Dependencies

In pure data-flow evaluation, cycles are forbidden. Attempting to evaluate a cyclic data dependency throws `GraphCycleException`:

```java
import net.minex.nodeforge.api.execution.GraphCycleException;

try {
    List<NodeId> order = TopologicalSorter.sort(graph);
} catch (GraphCycleException e) {
    System.err.println("Cyclic wiring detected: " + e.getMessage());
    // Informs the user which nodes participate in the cycle
}
```

---

## 5. Next Steps

- Review target platforms and compatibility in the [Compatibility Reference](compatibility.md).
- Examine complete runnable examples in the [Examples Directory](../examples/minimal-graph.md).
