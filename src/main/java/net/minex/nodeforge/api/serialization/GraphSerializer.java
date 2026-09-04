package net.minex.nodeforge.api.serialization;

import com.google.gson.*;
import net.minex.nodeforge.api.graph.*;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;

/**
 * Serializes and deserializes {@link Graph} instances to and from JSON format using Gson.
 *
 * <p>Preserves all node geometries, typed ports, properties, metadata, and connections with
 * schema versioning and strict integrity validation.
 */
public final class GraphSerializer {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON_COMPACT = new GsonBuilder().create();

    private final PortTypeRegistry portTypeRegistry;

    public GraphSerializer(PortTypeRegistry portTypeRegistry) {
        this.portTypeRegistry = Objects.requireNonNull(portTypeRegistry, "portTypeRegistry must not be null");
    }

    public GraphSerializer() {
        this(PortTypeRegistry.getInstance());
    }

    // ========== Serialization ==========

    /**
     * Serializes a graph into a pretty-printed JSON string.
     */
    public String toJson(Graph graph) {
        return toJson(graph, true);
    }

    /**
     * Serializes a graph into a JSON string with optional pretty-printing.
     */
    public String toJson(Graph graph, boolean pretty) {
        JsonObject obj = toJsonTree(graph);
        return pretty ? GSON_PRETTY.toJson(obj) : GSON_COMPACT.toJson(obj);
    }

    /**
     * Serializes a graph into a Gson {@link JsonObject}.
     */
    public JsonObject toJsonTree(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        JsonObject root = new JsonObject();
        root.addProperty("schema_version", CURRENT_SCHEMA_VERSION);
        root.addProperty("id", graph.id());

        // Metadata
        JsonObject metaObj = new JsonObject();
        for (Map.Entry<String, String> entry : graph.metadata().entrySet()) {
            metaObj.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("metadata", metaObj);

        // Nodes
        JsonArray nodesArray = new JsonArray();
        for (Node node : graph.getNodes()) {
            JsonObject nodeObj = new JsonObject();
            nodeObj.addProperty("id", node.id().value());
            nodeObj.addProperty("type", node.typeKey());
            nodeObj.addProperty("display_name", node.displayName());

            // Position
            JsonObject posObj = new JsonObject();
            posObj.addProperty("x", node.position().x());
            posObj.addProperty("y", node.position().y());
            nodeObj.add("position", posObj);

            // Size
            JsonObject sizeObj = new JsonObject();
            sizeObj.addProperty("width", node.size().width());
            sizeObj.addProperty("height", node.size().height());
            nodeObj.add("size", sizeObj);

            // Ports
            JsonArray portsArray = new JsonArray();
            for (Port port : node.ports().values()) {
                JsonObject portObj = new JsonObject();
                portObj.addProperty("id", port.id().value());
                portObj.addProperty("name", port.name());
                portObj.addProperty("type", port.typeKey());
                portObj.addProperty("direction", port.direction().name());
                portsArray.add(portObj);
            }
            nodeObj.add("ports", portsArray);

            // Node Metadata
            JsonObject nodeMetaObj = new JsonObject();
            for (Map.Entry<String, String> entry : node.metadata().entrySet()) {
                nodeMetaObj.addProperty(entry.getKey(), entry.getValue());
            }
            nodeObj.add("metadata", nodeMetaObj);

            nodesArray.add(nodeObj);
        }
        root.add("nodes", nodesArray);

        // Connections
        JsonArray connsArray = new JsonArray();
        for (Connection conn : graph.getConnections()) {
            JsonObject connObj = new JsonObject();
            connObj.addProperty("id", conn.id().value());
            connObj.addProperty("from_node", conn.fromNode().value());
            connObj.addProperty("from_port", conn.fromPort().value());
            connObj.addProperty("to_node", conn.toNode().value());
            connObj.addProperty("to_port", conn.toPort().value());
            connsArray.add(connObj);
        }
        root.add("connections", connsArray);

        // Comments
        JsonArray commentsArray = new JsonArray();
        for (net.minex.nodeforge.api.graph.CommentBox box : graph.getCommentBoxes()) {
            JsonObject boxObj = new JsonObject();
            boxObj.addProperty("id", box.id());
            boxObj.addProperty("title", box.title());
            boxObj.addProperty("color", box.color());

            JsonObject posObj = new JsonObject();
            posObj.addProperty("x", box.position().x());
            posObj.addProperty("y", box.position().y());
            boxObj.add("position", posObj);

            JsonObject sizeObj = new JsonObject();
            sizeObj.addProperty("width", box.size().width());
            sizeObj.addProperty("height", box.size().height());
            boxObj.add("size", sizeObj);

            commentsArray.add(boxObj);
        }
        root.add("comments", commentsArray);

        return root;
    }

    // ========== Deserialization ==========

    /**
     * Deserializes a graph from a JSON string.
     */
    public Graph fromJson(String json) {
        Objects.requireNonNull(json, "json string must not be null");
        try {
            JsonElement element = JsonParser.parseString(json);
            return fromJsonTree(element);
        } catch (JsonSyntaxException e) {
            throw new GraphSerializationException("Malformed JSON string: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a graph from a Gson {@link JsonElement}.
     */
    public Graph fromJsonTree(JsonElement element) {
        Objects.requireNonNull(element, "json element must not be null");
        if (!element.isJsonObject()) {
            throw new GraphSerializationException("Expected JSON object at root of graph");
        }

        try {
            JsonObject root = element.getAsJsonObject();

            // 1. Validate schema version
            if (!root.has("schema_version")) {
                throw new GraphSerializationException("Missing required 'schema_version' property");
            }
            int version = root.get("schema_version").getAsInt();
            if (version != CURRENT_SCHEMA_VERSION) {
                throw new GraphSerializationException("Unsupported schema_version: " + version + " (expected " + CURRENT_SCHEMA_VERSION + ")");
            }

            // 2. Graph ID
            if (!root.has("id") || root.get("id").getAsString().isBlank()) {
                throw new GraphSerializationException("Missing or blank 'id' property in graph JSON");
            }
            String graphId = root.get("id").getAsString();
            Graph graph = new Graph(graphId);

            // 3. Metadata
            if (root.has("metadata") && root.get("metadata").isJsonObject()) {
                JsonObject metaObj = root.getAsJsonObject("metadata");
                for (Map.Entry<String, JsonElement> entry : metaObj.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        graph.setMetadata(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }

            // 4. Nodes
            if (root.has("nodes") && root.get("nodes").isJsonArray()) {
                JsonArray nodesArray = root.getAsJsonArray("nodes");
                for (int nodeIdx = 0; nodeIdx < nodesArray.size(); nodeIdx++) {
                    JsonElement nodeElem = nodesArray.get(nodeIdx);
                    if (!nodeElem.isJsonObject()) continue;
                    JsonObject nodeObj = nodeElem.getAsJsonObject();

                    String idStr = getRequiredString(nodeObj, "id", "node at index " + nodeIdx);
                    String typeStr = getRequiredString(nodeObj, "type", "node '" + idStr + "' (index " + nodeIdx + ")");
                    String nameStr = nodeObj.has("display_name") ? nodeObj.get("display_name").getAsString() : idStr;

                    Node.Builder builder = Node.builder(NodeId.of(idStr), typeStr)
                            .displayName(nameStr);

                    // Position
                    if (nodeObj.has("position") && nodeObj.get("position").isJsonObject()) {
                        JsonObject posObj = nodeObj.getAsJsonObject("position");
                        double x = posObj.has("x") ? posObj.get("x").getAsDouble() : 0.0;
                        double y = posObj.has("y") ? posObj.get("y").getAsDouble() : 0.0;
                        builder.position(x, y);
                    }

                    // Size
                    if (nodeObj.has("size") && nodeObj.get("size").isJsonObject()) {
                        JsonObject sizeObj = nodeObj.getAsJsonObject("size");
                        double w = sizeObj.has("width") ? sizeObj.get("width").getAsDouble() : 160.0;
                        double h = sizeObj.has("height") ? sizeObj.get("height").getAsDouble() : 80.0;
                        builder.size(new Size(w, h));
                    }

                    // Ports
                    if (nodeObj.has("ports") && nodeObj.get("ports").isJsonArray()) {
                        JsonArray portsArray = nodeObj.getAsJsonArray("ports");
                        for (JsonElement portElem : portsArray) {
                            if (!portElem.isJsonObject()) continue;
                            JsonObject portObj = portElem.getAsJsonObject();

                            String portId = getRequiredString(portObj, "id", "port");
                            String portName = portObj.has("name") ? portObj.get("name").getAsString() : portId;
                            String typeKey = portObj.has("type") ? portObj.get("type").getAsString() : "nodeforge:any";
                            String dirStr = getRequiredString(portObj, "direction", "port");

                            PortDirection dir;
                            try {
                                dir = PortDirection.valueOf(dirStr.toUpperCase(Locale.ROOT));
                            } catch (IllegalArgumentException e) {
                                throw new GraphSerializationException("Invalid port direction '" + dirStr + "' on port '" + portId + "'");
                            }
                            PortType<?> portType = portTypeRegistry.get(typeKey);

                            if (dir == PortDirection.INPUT) {
                                if (portType != null) {
                                    builder.inputPort(portId, portName, portType);
                                } else {
                                    builder.inputPort(portId, portName, typeKey);
                                }
                            } else {
                                if (portType != null) {
                                    builder.outputPort(portId, portName, portType);
                                } else {
                                    builder.outputPort(portId, portName, typeKey);
                                }
                            }
                        }
                    }

                    // Properties & Metadata
                    if (nodeObj.has("properties") && nodeObj.get("properties").isJsonObject()) {
                        JsonObject propsObj = nodeObj.getAsJsonObject("properties");
                        for (Map.Entry<String, JsonElement> entry : propsObj.entrySet()) {
                            JsonElement val = entry.getValue();
                            if (val.isJsonPrimitive()) {
                                builder.metadata(entry.getKey(), val.getAsString());
                            }
                        }
                    }

                    if (nodeObj.has("metadata") && nodeObj.get("metadata").isJsonObject()) {
                        JsonObject nodeMetaObj = nodeObj.getAsJsonObject("metadata");
                        for (Map.Entry<String, JsonElement> entry : nodeMetaObj.entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                builder.metadata(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                    }

                    graph.addNode(builder.build());
                }
            }

            // 5. Connections
            if (root.has("connections") && root.get("connections").isJsonArray()) {
                JsonArray connsArray = root.getAsJsonArray("connections");
                for (JsonElement connElem : connsArray) {
                    if (!connElem.isJsonObject()) continue;
                    JsonObject connObj = connElem.getAsJsonObject();

                    String fromNode = getRequiredString(connObj, "from_node", "connection");
                    String fromPort = getRequiredString(connObj, "from_port", "connection");
                    String toNode = getRequiredString(connObj, "to_node", "connection");
                    String toPort = getRequiredString(connObj, "to_port", "connection");

                    ConnectionResult result = graph.connect(
                            NodeId.of(fromNode), PortId.of(fromPort),
                            NodeId.of(toNode), PortId.of(toPort)
                    );

                    if (result instanceof ConnectionResult.Failure failure) {
                        throw new GraphSerializationException("Failed to restore connection (" +
                                fromNode + ":" + fromPort + " -> " + toNode + ":" + toPort + "): " + failure.reason());
                    }
                }
            }

            // 6. Comments
            if (root.has("comments") && root.get("comments").isJsonArray()) {
                JsonArray commentsArray = root.getAsJsonArray("comments");
                for (JsonElement commentElem : commentsArray) {
                    if (!commentElem.isJsonObject()) continue;
                    JsonObject boxObj = commentElem.getAsJsonObject();
                    String boxId = getRequiredString(boxObj, "id", "comment");
                    String title = boxObj.has("title") ? boxObj.get("title").getAsString() : "Comment";
                    int color = boxObj.has("color") ? boxObj.get("color").getAsInt() : net.minex.nodeforge.api.graph.CommentBox.DEFAULT_COLOR;

                    double x = 0.0, y = 0.0;
                    if (boxObj.has("position") && boxObj.get("position").isJsonObject()) {
                        JsonObject pos = boxObj.getAsJsonObject("position");
                        x = pos.has("x") ? pos.get("x").getAsDouble() : 0.0;
                        y = pos.has("y") ? pos.get("y").getAsDouble() : 0.0;
                    }

                    double w = 200.0, h = 150.0;
                    if (boxObj.has("size") && boxObj.get("size").isJsonObject()) {
                        JsonObject size = boxObj.getAsJsonObject("size");
                        w = size.has("width") ? size.get("width").getAsDouble() : 200.0;
                        h = size.has("height") ? size.get("height").getAsDouble() : 150.0;
                    }

                    graph.addCommentBox(new net.minex.nodeforge.api.graph.CommentBox(
                            boxId, title,
                            new net.minex.nodeforge.api.graph.Position(x, y),
                            new net.minex.nodeforge.api.graph.Size(w, h),
                            color
                    ));
                }
            }

            return graph;
        } catch (GraphSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphSerializationException("Malformed graph JSON structure: " + e.getMessage(), e);
        }
    }

    private static String getRequiredString(JsonObject obj, String member, String context) {
        if (!obj.has(member) || obj.get(member).getAsString().isBlank()) {
            throw new GraphSerializationException("Missing or blank '" + member + "' in " + context + " JSON");
        }
        return obj.get(member).getAsString();
    }
}
