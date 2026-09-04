package net.minex.nodeforge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minex.nodeforge.api.registry.GraphRegistry;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.serialization.GraphSerializer;
import net.minex.nodeforge.core.resource.GraphResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * NodeForge — A reusable Minecraft Fabric library for building node-based editors,
 * visual programming systems, skill trees, and other graph-based interfaces.
 *
 * <p>This is the common (client + server) mod initializer. It registers shared
 * <p>For client-specific editor initializations, see {@code NodeForgeClient} in the client source set.
 */
public class NodeForge implements ModInitializer {
    public static final String MOD_ID = "nodeforge";
    public static final String MOD_NAME = "NodeForge";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("NodeForge {} initializing", getVersion());

        // Register Minecraft Resource Graph Persistence Reload Listener
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new GraphResourceReloader());

        // Initialize and discover external plugins
        net.minex.nodeforge.api.plugin.PluginManager.getInstance().initializePlugins();
    }

    /**
     * Programmatically registers an external {@link net.minex.nodeforge.api.plugin.NodeForgePlugin}.
     *
     * @param plugin the plugin to register
     */
    public static void registerPlugin(net.minex.nodeforge.api.plugin.NodeForgePlugin plugin) {
        net.minex.nodeforge.api.plugin.PluginManager.getInstance().registerPlugin(plugin);
    }

    /**
     * Registers a custom node definition into the global NodeForge node registry.
     *
     * @param definition the node definition to register
     */
    public static void registerNode(NodeDefinition definition) {
        NodeDefinitionRegistry.getInstance().register(definition);
    }

    /**
     * Returns the global default node definition registry.
     *
     * @return the node definition registry
     */
    public static NodeDefinitionRegistry getNodeRegistry() {
        return NodeDefinitionRegistry.getInstance();
    }

    /**
     * Returns the global default graph registry.
     *
     * @return the graph registry
     */
    public static GraphRegistry getGraphRegistry() {
        return GraphRegistry.getInstance();
    }

    /**
     * Returns the global default node executor registry.
     *
     * @return the node executor registry
     */
    public static net.minex.nodeforge.api.execution.NodeExecutorRegistry getExecutorRegistry() {
        return net.minex.nodeforge.api.execution.NodeExecutorRegistry.getInstance();
    }

    /**
     * Returns a new instance of {@link net.minex.nodeforge.core.execution.GraphEvaluator} with standard built-ins preloaded.
     *
     * @return a new graph evaluator
     */
    public static net.minex.nodeforge.core.execution.GraphEvaluator getEvaluator() {
        return new net.minex.nodeforge.core.execution.GraphEvaluator();
    }

    /**
     * Returns a new instance of {@link GraphSerializer} with default port types preloaded.
     *
     * @return a new graph serializer
     */
    public static GraphSerializer getSerializer() {
        return new GraphSerializer();
    }

    /**
     * Resolves the current mod version dynamically from Fabric Loader,
     * falling back to "1.0.0" if running outside of a Fabric environment.
     *
     * @return the resolved version string
     */
    public static String getVersion() {
        try {
            Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(MOD_ID);
            if (container.isPresent()) {
                return container.get().getMetadata().getVersion().getFriendlyString();
            }
        } catch (VirtualMachineError e) {
            throw e;
        } catch (Throwable ignored) {
            // Outside of Fabric environment (e.g. standalone unit test)
        }
        return "1.0.0";
    }

    /**
     * Creates a {@link Identifier} namespaced to the NodeForge mod.
     *
     * @param path the resource path
     * @return a new identifier with the {@code nodeforge} namespace
     */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
