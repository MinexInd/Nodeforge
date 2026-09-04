package net.minex.nodeforge.api.serialization;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minex.nodeforge.core.graph.Graph;

import java.util.Objects;

/**
 * Mojang {@link Codec} integration for serializing and deserializing {@link Graph} instances.
 */
public final class GraphCodec implements Codec<Graph> {

    public static final GraphCodec INSTANCE = new GraphCodec(new GraphSerializer());
    public static final Codec<Graph> CODEC = INSTANCE;

    private final GraphSerializer serializer;

    public GraphCodec(GraphSerializer serializer) {
        this.serializer = Objects.requireNonNull(serializer, "serializer must not be null");
    }

    @Override
    public <T> DataResult<T> encode(Graph input, DynamicOps<T> ops, T prefix) {
        if (input == null) {
            return DataResult.error(() -> "Graph cannot be null");
        }
        try {
            JsonElement jsonTree = serializer.toJsonTree(input);
            T encoded = JsonOps.INSTANCE.convertTo(ops, jsonTree);
            return DataResult.success(encoded);
        } catch (Exception e) {
            return DataResult.error(() -> "Failed to encode Graph: " + e.getMessage());
        }
    }

    @Override
    public <T> DataResult<Pair<Graph, T>> decode(DynamicOps<T> ops, T input) {
        if (input == null) {
            return DataResult.error(() -> "Input data cannot be null");
        }
        try {
            JsonElement jsonTree = ops.convertTo(JsonOps.INSTANCE, input);
            Graph graph = serializer.fromJsonTree(jsonTree);
            return DataResult.success(Pair.of(graph, ops.empty()));
        } catch (Exception e) {
            return DataResult.error(() -> "Failed to decode Graph: " + e.getMessage());
        }
    }
}
