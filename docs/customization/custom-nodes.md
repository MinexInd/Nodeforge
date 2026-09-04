# Creating Custom Nodes

This guide walks through creating, registering, executing, and styling a custom node archetype in NodeForge.

---

## 1. The Custom Node Lifecycle

Creating a custom node requires four sequential steps:

```text
Define Port Types ──► Define Node Archetype ──► Implement Executor ──► Register in Plugin
   (PortType)             (NodeDefinition)        (NodeExecutor)         (NodeForgePlugin)
```

---

## 2. Step 1: Defining Port Types

If your node uses standard types (`INT`, `FLOAT`, `DOUBLE`, `STRING`, `BOOLEAN`, `EXECUTION`), you can use `BuiltinPortTypes` directly.

If your node introduces a domain object (such as `Mana`), define a custom `PortType`:

```java
package com.example.mymod.graph;

import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;

public class MyPortTypes {
    public static final PortTypeId MANA_ID = PortTypeId.of("magic:mana");

    public static final PortType<Integer> MANA = PortType.builder(MANA_ID, Integer.class)
            .displayName("Mana")
            .color(0xFF3366FF) // Royal Blue socket
            .build();
}
```

---

## 3. Step 2: Defining the `NodeDefinition`

A `NodeDefinition` declares the archetype for the node, specifying its category, display name, default dimensions, and port templates:

```java
package com.example.mymod.graph;

import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.registry.NodeDefinition;

public class SpellAmplifierDefinition {

    public static final String TYPE_KEY = "magic:spell_amplifier";

    public static NodeDefinition create() {
        return NodeDefinition.builder(TYPE_KEY)
                .displayName("Spell Amplifier")
                .category("Magic")
                .description("Multiplies incoming mana by an amplification factor")
                .defaultWidth(160)
                .defaultHeight(90)
                // Define inputs
                .inputPort("mana_in", "Mana Input", MyPortTypes.MANA)
                .inputPort("factor", "Multiplier", BuiltinPortTypes.FLOAT)
                // Define output
                .outputPort("mana_out", "Amplified Mana", MyPortTypes.MANA)
                .build();
    }
}
```

---

## 4. Step 3: Implementing the `NodeExecutor`

The `NodeExecutor` functional interface defines what occurs when the node executes during graph evaluation:

```java
package com.example.mymod.graph;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutor;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.id.PortId;

public class SpellAmplifierExecutor implements NodeExecutor {

    @Override
    public ExecutionResult execute(Node node, ExecutionContext context) {
        // 1. Read input values from the execution context
        Integer mana = (Integer) context.getInputValue(node.id(), PortId.of("mana_in"));
        Float factor = (Float) context.getInputValue(node.id(), PortId.of("factor"));

        // 2. Handle missing or default inputs
        int baseMana = mana != null ? mana : 0;
        float mult = factor != null ? factor : 1.5f;

        // 3. Validation & Error Handling
        if (mult < 0.0f) {
            return ExecutionResult.Failure.of("Amplification factor cannot be negative: " + mult);
        }

        // 4. Compute result
        int amplifiedMana = Math.round(baseMana * mult);

        // 5. Emit output value into context buffer
        context.setOutputValue(node.id(), PortId.of("mana_out"), amplifiedMana);

        // 6. Return success
        return ExecutionResult.Success.of();
    }
}
```

---

## 5. Step 4: Registering via `NodeForgePlugin`

Register your port types, node definitions, and executors together in a `NodeForgePlugin`:

```java
package com.example.mymod.graph;

import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.plugin.NodeForgePlugin;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;

public class MyMagicPlugin implements NodeForgePlugin {

    @Override
    public String id() {
        return "mymod:magic";
    }

    @Override
    public void registerPortTypes(PortTypeRegistry registry) {
        registry.register(MyPortTypes.MANA);
    }

    @Override
    public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
        registry.register(SpellAmplifierDefinition.create());
    }

    @Override
    public void registerExecutors(NodeExecutorRegistry registry) {
        registry.register(SpellAmplifierDefinition.TYPE_KEY, new SpellAmplifierExecutor());
    }
}
```

Declare this plugin in your `fabric.mod.json`:

```json
"entrypoints": {
  "nodeforge:plugin": [
    "com.example.mymod.graph.MyMagicPlugin"
  ]
}
```

Once registered:
- The node automatically appears in the editor's **Creation Palette** under the "Magic" category.
- Dragging it onto the canvas creates an instance with the defined sockets.
- Running `GraphEvaluator` evaluates the node using `SpellAmplifierExecutor`.

---

## 6. Next Steps

- Learn how to customize the visual card rendering in the [Custom Node Renderers Guide](node-renderers.md).
- Learn how to add interactive sliders or text fields to your node in the [Property Widgets Guide](property-widgets.md).
