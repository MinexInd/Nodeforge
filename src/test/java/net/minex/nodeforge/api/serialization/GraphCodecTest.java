package net.minex.nodeforge.api.serialization;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphCodec & Mojang DynamicOps")
class GraphCodecTest {

    @Test
    @DisplayName("encodes and decodes Graph via Mojang Codec and JsonOps")
    void encodeDecodeRoundtrip() {
        Graph graph = new Graph("codec_test");
        graph.addNode(Node.builder(NodeId.of("node1"), "type:test")
                .inputPort("in", "In", BuiltinPortTypes.INTEGER)
                .outputPort("out", "Out", BuiltinPortTypes.INTEGER)
                .build());

        // Encode to JsonElement via Codec
        DataResult<JsonElement> encodeResult = GraphCodec.CODEC.encodeStart(JsonOps.INSTANCE, graph);
        assertTrue(encodeResult.result().isPresent());
        JsonElement json = encodeResult.result().get();
        assertTrue(json.isJsonObject());

        // Decode back via Codec
        DataResult<Graph> decodeResult = GraphCodec.CODEC.parse(JsonOps.INSTANCE, json);
        assertTrue(decodeResult.result().isPresent());
        Graph restored = decodeResult.result().get();

        assertEquals("codec_test", restored.id());
        assertEquals(1, restored.nodeCount());
        assertTrue(restored.hasNode(NodeId.of("node1")));
    }

    @Test
    @DisplayName("returns DataResult error on null input")
    void errorHandling() {
        DataResult<JsonElement> result = GraphCodec.CODEC.encodeStart(JsonOps.INSTANCE, null);
        assertTrue(result.error().isPresent());
    }
}
