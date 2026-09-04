package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeLifecycleHooks")
class NodeLifecycleHooksTest {

    @Test
    @DisplayName("default EMPTY hooks execute without exception")
    void emptyHooks() {
        assertNotNull(NodeLifecycleHooks.EMPTY);
        Graph graph = new Graph("g");
        Node node = Node.builder(NodeId.of("n"), "type").build();

        assertDoesNotThrow(() -> {
            NodeLifecycleHooks.EMPTY.onNodeCreated(node, graph);
            NodeLifecycleHooks.EMPTY.onNodeRemoved(node, graph);
            NodeLifecycleHooks.EMPTY.onConnected(node, null, null, graph);
            NodeLifecycleHooks.EMPTY.onDisconnected(node, null, null, graph);
        });
    }

    @Test
    @DisplayName("custom lifecycle hooks receive event callbacks")
    void customLifecycleHooks() {
        AtomicBoolean created = new AtomicBoolean(false);
        AtomicBoolean removed = new AtomicBoolean(false);
        AtomicInteger connectCount = new AtomicInteger(0);

        NodeLifecycleHooks hooks = new NodeLifecycleHooks() {
            @Override
            public void onNodeCreated(Node node, Graph graph) {
                created.set(true);
            }

            @Override
            public void onNodeRemoved(Node node, Graph graph) {
                removed.set(true);
            }

            @Override
            public void onConnected(Node node, Port port, Connection connection, Graph graph) {
                connectCount.incrementAndGet();
            }
        };

        NodeDefinition def = NodeDefinition.builder("test:hook_node")
                .inputPort("in", "In", BuiltinPortTypes.STRING)
                .lifecycleHooks(hooks)
                .build();

        assertSame(hooks, def.lifecycleHooks());

        Graph graph = new Graph("test");
        Node node = def.createNode(NodeId.of("hook_inst"));

        hooks.onNodeCreated(node, graph);
        assertTrue(created.get());

        hooks.onConnected(node, node.getPort(null), null, graph);
        assertEquals(1, connectCount.get());

        hooks.onNodeRemoved(node, graph);
        assertTrue(removed.get());
    }
}
