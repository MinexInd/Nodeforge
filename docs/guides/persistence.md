# Minecraft Resource Graph Persistence

NodeForge provides a generic serialization and resource loading pipeline enabling graphs to be persisted to disk, transmitted over network packets, and loaded automatically from Minecraft resource packs.

---

## 1. JSON Serialization with `GraphSerializer`

The `GraphSerializer` (`net.minex.nodeforge.api.serialization.GraphSerializer`) converts `Graph` instances to and from versioned JSON strings.

### 1.1 Serializing to JSON
```java
import net.minex.nodeforge.api.serialization.GraphSerializer;
import net.minex.nodeforge.core.graph.Graph;

GraphSerializer serializer = new GraphSerializer();

// Serialize with pretty-printing (indented for human editing)
String prettyJson = serializer.toJson(graph, true);

// Serialize compact (optimized for network transmission or storage)
String compactJson = serializer.toJson(graph, false);
```

### 1.2 Deserializing from JSON
```java
import net.minex.nodeforge.api.serialization.GraphSerializationException;

try {
    Graph restored = serializer.fromJson(jsonString);
    System.out.println("Restored graph: " + restored.id() + " with " + restored.nodeCount() + " nodes");
} catch (GraphSerializationException e) {
    System.err.println("Failed to parse graph JSON: " + e.getMessage());
}
```

---

## 2. JSON Schema Format (`schema_version: 1`)

NodeForge uses a standardized JSON schema:

```json
{
  "schema_version": 1,
  "id": "sample_math_graph",
  "metadata": {
    "author": "MinexInd",
    "version": "1.0.0"
  },
  "nodes": [
    {
      "id": "node_add_1",
      "type": "math:add",
      "display_name": "Add",
      "position": { "x": 100.0, "y": 80.0 },
      "size": { "width": 140.0, "height": 80.0 },
      "properties": {
        "operation": "SUM"
      },
      "ports": [
        { "id": "a", "name": "A", "type": "nodeforge:float", "direction": "INPUT" },
        { "id": "b", "name": "B", "type": "nodeforge:float", "direction": "INPUT" },
        { "id": "sum", "name": "Sum", "type": "nodeforge:float", "direction": "OUTPUT" }
      ]
    }
  ],
  "connections": [
    {
      "id": "conn_1",
      "from_node": "node_add_1",
      "from_port": "sum",
      "to_node": "node_mult_1",
      "to_port": "a"
    }
  ],
  "comment_boxes": [
    {
      "id": "frame_1",
      "title": "Math Section",
      "bounds": { "min_x": 80.0, "min_y": 50.0, "max_x": 300.0, "max_y": 200.0 },
      "color": 855638015
    }
  ]
}
```

---

## 3. Automated Resource Reloading (`GraphResourceReloader`)

NodeForge integrates with Fabric's resource reload listener infrastructure via `GraphResourceReloader` (`net.minex.nodeforge.core.resource.GraphResourceReloader`).

### 3.1 Default Reloader
By default, NodeForge monitors the resource path:
$$\text{data}/\langle\text{namespace}\rangle/\text{nodeforge/graphs}/*.json$$

When the server loads or executes `/reload`, graphs are parsed, validated, and registered into the global `GraphRegistry`.

### 3.2 Consumer-Specific Custom Paths
Consumer mods can instantiate their own `GraphResourceReloader` to maintain isolated resource subdirectories and reload sequencing:

```java
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minex.nodeforge.api.registry.GraphRegistry;
import net.minex.nodeforge.core.resource.GraphResourceReloader;

// Register custom resource reloader for "my_mod/skill_trees"
GraphRegistry myModRegistry = new GraphRegistry();
String customDirectory = "my_mod/skill_trees";
Identifier listenerId = Identifier.of("mymod", "skill_tree_reloader");

GraphResourceReloader customReloader = new GraphResourceReloader(
        myModRegistry, customDirectory, listenerId
);

ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(customReloader);
```

With this configuration, your mod discovers graphs from:
$$\text{data}/\langle\text{namespace}\rangle/\text{my\_mod/skill\_trees}/*.json$$

---

## 4. Next Steps

- Explore the interactive visual canvas in the [Visual Editor Guide](visual-editor.md).
- Learn how to register custom node archetypes in the [Custom Nodes Guide](../customization/custom-nodes.md).
