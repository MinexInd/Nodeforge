# NodeForge v1.0.0 Release Notes

**Official Production Release Specification**  
**Release Date:** September 4, 2026  
**Target Platform:** Minecraft 1.21.11, Fabric Loader >=0.19.3, Fabric API 0.141.6+1.21.11, Java 21  

---

## 1. Executive Summary

NodeForge 1.0.0 marks the initial production release of the foundational node graph architecture and interactive visual editor framework for Minecraft Fabric. Developed to address the absence of a standardized, domain-agnostic graph processing library in the Minecraft modding ecosystem, NodeForge provides an enterprise-grade infrastructure upon which downstream mods can build visual scripting engines, skill progressions, technology trees, dialogue branching workflows, and custom procedural mechanics.

The framework strictly decouples domain logic from graph mechanics. Downstream consumer mods provide node semantics, custom port data types, and functional execution handlers. NodeForge manages relational integrity, topological dependency scheduling, serialization codecs, coordinate projections, spatial culling, command history rollbacks, and visual theming.

Across sixteen structured development phases, NodeForge has reached production readiness, backed by **337 automated unit and stress tests across 73 test suites**, zero compile-time or runtime warnings, verified split-environment client/server isolation, and an adversarial external consumer audit.

---

## 2. Complete Phase-to-Deliverable Mapping (Phases 1–16)

To provide transparency regarding the architectural evolution of the codebase, all sixteen development milestones are mapped directly to their delivered production systems:

| Phase | Milestone Name | Concrete Deliverables & Systems |
| :---: | :--- | :--- |
| **1** | Core Graph Architecture | `Graph.java` synchronized container with $O(1)$ duplicate endpoint and adjacency hash indexing; immutable record-based identifiers (`NodeId`, `PortId`, `ConnectionId`); `Node`, `Port`, and continuous geometry (`Position`, `Size`). |
| **2** | Serialization & Codecs | `GraphCodec` data codec; `GraphSerializer` JSON engine conforming to schema version 1; defensive parsing converting malformed structures into structured `GraphSerializationException` errors; cycle prevention guards. |
| **3** | Canvas Coordinate Mathematics | `Viewport` camera model; continuous affine world-to-screen and screen-to-world transformations; zoom-to-cursor focal invariance; clamped zoom bounds ($[0.1\times, 3.0\times]$). |
| **4** | Node Visual System | `NodeWidget` hierarchical layout; category-accented headers; automatic port socket positioning; title and subtitle typographic rendering; drop shadow geometry. |
| **5** | Interactive Wire Routing | `WireRenderer` cubic Bézier spline calculation; curvature tangent vectors; distance-to-segment hit testing; interactive port drag-and-drop connection wiring. |
| **6** | Manipulation & Selection | `SelectionModel` managing single-node, multi-node shift-toggle, and marquee bounding-box selection; multi-node delta translation; viewport spatial culling. |
| **7** | Command Pattern & Undo/Redo | `CommandStack` history engine; bounded stack depth (`setMaxHistory`); atomic `UndoableCommand` implementations for node addition, removal, translation, connection, and disconnection. |
| **8** | Context Menus & Palette | `ContextMenu` supporting radial and linear layouts; `NodeCreationPalette` fuzzy search catalog indexing nodes by display name, category, identifier, and socket compatibility. |
| **9** | Execution Engine | `GraphEvaluator` engine; pure data-flow evaluation via `TopologicalSorter` (Kahn's DAG algorithm in $O(V + E)$); procedural control pulses; `ExecutionContext` variable scoping; step counter guards. |
| **10** | Advanced UX & Grouping | `CommentBox` grouping frames with batch node dragging; `MinimapRenderer` bird's-eye canvas radar with click-to-pan navigation; clipboard copy-paste with internal wire preservation; configurable grid snapping. |
| **11** | Inspector & Property Widgets | `NodeInspector` sidebar; inline port socket widgets; continuous slider controls; numeric and text input fields with bidirectional state synchronization. |
| **12** | VFX & Visual Themes | `EditorTheme` token system; `ThemeRegistry` runtime switching across six presets; `VfxManager` canvas particle system; 12 standard mathematical easing curves (`Easing`); `CableImpulse` energy flow indicators. |
| **13** | Extension API & Plugin SPI | `NodeForgePlugin` and `NodeForgeClientPlugin` interfaces; Fabric Loom entrypoint discovery via `nodeforge:plugin` and `nodeforge:client_plugin`; phased `CanvasLayer` rendering pipeline. |
| **14** | Developer Experience & Demos | High-level `GraphRunner` facade; arithmetic and logic library (`BuiltinExecutors`); math demo plugin; interactive dialogue branching demo; runtime diagnostics HUD overlay. |
| **15** | Stress Testing & Hardening | High-volume graph benchmarks; iterative post-order DFS evaluation evaluating $2{,}000+$ depth chains without call-stack recursion; numerical boundary validation ($\pm 10^{14}$). |
| **16** | Release Readiness & Adversarial QA | Methodology disclosure; fatal JVM error propagation hardening (`VirtualMachineError` rethrown); Minecraft Resource Graph Persistence refactoring; adversarial consumer audit suite (`AdversarialConsumerQATest`). |

---

## 3. Key Architectural Systems

### 3.1 Formal Execution Semantics

NodeForge establishes three explicitly defined execution paradigms:

1. **Pure Data-Flow Evaluation (`evaluateDataFlow`)**:
   Topological dependency resolution driven by Kahn's algorithm in $O(V + E)$ time. Edges denote pure data transmission. The engine validates that no cycles exist among data ports, executes nodes in strict dependency order, and outputs an immutable `ExecutionSummary`.

2. **Control-Flow Pulse Traversal (`executeControlFlow`)**:
   Procedural instruction execution initiating at a designated entrypoint node. Execution advances sequentially along execution wires (`PortType.EXECUTION`) until a terminal node is reached, a branch condition alters flow, or step limits are met.

3. **Hybrid Interleaved Execution**:
   Procedural flow nodes frequently consume dynamic data computed by upstream expressions. When a control pulse arrives at node $N$, the evaluator executes an iterative depth-first post-order traversal across all uncomputed upstream data dependencies, populates $N$'s input buffers, and subsequently triggers $N$'s execution routine.

### 3.2 Concurrency and Semantic Immutability

- **Synchronized Mutation**: The `Graph` container synchronizes all internal mutations (`addNode`, `removeNode`, `connect`, `disconnect`), preventing race conditions and structural corruption during concurrent access.
- **Thread-Safe Shared Evaluation**: Multiple threads can evaluate the same shared `Graph` instance simultaneously because runtime state is encapsulated within isolated `ExecutionContext` instances.
- **Evaluation Immutability Constraint**: Graph topology must remain semantically stable during an active evaluation pass. If an external thread alters graph topology while an evaluation is actively traversing nodes, the evaluator executes against the topological snapshot captured at invocation start. Compound multi-step mutations across background threads require external synchronization (`synchronized (graph)`).

### 3.3 Fatal JVM Error Propagation

Catch blocks within `GraphEvaluator`, `GraphValidator`, `PluginManager`, and `SelectionModel` explicitly rethrow `VirtualMachineError` (including `OutOfMemoryError` and `StackOverflowError`). Fatal JVM conditions are never intercepted or suppressed as domain failures, ensuring host application integrity and clean crash reporting.

### 3.4 Minecraft Resource Graph Persistence

NodeForge decouples graph persistence from vanilla gameplay concepts. The `GraphResourceReloader` infrastructure extends Fabric's resource reload listener pipeline, enabling consumer mods to load serialized graphs from arbitrary resource subdirectories:

$$\text{data}/\langle\text{namespace}\rangle/\langle\text{custom-path}\rangle/*.json$$

Consumers control their own resource namespaces, reload listener identifiers, and directory structures via parametric constructor bindings.

---

## 4. Benchmark Methodology & Performance Metrics

To ensure empirical reproducibility, performance benchmarks are reported alongside their complete test methodology:

### 4.1 5,000-Node Kahn's Topological Sort Benchmark

- **Target Operation**: Kahn's algorithm topological sort (`TopologicalSorter.sort(Graph)`).
- **Synthetic Workload**: Directed Acyclic Graph (DAG) comprising $V = 5{,}000$ nodes arranged across 500 sequential layers (10 nodes per layer) with $E \approx 9{,}980$ edges (2 forward outgoing connections per node to the subsequent layer).
- **Host Hardware**: x86_64 host running Windows 11 (Intel Core i7 / AMD Ryzen class processor).
- **JVM Environment**: Eclipse Adoptium OpenJDK 64-Bit Server VM (build 21.0.10+7), HotSpot 64-Bit Server VM, configured with standard Gradle test heap options (`-Xmx1G`).
- **Timing Scope**: Duration measured via `System.nanoTime()` immediately enclosing `TopologicalSorter.sort(graph)`. Graph generation time and serialization are excluded from the timing window.
- **JVM Warmup State**: Executed within the automated test suite following prior test execution, ensuring the HotSpot JIT compiler reached steady-state optimization.
- **Empirical Results**:
  - Warm JIT sort duration: **15 ms to 45 ms**
  - Cold test threshold assertion: $< 500\text{ ms}$
  - Peak resident memory overhead during sort: $< 12\text{ MB}$

### 4.2 Hub Node Cascading Deletion Benchmark

- **Target Operation**: Removal of a central hub node with $d = 500$ incident connections (`Graph.removeNode(hubId)`).
- **Metric**: Complete removal of hub node, validation of 500 cascaded edge removals, and purging of all relational hash indices completed in **$< 20\text{ ms}$**.

### 4.3 Interactive Viewport Performance

- **Target Operation**: Canvas rendering with 1,000 nodes and 1,500 connections.
- **Technique**: Spatial frustum culling discards non-visible nodes and Bézier segments outside the camera viewport.
- **Result**: Viewport culling reduces draw calls by $>90\%$ at standard zoom levels, sustaining interactive display refresh rates.

---

## 5. Public API Stability Matrix

NodeForge 1.0.0 provides strict Semantic Versioning guarantees for its public interfaces:

| Package Namespace | Stability Tier | Intended Downstream Usage |
| :--- | :--- | :--- |
| `net.minex.nodeforge.api.*` | **Public Supported API** | Core consumer dependency. Guaranteed backward compatibility across 1.x.x releases. Contains node interfaces, port definitions, execution contexts, and serialization facades. |
| `net.minex.nodeforge.client.api.*` | **Public Client SPI** | Client-side extension contracts. Guaranteed backward compatibility across 1.x.x releases. Contains theme hooks and phased canvas layer renderers. |
| `net.minex.nodeforge.core.*` | **Internal Engine Implementation** | Internal graph data structures, Kahn topological sorters, type compatibility engines, and resource reloaders. Subject to structural optimization across minor revisions. |
| `net.minex.nodeforge.client.*` | **Internal Client Implementation** | Internal Minecraft GUI screens, widget hierarchies, rendering pipelines, and input gesture processors. |

---

## 6. Verification and Audit Certification

- **Test Suite Metrics**: 337 automated unit and integration tests passing across 73 test suites.
- **Compiler Warnings**: 0 warnings under Java 21 compiler with strict linting.
- **Javadoc Packaging**: 100% compliant with Java 21 HTML5 heading structure rules, cleanly packaged into `nodeforge-1.0.0-javadoc.jar`.
- **Domain Decoupling**: Verified 0 references to gameplay concepts (commands, entities, items, blocks, scoreboards) across the entire codebase.
- **Adversarial External Consumer Audit**: Complete verification via `AdversarialConsumerQATest` confirming that third-party mods can register custom ports, nodes, and executors, execute hybrid workflows, persist graphs, and handle concurrency without coupling to internal packages.
