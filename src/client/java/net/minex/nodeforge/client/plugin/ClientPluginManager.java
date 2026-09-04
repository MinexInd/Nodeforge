package net.minex.nodeforge.client.plugin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages discovery, registration, and lifecycle orchestration of client-side {@link NodeForgeClientPlugin} extensions.
 */
public class ClientPluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("NodeForge/ClientPlugins");
    private static final ClientPluginManager INSTANCE = new ClientPluginManager();

    private final List<NodeForgeClientPlugin> plugins = new CopyOnWriteArrayList<>();
    private volatile boolean initialized = false;

    public ClientPluginManager() {}

    public static ClientPluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * Programmatically registers a client plugin.
     *
     * @param plugin the plugin to register
     */
    public synchronized void registerPlugin(NodeForgeClientPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        if (plugins.contains(plugin)) return;
        plugins.add(plugin);

        if (initialized) {
            initializeSinglePlugin(plugin, NodeForgeClientContext.createDefault());
        }
    }

    /**
     * Discovers and initializes all registered and entrypoint client plugins.
     */
    public synchronized void initializePlugins() {
        if (initialized) return;

        discoverFabricEntrypoints();

        NodeForgeClientContext context = NodeForgeClientContext.createDefault();

        // 1. Themes
        for (NodeForgeClientPlugin plugin : plugins) {
            try {
                plugin.registerThemes(context.themeRegistry());
            } catch (Exception e) {
                LOGGER.error("Client plugin '{}' failed during registerThemes", plugin.id(), e);
            }
        }

        // 2. Custom Node Renderers
        for (NodeForgeClientPlugin plugin : plugins) {
            try {
                plugin.registerCustomNodeRenderers(context.nodeRendererRegistry());
            } catch (Exception e) {
                LOGGER.error("Client plugin '{}' failed during registerCustomNodeRenderers", plugin.id(), e);
            }
        }

        // 3. Node Icons
        for (NodeForgeClientPlugin plugin : plugins) {
            try {
                plugin.registerNodeIcons(context.iconRegistry());
            } catch (Exception e) {
                LOGGER.error("Client plugin '{}' failed during registerNodeIcons", plugin.id(), e);
            }
        }

        // 4. Canvas Layers
        for (NodeForgeClientPlugin plugin : plugins) {
            try {
                plugin.registerCanvasLayers(context.canvasLayerRegistry());
            } catch (Exception e) {
                LOGGER.error("Client plugin '{}' failed during registerCanvasLayers", plugin.id(), e);
            }
        }

        // 5. Client Lifecycle Initialization
        for (NodeForgeClientPlugin plugin : plugins) {
            try {
                plugin.onInitializeClient(context);
            } catch (Exception e) {
                LOGGER.error("Client plugin '{}' failed during onInitializeClient", plugin.id(), e);
            }
        }

        initialized = true;
        LOGGER.info("Successfully initialized {} NodeForge client plugins", plugins.size());
    }

    private void initializeSinglePlugin(NodeForgeClientPlugin plugin, NodeForgeClientContext context) {
        try {
            plugin.registerThemes(context.themeRegistry());
        } catch (Exception e) {
            LOGGER.error("Client plugin '{}' failed during registerThemes", plugin.id(), e);
        }
        try {
            plugin.registerCustomNodeRenderers(context.nodeRendererRegistry());
        } catch (Exception e) {
            LOGGER.error("Client plugin '{}' failed during registerCustomNodeRenderers", plugin.id(), e);
        }
        try {
            plugin.registerNodeIcons(context.iconRegistry());
        } catch (Exception e) {
            LOGGER.error("Client plugin '{}' failed during registerNodeIcons", plugin.id(), e);
        }
        try {
            plugin.registerCanvasLayers(context.canvasLayerRegistry());
        } catch (Exception e) {
            LOGGER.error("Client plugin '{}' failed during registerCanvasLayers", plugin.id(), e);
        }
        try {
            plugin.onInitializeClient(context);
        } catch (Exception e) {
            LOGGER.error("Client plugin '{}' failed during onInitializeClient", plugin.id(), e);
        }
    }

    private void discoverFabricEntrypoints() {
        try {
            FabricLoader loader = FabricLoader.getInstance();
            if (loader != null) {
                List<EntrypointContainer<NodeForgeClientPlugin>> containers =
                        loader.getEntrypointContainers("nodeforge:client_plugin", NodeForgeClientPlugin.class);
                for (EntrypointContainer<NodeForgeClientPlugin> container : containers) {
                    try {
                        NodeForgeClientPlugin plugin = container.getEntrypoint();
                        if (plugin != null && !plugins.contains(plugin)) {
                            plugins.add(plugin);
                            LOGGER.debug("Discovered NodeForge client plugin from mod '{}': {}",
                                    container.getProvider().getMetadata().getId(), plugin.id());
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load client plugin entrypoint from mod '{}'",
                                container.getProvider().getMetadata().getId(), e);
                    }
                }
            }
        } catch (VirtualMachineError t) {
            throw t;
        } catch (Throwable t) {
            // Standalone test environment where FabricLoader entrypoints are unavailable
            LOGGER.debug("Fabric client entrypoint discovery bypassed or not running in Fabric environment");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<NodeForgeClientPlugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public synchronized void resetForTesting() {
        plugins.clear();
        initialized = false;
    }
}
