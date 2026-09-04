# Connections and Wiring

A **Connection** (`net.minex.nodeforge.api.graph.Connection`) is an immutable directed edge linking an output port to an input port.

---

## 1. Establishing Connections

Connections are created by invoking `graph.connect()`:

```java
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

ConnectionResult result = graph.connect(
        NodeId.of("generator_1"), PortId.of("power_out"),
        NodeId.of("machine_1"),   PortId.of("power_in")
);
```

### 1.1 Inspecting `ConnectionResult`
The `ConnectionResult` record reports the success or failure of the connection attempt:

```java
if (result.isSuccess()) {
    Connection wire = result.connection().orElseThrow();
    System.out.println("Connection ID: " + wire.id());
} else {
    // Descriptive rejection reason
    System.err.println("Wiring failed: " + result.errorMessage());
}
```

---

## 2. Enforced Connection Invariants

NodeForge enforces five structural invariants whenever `graph.connect()` is called:

```text
Validation Invariant             Rejection Consequence
-----------------------------------------------------------------------------------------
1. Existence Invariant           Both source and destination nodes and ports must exist.
2. Directional Invariant         Must connect OUTPUT -> INPUT.
3. Non-Self-Loop Invariant       Cannot connect a port to another port on the same node.
4. Non-Duplicate Invariant       Duplicate wires between the same port pair are rejected in O(1).
5. Type Compatibility            Data types must match, widen, or satisfy assignability rules.
```

If any invariant is violated, `ConnectionResult.failure("...")` is returned without altering graph state.

---

## 3. Fan-Out and Multi-Connection Rules

### 3.1 Output Fan-Out (Broadcasting)
A single output port can connect to multiple different input ports simultaneously. When the source node produces an output value, that value is broadcast to all downstream targets:

```text
                   ┌────────► [Input a] Node B
[Output] Node A ───┤
                   └────────► [Input a] Node C
```

### 3.2 Input Sockets
By default in data-flow evaluation, an input socket receives its value from a single upstream connection. If multiple wires target the same input socket, the evaluated value will be overwritten by whichever upstream connection evaluates last. For deterministic execution, avoid wiring multiple output wires to the same input data socket.

---

## 4. Severing Connections

Connections can be severed using either connection identifiers or endpoint coordinates:

```java
// Option A: Disconnect by unique ConnectionId
graph.disconnect(connection.id());

// Option B: Disconnect by port endpoints
boolean removed = graph.disconnect(
        NodeId.of("generator_1"), PortId.of("power_out"),
        NodeId.of("machine_1"),   PortId.of("power_in")
);
```

---

## 5. Next Steps

- Understand how data and execution flow across connections in the [Execution Engine Guide](execution.md).
- Learn how to serialize connections to JSON in the [Resource Persistence Guide](persistence.md).
