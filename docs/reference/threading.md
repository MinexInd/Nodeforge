# Concurrency & Threading

This reference details the thread-safety guarantees, concurrency invariants, and synchronization rules governing NodeForge.

---

## 1. Thread Safety Invariants

NodeForge avoids vague claims of universal thread safety. Concurrency guarantees are precisely defined across four distinct operational tiers:

| Subsystem | Thread-Safety Level | Concurrency Invariant |
| :--- | :--- | :--- |
| **`Graph` Mutations** | **Thread-Safe (Synchronized)** | Individual mutations (`addNode`, `removeNode`, `connect`, `disconnect`) are synchronized on the `Graph` object monitor. |
| **`Graph` Iteration** | **Thread-Safe (Snapshots)** | Methods returning collections (`getNodes()`, `getConnections()`) return unmodifiable copy snapshots (`List.copyOf(...)`), preventing `ConcurrentModificationException`. |
| **`ExecutionContext`** | **Thread-Safe** | Variable scopes and port value buffers utilize `ConcurrentHashMap`. Step counters use atomic integers. Cancellation uses atomic booleans. |
| **Concurrent Evaluations** | **Thread-Safe (Isolated Contexts)** | Multiple threads can evaluate the same shared `Graph` simultaneously without lock contention, provided each thread uses its own `ExecutionContext`. |
| **Evaluation Immutability** | **Compound Constraint** | Graph topology must remain semantically immutable during an active evaluation run. Mutating topology while an evaluation is traversing nodes is unsupported without external locking. |

---

## 2. Multi-Threaded Evaluation of Shared Graphs

A primary design requirement of NodeForge is allowing multiple server threads (e.g. player ticks, spell resolvers, or AI background workers) to evaluate the same pre-compiled graph concurrently without thread locks.

```text
               Shared Immutable Graph (Compiled Logic)
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
    Thread 1 (Player A)     Thread 2 (Player B)     Thread 3 (Player C)
   ExecutionContext A      ExecutionContext B      ExecutionContext C
          │                       │                       │
          ▼                       ▼                       ▼
   Result: 15.0            Result: 42.0            Result: 99.0
```

Because node instances do not store runtime values (all runtime data resides inside `ExecutionContext`), concurrent evaluation passes operate in complete memory isolation without cross-talk or race conditions.

---

## 3. The Evaluation Immutability Constraint

While individual method calls on `Graph` are synchronized, a graph evaluation pass is a compound, multi-step operation:

```java
// 1. Sorts graph topologically
List<NodeId> order = TopologicalSorter.sort(graph);

// 2. Iterates across the sorted order and executes each node
for (NodeId id : order) {
    Node node = graph.getNode(id);
    executor.execute(node, context);
}
```

If another thread removes a node or severs a wire *between* step 1 and step 2:
- The pre-computed topological order no longer matches the mutated graph topology.
- NodeForge will encounter null node lookups or stale connection targets.

### 3.1 Best Practice for Dynamic Editing & Evaluation
If your mod allows players to dynamically edit graphs while background threads evaluate them:
1. **External Monitor Locking**: Wrap both mutation and evaluation blocks with `synchronized (graph)`:

   ```java
   synchronized (sharedGraph) {
       evaluator.evaluateDataFlow(sharedGraph, context);
   }
   ```

2. **Snapshot Evaluation**: Create a copy of the graph before passing it to background evaluation threads:

   ```java
   Graph snapshot = serializer.fromJson(serializer.toJson(sharedGraph, false));
   evaluator.evaluateDataFlowAsync(snapshot, context, workerPool);
   ```

---

## 4. Next Steps

- Understand how exceptions are isolated in the [Error Handling Reference](error-handling.md).
- Review platform requirements in the [Compatibility Reference](compatibility.md).
