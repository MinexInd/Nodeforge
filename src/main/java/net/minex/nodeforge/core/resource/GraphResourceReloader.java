package net.minex.nodeforge.core.resource;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minex.nodeforge.api.registry.GraphRegistry;
import net.minex.nodeforge.api.serialization.GraphCodec;
import net.minex.nodeforge.core.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Server resource reload listener facilitating Minecraft Resource Graph Persistence.
 * Discovers, deserializes, validates, and loads NodeForge graph definitions from JSON files
 * located within data resource directories (by default {@code data/<namespace>/nodeforge/graphs/*.json}).
 *
 * <p>Consumer mods may instantiate this reloader with custom resource subdirectories and listener
 * identifiers to control their own resource namespaces and storage structures.
 */
public class GraphResourceReloader extends JsonDataLoader<Graph> implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("NodeForge/ResourcePersistence");
    private static final Identifier DEFAULT_RELOADER_ID = Identifier.of("nodeforge", "graphs");
    public static final String DEFAULT_DIRECTORY_PATH = "nodeforge/graphs";

    private final GraphRegistry registry;
    private final Identifier reloaderId;
    private final String directoryPath;

    /**
     * Constructs a resource graph reloader targeting a consumer-specified directory path and identifier.
     *
     * @param registry      the destination graph registry
     * @param directoryPath the resource subdirectory within {@code data/<namespace>/}
     * @param reloaderId    unique reload listener identifier for Fabric resource sequencing
     */
    public GraphResourceReloader(GraphRegistry registry, String directoryPath, Identifier reloaderId) {
        super(GraphCodec.CODEC, ResourceFinder.json(Objects.requireNonNull(directoryPath, "directoryPath must not be null")));
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.directoryPath = directoryPath;
        this.reloaderId = Objects.requireNonNull(reloaderId, "reloaderId must not be null");
    }

    /**
     * Constructs a resource graph reloader using the default {@code nodeforge/graphs} subdirectory.
     *
     * @param registry the destination graph registry
     */
    public GraphResourceReloader(GraphRegistry registry) {
        this(registry, DEFAULT_DIRECTORY_PATH, DEFAULT_RELOADER_ID);
    }

    /**
     * Constructs a resource graph reloader using the global {@link GraphRegistry} singleton
     * and default resource paths.
     */
    public GraphResourceReloader() {
        this(GraphRegistry.getInstance());
    }

    @Override
    public Identifier getFabricId() {
        return reloaderId;
    }

    /** Returns the resource subdirectory path monitored by this reloader. */
    public String getDirectoryPath() {
        return directoryPath;
    }

    @Override
    protected void apply(Map<Identifier, Graph> prepared, ResourceManager manager, Profiler profiler) {
        LOGGER.info("Loading persisted NodeForge graphs from resource path '{}'...", directoryPath);
        int loadedCount = 0;

        for (Map.Entry<Identifier, Graph> entry : prepared.entrySet()) {
            Identifier id = entry.getKey();
            Graph graph = entry.getValue();
            try {
                registry.register(id, graph);
                loadedCount++;
                LOGGER.debug("Loaded persisted graph '{}' (nodes: {}, connections: {})",
                        id, graph.nodeCount(), graph.connectionCount());
            } catch (Exception e) {
                LOGGER.error("Failed to register persisted graph '{}': {}", id, e.getMessage());
            }
        }

        LOGGER.info("Successfully loaded {} persisted NodeForge graph(s).", loadedCount);
    }
}
