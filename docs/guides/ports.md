# Ports and Type System

Ports represent connection sockets through which data or execution triggers pass into and out of nodes. Every port is strongly typed and directional.

---

## 1. Port Directions

Every port belongs to one of two directions defined by `PortDirection`:
- **`PortDirection.INPUT`**: Placed along the left edge of a node card. Consumes incoming values.
- **`PortDirection.OUTPUT`**: Placed along the right edge of a node card. Produces outgoing values.

```text
               +-----------------------------+
               |         Node Card           |
               |-----------------------------|
  Input Port o | [In]                   [Out]| o Output Port
  Input Port o | [In]                        |
               +-----------------------------+
```

---

## 2. Data Ports vs. Execution Ports

NodeForge maintains a fundamental distinction between **Data Ports** and **Execution Ports**:

| Attribute | Data Ports | Execution Ports |
| :--- | :--- | :--- |
| **Payload** | Transmits typed values (numbers, text, objects) | Transmits control-flow trigger pulses |
| **Type** | `PortType<T>` (where $T \neq \text{Void}$) | `BuiltinPortTypes.EXECUTION` (`PortType<Void>`) |
| **Socket Shape** | Circular, square, or diamond colored socket | White triangular socket |
| **Wire Traversal** | Evaluated via topological sort / Kahn's DAG | Evaluated sequentially step-by-step |
| **Cycles** | Cycles are forbidden (throws `GraphCycleException`) | Cycles are allowed (e.g. while-loops, procedural state machines) |

---

## 3. Built-in Port Types

NodeForge includes standard port types in `BuiltinPortTypes`:

| Port Type Identifier | Payload Class | Socket Color (ARGB) | Description |
| :--- | :--- | :--- | :--- |
| `nodeforge:execution` | `Void` | `0xFFFFFFFF` (White) | Procedural execution pulse |
| `nodeforge:boolean` | `Boolean` | `0xFFFF3366` (Crimson Pink) | True / false Boolean flag |
| `nodeforge:int` | `Integer` | `0xFF00E5FF` (Cyan) | 32-bit signed integer |
| `nodeforge:float` | `Float` | `0xFFFF9100` (Amber Orange) | 32-bit IEEE floating-point |
| `nodeforge:double` | `Double` | `0xFFFFD600` (Gold Yellow) | 64-bit IEEE floating-point |
| `nodeforge:string` | `String` | `0xFF00E676` (Mint Green) | Text string |
| `nodeforge:any` | `Object` | `0xFFB0BEC5` (Light Gray) | Wildcard socket (accepts any data payload) |

---

## 4. Type Compatibility Engine

When `graph.connect()` is called, the `TypeCompatibilityEngine` validates whether the output port's data type can safely flow into the input port.

### 4.1 Exact Type Matching
Ports sharing identical `PortTypeId` instances are always compatible.

### 4.2 Implicit Numeric Widening
NodeForge automatically allows lossless numeric widening along wires:
- An `INT` output can connect to a `FLOAT` or `DOUBLE` input.
- A `FLOAT` output can connect to a `DOUBLE` input.
- A `DOUBLE` output **cannot** connect to an `INT` input without explicit conversion (preventing silent precision loss).

### 4.3 Class Assignability
If both ports define standard Java payload classes, NodeForge checks `targetClass.isAssignableFrom(sourceClass)`. For example:
- A custom `PortType<EntityLiving>` output can connect to a `PortType<Entity>` input.

### 4.4 Wildcard Socket (`nodeforge:any`)
The `BuiltinPortTypes.ANY` socket acts as a universal adapter:
- An output of type `ANY` can connect to any data input port.
- Any data output port can connect to an input of type `ANY`.
- **Note**: An `ANY` port cannot connect to an `EXECUTION` port. Data and control flow remain strictly partitioned.

---

## 5. Defining Custom Port Types

Consumer mods can create custom port types for domain objects (such as `Mana`, `ItemStack`, `Vec3d`, or `DialogueChoice`):

```java
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;

public class CustomPortTypes {
    public static final PortTypeId MANA_ID = PortTypeId.of("magic:mana");

    public static final PortType<Integer> MANA = PortType.builder(MANA_ID, Integer.class)
            .displayName("Mana")
            .color(0xFF3366FF) // Royal Blue socket
            .build();
}
```

Register your custom port type in your plugin's `registerPortTypes` callback:

```java
@Override
public void registerPortTypes(PortTypeRegistry registry) {
    registry.register(CustomPortTypes.MANA);
}
```

---

## 6. Next Steps

- Learn how to wire ports together in the [Connections Guide](connections.md).
- Learn how to evaluate connected graphs in the [Execution Engine Guide](execution.md).
