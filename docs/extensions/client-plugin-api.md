# Client Plugin API

The Client Plugin API allows external mods to extend NodeForge's visual interface, custom card renderers, theme presets, title bar icons, and phased canvas layers.

---

## 1. The `NodeForgeClientPlugin` Interface

Client extensions implement `NodeForgeClientPlugin` (`net.minex.nodeforge.client.plugin.NodeForgeClientPlugin`):

```java
package net.minex.nodeforge.client.plugin;

import net.minex.nodeforge.client.render.icon.NodeIconRegistry;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;

public interface NodeForgeClientPlugin {

    default String id() {
        return getClass().getSimpleName();
    }

    /** Register custom color themes. */
    default void registerThemes(ThemeRegistry registry) {}

    /** Register custom node card visual renderers. */
    default void registerCustomNodeRenderers(NodeRendererRegistry registry) {}

    /** Register custom category icons for node headers. */
    default void registerNodeIcons(NodeIconRegistry registry) {}

    /** Register custom phased canvas rendering passes. */
    default void registerCanvasLayers(CanvasLayerRegistry registry) {}

    /** Client lifecycle hook invoked after all registries are populated. */
    default void onInitializeClient(NodeForgeClientContext context) {}
}
```

---

## 2. Declaration in `fabric.mod.json`

Declare your client plugin entrypoint inside your `src/main/resources/fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "my_client_mod",
  "version": "1.0.0",
  "entrypoints": {
    "nodeforge:client_plugin": [
      "com.example.mymod.client.MyCustomClientPlugin"
    ]
  }
}
```

### 2.1 Programmatic Registration
Alternatively, register the client plugin inside your mod's `ClientModInitializer`:

```java
import net.minex.nodeforge.client.NodeForgeClient;

public class MyModClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NodeForgeClient.registerClientPlugin(new MyCustomClientPlugin());
    }
}
```

---

## 3. Client Plugin Implementation Example

```java
package com.example.mymod.client;

import net.minex.nodeforge.client.plugin.NodeForgeClientPlugin;
import net.minex.nodeforge.client.render.layer.CanvasLayerRegistry;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;

public class MyCustomClientPlugin implements NodeForgeClientPlugin {

    @Override
    public String id() {
        return "my_client_mod:plugin";
    }

    @Override
    public void registerThemes(ThemeRegistry registry) {
        // Register custom color palette
        registry.register(MyTheme.ID, MyTheme.create());
    }

    @Override
    public void registerCustomNodeRenderers(NodeRendererRegistry registry) {
        // Override visual layout for specialized node cards
        registry.register("magic:generator", new GlowingGeneratorRenderer());
    }

    @Override
    public void registerCanvasLayers(CanvasLayerRegistry registry) {
        // Add a custom canvas watermark or grid overlay
        registry.register(new WatermarkCanvasLayer());
    }
}
```

---

## 4. Next Steps

- Learn how to implement custom rendering passes in the [Canvas Layers Guide](canvas-layers.md).
- Review the package stability matrix in the [API Stability Reference](../reference/api-overview.md).
