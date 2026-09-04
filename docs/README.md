# NodeForge Documentation

**A Domain-Agnostic Node Graph Architecture and Interactive Visual Editor Framework for Minecraft (Fabric 1.21.11, Java 21)**

Welcome to the central documentation library for NodeForge. This documentation provides comprehensive guides, architectural references, and copy-paste-ready code examples for developing with NodeForge across dedicated servers and graphical clients.

---

## 1. What is NodeForge?

NodeForge is a generic, reusable framework designed to solve a recurring problem in Minecraft modding: building node-based systems without writing custom graph data structures, math projections, canvas renderers, or serialization codecs from scratch.

NodeForge manages graph topology, execution scheduling, affine camera projections, undo-redo stacks, visual themes, and persistence. Your mod provides the domain semantics: the nodes, the data types, and the execution logic.

```text
+-------------------------------------------------------------------------+
|                           Consumer Mod Domain                           |
|       (Skill Trees, Tech Progressions, Quest Trees, Dialogue Graphs)    |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                   Public API (net.minex.nodeforge.api)                  |
|  - Graph Model Interfaces          - Execution Contracts                |
|  - Port System & Registries        - Extension SPI & Lifecycle          |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                 Core Engine (net.minex.nodeforge.core)                  |
|  - Graph Container (Indices)       - Execution Schedulers (Kahn DAG)    |
|  - Type Compatibility Engine       - Resource Persistence Reloader      |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|             Client Architecture (net.minex.nodeforge.client)            |
|  - NodeEditorScreen (GUI)          - CanvasRenderer (World / Screen)    |
|  - CommandStack (Undo / Redo)      - Viewport Mathematics & Culling     |
|  - ThemeRegistry (Visual Tokens)   - Animation & VFX Engine             |
+-------------------------------------------------------------------------+
```

---

## 2. What NodeForge Is NOT

To maintain architectural purity and prevent coupling, NodeForge enforces strict domain boundaries:

- **Not a Datapack Framework**: NodeForge provides generic JSON codecs and resource reload listeners. It does not dictate gameplay datapack formats.
- **Not a Skill-Tree Mod**: NodeForge provides graph structures, sockets, and canvas screens. It contains zero concepts of player XP, levels, or abilities.
- **Not a Gameplay System**: NodeForge contains zero references to Minecraft entities, blocks, items, commands, or scoreboards.
- **Not a Monolithic Tool**: NodeForge is designed as a library dependency (`modImplementation`) that runs both headlessly on dedicated servers and interactively on graphical clients.

---

## 3. Documentation Index

### Getting Started
- [Installation](getting-started/installation.md): Adding NodeForge to your development environment and Gradle build.
- [Project Setup](getting-started/project-setup.md): Configuring `fabric.mod.json`, dependencies, and split-environment source sets.
- [Core Concepts](getting-started/concepts.md): Fundamental graph terminology (nodes, ports, connections, contexts).
- [First Graph Tutorial](getting-started/first-graph.md): A 5-minute hands-on walkthrough building and evaluating an arithmetic graph.

### Core Guides
- [Graph API](guides/graphs.md): Graph lifecycle, mutations, queries, cascading deletions, and comment boxes.
- [Node API](guides/nodes.md): Node instances, builders, port assignments, positions, and bounds.
- [Ports & Types](guides/ports.md): Data ports, execution ports, type compatibility, widening, and wildcard sockets.
- [Connections](guides/connections.md): Wiring rules, duplicate prevention, self-connection rejection, and disconnection.
- [Execution Engine](guides/execution.md): Topological sort, procedural control pulses, variable contexts, and step limits.
- [Properties](guides/properties.md): Node property bags and bidirectional UI value binding.
- [Validation](guides/validation.md): Graph validation, schema rules, and third-party custom validation rules.
- [Resource Persistence](guides/persistence.md): Serializing graphs, JSON schema v1, and Fabric resource reloading.
- [Visual Editor Screen](guides/visual-editor.md): Opening the GUI, gestures, panning, zooming, minimap, and shortcuts.

### Customization
- [Custom Nodes](customization/custom-nodes.md): Defining, registering, executing, and styling custom node archetypes.
- [Custom Node Renderers](customization/node-renderers.md): Overriding node card visual layout and drawing custom graphics.
- [Property Widgets](customization/property-widgets.md): Using and extending inline sliders, text inputs, toggles, and pickers.
- [Themes](customization/themes.md): Theme tokens, switching themes, built-in presets, and custom socket geometry.
- [Canvas & Camera Mathematics](customization/canvas.md): World space vs screen space, coordinate projections, and culling.
- [Rendering & VFX](customization/rendering.md): Particle systems, cable impulses, easing mathematics, and accessibility.

### Extensions & SPI
- [Common Plugin API](extensions/plugin-api.md): Extending NodeForge headlessly via `NodeForgePlugin`.
- [Client Plugin API](extensions/client-plugin-api.md): Client UI extensions via `NodeForgeClientPlugin`.
- [Canvas Layers](extensions/canvas-layers.md): Hooking custom render passes across the 5 canvas phases.

### Technical Reference
- [API Stability & Package Matrix](reference/api-overview.md): Public supported APIs vs internal implementation classes.
- [Execution Model Specification](reference/execution-model.md): Mathematical semantics for data-flow, control-flow, and hybrid models.
- [Concurrency & Threading](reference/threading.md): Exact thread-safety guarantees and evaluation immutability constraints.
- [Error Handling & Isolation](reference/error-handling.md): Error taxonomy, failure summaries, and fatal JVM error propagation.
- [Compatibility & Requirements](reference/compatibility.md): Platform targets, Java 21 requirements, and versioning rules.

### Examples
- [Minimal Graph Example](examples/minimal-graph.md): Standalone pure-Java execution example.
- [Custom Node Example](examples/custom-node.md): Complete custom node definition and executor implementation.
- [Custom Editor Integration](examples/custom-editor.md): Launching `NodeEditorScreen` from items, blocks, or client commands.

### Troubleshooting & Contributing
- [Common Problems](troubleshooting/common-problems.md): Diagnostics for dependency conflicts, classpath leaks, and rejected connections.
- [FAQ](troubleshooting/faq.md): Frequently asked questions.
- [Contributing Guide](contributing.md): Coding guidelines, testing procedures, and architecture invariants.
