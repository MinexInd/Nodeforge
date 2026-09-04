# Custom Editor Integration Example

This guide explains how to open, configure, and integrate the NodeForge visual graph editor (`NodeEditorScreen`) into your Minecraft mod.

---

## Architecture & Client Isolation

`NodeEditorScreen` is a Minecraft client screen (`net.minecraft.client.gui.screen.Screen`). 

> [!WARNING]
> All code referencing `NodeEditorScreen`, `MinecraftClient`, or `net.minex.nodeforge.client.*` must reside exclusively within your client source set (`src/client/java`). Referencing client classes from common or server code will cause a fatal `ClassNotFoundException` when running on a dedicated server.

```text
               Common Code (src/main/java)
┌────────────────────────────────────────────────────────┐
│  Graph graph = loadOrRetrieveGraph();                  │
│  (Data containers, schemas, serializers, validation)  │
└────────────────────────────────────────────────────────┘
                            │
               Client Code (src/client/java)
┌────────────────────────────────────────────────────────┐
│  MinecraftClient.getInstance().setScreen(              │
│      new NodeEditorScreen(Text.literal("Editor"), graph)│
│  );                                                    │
└────────────────────────────────────────────────────────┘
```

---

## Launch Method 1: Client Command

The cleanest way to test or expose the editor in development is through a client command using Fabric Client Command API v2:

```java
package com.example.mymod.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minex.nodeforge.client.editor.NodeEditorScreen;
import net.minex.nodeforge.core.graph.Graph;

public final class EditorCommandIntegration {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(EditorCommandIntegration::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("nodeforge-editor")
                .executes(context -> {
                    // Create or retrieve the graph instance
                    Graph demoGraph = new Graph("editor_session");

                    // Always schedule screen transitions on the client main render thread
                    MinecraftClient.getInstance().send(() -> {
                        NodeEditorScreen screen = new NodeEditorScreen(
                                Text.literal("NodeForge Visual Editor"),
                                demoGraph
                        );
                        MinecraftClient.getInstance().setScreen(screen);
                    });

                    return 1;
                })
        );
    }
}
```

---

## Launch Method 2: Custom Item Interaction

If your mod provides a tool or tablet item that opens the graph editor when right-clicked:

### 1. In Common Code (`src/main/java`)

```java
package com.example.mymod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GraphEditorItem extends Item {

    public GraphEditorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            // Forward call to client proxy/interface
            ClientEditorBridge.openEditor(user, stack);
        }
        return TypedActionResult.success(stack);
    }
}
```

### 2. In Client Code (`src/client/java`)

```java
package com.example.mymod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minex.nodeforge.client.editor.NodeEditorScreen;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.core.graph.Graph;

public final class ClientEditorBridge {

    public static void openEditor(PlayerEntity player, ItemStack stack) {
        // Load existing graph stored on item or create a new session
        Graph graph = new Graph("item_graph_" + player.getUuidAsString());

        MinecraftClient.getInstance().send(() -> {
            NodeEditorScreen screen = new NodeEditorScreen(
                    Text.literal("Skill Graph Editor"),
                    graph
            );
            // Optionally apply a specific theme preset
            screen.setTheme(NodeTheme.CYBERPUNK);

            MinecraftClient.getInstance().setScreen(screen);
        });
    }
}
```

---

## Customizing the Editor

### Applying Themes

`NodeEditorScreen` supports switching visual styles at runtime:

```java
NodeEditorScreen screen = new NodeEditorScreen(Text.literal("Editor"), graph);

// Preset choices: DARK, LIGHT, MIDNIGHT, CYBERPUNK, HIGH_CONTRAST, MINECRAFT_DARK
screen.setTheme(NodeTheme.MIDNIGHT);

// Or by registered ThemeId
screen.setTheme(ThemeId.of("mymod:custom_theme"));
```

### Setting Custom Canvas Layers

You can extend the editor canvas with custom watermarks, grid overlays, or game-specific HUD elements using `CanvasLayer`:

```java
// Registering a client plugin to inject a custom layer into all editor screens
public class MyModClientPlugin implements NodeForgeClientPlugin {
    @Override
    public void registerCanvasLayers(CanvasLayerRegistry registry) {
        registry.register(new WatermarkCanvasLayer());
    }
}
```

---

## Saving and Persisting Changes

When the user finishes editing and closes the screen (e.g., pressing `Escape`), the mutations reside in memory within the `Graph` instance.

To persist the graph or transmit changes back to a server:

```java
package com.example.mymod.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minex.nodeforge.client.editor.NodeEditorScreen;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.serialization.GraphSerializer;

public class SavingNodeEditorScreen extends NodeEditorScreen {

    private final Runnable onSaveCallback;

    public SavingNodeEditorScreen(Text title, Graph graph, Runnable onSaveCallback) {
        super(title, graph);
        this.onSaveCallback = onSaveCallback;
    }

    @Override
    public void close() {
        // 1. Retrieve the mutated graph from editor state
        Graph graph = getEditorState().getGraph();

        // 2. Serialize graph to JSON payload
        String json = GraphSerializer.toJson(graph);

        // 3. Save locally or send network packet to server
        saveGraphData(graph.id(), json);

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        // 4. Close screen and return to previous Minecraft menu
        super.close();
    }

    private void saveGraphData(String graphId, String jsonPayload) {
        // Implement disk write or Fabric network packet dispatch
    }
}
```

---

## Next Steps

* Master all keyboard and mouse interactions in the [Visual Editor Guide](../guides/visual-editor.md).
* Create custom node shapes and card headers in [Custom Node Renderers](../customization/node-renderers.md).
* Add specialized property inputs using [Property Widgets](../customization/property-widgets.md).
