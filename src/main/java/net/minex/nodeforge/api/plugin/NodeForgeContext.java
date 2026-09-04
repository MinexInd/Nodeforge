package net.minex.nodeforge.api.plugin;

import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.GraphRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;

import java.util.Objects;

/**
 * Context provided to {@link NodeForgePlugin} instances during initialization.
 */
public record NodeForgeContext(
        NodeDefinitionRegistry nodeRegistry,
        PortTypeRegistry portTypeRegistry,
        NodeExecutorRegistry executorRegistry,
        GraphRegistry graphRegistry
) {

    public NodeForgeContext {
        Objects.requireNonNull(nodeRegistry, "nodeRegistry must not be null");
        Objects.requireNonNull(portTypeRegistry, "portTypeRegistry must not be null");
        Objects.requireNonNull(executorRegistry, "executorRegistry must not be null");
        Objects.requireNonNull(graphRegistry, "graphRegistry must not be null");
    }

    /** Creates a default context using the global singleton registries. */
    public static NodeForgeContext createDefault() {
        return new NodeForgeContext(
                NodeDefinitionRegistry.getInstance(),
                PortTypeRegistry.getInstance(),
                NodeExecutorRegistry.getInstance(),
                GraphRegistry.getInstance()
        );
    }
}
