# NodeForge Architecture Specification

**Formal Architecture, Execution Semantics, Concurrency Guarantees, and Coordinate Mathematics for NodeForge 1.0.0**

---

## 1. System Overview and Topology

NodeForge is a generic, domain-agnostic graph processing engine and interactive visual canvas editor for Minecraft Fabric (1.21.11, Java 21). The system establishes a strict architectural boundary between headless graph processing models (`src/main`) and hardware-accelerated client presentation systems (`src/client`).

Consumer mods register domain-specific node definitions, custom port types, and functional execution handlers. NodeForge manages graph relational integrity, topological dependency scheduling, asymptotic index maintenance, affine camera mathematics, spatial culling, command history rollbacks, and serialization codecs.

```text
+-------------------------------------------------------------------------+
|                           Consumer Mod Domain                           |
|       (Skill Trees, Tech Progressions, Quest Trees, Dialogue Graphs)    |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                   Public API (net.minex.nodeforge.api)                  |
|  +--------------------------------+ +--------------------------------+  |
|  | Graph Model Interfaces         | | Execution Contracts            |  |
|  | - Node, Port, Connection       | | - NodeExecutor, ExecutionResult|  |
|  | - Position, CommentBox         | | - ExecutionContext, Summary    |  |
|  +--------------------------------+ +--------------------------------+  |
|  +--------------------------------+ +--------------------------------+  |
|  | Port System                    | | Registry & Lifecycle SPI       |  |
|  | - PortType, BuiltinPortTypes   | | - NodeDefinitionRegistry       |  |
|  | - PortTypeRegistry             | | - NodeForgePlugin SPI          |  |
|  +--------------------------------+ +--------------------------------+  |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                 Core Engine (net.minex.nodeforge.core)                  |
|  +--------------------------------+ +--------------------------------+  |
|  | Synchronized Container         | | Execution Schedulers           |  |
|  | - Graph (Atomic Indices)       | | - GraphEvaluator Engine        |  |
|  | - TypeCompatibilityEngine      | | - TopologicalSorter (Kahn DAG) |  |
|  +--------------------------------+ +--------------------------------+  |
|  +--------------------------------+ +--------------------------------+  |
|  | Validation & Resilience        | | Resource Persistence           |  |
|  | - GraphValidator & Rules       | | - GraphResourceReloader        |  |
|  | - GraphSerializer (JSON)       | | - GraphCodec Data Serialization|  |
|  +--------------------------------+ +--------------------------------+  |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|             Client Architecture (net.minex.nodeforge.client)            |
|  +--------------------------------+ +--------------------------------+  |
|  | Interaction & State            | | Canvas Rendering Pipeline      |  |
|  | - NodeGraphEditorScreen        | | - CanvasRenderer (World/Screen)|  |
|  | - CommandStack (Undo/Redo)     | | - Viewport & Camera Mathematics|  |
|  | - SelectionModel & Gestures    | | - WireRenderer (Cubic Bezier)  |  |
|  +--------------------------------+ +--------------------------------+  |
|  +--------------------------------+ +--------------------------------+  |
|  | Visual Customization           | | Animation & VFX Engine         |  |
|  | - ThemeRegistry (Tokens)       | | - VfxManager & CanvasParticle  |  |
|  | - CanvasLayer SPI Pipeline     | | - AnimationTimeline & Tweens   |  |
|  +--------------------------------+ +--------------------------------+  |
+-------------------------------------------------------------------------+
```

---

## 2. Source Set Partitioning and Split-Environment Isolation

NodeForge enforces compile-time and runtime split-environment isolation via Fabric Loom:

```text
d:\nodeforge-template-1.21.11\
├── src/main/java/                     [COMMON SOURCE SET]
│   └── net/minex/nodeforge/
│       ├── api/                       Supported public API (SemVer 1.0.0 guarantee)
│       │   ├── execution/             ExecutionContext, NodeExecutor, ExecutionSummary
│       │   ├── graph/                 Node, Port, Connection, CommentBox, Position
│       │   ├── plugin/                NodeForgePlugin SPI, PluginContext, PluginManager
│       │   ├── port/                  PortType, PortTypeId, PortTypeRegistry, BuiltinPortTypes
│       │   ├── registry/              NodeDefinition, NodeDefinitionRegistry, GraphRegistry
│       │   ├── runner/                GraphRunner ergonomic facade
│       │   └── serialization/         GraphCodec, GraphSerializer, GraphSerializationException
│       └── core/                      Internal engine implementation
│           ├── execution/             GraphEvaluator, TopologicalSorter, BuiltinExecutors
│           ├── graph/                 Graph container, ConnectionResult
│           ├── id/                    NodeId, PortId, ConnectionId strongly-typed records
│           ├── port/                  TypeCompatibilityEngine
│           ├── resource/              GraphResourceReloader (Resource Persistence)
│           └── validation/            GraphValidator, ValidationError, NodeValidationRule
│
└── src/client/java/                   [CLIENT-ONLY SOURCE SET]
    └── net/minex/nodeforge/client/
        ├── animation/                 Easing curves, Tween, AnimationTimeline
        ├── api/                       Public client extension hooks (CanvasLayer, Theme SPI)
        ├── editor/                    NodeGraphEditorScreen, EditorState, SelectionModel
        │   ├── camera/                Viewport, GridSnap, CoordinateSystem
        │   ├── command/               UndoableCommand, CommandStack history
        │   ├── hud/                   Diagnostics HUD overlay
        │   ├── menu/                  ContextMenu, MenuItem, RadialLayout
        │   ├── minimap/               MinimapRenderer interactive radar
        │   └── palette/               NodeCreationPalette fuzzy-search catalog
        ├── plugin/                    NodeForgeClientPlugin, ClientPluginManager
        └── render/                    CanvasRenderer, WireRenderer, NodeWidget
            ├── layer/                 CanvasLayer, CanvasLayerRegistry, RenderPhase
            ├── shape/                 PortShapeRenderer (Circle, Square, Diamond, Triangle)
            ├── theme/                 EditorTheme, ThemeRegistry, Built-in presets
            └── vfx/                   VfxManager, CanvasParticle, CableImpulse
```

### 2.1 Environmental Boundary Invariant

Classes residing in `src/main` contain zero references to client-only Minecraft classes (`net.minecraft.client.*`, `DrawContext`, `Screen`, or OpenGL state). The graph model, topological sorter, type compatibility engine, schema serializer, and execution engine run headlessly on dedicated Minecraft servers, continuous integration containers, or background worker threads.

---

## 3. Graph Model and Relational Indexing

### 3.1 Mathematical Definition

A NodeForge graph is a directed, attributed multi-graph:

$$G = (V, E, M)$$

Where:
- $V$ is the set of vertices (nodes), where each $v \in V$ possesses a unique identifier $\text{id}(v) \in \text{NodeId}$, a position $\mathbf{p}_v \in \mathbb{R}^2$, and ordered port collections $P_{\text{in}}(v)$ and $P_{\text{out}}(v)$.
- $E$ is the set of directed edges (connections), where each $e \in E$ is a directed 4-tuple $(v_{\text{from}}, p_{\text{from}}, v_{\text{to}}, p_{\text{to}})$ such that $p_{\text{from}} \in P_{\text{out}}(v_{\text{from}})$ and $p_{\text{to}} \in P_{\text{in}}(v_{\text{to}})$.
- $M$ is a map of string key-value metadata pairs.

### 3.2 Relational Complexity and Indexing

To prevent $O(N)$ linear traversals during interactive canvas editing and execution, `Graph.java` maintains four concurrent relational hash indices:

```text
1. Primary Node Index:
   Map<NodeId, Node>                                  [O(1) lookup, LinkedHashMap preserves insertion order]

2. Connection Endpoint Uniqueness Index:
   Map<ConnectionEndpoints, ConnectionId>             [O(1) duplicate connection detection]
   where ConnectionEndpoints = (fromNode, fromPort, toNode, toPort)

3. Incident Node Adjacency Index:
   Map<NodeId, Set<ConnectionId>>                     [O(1) retrieval of all incident edges for node v]

4. Incident Port Adjacency Index:
   Map<PortEndpoint, Set<ConnectionId>>               [O(1) retrieval of connections targeting specific port]
   where PortEndpoint = (nodeId, portId)
```

Through these indices, deleting a node with degree $d = \text{deg}(v)$ executes in $O(d)$ time rather than $O(|E|)$, because all incident connection identifiers are retrieved directly without iterating the full edge collection.

---

## 4. Formal Execution Semantics

NodeForge provides three mutually compatible execution paradigms.

### 4.1 Paradigm A: Pure Data-Flow Evaluation

In a pure data-flow graph, edges denote functional data transmission. No procedural control execution ports are utilized.

1. **Topological Sort**: The evaluator executes Kahn's Algorithm on $G$, considering only data-typed connections. Execution connections (`isExecution() == true`) are ignored during data dependency sorting.
2. **In-Degree Computation**:

   $$\forall v \in V, \quad \text{deg}^{-}(v) = \sum_{e \in E_{\text{data}}} [\text{toNode}(e) = v]$$

3. **Queue Processing**: Nodes with $\text{deg}^{-}(v) = 0$ are enqueued. Upon dequeue of node $u$, $u$ is executed via its registered `NodeExecutor`. Emitted outputs are propagated to downstream input buffers in `ExecutionContext`. For each outgoing data connection $(u, p_u, w, p_w)$, $\text{deg}^{-}(w)$ is decremented. When $\text{deg}^{-}(w) = 0$, $w$ is enqueued.
4. **Circular Dependency Detection**: If the number of processed nodes is less than $|V|$, a cycle exists. The sort halts and throws `GraphCycleException`, reporting all nodes whose remaining in-degree exceeds zero.
5. **Asymptotic Complexity**: Time complexity is strictly $O(|V| + |E|)$. Space complexity is $O(|V| + |E|)$ for adjacency structures.

### 4.2 Paradigm B: Control-Flow Pulse Execution

In a procedural control-flow graph, execution models an instruction sequence governed by discrete trigger pulses.

1. **Entrypoint Initiation**: Execution begins at an explicit entrypoint node identifier $v_0$.
2. **Sequential Traversal**:
   - The evaluator fetches $v_i$ from $G$.
   - The step counter is incremented; if $\text{stepCount} > \text{maxSteps}$, execution halts with a step-limit error.
   - Node $v_i$'s registered executor executes: $R = \text{executor}.\text{execute}(v_i, \text{context})$.
   - If $R$ is `ExecutionResult.Halt`, execution terminates successfully.
   - If $R$ is `ExecutionResult.Failure`, execution halts immediately, returning failure diagnostics.
   - If $R$ is `ExecutionResult.Success`:
     - If an explicit next flow port $p_{\text{flow}}$ is returned, the next node $v_{i+1}$ is resolved via outgoing connection $(v_i, p_{\text{flow}}, v_{i+1}, p_{\text{in}})$.
     - Otherwise, the evaluator falls back to standard execution ports (`exec_out` or `out`).
     - If no outgoing execution connection exists, procedural execution terminates cleanly.

### 4.3 Paradigm C: Hybrid Interleaved Execution

Real-world visual scripts frequently combine procedural branching with upstream mathematical calculations or data queries.

When the control-flow pulse arrives at node $N$, $N$ may require input data from upstream nodes that do not participate in procedural execution wires:

1. **Upstream Dependency Discovery**: Before invoking $N$'s executor, the evaluator queries all non-execution input ports $p \in P_{\text{in}}(N)$. For any incoming connection $(u, p_u, N, p)$, if `context.getOutputValue(u, p_u)` is null, node $u$ is marked as pending.
2. **Iterative Heap-Stack Post-Order Traversal**: Upstream dependencies are traversed using an explicit heap stack (`Deque<NodeId>`) rather than JVM call-stack recursion:
   - Eliminates recursion limits, permitting arbitrarily deep upstream computation chains ($>2{,}000$ nodes) without encountering `StackOverflowError`.
   - Cyclic data paths in upstream dependencies are detected via an ancestor tracking set (`inStack`) and throw `GraphCycleException`.
3. **Execution and Propagation**: Each uncomputed upstream data node is executed in topological post-order, and its computed outputs are injected into downstream input buffers.
4. **Pulse Continuation**: Once all inputs for node $N$ are resolved, node $N$'s executor runs, emits its data outputs, and yields its next control-flow port.

---

## 5. Thread Safety and Concurrency Model

### 5.1 Internal Mutation Synchronization

The `Graph` container synchronizes all mutating methods using Java object monitor synchronization (`synchronized`). Operations including `addNode`, `removeNode`, `connect`, and `disconnect` are atomic with respect to the graph's internal index structures.

Methods returning collections (`getNodes()`, `getConnections()`) return unmodifiable copy snapshots (`List.copyOf(...)`), preventing `ConcurrentModificationException` if a collection is iterated while another thread inserts or removes an element.

### 5.2 Concurrent Evaluation Guarantees

Multiple threads can evaluate the same shared `Graph` instance simultaneously without lock contention under the following precondition:

> **Evaluation Invariant**: Each thread must evaluate using its own independent `ExecutionContext` instance.

Because runtime variables, input port buffers, and output port values are stored exclusively within `ExecutionContext` (backed by `ConcurrentHashMap`), evaluation passes do not mutate `Graph` state.

### 5.3 Semantic Immutability During Evaluation

While individual method calls on `Graph` are synchronized, a graph evaluation pass is a multi-step operation. The evaluator assumes that the graph topology remains semantically stable throughout the evaluation:

- If thread $T_1$ mutates graph topology (e.g. removes a node or severs a connection) while thread $T_2$ is in the middle of an evaluation loop, thread $T_2$ continues evaluating against the node sequence established at invocation start.
- If a consumer mod requires absolute atomicity across compound mutation and evaluation phases, the caller must coordinate access externally via monitor locking:

```java
synchronized (sharedGraph) {
    evaluator.evaluateDataFlow(sharedGraph, context);
}
```

---

## 6. JVM Error Handling and Failure Boundaries

To ensure robust host application stability, NodeForge enforces strict distinction between runtime errors and fatal JVM errors:

```text
                           java.lang.Throwable
                                    |
          +-------------------------+-------------------------+
          |                                                   |
 java.lang.Exception                                  java.lang.Error
          |                                                   |
   [DOMAIN FAILURE]                                   [FATAL SYSTEM FAULT]
   - Evaluator captures exception                     - OutOfMemoryError
   - Records in ExecutionSummary.Failure              - StackOverflowError
   - Host thread continues safely                     - VirtualMachineError
                                                              |
                                                      [RETHROWN IMMEDIATELY]
                                                      Never caught or masked
```

1. **Fatal JVM Errors (`VirtualMachineError`)**: Errors such as `OutOfMemoryError`, `StackOverflowError`, and `InternalError` are never intercepted or suppressed. Catch blocks throughout `GraphEvaluator`, `GraphValidator`, `PluginManager`, and `SelectionModel` explicitly rethrow `VirtualMachineError`, allowing the JVM or host crash-reporting infrastructure to act without corrupted state.
2. **Domain Fault Isolation**: User executor exceptions, syntax errors, and third-party validation rule crashes are intercepted and converted into structured error records (`ExecutionSummary.Failure`, `ValidationError`), ensuring that a single failing node does not crash the dedicated server or GUI loop.

---

## 7. Viewport Coordinate Mathematics

The visual canvas separates 2D World Space from Screen Pixel Space through affine camera transformations.

```text
World Space (Continuous R^2)                  Screen Space (Window Pixels)
Node Position (x_w, y_w) in double  ------->  Drawn at (x_s, y_s) in int
```

### 7.1 Forward Transformation (World to Screen)

Given a world coordinate vector $\mathbf{p}_w = (x_w, y_w) \in \mathbb{R}^2$, camera pan offset $\mathbf{c} = (c_x, c_y) \in \mathbb{R}^2$, zoom scale factor $s \in [0.1, 3.0]$, and screen center offset $\mathbf{o} = (w_{\text{screen}}/2, h_{\text{screen}}/2)$:

$$\mathbf{p}_s = (\mathbf{p}_w - \mathbf{c}) \cdot s + \mathbf{o}$$

Component-wise:

$$x_s = (x_w - c_x) \cdot s + \frac{w_{\text{screen}}}{2}$$

$$y_s = (y_w - c_y) \cdot s + \frac{h_{\text{screen}}}{2}$$

### 7.2 Inverse Transformation (Screen to World)

To convert mouse cursor coordinates $\mathbf{p}_s = (x_s, y_s)$ to world coordinates:

$$\mathbf{p}_w = \frac{\mathbf{p}_s - \mathbf{o}}{s} + \mathbf{c}$$

Component-wise:

$$x_w = \frac{x_s - w_{\text{screen}}/2}{s} + c_x$$

$$y_w = \frac{y_s - h_{\text{screen}}/2}{s} + c_y$$

### 7.3 Zoom-to-Cursor Invariance

When zooming via the mouse scroll wheel at screen point $\mathbf{p}_{\text{cursor}}$, the world coordinate under the cursor $\mathbf{p}_{\text{world\_cursor}}$ must remain invariant before and after the zoom adjustment:

$$\mathbf{p}_{\text{world\_cursor}} = \frac{\mathbf{p}_{\text{cursor}} - \mathbf{o}}{s_{\text{old}}} + \mathbf{c}_{\text{old}} = \frac{\mathbf{p}_{\text{cursor}} - \mathbf{o}}{s_{\text{new}}} + \mathbf{c}_{\text{new}}$$

Solving for the updated camera pan $\mathbf{c}_{\text{new}}$:

$$\mathbf{c}_{\text{new}} = \mathbf{c}_{\text{old}} + (\mathbf{p}_{\text{cursor}} - \mathbf{o}) \left( \frac{1}{s_{\text{old}}} - \frac{1}{s_{\text{new}}} \right)$$

This formula prevents visual jitter during zoom interactions by preserving the focal point under the user's cursor.

### 7.4 Spatial Viewport Culling

Prior to issuing draw commands for nodes, labels, and Bézier cables, the viewport computes the bounding frustum in world coordinates:

$$\text{Frustum} = \left[ \mathbf{p}_w(0, 0) - \mathbf{m}, \quad \mathbf{p}_w(w_{\text{screen}}, h_{\text{screen}}) + \mathbf{m} \right]$$

Where $\mathbf{m} = (64, 64)$ is a safety margin accommodating node header drop shadows and socket radii. Nodes whose bounding boxes do not intersect this rectangle are culled immediately, preserving rendering performance on graphs containing thousands of nodes.

---

## 8. Layered Canvas Rendering Pipeline

Rendering executes in six sequential phases via `CanvasLayerRegistry`:

```text
Render Phase               Space         Description
---------------------------------------------------------------------------------------------
1. PRE_GRID                World         Custom canvas backgrounds, decorative underlays
2. POST_GRID               World         Coordinate grid (dots, lines, crosses), comment boxes
3. POST_CONNECTIONS        World         Cubic Bezier cables, connection flow impulses
4. POST_NODES              World         Node cards, headers, sockets, inline port widgets
5. POST_SELECTION          World         Active dragging cable, selection halos, particle VFX
6. SCREEN_OVERLAY          Screen        Marquee rectangle, minimap radar, diagnostics HUD,
                                         context menus, node creation palette, tooltips
```

---

## 9. Minecraft Resource Graph Persistence

Persistence is decoupled from vanilla gameplay mechanics:

- **Data Serialization**: `GraphCodec` and `GraphSerializer` serialize and deserialize graph structures to JSON conforming to a standardized schema (`schema_version: 1`).
- **Resource Reloading**: `GraphResourceReloader` implements Fabric's `IdentifiableResourceReloadListener` interface.
- **Parametric Storage Paths**: Consumers instantiate the reloader with custom paths (e.g. `data/<namespace>/skill_trees/*.json`) and custom listener identifiers, maintaining isolation between different mods using NodeForge.
- **Atomic Registration**: Reloaded graphs are validated for duplicate node IDs, valid connections, and type compatibility prior to atomic registration in `GraphRegistry`.

---

## 10. Complete Development Phase Mapping (Phases 1–16)

NodeForge was developed across sixteen structured engineering milestones:

| Phase | Milestone Title | Primary Architectural Deliverables |
| :---: | :--- | :--- |
| **1** | Core Graph Architecture | Directed multi-graph container (`Graph`), `Node`, `Port`, `Connection`, relational hash indices. |
| **2** | Serialization & Codecs | `GraphCodec`, `GraphSerializer`, JSON schema validation, cycle prevention. |
| **3** | Canvas Coordinate Mathematics | `Viewport`, `CoordinateSystem`, affine matrix projections, zoom-to-cursor invariance. |
| **4** | Node Visual System | `NodeWidget`, header layout, socket positioning, category styling. |
| **5** | Wire Routing & Cables | `WireRenderer`, cubic Bézier splines, distance hit-testing, port dragging. |
| **6** | Manipulation & Selection | Marquee selection, multi-node dragging, box culling, pan gestures. |
| **7** | Command Pattern & History | `CommandStack`, atomic `UndoableCommand` implementations (`Add`, `Remove`, `Move`, `Connect`). |
| **8** | Context Menus & Palette | `ContextMenu`, `NodeCreationPalette`, fuzzy search, category filtering. |
| **9** | Execution Engine | `GraphEvaluator`, `TopologicalSorter` (Kahn's DAG), `ExecutionContext`, step limits. |
| **10** | UX Refinements | Minimap radar (`MinimapRenderer`), clipboard copy-paste with wire preservation, grid snapping. |
| **11** | Inspector & Widgets | Property inspector, inline port widgets, sliders, text fields, value binding. |
| **12** | VFX & Visual Polish | `VfxManager`, `CanvasParticle`, `CableImpulse`, 12 mathematical easing curves, 6 theme presets. |
| **13** | Extension SPI | `NodeForgePlugin`, `NodeForgeClientPlugin`, Fabric entrypoints, lifecycle sequencing. |
| **14** | Developer Experience & Demos | Math demo plugin, dialogue workflow demo, sample graphs, debugging HUD. |
| **15** | Stress Testing & Hardening | 5,000-node benchmarks, 2,000-depth stack safety, numeric limit validation. |
| **16** | Release Readiness & Adversarial QA | Methodology disclosure, fatal error hardening, documentation audit, zero-emoji compliance. |
