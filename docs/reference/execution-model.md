# Execution Model Specification

This specification formalizes the three execution models implemented in NodeForge: Pure Data-Flow, Control-Flow Pulse, and Hybrid Interleaved Execution.

---

## 1. Pure Data-Flow Evaluation

Pure data-flow evaluation treats the graph as a Directed Acyclic Graph (DAG) where nodes compute functional expressions and edges denote typed value transfers.

```text
A ──► C ──► D
     ▲
B ───┘

Topological Sort Execution Sequence:
1. Evaluate A (In-degree = 0)
2. Evaluate B (In-degree = 0)
3. Evaluate C (In-degree = 0 after A and B complete)
4. Evaluate D (In-degree = 0 after C completes)
```

### 1.1 Algorithmic Procedure (Kahn's Algorithm)
1. **In-Degree Calculation**:
   For every node $v \in V$, compute the in-degree $\text{deg}^{-}(v)$ considering exclusively non-execution data connections:

   $$\text{deg}^{-}(v) = \sum_{e \in E_{\text{data}}} [\text{toNode}(e) = v]$$

2. **Source Enqueueing**:
   All nodes with $\text{deg}^{-}(v) = 0$ are enqueued into an evaluation queue $Q$.
3. **Topological Stepping**:
   While $Q$ is non-empty:
   - Dequeue node $u$.
   - Invoke registered `NodeExecutor` for node $u$.
   - Push emitted output values from $u$ into target input buffers in `ExecutionContext`.
   - For every outgoing edge $(u, w)$, decrement $\text{deg}^{-}(w)$.
   - If $\text{deg}^{-}(w) = 0$, enqueue $w$.
4. **Cycle Detection Guard**:
   If the total evaluated node count is less than $|V|$, a circular dependency exists. The evaluator halts immediately and throws `GraphCycleException`, reporting all nodes participating in the cycle.
5. **Complexity**:
   Time complexity is strictly $O(|V| + |E|)$. Space complexity is $O(|V| + |E|)$.

---

## 2. Control-Flow Pulse Execution

Procedural control-flow evaluation treats the graph as an imperative state machine or instruction pipeline.

```text
[Entrypoint] ──► [Task 1] ──► [Branch Gate] ──┬─► (True)  [Reward]
                                              └─► (False) [Log]
```

### 2.1 Algorithmic Procedure
1. **Entrypoint Dispatch**: Execution initiates at a designated entrypoint node $v_0$.
2. **Step Increment**: The step counter is incremented. If $\text{stepCount} > \text{maxSteps}$, execution halts with a step-limit error.
3. **Execution**: Node $v_i$'s executor is invoked.
4. **Branch Determination**:
   - If the executor returns `ExecutionResult.Halt`, execution terminates cleanly.
   - If the executor returns `ExecutionResult.Failure`, execution halts and returns error diagnostics.
   - If the executor returns `ExecutionResult.Success` specifying next port $p_{\text{flow}}$, the next node $v_{i+1}$ is resolved via outgoing connection $(v_i, p_{\text{flow}}, v_{i+1}, p_{\text{in}})$.
   - If no next port is specified, the evaluator falls back to default execution sockets (`exec_out` or `out`).
   - If no outgoing wire exists, procedural execution completes successfully.

---

## 3. Hybrid Interleaved Execution

Real-world visual scripts frequently combine procedural branching with upstream mathematical calculations.

```text
[Sensor / XP Query] ──(Data Wire)──► [Check Level Gate] ──(Execution Wire)──► [Grant Perk]
                                           ▲
[Start Trigger] ──────(Execution Wire)────┘
```

When the execution pulse arrives at `Check Level Gate`, the gate requires data from `Sensor / XP Query`.

### 3.1 On-Demand Upstream Resolution
1. Before invoking `Check Level Gate`'s executor, the evaluator inspects all incoming data connections.
2. If an upstream data port has not yet emitted a value into `ExecutionContext`, the upstream node is marked as uncomputed.
3. The evaluator executes an **iterative depth-first post-order traversal** across uncomputed upstream nodes using an explicit heap stack (`Deque<NodeId>`).
4. **Call-Stack Invariance**: By replacing JVM method recursion with an explicit heap-allocated stack, chains of $2{,}000+$ upstream nodes evaluate safely with zero `StackOverflowError`.
5. Upstream nodes are evaluated in dependency order, outputs are injected into downstream input buffers, and the procedural control node resumes execution with fully populated inputs.

---

## 4. Next Steps

- Review concurrency guarantees in the [Threading Reference](threading.md).
- Understand exception handling boundaries in the [Error Handling Reference](error-handling.md).
