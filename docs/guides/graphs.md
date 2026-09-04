# Working with Graphs

The `Graph` class (`net.minex.nodeforge.core.graph.Graph`) is the central container managing nodes, connections, visual comment boxes, and metadata.

---

## 1. Creating a Graph

Instantiate a graph by providing a non-null, non-blank string identifier:

```java
import net.minex.nodeforge.core.graph.Graph;

Graph graph = new Graph("skill_tree_combat");
System.out.println("Graph ID: " + graph.id()); // skill_tree_combat
```

---

## 2. Managing Nodes

### 2.1 Adding Nodes
Nodes are added via `addNode(Node)`. The graph enforces unique node identifiers:

```java
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.id.NodeId;

Node nodeA = Node.builder(NodeId.of("node_a"), "math:add").build();
graph.addNode(nodeA);

// Attempting to add another node with the same NodeId throws IllegalArgumentException
Node duplicate = Node.builder(NodeId.of("node_a"), "math:multiply").build();
// graph.addNode(duplicate); // Throws IllegalArgumentException!
```

### 2.2 Retrieving Nodes
Query nodes safely by ID:

```java
// Check existence in O(1) time
boolean exists = graph.hasNode(NodeId.of("node_a"));

// Retrieve node instance (returns null if absent)
Node retrieved = graph.getNode(NodeId.of("node_a"));

// Access all nodes as an unmodifiable snapshot collection
Collection<Node> allNodes = graph.getNodes();
int totalNodes = graph.nodeCount();
```

### 2.3 Removing Nodes & Cascading Deletion
When a node is removed, NodeForge automatically performs **cascading connection deletion**: all connections attached to any of the node's input or output ports are purged simultaneously:

```java
boolean removed = graph.removeNode(NodeId.of("node_a"));
// All incident wires connected to "node_a" are automatically removed from the graph
```

Because the graph maintains internal adjacency indices, node removal operates in $O(\text{deg}(v))$ time rather than $O(|E|)$ full-graph scans.

---

## 3. Managing Connections

### 3.1 Establishing Connections
Connect ports using `graph.connect(...)`. The method returns a `ConnectionResult`:

```java
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

ConnectionResult result = graph.connect(
        NodeId.of("node_a"), PortId.of("out"),
        NodeId.of("node_b"), PortId.of("in")
);

if (result.isSuccess()) {
    System.out.println("Connected: " + result.connection().get().id());
} else {
    System.err.println("Connection rejected: " + result.errorMessage());
}
```

### 3.2 Querying Connections
Connections can be inspected via relational queries:

```java
// All connections in the graph
Collection<Connection> allConnections = graph.getConnections();
int totalConnections = graph.connectionCount();

// Connections attached to a specific node (either as source or target)
Set<Connection> nodeWires = graph.getConnectionsForNode(NodeId.of("node_a"));

// Connections attached to a specific port
Set<Connection> portWires = graph.getConnectionsForPort(NodeId.of("node_a"), PortId.of("out"));
```

### 3.3 Disconnecting
Remove connections by connection ID or by endpoint pair:

```java
// Disconnect by specific ConnectionId
graph.disconnect(connectionId);

// Disconnect by endpoints
boolean severed = graph.disconnect(
        NodeId.of("node_a"), PortId.of("out"),
        NodeId.of("node_b"), PortId.of("in")
);
```

---

## 4. Comment Boxes & Visual Grouping

Comment boxes (`CommentBox`) enclose sets of nodes within a translucent, titled rectangular frame on the visual canvas:

```java
import net.minex.nodeforge.api.graph.BoundingBox;
import net.minex.nodeforge.api.graph.CommentBox;

CommentBox frame = new CommentBox(
        "math_cluster",                                    // Unique box ID
        "Arithmetic Logic Cluster",                       // Display title
        new BoundingBox(100.0, 50.0, 500.0, 400.0),       // Bounds (minX, minY, maxX, maxY)
        0x3300FF88                                         // ARGB color (semi-transparent green)
);

graph.addCommentBox(frame);

// Querying comment boxes
Map<String, CommentBox> boxes = graph.getCommentBoxes();
graph.removeCommentBox("math_cluster");
```

---

## 5. Custom Metadata

Graphs support string metadata for mod-specific tagging, schema revisions, or author attribution:

```java
graph.setMetadata("version", "1.2.0");
graph.setMetadata("author", "MinexInd");

String version = graph.getMetadata("version"); // "1.2.0"
Map<String, String> allMetadata = graph.getMetadata();
```

---

## 6. Concurrency & Thread-Safety Guarantees

- **Atomic Mutations**: Every mutating method on `Graph` is `synchronized`. Internal indices remain consistent across concurrent modifications.
- **Snapshot Iteration**: Methods such as `getNodes()` and `getConnections()` return `List.copyOf(...)` unmodifiable snapshots, guaranteeing that iterating nodes while another thread mutates the graph will not throw `ConcurrentModificationException`.
- **Evaluation Immutability**: Graph evaluation assumes the topological structure of the graph is stable. If you mutate a graph while another thread evaluates it, synchronize externally:

```java
synchronized (graph) {
    graph.removeNode(nodeId);
}
```

---

## 7. Next Steps

- Learn how to configure individual node instances in the [Node API Guide](nodes.md).
- Understand port sockets and type matching in the [Ports & Types Guide](ports.md).
