# Properties and Node State

Nodes in NodeForge maintain an internal property bag (`Map<String, Object>`) for configuration settings that do not require explicit connection wires.

---

## 1. When to Use Properties vs. Ports

| Characteristic | Port Sockets | Node Properties |
| :--- | :--- | :--- |
| **Purpose** | Dynamic data flowing from upstream nodes | Static configuration parameters |
| **Visual Appearance** | Sockets with connecting Bézier wires | Inline widgets (sliders, text fields, dropdowns) |
| **Runtime Mutation** | Computed per evaluation pass | Configured in editor or persisted in JSON |
| **Example Use Cases** | Incoming spell damage, sensor readings | Constant numbers, operational modes, labels |

---

## 2. Managing Properties Programmatically

### 2.1 Setting Properties via Builder
Properties can be pre-configured during node construction:

```java
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.id.NodeId;

Node clampNode = Node.builder(NodeId.of("clamp_1"), "math:clamp")
        .displayName("Clamp")
        .property("min_value", 0.0)
        .property("max_value", 100.0)
        .property("clamp_mode", "STRICT")
        .build();
```

### 2.2 Reading and Mutating Properties
Properties can be updated or queried dynamically:

```java
// Read a property with safe casting
Double minVal = (Double) clampNode.getProperty("min_value");

// Mutate an existing property
clampNode.setProperty("max_value", 200.0);

// Inspect the complete unmodifiable property map
Map<String, Object> allProperties = clampNode.properties();
```

---

## 3. Reading Properties in Custom Executors

When writing a `NodeExecutor`, access the node's properties to configure the computation:

```java
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutor;
import net.minex.nodeforge.core.id.PortId;

public class ClampExecutor implements NodeExecutor {

    @Override
    public ExecutionResult execute(Node node, ExecutionContext context) {
        Double inputVal = (Double) context.getInputValue(node.id(), PortId.of("in"));
        if (inputVal == null) inputVal = 0.0;

        // Retrieve properties configured on the node
        double min = (double) node.properties().getOrDefault("min_value", 0.0);
        double max = (double) node.properties().getOrDefault("max_value", 1.0);

        double clamped = Math.max(min, Math.min(max, inputVal));
        context.setOutputValue(node.id(), PortId.of("out"), clamped);

        return ExecutionResult.Success.of();
    }
}
```

---

## 4. Binding Properties to Editor Widgets

In the visual editor, property values are presented using interactive `PropertyWidget` instances.

When a user drags a slider or edits a text field inside the node inspector or node body:
1. The widget triggers `setOnChanged(value -> ...)`.
2. The callback calls `node.setProperty(propertyName, value)`.
3. The change is wrapped in an `UndoableCommand` on the `CommandStack`, enabling undo/redo.

---

## 5. Next Steps

- Learn how to validate node properties in the [Validation Guide](validation.md).
- Learn about the built-in UI controls in the [Property Widgets Guide](../customization/property-widgets.md).
