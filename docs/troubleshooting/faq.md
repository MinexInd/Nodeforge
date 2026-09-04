# Frequently Asked Questions (FAQ)

This document answers common architectural, operational, and development questions regarding NodeForge 1.0.0.

---

## General & Architectural

### Is NodeForge a datapack editor?

**No.** NodeForge is a domain-agnostic node graph infrastructure and visual editor framework. It provides generic nodes, ports, connections, topological execution, validation, canvas rendering, and serialization. It contains zero datapack generation, zero skill tree logic, and zero gameplay rules. Your consumer mod defines the domain semantics.

```text
                 NodeForge
          (Generic Infrastructure)
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   Skill Mod     Quest Mod    Dialogue Mod
 (Player XP)    (Objectives)  (Conversations)
```

---

### Can NodeForge run on a headless dedicated server?

**Yes.** NodeForge is strictly decoupled into common and client modules:
* **Common API & Core** (`net.minex.nodeforge.api.*`, `net.minex.nodeforge.core.*`): Fully operational on headless dedicated servers and standalone Java 21 runtimes without any AWT, Swing, OpenGL, or client Minecraft classes.
* **Client Module** (`net.minex.nodeforge.client.*`): Contains the canvas renderer, shaders, themes, and `NodeEditorScreen`. These are loaded only when running the Minecraft client.

---

### What Minecraft and Fabric versions are supported?

NodeForge 1.0.0 requires:
* **Java**: 21 or newer
* **Minecraft**: `~1.21.11`
* **Fabric Loader**: `>=0.16.10`
* **Fabric API**: `>=0.115.0`

---

## Execution & Runtime

### Can graphs contain circular connections (cycles)?

It depends on the execution mode:
* **Data-flow graphs** (`evaluateDataFlow`): Must be Directed Acyclic Graphs (DAGs). Circular data dependencies ($A \to B \to A$) are mathematically invalid in a single evaluation tick and will be rejected by `GraphValidator` and `GraphEvaluator`.
* **Procedural control-flow graphs** (`executeControlFlow`): Can contain loops and branches using execution ports (`nodeforge:exec`). Loop execution is bounded by the step limit defined on `ExecutionContext` (default: 10,000 steps) to prevent server hangs.

---

### Is graph execution thread-safe?

* **Context Isolation**: Each evaluation tick creates its own `ExecutionContext`. Multiple evaluations can run concurrently across different worker threads as long as they operate on separate contexts.
* **Graph Mutation**: Mutations (`addNode`, `removeNode`, `connect`) are synchronized on the `Graph` instance. However, you should not mutate the structure of a graph while an evaluation is actively traversing it.
* **Background Tasks**: Use `evaluator.evaluateDataFlowAsync()` or `ForkJoinPool` to offload heavy computations from the main server tick thread.

---

### What happens when an executor throws an exception?

`GraphEvaluator` wraps all executor invocations in structured exception boundaries:
1. **Application Exceptions**: Caught, isolated, and converted into `ExecutionSummary.failure(errorMessage)`. The server tick thread remains alive.
2. **VirtualMachineError** (e.g. `OutOfMemoryError`, `StackOverflowError`): Immediately rethrown to allow the JVM to handle catastrophic resource exhaustion.

---

## Editor & Rendering

### Can I customize the visual appearance of nodes?

**Yes.** You can customize node visuals at two levels:
1. **Themes**: Register a custom `NodeTheme` to customize background, header, socket, and cable colors.
2. **Custom Renderers**: Implement `CustomNodeRenderer` to completely override the card drawing logic for specific node types, rendering custom textures, bars, or interactive elements.

---

### How do I open the editor from my mod?

From your client code (`src/client/java`):

```java
MinecraftClient.getInstance().send(() -> {
    NodeEditorScreen screen = new NodeEditorScreen(Text.literal("My Editor"), graph);
    MinecraftClient.getInstance().setScreen(screen);
});
```

---

## Persistence & Networking

### How do I save and load graphs?

NodeForge provides `GraphSerializer`, which encodes and decodes graphs to and from standard JSON schema v1:

```java
// Serialization
String json = GraphSerializer.toJson(graph);

// Deserialization
Graph restoredGraph = GraphSerializer.fromJson(json);
```

---

### How do I synchronize graphs between server and client?

NodeForge does not enforce a proprietary networking protocol. Because `GraphSerializer` outputs clean JSON strings, you can transmit graphs using standard Fabric networking (`ServerPlayNetworking` and `ClientPlayNetworking`) payloads:

```java
// Server sending to client
CustomPayload payload = new GraphSyncPayload(graph.id(), GraphSerializer.toJson(graph));
ServerPlayNetworking.send(player, payload);
```

The client receives the JSON payload, reconstructs the `Graph` via `GraphSerializer.fromJson(json)`, and passes it to `NodeEditorScreen`.
