# Platform Compatibility & Versioning

This reference defines the supported Minecraft runtime environments, Java requirements, and Semantic Versioning policies for NodeForge.

---

## 1. Supported Platform Matrix

NodeForge 1.0.0 is built and verified against the following runtime targets:

| Component | Target Version | Supported Range |
| :--- | :--- | :--- |
| **Minecraft** | **1.21.11** | Compatible with the 1.21.11 release cycle |
| **Fabric Loader** | **0.19.3** | $\ge 0.19.3$ |
| **Fabric API** | **0.141.6+1.21.11** | $\ge 0.140.0$ |
| **Java Version** | **Java 21** | OpenJDK 21 LTS (Temurin, Corretto, Zulu, Oracle) |

---

## 2. Java 21 Requirements

NodeForge strictly requires **Java 21**. It utilizes modern language features throughout its API:
- **Record Classes**: Used for immutable IDs (`NodeId`, `PortId`, `ConnectionId`), geometries (`Position`, `Size`, `BoundingBox`), and results (`ConnectionResult`, `ExecutionSummary`).
- **Sealed Interfaces**: Used for execution results (`ExecutionResult` permitting `Success`, `Failure`, `Halt`).
- **Pattern Matching for Switch**: Used across type compatibility engines and event dispatchers.
- **Sequenced Collections**: Used for deterministic node and connection iteration ordering.

Attempting to run NodeForge on Java 17 or older will result in an `UnsupportedClassVersionError` (class file version 65.0 required).

---

## 3. Semantic Versioning Policy (SemVer 2.0.0)

NodeForge adheres strictly to Semantic Versioning:

$$\text{MAJOR}.\text{MINOR}.\text{PATCH}$$

1. **PATCH Releases ($1.0.1, 1.0.2$)**:
   - Backward-compatible bug fixes, performance optimizations, and documentation refinements.
   - Zero changes to public method signatures in `net.minex.nodeforge.api.*` or `net.minex.nodeforge.client.api.*`.
2. **MINOR Releases ($1.1.0, 1.2.0$)**:
   - New backward-compatible features (e.g. additional built-in themes, new property widgets, new standard port types).
   - Deprecations flagged with `@Deprecated` annotations at least one minor version prior to removal.
3. **MAJOR Releases ($2.0.0$)**:
   - Incompatible API modifications or structural breaking changes.

---

## 4. Consumer Dependency Declaration

In your mod's `fabric.mod.json`, specify NodeForge under `depends` with a lower-bound version constraint:

```json
"depends": {
  "fabricloader": ">=0.19.3",
  "minecraft": "~1.21.11",
  "java": ">=21",
  "fabric-api": "*",
  "nodeforge": ">=1.0.0"
}
```

---

## 5. Headless Dedicated Server Support

NodeForge is certified for headless dedicated server environments:
- **Zero OpenGL or Client Coupling in Common Code**: All graph structures, Kahn DAG topological sorters, serializers, and execution engines run headlessly without graphics drivers or X11/Wayland displays.
- Dedicated servers can load, validate, and execute visual graphs created in single-player or external editor tools.

---

## 6. Next Steps

- Explore runnable integration examples in the [Examples Directory](../examples/minimal-graph.md).
- Review troubleshooting advice in the [Troubleshooting Guide](../troubleshooting/common-problems.md).
