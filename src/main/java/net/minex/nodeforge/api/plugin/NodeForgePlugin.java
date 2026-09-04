package net.minex.nodeforge.api.plugin;

import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;

/**
 * Common entrypoint interface for external mods and extensions extending NodeForge.
 *
 * <p>Third-party mods can declare plugins in their {@code fabric.mod.json}:
 * <pre>{@code
 * "entrypoints": {
 *   "nodeforge:plugin": [
 *     "com.example.mymod.MyNodeForgePlugin"
 *   ]
 * }
 * }</pre>
 * Alternatively, plugins can be registered programmatically via {@link PluginManager#registerPlugin(NodeForgePlugin)}.
 */
public interface NodeForgePlugin {

    /**
     * Unique identifier for this plugin (defaults to simple class name or anonymous fallback).
     */
    default String id() {
        String name = getClass().getSimpleName();
        return name.isEmpty() ? "anonymous@" + Integer.toHexString(System.identityHashCode(this)) : name;
    }

    /**
     * Registers custom {@link net.minex.nodeforge.api.port.PortType} definitions.
     * Invoked first before node definitions and executors.
     *
     * @param registry the port type registry
     */
    default void registerPortTypes(PortTypeRegistry registry) {
    }

    /**
     * Registers custom {@link net.minex.nodeforge.api.registry.NodeDefinition} definitions.
     * Invoked second after port types are registered.
     *
     * @param registry the node definition registry
     */
    default void registerNodeDefinitions(NodeDefinitionRegistry registry) {
    }

    /**
     * Registers custom {@link net.minex.nodeforge.api.execution.NodeExecutor} handlers.
     * Invoked third after node definitions.
     *
     * @param registry the node executor registry
     */
    default void registerExecutors(NodeExecutorRegistry registry) {
    }

    /**
     * General lifecycle initialization hook invoked after all registrations are complete.
     *
     * @param context the NodeForge plugin context
     */
    default void onInitialize(NodeForgeContext context) {
    }
}
