# API Stability & Package Matrix

To guarantee predictable binary compatibility for downstream mods, NodeForge strictly categorizes all packages into stability tiers under Semantic Versioning (SemVer 2.0.0).

---

## 1. Stability Tier Matrix

| Package Root | Stability Level | Binary Compatibility Guarantee | Intended Audience |
| :--- | :--- | :--- | :--- |
| **`net.minex.nodeforge.api.*`** | **Public Supported API** | **Guaranteed**. No breaking signature changes without major version increment ($1.x.x \rightarrow 2.0.0$). | External mod developers building graphs, nodes, and common plugins. |
| **`net.minex.nodeforge.client.api.*`** | **Public Client SPI** | **Guaranteed**. Stable extension contracts across $1.x.x$ releases. | Client mod developers extending themes and canvas rendering layers. |
| **`net.minex.nodeforge.core.*`** | **Internal Engine** | *Unstable*. Subject to refactoring and algorithmic optimization across minor releases. | Internal NodeForge engine subsystems. Avoid direct coupling. |
| **`net.minex.nodeforge.client.*`** | **Internal Client GUI** | *Unstable*. Subject to UI redesigns and rendering optimization. | Internal Minecraft GUI screens and widget implementations. |

---

## 2. Package-by-Package Breakdown

### 2.1 Public Common API (`net.minex.nodeforge.api.*`)
- **`api.graph`**: Core graph interfaces (`Node`, `Port`, `Connection`, `CommentBox`, `Position`, `Size`, `BoundingBox`).
- **`api.port`**: Port typing contracts (`PortType`, `PortTypeId`, `PortTypeRegistry`, `BuiltinPortTypes`).
- **`api.registry`**: Node archetype registries (`NodeDefinition`, `NodeTypeId`, `NodeCategory`, `NodeDefinitionRegistry`, `GraphRegistry`).
- **`api.execution`**: Evaluation contracts (`NodeExecutor`, `ExecutionContext`, `ExecutionResult`, `ExecutionSummary`, `GraphCycleException`).
- **`api.plugin`**: Common mod extension hooks (`NodeForgePlugin`, `NodeForgeContext`, `PluginManager`).
- **`api.runner`**: High-level consumer facade (`GraphRunner`).
- **`api.serialization`**: Codecs and schema parsers (`GraphCodec`, `GraphSerializer`, `GraphSerializationException`).

### 2.2 Core Engine Internals (`net.minex.nodeforge.core.*`)
- **`core.graph`**: Concrete thread-synchronized container (`Graph`, `ConnectionResult`).
- **`core.execution`**: Engine implementations (`GraphEvaluator`, `TopologicalSorter`, `BuiltinExecutors`).
- **`core.id`**: Strongly typed identity records (`NodeId`, `PortId`, `ConnectionId`).
- **`core.port`**: Algorithmic typing rules (`TypeCompatibilityEngine`).
- **`core.resource`**: Fabric resource reload listener (`GraphResourceReloader`).
- **`core.validation`**: Structural validation rules (`GraphValidator`, `ValidationError`, `NodeValidationRule`).

### 2.3 Client Packages (`net.minex.nodeforge.client.*`)
- **`client.editor`**: Minecraft GUI screens (`NodeEditorScreen`, `EditorState`, `SelectionModel`, `CommandStack`).
- **`client.render`**: Rendering pipeline (`CanvasRenderer`, `WireRenderer`, `NodeWidget`).
- **`client.render.theme`**: Visual color themes (`NodeTheme`, `ThemeId`, `ThemeRegistry`).
- **`client.render.layer`**: Phased rendering hooks (`CanvasLayer`, `CanvasLayerPhase`, `CanvasLayerRegistry`).
- **`client.plugin`**: Client extension SPI (`NodeForgeClientPlugin`, `NodeForgeClientContext`, `ClientPluginManager`).
- **`client.animation`**: Easing and tweens (`Easing`, `Tween`, `AnimationTimeline`).
- **`client.render.vfx`**: Visual effects coordinator (`VfxManager`, `CanvasParticle`, `CableImpulse`).

---

## 3. Recommended Consumer Practices

1. **Depend on `api.*` Wherever Possible**: Always use `NodeDefinition`, `PortType`, `ExecutionContext`, and `GraphRunner` rather than internal helper classes.
2. **Isolate Client References**: Ensure any call referencing `net.minex.nodeforge.client.*` resides strictly within your `src/client/java` source set to prevent dedicated server crashes.
3. **Use Facades for Standard Workflows**: Use `GraphRunner.evaluateDataFlow(graph, context)` for standard evaluation tasks.

---

## 4. Next Steps

- Understand the mathematical execution semantics in the [Execution Model Reference](execution-model.md).
- Review concurrency contracts in the [Threading Reference](threading.md).
