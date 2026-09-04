# Custom Node Example

This example demonstrates how to build and register a production-ready custom node in NodeForge 1.0.0. We will implement a `math:clamp` node that restricts a numeric value within a minimum and maximum boundary, using default property fallbacks if ports are unconnected.

---

## Architectural Responsibility

NodeForge strictly separates node metadata, runtime execution, and visual presentation:

```text
               Common Code (Client + Server)
┌─────────────────────────────────────────────────────────┐
│  NodeDefinition: Defines ports, types, properties, name  │
│  NodeExecutor:   Pure logic receiving ExecutionContext  │
│  NodeForgePlugin: Registers definition and executor     │
└─────────────────────────────────────────────────────────┘
                            │
               Optional Client Code
┌─────────────────────────────────────────────────────────┐
│  CustomNodeRenderer: (Optional) Custom card styling     │
│  NodeForgeClientPlugin: Registers visual theme/renderer  │
└─────────────────────────────────────────────────────────┘
```

The node logic runs safely on headless dedicated servers without dragging in any graphical dependencies.

---

## Step 1: Define the Node Definition

Create a factory method or class in your common source set (`src/main/java`) that builds the `NodeDefinition`:

```java
package com.example.mymod.node;

import net.minex.nodeforge.api.property.PropertyDefinition;
import net.minex.nodeforge.api.registry.NodeCategory;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.type.BuiltinPortTypes;

public final class ClampNodeDefinition {

    public static final String TYPE_ID = "mymod:clamp";

    public static NodeDefinition create() {
        return NodeDefinition.builder(TYPE_ID)
                .displayName("Clamp")
                .description("Restricts a number between specified minimum and maximum bounds.")
                .category(NodeCategory.MATH)
                // Data input ports
                .inputPort("value", "Value", BuiltinPortTypes.FLOAT)
                .inputPort("min", "Min", BuiltinPortTypes.FLOAT)
                .inputPort("max", "Max", BuiltinPortTypes.FLOAT)
                // Result output port
                .outputPort("result", "Result", BuiltinPortTypes.FLOAT)
                // Fallback properties if ports are unplugged
                .property(PropertyDefinition.doubleProp("default_min", 0.0))
                .property(PropertyDefinition.doubleProp("default_max", 1.0))
                .build();
    }
}
```

### Explanation of Properties vs. Ports

* **Inputs (`value`, `min`, `max`)**: Dynamic sockets wired to upstream calculations.
* **Properties (`default_min`, `default_max`)**: Internal node state configured in the inspector panel when a cable is not connected to that port.

---

## Step 2: Implement the Node Executor

The executor implements the functional interface `NodeExecutor`. It receives the immutable `Node` snapshot and the mutable `ExecutionContext`:

```java
package com.example.mymod.node;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutor;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.id.PortId;

public class ClampNodeExecutor implements NodeExecutor {

    private static final PortId PORT_VALUE = PortId.of("value");
    private static final PortId PORT_MIN = PortId.of("min");
    private static final PortId PORT_MAX = PortId.of("max");
    private static final PortId PORT_RESULT = PortId.of("result");

    @Override
    public ExecutionResult execute(Node node, ExecutionContext context) {
        // 1. Resolve input value (defaults to 0.0 if unwired)
        Object rawVal = context.getInputValue(node.id(), PORT_VALUE);
        double value = rawVal instanceof Number n ? n.doubleValue() : 0.0;

        // 2. Resolve min bound: check incoming cable first, fallback to property
        Object rawMin = context.getInputValue(node.id(), PORT_MIN);
        double min;
        if (rawMin instanceof Number n) {
            min = n.doubleValue();
        } else {
            min = node.getProperty("default_min", Double.class, 0.0);
        }

        // 3. Resolve max bound: check incoming cable first, fallback to property
        Object rawMax = context.getInputValue(node.id(), PORT_MAX);
        double max;
        if (rawMax instanceof Number n) {
            max = n.doubleValue();
        } else {
            max = node.getProperty("default_max", Double.class, 1.0);
        }

        // 4. Fault validation: enforce invariant min <= max
        if (min > max) {
            return ExecutionResult.Failure.of(
                    "Clamp node '" + node.id().value() + "' invalid range: min (" + min + ") > max (" + max + ")"
            );
        }

        // 5. Perform mathematical calculation
        double clamped = Math.max(min, Math.min(max, value));

        // 6. Emit result to outgoing output port
        context.setOutputValue(node.id(), PORT_RESULT, clamped);

        // 7. Complete successfully
        return ExecutionResult.Success.of();
    }
}
```

### Safety and Invariant Rules

> [!IMPORTANT]
> Never throw unhandled runtime exceptions inside `execute()`. If an input is malformed or an invariant is violated, return `ExecutionResult.Failure.of(message)`. This allows `GraphEvaluator` to isolate the failure, report the node ID, and halt gracefully without crashing the server thread.

---

## Step 3: Register via NodeForgePlugin

Implement `NodeForgePlugin` in your common code. NodeForge automatically calls these lifecycle callbacks during Fabric mod initialization:

```java
package com.example.mymod.plugin;

import com.example.mymod.node.ClampNodeDefinition;
import com.example.mymod.node.ClampNodeExecutor;
import net.minex.nodeforge.api.plugin.NodeForgePlugin;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.registry.NodeExecutorRegistry;

public class MyModCommonPlugin implements NodeForgePlugin {

    @Override
    public String id() {
        return "mymod";
    }

    @Override
    public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
        registry.register(ClampNodeDefinition.create());
    }

    @Override
    public void registerExecutors(NodeExecutorRegistry registry) {
        registry.register(ClampNodeDefinition.TYPE_ID, new ClampNodeExecutor());
    }
}
```

---

## Step 4: Declare Plugin in fabric.mod.json

Expose your plugin class through the `"nodeforge"` entrypoint:

```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "1.0.0",
  "name": "My Mod",
  "environment": "*",
  "entrypoints": {
    "main": [
      "com.example.mymod.MyMod"
    ],
    "nodeforge": [
      "com.example.mymod.plugin.MyModCommonPlugin"
    ]
  },
  "depends": {
    "fabricloader": ">=0.16.10",
    "minecraft": "~1.21.11",
    "nodeforge": ">=1.0.0"
  }
}
```

---

## Step 5: Verification in Code

You can test your custom node in a unit test without launching a Minecraft client:

```java
@Test
void testClampNodeEvaluation() {
    Graph graph = new Graph("test_clamp");

    // Construct an instance from our definition
    NodeDefinition def = ClampNodeDefinition.create();
    Node clampNode = def.instantiate("clamp_1")
            .property("default_min", 10.0)
            .property("default_max", 50.0)
            .build();
    graph.addNode(clampNode);

    // Register executor locally for unit testing
    NodeExecutorRegistry registry = new NodeExecutorRegistry();
    registry.register(ClampNodeDefinition.TYPE_ID, new ClampNodeExecutor());
    GraphEvaluator evaluator = new GraphEvaluator(registry);

    // Context providing an input of 75.0 to 'value'
    ExecutionContext ctx = ExecutionContext.create();
    ctx.setInputValue(clampNode.id(), PortId.of("value"), 75.0);

    ExecutionSummary summary = evaluator.evaluateDataFlow(graph, ctx);

    assertTrue(summary.isSuccess());
    assertEquals(50.0, (Double) ctx.getOutputValue(clampNode.id(), PortId.of("result")), 1e-6);
}
```

---

## Next Steps

* Give your node a unique visual card style with [Custom Node Renderers](../customization/node-renderers.md).
* Add interactive inspector widgets for properties in [Property Widgets](../customization/property-widgets.md).
* Integrate the editor into an in-game item or block screen in [Custom Editor Example](custom-editor.md).
