package net.minex.nodeforge.client;

import net.fabricmc.api.ClientModInitializer;
import net.minex.nodeforge.NodeForge;

/**
 * Client-side initializer for NodeForge.
 *
 * <p>Registers client-only systems such as the editor UI, rendering pipeline,
 * input handling, and animation framework. This class is never loaded on a
 * dedicated server.
 *
 * @see NodeForge
 */
public class NodeForgeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		NodeForge.LOGGER.info("NodeForge client initializing");

		// Initialize and discover client plugins
		net.minex.nodeforge.client.plugin.ClientPluginManager.getInstance().initializePlugins();
	}

	/**
	 * Programmatically registers an external {@link net.minex.nodeforge.client.plugin.NodeForgeClientPlugin}.
	 *
	 * @param plugin the client plugin to register
	 */
	public static void registerClientPlugin(net.minex.nodeforge.client.plugin.NodeForgeClientPlugin plugin) {
		net.minex.nodeforge.client.plugin.ClientPluginManager.getInstance().registerPlugin(plugin);
	}
}
