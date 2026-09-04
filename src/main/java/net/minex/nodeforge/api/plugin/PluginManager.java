package net.minex.nodeforge.api.plugin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minex.nodeforge.NodeForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages discovery, registration, and lifecycle orchestration of {@link NodeForgePlugin} extensions.
 */
public class PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("NodeForge/Plugins");
    private static final PluginManager INSTANCE = new PluginManager();

    private final List<NodeForgePlugin> plugins = new CopyOnWriteArrayList<>();
    private volatile boolean initialized = false;

    public PluginManager() {}

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * Programmatically registers a {@link NodeForgePlugin}.
     *
     * <p>If plugins have already been initialized, the plugin's lifecycle methods
     * are invoked immediately in sequence.
     *
     * @param plugin the plugin to register, must not be null
     */
    public synchronized void registerPlugin(NodeForgePlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        if (plugins.contains(plugin)) return;
        plugins.add(plugin);

        if (initialized) {
            initializeSinglePlugin(plugin, NodeForgeContext.createDefault());
        }
    }

    /**
     * Discovers and initializes all registered and entrypoint plugins.
     */
    public synchronized void initializePlugins() {
        if (initialized) return;

        discoverFabricEntrypoints();

        NodeForgeContext context = NodeForgeContext.createDefault();

        // Phase 1: Port Types
        for (NodeForgePlugin plugin : plugins) {
            try {
                plugin.registerPortTypes(context.portTypeRegistry());
            } catch (Exception e) {
                LOGGER.error("Plugin '{}' failed during registerPortTypes", plugin.id(), e);
            }
        }

        // Phase 2: Node Definitions
        for (NodeForgePlugin plugin : plugins) {
            try {
                plugin.registerNodeDefinitions(context.nodeRegistry());
            } catch (Exception e) {
                LOGGER.error("Plugin '{}' failed during registerNodeDefinitions", plugin.id(), e);
            }
        }

        // Phase 3: Executors
        for (NodeForgePlugin plugin : plugins) {
            try {
                plugin.registerExecutors(context.executorRegistry());
            } catch (Exception e) {
                LOGGER.error("Plugin '{}' failed during registerExecutors", plugin.id(), e);
            }
        }

        // Phase 4: Lifecycle Initialization
        for (NodeForgePlugin plugin : plugins) {
            try {
                plugin.onInitialize(context);
            } catch (Exception e) {
                LOGGER.error("Plugin '{}' failed during onInitialize", plugin.id(), e);
            }
        }

        initialized = true;
        LOGGER.info("Successfully initialized {} NodeForge plugins", plugins.size());
    }

    private void initializeSinglePlugin(NodeForgePlugin plugin, NodeForgeContext context) {
        try {
            plugin.registerPortTypes(context.portTypeRegistry());
        } catch (Exception e) {
            LOGGER.error("Plugin '{}' failed during registerPortTypes", plugin.id(), e);
        }
        try {
            plugin.registerNodeDefinitions(context.nodeRegistry());
        } catch (Exception e) {
            LOGGER.error("Plugin '{}' failed during registerNodeDefinitions", plugin.id(), e);
        }
        try {
            plugin.registerExecutors(context.executorRegistry());
        } catch (Exception e) {
            LOGGER.error("Plugin '{}' failed during registerExecutors", plugin.id(), e);
        }
        try {
            plugin.onInitialize(context);
        } catch (Exception e) {
            LOGGER.error("Plugin '{}' failed during onInitialize", plugin.id(), e);
        }
    }

    private void discoverFabricEntrypoints() {
        try {
            FabricLoader loader = FabricLoader.getInstance();
            if (loader != null) {
                List<EntrypointContainer<NodeForgePlugin>> containers =
                        loader.getEntrypointContainers("nodeforge:plugin", NodeForgePlugin.class);
                for (EntrypointContainer<NodeForgePlugin> container : containers) {
                    try {
                        NodeForgePlugin plugin = container.getEntrypoint();
                        if (plugin != null && !plugins.contains(plugin)) {
                            plugins.add(plugin);
                            LOGGER.debug("Discovered NodeForge plugin from mod '{}': {}",
                                    container.getProvider().getMetadata().getId(), plugin.id());
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load plugin entrypoint from mod '{}'",
                                container.getProvider().getMetadata().getId(), e);
                    }
                }
            }
        } catch (VirtualMachineError t) {
            throw t;
        } catch (Throwable t) {
            // In standalone test environment where FabricLoader entrypoints are unavailable
            LOGGER.debug("Fabric entrypoint discovery bypassed or not running in Fabric environment");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<NodeForgePlugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    /** Reset state for unit testing purposes. */
    public synchronized void resetForTesting() {
        plugins.clear();
        initialized = false;
    }
}
