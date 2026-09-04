# Core Concepts

This guide introduces the structural concepts and terminology underpinning NodeForge. Understanding these primitives enables developers to model arbitrary workflows, from mathematical calculators to branching dialogue trees.

---

## 1. The Anatomy of a Graph

A **Graph** (`Graph`) is a container managing nodes and the connections established between them.

```text
  +-------------------------------------------------------------+
  |                           Graph                             |
  |                                                             |
  |  +--------------------+             +--------------------+  |
  |  |     Node A         |             |     Node B         |  |
  |  |                    |             |                    |  |
  |  |  [In 1]   [Out 1]o======Connection======>[In 1] [Out 1]  |  |
  |  |  [In 2]            |             |                    |  |
  |  +--------------------+             +--------------------+  |
  +-------------------------------------------------------------+
```

A graph manages:
1. **Nodes (`Node`)**: The functional vertices representing operations, expressions, or logical steps.
2. **Connections (`Connection`)**: The directed edges routing data or execution pulses between ports.
3. **Comment Boxes (`CommentBox`)**: Visual bounding frames used in the editor to group related nodes.
4. **Metadata**: Key-value string attributes used for mod-specific tagging or versioning.

---

## 2. Node Definition vs. Node Instance

A critical architectural distinction exists between a **Node Definition** and a **Node Instance**:

```text
+------------------------------+             +------------------------------+
|   NodeDefinition (Template)  |             |      Node (Concrete Instance)|
|------------------------------|             |------------------------------|
| - Type ID: "math:add"        |             | - Instance ID: "add_node_1"  |
| - Display Name: "Add"        |  Creates    | - Type Key: "math:add"       |
| - Category: "Arithmetic"     |===========> | - Position: (x: 120, y: 80)  |
| - Default Width: 140         |             | - Concrete Input Ports       |
| - Port Templates: A, B -> Sum|             | - Concrete Output Ports      |
+------------------------------+             +------------------------------+
```

- **NodeDefinition** (`net.minex.nodeforge.api.registry.NodeDefinition`):
  An immutable archetype registered in the `NodeDefinitionRegistry`. It describes how a node is presented in the creation palette, what ports it contains by default, its display color, and its category.
- **Node** (`net.minex.nodeforge.api.graph.Node`):
  A concrete instance living inside a specific `Graph`. It possesses a unique `NodeId`, an $(X, Y)$ world position, and instances of ports. A single graph can contain dozens of distinct node instances instantiated from the same `NodeDefinition`.

---

## 3. Ports: Data vs. Execution

A **Port** (`Port`) is a connection socket located on the boundary of a node card. Every port has a direction:
- `PortDirection.INPUT`: Receives values or execution pulses from upstream connections.
- `PortDirection.OUTPUT`: Emits evaluated results or execution pulses to downstream connections.

NodeForge categorizes ports into two fundamental functional types:

### 3.1 Data Ports
Data ports carry typed values (integers, strings, vectors, or custom objects).
- When a connection links an output data port to an input data port, the value computed by the source node is propagated across the wire to the target node.
- Data ports are color-coded based on their `PortType` (for example, Orange for Int, Cyan for Float, Green for String).

### 3.2 Execution Ports
Execution ports (`isExecution() == true`) control procedural sequencing rather than transmitting data.
- Standard execution ports carry the type `BuiltinPortTypes.EXECUTION` (represented as white triangular sockets).
- An execution connection indicates: *"When this node finishes its task, pulse the next node."*
- Procedural loops (such as while-loops or sequential dialogue branches) connect execution ports.

---

## 4. Connections & Type Compatibility

A **Connection** (`Connection`) is a directed 4-tuple:

$$\text{Connection} = (\text{fromNode}, \text{fromPort}, \text{toNode}, \text{toPort})$$

Connections must obey structural invariants enforced by `Graph.connect()`:
1. **Directional Invariant**: Must connect an `OUTPUT` port to an `INPUT` port. Connecting input-to-input or output-to-output is rejected.
2. **Endpoint Invariant**: Both source and target nodes and ports must exist within the graph.
3. **Loop Invariant**: A port cannot connect to itself on the same node.
4. **Duplicate Invariant**: Duplicate connections between identical port pairs are rejected in $O(1)$ time.
5. **Type Compatibility**: The data type emitted by the output port must be compatible with the target input port according to the `TypeCompatibilityEngine` (supporting implicit numeric widening and subclass assignability).

---

## 5. Execution Buffers & Runtime Context

During graph execution, **nodes are not mutated**. The node's position, configuration, and ports remain unchanged.

Instead, all dynamic runtime values are managed inside an **`ExecutionContext`**:

```text
ExecutionContext
├── Global Variables:   {"player_level": 42, "multiplier": 2.5}
├── Step Counter:       Current: 14 / Limit: 10,000
├── Cancellation Token: false
├── Input Buffers:      (Node "add_1", Port "a") => 10.0
│                       (Node "add_1", Port "b") => 5.0
└── Output Buffers:     (Node "add_1", Port "sum") => 15.0
```

Because runtime state is encapsulated inside the `ExecutionContext`, **multiple threads can evaluate the same graph simultaneously** by passing their own separate `ExecutionContext` instances.

---

## 6. Execution Paradigms

NodeForge natively supports three execution modes:

1. **Pure Data-Flow**:
   The entire graph is sorted topologically via Kahn's algorithm. Nodes execute strictly in dependency order from sources to sinks. Cycles are forbidden.
2. **Control-Flow Pulse**:
   Execution starts at a specified entrypoint node and steps sequentially along execution wires, branching based on conditional logic.
3. **Hybrid Interleaved**:
   A control-flow node automatically pulls data from upstream data nodes on demand before running its procedural action.

---

## 7. Next Steps

- Follow the [First Graph Tutorial](first-graph.md) to put these concepts into practice with runnable Java code.
- Read the [Graph API Guide](../guides/graphs.md) for detailed mutation and query methods.
