# Working with Nodes

A **Node** (`net.minex.nodeforge.api.graph.Node`) is a discrete computational or visual entity placed within a `Graph`.

---

## 1. Node Architecture

Every node instance encapsulates:
- **Identity (`NodeId`)**: A unique string-backed record identifying this specific instance in the graph.
- **Type Key (`typeKey`)**: Identifies the underlying archetype or definition (such as `"math:add"` or `"dialogue:prompt"`).
- **Display Name**: Human-readable label displayed in the node card header.
- **Position (`Position`)**: Continuous 64-bit IEEE 754 $(X, Y)$ coordinates in canvas world space.
- **Size (`Size`)**: Width and height dimensions in pixels (defaulting to $140 \times 80$).
- **Port Collections**: Ordered lists of input ports and output ports.
- **Property Bag**: Key-value map storing instance-specific configuration parameters.

---

## 2. Constructing Nodes with the Builder

Use `Node.builder(NodeId, String typeKey)` to create node instances:

```java
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.NodeId;

Node damageNode = Node.builder(NodeId.of("dmg_calc_1"), "combat:calculate_damage")
        .displayName("Damage Calculator")
        .position(new Position(150.0, 300.0))
        .size(new Size(160.0, 100.0))
        // Add input ports
        .inputPort("base_dmg", "Base Damage", BuiltinPortTypes.FLOAT)
        .inputPort("crit_multiplier", "Crit Multiplier", BuiltinPortTypes.FLOAT)
        // Add output port
        .outputPort("total_dmg", "Total Damage", BuiltinPortTypes.FLOAT)
        // Set instance configuration properties
        .property("allow_critical", true)
        .property("element", "fire")
        .build();
```

---

## 3. Positioning and Dimensions

Nodes exist on an effectively unbounded 2D plane:

```java
// Query current position
Position pos = damageNode.position();
double x = pos.x();
double y = pos.y();

// Move a node directly
damageNode.setPosition(new Position(200.0, 350.0));

// Or move via the Graph container (recommended for multi-threaded safety)
graph.moveNode(damageNode.id(), new Position(200.0, 350.0));

// Bounding box calculation (useful for spatial queries and collision detection)
BoundingBox bounds = damageNode.bounds();
// bounds.minX(), bounds.minY(), bounds.maxX(), bounds.maxY()
```

---

## 4. Querying and Managing Ports

Nodes own their concrete port instances:

```java
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.core.id.PortId;

// Query a port by PortId
Port baseDmgPort = damageNode.getPort(PortId.of("base_dmg"));

// Inspect all ports
List<Port> inputs = damageNode.inputPorts();
List<Port> outputs = damageNode.outputPorts();

for (Port p : inputs) {
    System.out.println("Input: " + p.displayName() + " [" + p.type().id() + "]");
}
```

---

## 5. Instance Properties

Nodes contain an internal property map (`Map<String, Object>`) for configuration settings that do not require dedicated input wires (e.g., dropdown selections, operation modes, toggles):

```java
// Set an instance property
damageNode.setProperty("formula_mode", "EXPONENTIAL");

// Retrieve an instance property
String mode = (String) damageNode.getProperty("formula_mode");

// Inspect the unmodifiable property snapshot
Map<String, Object> props = damageNode.properties();
```

---

## 6. Next Steps

- Explore how data and flow travel across sockets in the [Ports & Types Guide](ports.md).
- Learn how to wire nodes together in the [Connections Guide](connections.md).
