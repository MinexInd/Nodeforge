# Project Setup & Environmental Boundaries

Integrating NodeForge requires structuring your Minecraft Fabric project to respect split-environment boundaries between server-safe graph execution and client-only canvas rendering.

---

## 1. Configuring `fabric.mod.json`

To declare that your mod requires NodeForge at runtime, specify it inside the `depends` section of `src/main/resources/fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "mymod",
  "version": "${version}",
  "name": "My Custom Mod",
  "environment": "*",
  "entrypoints": {
    "main": [
      "com.example.mymod.MyModCommon"
    ],
    "client": [
      "com.example.mymod.client.MyModClient"
    ],
    "nodeforge:plugin": [
      "com.example.mymod.plugin.MyCommonPlugin"
    ],
    "nodeforge:client_plugin": [
      "com.example.mymod.client.plugin.MyClientPlugin"
    ]
  },
  "depends": {
    "fabricloader": ">=0.19.3",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*",
    "nodeforge": ">=1.0.0"
  }
}
```

### 1.1 Entrypoint Roles
- `"main"`: Standard Fabric common initializer (`ModInitializer`). Runs on both dedicated servers and client environments.
- `"client"`: Standard Fabric client initializer (`ClientModInitializer`). Runs only on physical game clients.
- `"nodeforge:plugin"`: Discovered automatically by NodeForge on common startup. Used to register custom `PortType` definitions, `NodeDefinition` archetypes, and functional `NodeExecutor` handlers.
- `"nodeforge:client_plugin"`: Discovered automatically by NodeForge during client startup. Used to register custom themes, custom node card renderers, icons, and canvas rendering layers.

---

## 2. Split-Environment Isolation

Fabric Loom provides split-environment source set compilation (`splitEnvironmentSourceSets()`). This mechanism separates headless server code from OpenGL/GUI code at compile time.

```text
MyMod/
├── src/main/java/              [COMMON - Runs on Client & Server]
│   └── com/example/mymod/
│       ├── MyModCommon.java    Standard ModInitializer
│       ├── nodes/              Node definitions & Port types
│       ├── executors/          Functional evaluation logic
│       └── persistence/        Graph loading & saving logic
│
└── src/client/java/            [CLIENT-ONLY - Never loaded on Server]
    └── com/example/mymod/client/
        ├── MyModClient.java    Standard ClientModInitializer
        ├── screens/            NodeEditorScreen invocations
        ├── renderers/          Custom node drawing & shaders
        └── themes/             Visual color presets
```

### 2.1 The Golden Rule of Modding
> [!CAUTION]
> **Never import client classes into `src/main`**:
> Classes such as `NodeEditorScreen`, `CanvasRenderer`, `DrawContext`, `MinecraftClient`, or any class in package `net.minex.nodeforge.client.*` must **only** reside in `src/client`. If a class in `src/main` references a client class, your mod will throw `ClassNotFoundException` or `NoClassDefFoundError` and crash dedicated servers immediately upon boot.

---

## 3. Recommended Consumer Package Architecture

To maintain maintainability across large projects, structure your mod's packages along functional boundaries:

```text
com.example.mymod/
├── api/                       (Optional) Public interfaces your mod exposes to other mods
├── common/                    ModInitializer and global lifecycle
├── domain/                    Your gameplay systems (XP, quests, spells, crafting)
├── graph/                     NodeForge bridge package
│   ├── ports/                 Custom PortType constants
│   ├── definitions/           NodeDefinition registration
│   ├── executors/             NodeExecutor implementations
│   └── MyCommonPlugin.java    Implements NodeForgePlugin
└── client/
    ├── screens/               Keybindings and GUI triggers
    └── MyClientPlugin.java    Implements NodeForgeClientPlugin
```

---

## 4. Next Steps

- Review [Core Concepts](concepts.md) to understand node graphs, ports, and execution buffers.
- Follow the [First Graph Tutorial](first-graph.md) to build and run your first graph in code.
