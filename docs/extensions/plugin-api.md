# Common Plugin API

The Common Plugin API allows external Minecraft mods to register custom port types, node definitions, and execution handlers without accessing internal engine classes.

---

## 1. The `NodeForgePlugin` Interface

To hook into NodeForge headlessly, implement `NodeForgePlugin` (`net.minex.nodeforge.api.plugin.NodeForgePlugin`):

```java
package net.minex.nodeforge.api.plugin;

import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;

public interface NodeForgePlugin {

    default String id() {
        return getClass().getSimpleName();
    }

    /** 1. Register custom PortType instances first. */
    default void registerPortTypes(PortTypeRegistry registry) {}

    /** 2. Register custom NodeDefinition archetypes second. */
    default void registerNodeDefinitions(NodeDefinitionRegistry registry) {}

    /** 3. Register functional NodeExecutor handlers third. */
    default void registerExecutors(NodeExecutorRegistry registry) {}

    /** 4. General lifecycle hook invoked after all registrations. */
    default void onInitialize(NodeForgeContext context) {}
}
```

---

## 2. Declaration in `fabric.mod.json`

NodeForge automatically discovers plugins via the `"nodeforge:plugin"` entrypoint. Declare it in your mod's `src/main/resources/fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "my_magic_mod",
  "version": "1.0.0",
  "entrypoints": {
    "nodeforge:plugin": [
      "com.example.mymod.plugin.MagicNodeForgePlugin"
    ]
  },
  "depends": {
    "nodeforge": ">=1.0.0"
  }
}
```

### 2.1 Programmatic Registration
Alternatively, plugins can be registered programmatically during your mod's `ModInitializer`:

```java
import net.minex.nodeforge.NodeForge;

public class MyModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        NodeForge.registerPlugin(new MagicNodeForgePlugin());
    }
}
```

---

## 3. Registration Lifecycle Order

NodeForge guarantees deterministic registration sequencing:

```text
1. registerPortTypes()      ──► Ensures custom socket types are globally resolvable.
2. registerNodeDefinitions()──► Sockets defined in step 1 are linked into port templates.
3. registerExecutors()      ──► Executors are bound to the node type keys defined in step 2.
4. onInitialize()           ──► Context provided for cross-mod querying or graph preloading.
```

---

## 4. Complete Plugin Implementation Example

```java
package com.example.mymod.plugin;

import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.plugin.NodeForgePlugin;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.core.id.PortId;

public class MagicNodeForgePlugin implements NodeForgePlugin {

    public static final PortType<Integer> MANA_TYPE = PortType.builder(
            PortTypeId.of("magic:mana"), Integer.class
    ).displayName("Mana").color(0xFF3366FF).build();

    @Override
    public String id() {
        return "my_magic_mod:plugin";
    }

    @Override
    public void registerPortTypes(PortTypeRegistry registry) {
        registry.register(MANA_TYPE);
    }

    @Override
    public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
        registry.register(NodeDefinition.builder("magic:generator")
                .displayName("Mana Generator")
                .category("Magic")
                .outputPort("mana_out", "Mana Flow", MANA_TYPE)
                .build());

        registry.register(NodeDefinition.builder("magic:consumer")
                .displayName("Mana Consumer")
                .category("Magic")
                .inputPort("mana_in", "Mana Input", MANA_TYPE)
                .outputPort("active", "Is Active", BuiltinPortTypes.BOOLEAN)
                .build());
    }

    @Override
    public void registerExecutors(NodeExecutorRegistry registry) {
        // Bind executor for generator
        registry.register("magic:generator", (node, ctx) -> {
            ctx.setOutputValue(node.id(), PortId.of("mana_out"), 100);
            return ExecutionResult.Success.of();
        });

        // Bind executor for consumer
        registry.register("magic:consumer", (node, ctx) -> {
            Integer inMana = (Integer) ctx.getInputValue(node.id(), PortId.of("mana_in"));
            boolean active = inMana != null && inMana >= 50;
            ctx.setOutputValue(node.id(), PortId.of("active"), active);
            return ExecutionResult.Success.of();
        });
    }
}
```

---

## 5. Next Steps

- Extend client rendering via the [Client Plugin API Guide](client-plugin-api.md).
- Learn how to add custom canvas layers in the [Canvas Layers Guide](canvas-layers.md).
