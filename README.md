# NodeForge

**A Reusable, Domain-Agnostic Minecraft Node Graph Architecture and Visual Editor Framework**

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%E2%89%A50.16.10-blue.svg)](https://fabricmc.net/)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Automated Tests](https://img.shields.io/badge/Tests-337%20passed-success.svg)](https://github.com/MinexInd/Nodeforge)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## What is NodeForge?

> **NodeForge: Build Anything.**

NodeForge is a generic node graph engine and interactive visual editor for Minecraft (Fabric 1.21.11, Java 21). It provides the mathematical, structural, execution, and rendering infrastructure for graph-based systems, freeing developers from reinventing custom editors and DAG solvers.

### What NodeForge Provides

* **Core Graph Infrastructure**: Typed sockets, cyclic/acyclic connections, synchronized mutations, comment boxes, and validation invariants.
* **Dual Execution Engines**: Pure data-flow topological evaluation (Kahn's algorithm) and procedural control-flow pulse execution (`nodeforge:exec`).
* **Interactive Visual Editor**: In-game GUI (`NodeEditorScreen`) with infinite pan, zoom-to-cursor projection, cubic Bézier spline cables, multi-selection marquee, and full undo/redo command history.
* **Visual Presentation**: Six built-in themes, custom node renderers, interactive property widgets, and easing curves.
* **Resource Graph Persistence**: Versioned JSON schema codecs and asynchronous Minecraft resource reloaders.
* **Extension SPI**: Fabric plugin discovery (`NodeForgePlugin`, `NodeForgeClientPlugin`, `CanvasLayer`).

### What NodeForge Is NOT

NodeForge contains **zero gameplay logic, zero datapack generators, zero skill trees, and zero AI behavior**. It is strictly domain-agnostic infrastructure. Your consumer mod defines the domain semantics:

```text
                 NodeForge
           Generic Infrastructure
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   Skill Mod     Quest Mod    Dialogue Mod
 (Player XP)    (Objectives)  (Conversations)
```

---

## Supported Environment

| Component | Minimum Requirement | Recommended |
| :--- | :--- | :--- |
| **Java** | JDK 21 | JDK 21+ (Adoptium Temurin / Microsoft OpenJDK) |
| **Minecraft** | 1.21.11 | 1.21.11 |
| **Fabric Loader** | 0.16.10 | 0.16.10+ |
| **Fabric API** | 0.115.0 | 0.115.0+ |
| **Environment** | Common (Client + Dedicated Server) | Editor requires Client environment |

---

## Quick Installation

### 1. Declare Gradle Dependency

Add NodeForge to your consumer mod's `build.gradle`:

```groovy
repositories {
    mavenCentral()
    mavenLocal()
    // Published Modrinth Maven releases
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "net.minex:nodeforge:1.0.0"
}
```

### 2. Declare Mod Dependency

Add NodeForge to your `fabric.mod.json`:

```json
{
  "depends": {
    "fabricloader": ">=0.16.10",
    "minecraft": "~1.21.11",
    "fabric-api": ">=0.115.0",
    "nodeforge": ">=1.0.0"
  }
}
```

---

## Minimal Example

Create, wire, and evaluate an arithmetic graph in 15 lines of common Java code:

```java
// 1. Create container and build nodes
Graph graph = new Graph("math_demo");
Node constA = Node.builder(NodeId.of("a"), "data:constant")
        .metadata("value", "10.0")
        .addPort(Port.output(PortId.of("value"), "Out", BuiltinPortTypes.FLOAT))
        .build();
Node constB = Node.builder(NodeId.of("b"), "data:constant")
        .metadata("value", "25.0")
        .addPort(Port.output(PortId.of("value"), "Out", BuiltinPortTypes.FLOAT))
        .build();
Node addNode = Node.builder(NodeId.of("add"), "math:add")
        .addPort(Port.input(PortId.of("a"), "A", BuiltinPortTypes.FLOAT))
        .addPort(Port.input(PortId.of("b"), "B", BuiltinPortTypes.FLOAT))
        .addPort(Port.output(PortId.of("result"), "Result", BuiltinPortTypes.FLOAT))
        .build();

graph.addNode(constA);
graph.addNode(constB);
graph.addNode(addNode);

// 2. Wire connections: a.value -> add.a, b.value -> add.b
graph.connect(constA.id(), PortId.of("value"), addNode.id(), PortId.of("a"));
graph.connect(constB.id(), PortId.of("value"), addNode.id(), PortId.of("b"));

// 3. Evaluate topologically
ExecutionContext ctx = ExecutionContext.create();
GraphEvaluator evaluator = new GraphEvaluator();
ExecutionSummary summary = evaluator.evaluateDataFlow(graph, ctx);

if (summary.isSuccess()) {
    double result = (Double) ctx.getOutputValue(addNode.id(), PortId.of("result"));
    System.out.println("Result: " + result); // Output: 35.0
}
```

---

## Documentation Index

NodeForge provides comprehensive, beginner-friendly technical guides:

### Getting Started
* [Installation Guide](docs/getting-started/installation.md) - Player vs. developer setup and Maven coordinates.
* [Project Setup](docs/getting-started/project-setup.md) - Mod structure, `fabric.mod.json`, and client/common separation.
* [Core Concepts](docs/getting-started/concepts.md) - Graphs, nodes, sockets, and execution mental models.
* [First Graph Tutorial](docs/getting-started/first-graph.md) - Build your first functional graph from scratch.

### Guides
* [Working with Graphs](docs/guides/graphs.md) - Container operations, queries, and comments.
* [Working with Nodes](docs/guides/nodes.md) - Node instances, builders, bounds, and properties.
* [Ports & Data Types](docs/guides/ports.md) - Sockets, built-in types, widening, and `nodeforge:any`.
* [Connections & Wiring](docs/guides/connections.md) - Wire management, fan-out, and invariants.
* [Graph Execution](docs/guides/execution.md) - Evaluator, step limits, async workflows, and runners.
* [Node Properties](docs/guides/properties.md) - Internal node state vs. dynamic port connections.
* [Graph Validation](docs/guides/validation.md) - Structural checks, custom rules, and diagnostics.
* [Resource Persistence](docs/guides/persistence.md) - JSON serialization and Minecraft resource reloaders.
* [Visual Editor](docs/guides/visual-editor.md) - Pan, zoom, marquee, minimap, and keyboard shortcuts.

### Customization
* [Custom Nodes](docs/customization/custom-nodes.md) - Implement custom definitions and executors.
* [Custom Renderers](docs/customization/node-renderers.md) - Create custom visual card layouts and textures.
* [Property Widgets](docs/customization/property-widgets.md) - Sliders, toggles, dropdowns, and custom inputs.
* [Visual Themes](docs/customization/themes.md) - Presets and custom color palettes.
* [Canvas Math](docs/customization/canvas.md) - Coordinate projections, zoom invariance, and culling.
* [Rendering & VFX](docs/customization/rendering.md) - Particle systems, cable impulses, and easing curves.

### Extensions
* [Plugin API](docs/extensions/plugin-api.md) - Register nodes and executors via `NodeForgePlugin`.
* [Client Plugin API](docs/extensions/client-plugin-api.md) - Register themes, renderers, and layers.
* [Canvas Layers](docs/extensions/canvas-layers.md) - Custom render pipeline overlays.

### Technical Reference
* [API Stability & Overview](docs/reference/api-overview.md) - SemVer guarantees and package tiers.
* [Execution Model](docs/reference/execution-model.md) - Topological ordering and procedural pulses.
* [Threading & Concurrency](docs/reference/threading.md) - Synchronized mutations and context isolation.
* [Error Handling](docs/reference/error-handling.md) - Exception boundaries and fault tolerance.
* [Compatibility Matrix](docs/reference/compatibility.md) - Minecraft, Java, and Fabric specifications.

### Examples & Troubleshooting
* [Minimal Graph Example](docs/examples/minimal-graph.md) - Complete standalone runnable evaluation class.
* [Custom Node Example](docs/examples/custom-node.md) - End-to-end definition, executor, and plugin registration.
* [Custom Editor Example](docs/examples/custom-editor.md) - Opening `NodeEditorScreen` from commands or items.
* [Common Problems](docs/troubleshooting/common-problems.md) - Solutions to crashes, cycles, and wiring issues.
* [Frequently Asked Questions (FAQ)](docs/troubleshooting/faq.md) - Architectural and operational FAQ.
* [Contributing Guide](docs/contributing.md) - Contributor rules, build commands, and PR checklist.

---

## Quality Assurance

NodeForge enforces empirical correctness through comprehensive automated testing:

* **Automated Tests**: 337 passing unit, stress, and integration tests across 73 suites.
* **Topological Scaling**: Kahn's sort verified on graphs of 5,000 nodes and 10,000 connections completing in $<50$ ms.
* **Serialization Invariance**: Full round-trip JSON serialization verified on 1,000-node graphs with zero property loss.
* **Fault Isolation**: Exception barriers prevent `VirtualMachineError` suppression while isolating faulty node executions.

---

## Contributing

We welcome contributions! Please review our [Contributing Guide](docs/contributing.md) for architectural invariants, coding guidelines, and pull request procedures.

---

## License

NodeForge is released under the **MIT License**. See [LICENSE](LICENSE) for details.
