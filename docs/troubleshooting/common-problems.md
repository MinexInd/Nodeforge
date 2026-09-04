# Common Problems & Troubleshooting

This guide catalogues real-world issues encountered when integrating NodeForge 1.0.0, explaining root causes, diagnostic symptoms, and exact solutions.

---

## 1. Dedicated Server Crashes with ClassNotFoundException

### Symptoms

When launching a headless dedicated server, the server immediately crashes with an error trace similar to:

```text
java.lang.NoClassDefFoundError: net/minecraft/class_437 (net.minecraft.client.gui.screen.Screen)
    at com.example.mymod.item.EditorItem.use(EditorItem.java:24)
    ...
Caused by: java.lang.ClassNotFoundException: net.minecraft.client.gui.screen.Screen
```

### Root Cause

Minecraft dedicated servers do not bundle graphical libraries, OpenGL contexts, or client GUI classes. If common code (`src/main/java`) directly imports or references `NodeEditorScreen` or any class under `net.minex.nodeforge.client.*`, the JVM attempts to link client classes during class-loading, causing an immediate crash.

### Solution

Strictly separate common and client source sets:

1. **Keep common logic clean**: Never import `net.minex.nodeforge.client.*` inside `src/main/java`.
2. **Use client-side entrypoints**: Register screens and commands in `src/client/java` via the `"client"` entrypoint in `fabric.mod.json`.
3. **Bridge via environment checks**: If triggered by an item or block in common code, verify `world.isClient()` before dispatching to a client helper:

```java
// Common code (src/main/java)
if (world.isClient()) {
    ClientScreenOpener.open(player, graph);
}

// Client helper (src/client/java)
public class ClientScreenOpener {
    public static void open(PlayerEntity player, Graph graph) {
        MinecraftClient.getInstance().setScreen(new NodeEditorScreen(Text.literal("Editor"), graph));
    }
}
```

---

## 2. Connection Rejected (ConnectionResult Failed)

### Symptoms

Attempting to connect two ports via `graph.connect()` returns a failed `ConnectionResult`, or the visual editor refuses to create a cable between two sockets.

```text
ConnectionResult[success=false, error="Cannot connect incompatible types: nodeforge:exec -> nodeforge:float"]
```

### Root Cause

NodeForge strictly validates port compatibility at wiring time:
1. **Flow vs Data**: An `EXECUTION` port cannot be wired to a `DATA` port.
2. **Direction Invariant**: You cannot connect an `INPUT` to an `INPUT`, or an `OUTPUT` to an `OUTPUT`.
3. **Type Compatibility**: Types must be identical, or satisfy numeric widening (e.g. `INT` $\to$ `FLOAT`), or target `nodeforge:any`.
4. **Cycle Prevention**: Pure data connections that would introduce a directed cycle are rejected.

### Solution

Verify port configurations:

```java
// Inspect port properties
Port sourcePort = sourceNode.getPort(fromPortId);
Port targetPort = targetNode.getPort(toPortId);

System.out.println("Source: " + sourcePort.direction() + " / " + sourcePort.type().id());
System.out.println("Target: " + targetPort.direction() + " / " + targetPort.type().id());
```

Ensure that:
* `sourcePort.isOutput() == true`
* `targetPort.isInput() == true`
* Both ports share compatible types, or use `BuiltinPortTypes.ANY` if dynamic typing is required.

---

## 3. Custom Node Does Not Appear in Creation Palette

### Symptoms

In `NodeEditorScreen`, right-clicking or opening the node creation palette does not display your custom node type.

### Root Cause

The node definition was not registered with the global `NodeDefinitionRegistry` before the editor initialized.

### Solution

Verify the following three steps:

1. **Verify `NodeForgePlugin` implementation**:
   ```java
   public class MyPlugin implements NodeForgePlugin {
       @Override
       public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
           registry.register(MyCustomNodeDefinition.create());
       }
   }
   ```
2. **Verify `fabric.mod.json` declaration**:
   ```json
   "entrypoints": {
     "nodeforge": [
       "com.example.mymod.plugin.MyPlugin"
     ]
   }
   ```
3. **Check console logs during game boot**: Look for:
   `[NodeForge] Discovered plugin 'mymod'` or any initialization stack traces.

---

## 4. Evaluation Returns Failure: "Cycle detected"

### Symptoms

Calling `evaluator.evaluateDataFlow(graph, context)` returns an `ExecutionSummary` with `isSuccess() == false` and the error:

```text
Evaluation failed: Cycle detected in data-flow graph: [node_a -> node_b -> node_c -> node_a]
```

### Root Cause

`GraphEvaluator.evaluateDataFlow` resolves dependencies using Kahn's topological sort algorithm. A data-flow graph represents instantaneous functional dependencies; a cycle means value $A$ depends on $B$, which depends on $C$, which depends on $A$. This is mathematically impossible to evaluate in a single evaluation tick.

### Solution

* **For continuous loops**: Convert cyclic feedback loops into procedural control flow using execution ports (`nodeforge:exec`) and `flow:branch` nodes.
* **For state retention**: Break the cycle using stateful variables via `data:set_variable` and `data:get_variable`, which evaluate across separate ticks rather than a single topological pass.

---

## 5. Step Limit Exceeded (max: 10000)

### Symptoms

Graph evaluation terminates abruptly with:

```text
Evaluation failed: Step limit exceeded (max: 10000)
```

### Root Cause

To protect the server thread from runaway loops, unbounded recursions, or denial-of-service graphs, `ExecutionContext` increments a step counter on every node invocation. If `stepCount >= maxSteps`, execution halts immediately.

### Solution

1. **Inspect procedural loops**: Ensure control-flow branch conditions actually evaluate to `false` at some point to exit the loop.
2. **Increase step limit for complex graphs**: If your graph legitimately contains thousands of nodes:
   ```java
   ExecutionContext context = ExecutionContext.create();
   context.setMaxSteps(50000); // Increase limit
   ```

---

## 6. Port Value Missing / Null in NodeExecutor

### Symptoms

Inside `NodeExecutor.execute(node, context)`, calling `context.getInputValue(node.id(), PORT_ID)` returns `null`, even though a cable appears connected in the visual editor.

### Root Cause

* **Port ID String Mismatch**: The identifier used in `NodeDefinition.builder().inputPort("input_val", ...)` does not match the string in `PortId.of("inputVal")` used inside the executor.
* **Evaluation Order**: In procedural execution (`executeControlFlow`), upstream data nodes are only evaluated if they are connected directly to data ports of the executing node.

### Solution

Centralize port identifiers as `static final PortId` constants shared between definitions and executors:

```java
public final class MyNodeConstants {
    public static final PortId INPUT_A = PortId.of("input_a");
    public static final PortId RESULT = PortId.of("result");
}
```

---

## 7. Gradle Cannot Resolve NodeForge Dependency

### Symptoms

Running `./gradlew build` in your consumer project fails with:

```text
Could not find net.minex:nodeforge:1.0.0.
```

### Root Cause

The Gradle build script does not specify the repository containing the NodeForge artifact.

### Solution

In your consumer mod's `build.gradle`, add the appropriate repository block:

```groovy
repositories {
    mavenCentral()
    // For local development builds
    mavenLocal()
    // For published Modrinth releases
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "net.minex:nodeforge:1.0.0"
}
```
