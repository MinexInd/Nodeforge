# Phased Canvas Layers

The Canvas Layer API allows external mods to inject custom rendering operations into NodeForge's drawing pipeline without subclassing the editor screen.

---

## 1. Canvas Layer Lifecycle & Phases

The `CanvasLayerPhase` enum (`net.minex.nodeforge.client.render.layer.CanvasLayerPhase`) defines the five distinct execution stages of the canvas draw loop:

```text
1. PRE_GRID         [World Space]   Deep background underneath the grid (pans & zooms)
2. POST_GRID        [World Space]   Between background grid and connecting cables
3. POST_CABLES      [World Space]   Between connecting cables and node cards
4. POST_NODES       [World Space]   On top of node cards, selection halos, and VFX
5. SCREEN_OVERLAY   [Screen Space]  Fixed window overlay (independent of camera pan/zoom)
```

---

## 2. World Space vs. Screen Space Coordinates

A crucial distinction exists based on the active phase:

- **Phases 1 through 4 (`PRE_GRID`, `POST_GRID`, `POST_CABLES`, `POST_NODES`)**:
  Execute inside the camera's world-space matrix. Coordinates passed to `DrawContext` correspond directly to world-space units. The camera pan offset and zoom scale factor are already applied.
- **Phase 5 (`SCREEN_OVERLAY`)**:
  Executes after world-space transformations are popped. Coordinates represent physical window screen pixels $[0, w_{\text{screen}}] \times [0, h_{\text{screen}}]$. Ideal for HUD widgets, watermarks, or tooltips.

---

## 3. Implementing a `CanvasLayer`

The following example implements a branded screen-space watermark layer that renders during `SCREEN_OVERLAY`:

```java
package com.example.mymod.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.layer.CanvasLayer;
import net.minex.nodeforge.client.render.layer.CanvasLayerPhase;
import net.minex.nodeforge.client.render.theme.NodeTheme;

public class WatermarkCanvasLayer implements CanvasLayer {

    @Override
    public String id() {
        return "mymod:watermark";
    }

    @Override
    public int order() {
        return 200; // Sort order (lower values render earlier)
    }

    @Override
    public boolean shouldRender(CanvasLayerPhase phase) {
        // Only render during the fixed screen overlay phase
        return phase == CanvasLayerPhase.SCREEN_OVERLAY;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                       NodeTheme theme, CanvasLayerPhase phase, int screenW, int screenH) {
        String watermarkText = "My Custom Mod Canvas - NodeForge Engine";
        int textWidth = textRenderer.getWidth(watermarkText);

        // Position in bottom-left corner of the window
        int x = 12;
        int y = screenH - 18;

        context.drawText(textRenderer, watermarkText, x, y, 0x66FFFFFF, true);
    }
}
```

---

## 4. Registering Canvas Layers

Register your layer inside your client plugin's `registerCanvasLayers` callback:

```java
@Override
public void registerCanvasLayers(CanvasLayerRegistry registry) {
    registry.register(new WatermarkCanvasLayer());
}
```

Or register programmatically:

```java
CanvasLayerRegistry.getInstance().register(new WatermarkCanvasLayer());
```

---

## 5. Next Steps

- Review the package stability matrix in the [API Stability Reference](../reference/api-overview.md).
- Understand formal execution mechanics in the [Execution Model Reference](../reference/execution-model.md).
