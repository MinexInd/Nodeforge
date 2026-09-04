# NodeForge Developer Guide

Welcome to the **NodeForge Developer Guide**. NodeForge is a lightweight, high-performance, domain-agnostic node graph and visual programming framework for Minecraft Fabric 1.21.11+.

This guide walks you through extending NodeForge from external Minecraft mods using its clean public SPI.

---

## Table of Contents
1. [Core Architectural Philosophy](#1-core-architectural-philosophy)
2. [Mod Setup & Plugin Entrypoints](#2-mod-setup--plugin-entrypoints)
3. [Defining Custom Port Types](#3-defining-custom-port-types)
4. [Building Custom Node Definitions](#4-building-custom-node-definitions)
5. [Implementing Node Executors & Graph Evaluation](#5-implementing-node-executors--graph-evaluation)
6. [Client UI Customization (Themes & Canvas Layers)](#6-client-ui-customization)
7. [Complete Reference Implementation](#7-complete-reference-implementation)
8. [Best Practices & Rules](#8-best-practices--rules)

---

## 1. Core Architectural Philosophy

NodeForge strictly maintains a **domain-agnostic boundary**:
- NodeForge does **not** implement datapacks, Minecraft commands, scoreboard logic, skills, enchantments, or gameplay mechanics.
- NodeForge provides pure graph models, topological algorithms, execution pipelines, visual canvas renderers, and editor state management.
- **Client / Server Isolation**: Core logic and plugins live in common code (`src/main/`) and know nothing about client rendering classes (`DrawContext`, `TextRenderer`, OpenGL). Client extensions live in client code (`src/client/`).

---

## 2. Mod Setup & Plugin Entrypoints

External mods hook into NodeForge by implementing **`NodeForgePlugin`** (common) and optionally **`NodeForgeClientPlugin`** (client).

Declare them in your mod's `fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "1.0.0",
  "name": "My NodeForge Extension Mod",
  "entrypoints": {
    "nodeforge:plugin": [
      "com.example.mymod.MyModPlugin"
    ],
    "nodeforge:client_plugin": [
      "com.example.mymod.client.MyModClientPlugin"
    ]
  },
  "depends": {
    "nodeforge": ">=1.0.0"
  }
}
```

Alternatively, you can register plugins programmatically:
```java
// Common mod initializer
NodeForge.registerPlugin(new MyModPlugin());

// Client mod initializer
NodeForgeClient.registerClientPlugin(new MyModClientPlugin());
```

---

## 3. Defining Custom Port Types

Ports represent sockets through which data or execution flows pass. All ports are strongly typed.

```java
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;

public class MyPortTypes {
    public static final PortTypeId FLOAT_ID = PortTypeId.of("mymod:float");

    public static final PortType<Float> FLOAT = PortType.builder(FLOAT_ID, Float.class)
            .color(0xFF00E5FF)       // Cyan socket color
            .build();

    public static void register(PortTypeRegistry registry) {
        registry.register(FLOAT);
    }
}
```

---

## 4. Building Custom Node Definitions

Node definitions declare the archetype template for nodes that users can instantiate in the editor palette.

```java
import net.minex.nodeforge.api.registry.NodeCategory;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.registry.NodeTypeId;

public class MyNodes {
    public static final NodeTypeId ADD_ID = NodeTypeId.of("mymod:add");

    public static void register(NodeDefinitionRegistry registry) {
        registry.register(NodeDefinition.builder(ADD_ID)
                .displayName("Add (Float)")
                .category(NodeCategory.MATH)
                .description("Calculates the sum of two numbers.")
                .inputPort("a", "A", MyPortTypes.FLOAT)
                .inputPort("b", "B", MyPortTypes.FLOAT)
                .outputPort("result", "Result", MyPortTypes.FLOAT)
                .build());
    }
}
```

---

## 5. Implementing Node Executors & Graph Evaluation

Node executors define what happens when a node evaluates during runtime.

### Registering the Executor
```java
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.core.id.PortId;

public class MyExecutors {
    public static void register(NodeExecutorRegistry registry) {
        registry.register("mymod:add", (node, context) -> {
            Object aVal = context.getInputValue(node.id(), PortId.of("a"));
            Object bVal = context.getInputValue(node.id(), PortId.of("b"));

            float a = (aVal instanceof Number n) ? n.floatValue() : 0f;
            float b = (bVal instanceof Number n) ? n.floatValue() : 0f;

            context.setOutputValue(node.id(), PortId.of("result"), a + b);
            return ExecutionResult.Success.of();
        });
    }
}
```

### Evaluating a Graph with `GraphRunner`
Use the high-level `GraphRunner` facade to run graphs in a single line:

```java
import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.runner.GraphRunner;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

Graph graph = new Graph("sample_graph");
Node addNode = NodeForge.getNodeRegistry().get(MyNodes.ADD_ID).createNode(NodeId.of("add_1"));
graph.addNode(addNode);

ExecutionContext context = new ExecutionContext();
context.setInputValue(addNode.id(), PortId.of("a"), 20f);
context.setInputValue(addNode.id(), PortId.of("b"), 30f);

// Evaluate topological data-flow
ExecutionSummary summary = GraphRunner.evaluateDataFlow(graph, context);

if (summary.isSuccess()) {
    Object result = context.getOutputValue(addNode.id(), PortId.of("result"));
    System.out.println("Result: " + result); // 50.0
}
```

---

## 6. Client UI Customization

Extend the visual editor via `NodeForgeClientPlugin`:

### Custom Color Themes
```java
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.client.render.theme.ThemeId;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;

public void registerThemes(ThemeRegistry registry) {
    NodeTheme myTheme = NodeTheme.builder()
            .backgroundColor(0xFF1A1A1A)
            .nodeBackgroundColor(0xFF262626)
            .nodeSelectedBorderColor(0xFF00FF66)
            .cableDefaultColor(0xFF00E5FF)
            .build();

    registry.register(ThemeId.of("mymod:custom_theme"), myTheme);
}
```

### Custom Canvas Layers
Draw custom backgrounds, zone grids, or HUD overlays directly onto the canvas:

```java
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.layer.CanvasLayer;
import net.minex.nodeforge.client.render.layer.CanvasLayerPhase;
import net.minex.nodeforge.client.render.theme.NodeTheme;

public class MyWatermarkLayer implements CanvasLayer {
    @Override
    public boolean shouldRender(CanvasLayerPhase phase) {
        return phase == CanvasLayerPhase.SCREEN_OVERLAY;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                       NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
        context.drawText(textRenderer, "CUSTOM MOD ACTIVE", 10, 10, 0xFF00FF00, false);
    }
}
```

---

## 7. Complete Reference Implementation

A fully functional, living reference implementation is available inside the NodeForge repository:
- Common plugin: [`DemoMathPlugin.java`](file:///d:/nodeforge-template-1.21.11/src/main/java/net/minex/nodeforge/demo/DemoMathPlugin.java)
- Composite port type: [`Vector2.java`](file:///d:/nodeforge-template-1.21.11/src/main/java/net/minex/nodeforge/demo/Vector2.java)
- Client plugin: [`DemoClientPlugin.java`](file:///d:/nodeforge-template-1.21.11/src/client/java/net/minex/nodeforge/demo/client/DemoClientPlugin.java)
- Custom canvas layer: [`WatermarkCanvasLayer.java`](file:///d:/nodeforge-template-1.21.11/src/client/java/net/minex/nodeforge/demo/client/WatermarkCanvasLayer.java)

---

## 8. Best Practices & Rules

1. **Domain-Agnostic Purity**: Never put Minecraft gameplay, command strings, or server-side NBT logic inside NodeForge node definitions or executors.
2. **Deterministic Registration Order**: The plugin lifecycle guarantees:
   $$\text{Port Types} \longrightarrow \text{Node Definitions} \longrightarrow \text{Executors} \longrightarrow \text{onInitialize}$$
3. **Fault Tolerance**: Always handle edge cases gracefully. If an executor encounters invalid inputs, return `ExecutionResult.Failure.of("Descriptive reason")` rather than letting unchecked exceptions escape.
4. **Pure Client Isolation**: Never import `net.minecraft.client.*` inside common `NodeForgePlugin` classes. Keep all UI, renderers, and themes inside your client plugin classes.
