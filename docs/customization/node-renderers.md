# Custom Node Renderers

By default, NodeForge renders nodes as sleek, dark-themed rectangular cards with category headers, port sockets, and drop shadows.

The `CustomNodeRenderer` interface allows client-side mods to completely override or augment how specific node types are drawn on the canvas (e.g., rendering circular skill-tree nodes, progress bars, or custom icons).

---

## 1. The `CustomNodeRenderer` Interface

The `CustomNodeRenderer` (`net.minex.nodeforge.client.render.node.CustomNodeRenderer`) is a functional interface:

```java
package net.minex.nodeforge.client.render.node;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

@FunctionalInterface
public interface CustomNodeRenderer {

    void renderNode(
            DrawContext context,
            TextRenderer textRenderer,
            Node node,
            EditorState state,
            NodeTheme theme,
            boolean isSelected,
            boolean isHovered
    );
}
```

---

## 2. Implementing a Custom Renderer

The following example implements a glowing hexagonal skill node renderer:

```java
package com.example.mymod.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.node.CustomNodeRenderer;
import net.minex.nodeforge.client.render.theme.NodeTheme;

public class SkillNodeRenderer implements CustomNodeRenderer {

    @Override
    public void renderNode(
            DrawContext context,
            TextRenderer textRenderer,
            Node node,
            EditorState state,
            NodeTheme theme,
            boolean isSelected,
            boolean isHovered
    ) {
        int x = (int) node.position().x();
        int y = (int) node.position().y();
        int width = (int) node.size().width();
        int height = (int) node.size().height();

        // 1. Determine background color based on selection / hover
        int bgColor = isSelected ? 0xFF334466 : (isHovered ? 0xFF223344 : 0xFF181E28);
        int borderColor = isSelected ? 0xFF00E5FF : 0xFF445566;

        // 2. Draw card background and border
        context.fill(x, y, x + width, y + height, bgColor);
        context.drawBorder(x, y, width, height, borderColor);

        // 3. Draw Header Title
        context.drawText(textRenderer, node.displayName(), x + 8, y + 8, 0xFFFFFFFF, false);

        // 4. Draw Custom Badge or Icon
        context.drawText(textRenderer, "SKILL", x + width - 36, y + 8, 0xFF88AAFF, false);
    }
}
```

---

## 3. Registering via `NodeForgeClientPlugin`

Register the renderer in your client plugin's `registerCustomNodeRenderers` callback:

```java
package com.example.mymod.client;

import com.example.mymod.client.render.SkillNodeRenderer;
import net.minex.nodeforge.client.plugin.NodeForgeClientPlugin;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;

public class MyModClientPlugin implements NodeForgeClientPlugin {

    @Override
    public String id() {
        return "mymod:client";
    }

    @Override
    public void registerCustomNodeRenderers(NodeRendererRegistry registry) {
        registry.register("magic:spell_amplifier", new SkillNodeRenderer());
    }
}
```

Declare the client plugin in `fabric.mod.json`:

```json
"entrypoints": {
  "nodeforge:client_plugin": [
    "com.example.mymod.client.MyModClientPlugin"
  ]
}
```

---

## 4. Best Practices for Custom Renderers

1. **Respect World-Space Coordinates**: Coordinates passed to `renderNode` represent world-space pixels. Canvas camera panning and zoom transformations are already applied to the OpenGL matrix stack before this method is invoked.
2. **Handle State Gracefully**: Always visually distinguish between normal, hovered (`isHovered == true`), and selected (`isSelected == true`) states to provide responsive tactile feedback to the user.
3. **Keep Render Logic Lightweight**: This method is invoked every frame during active editing. Avoid expensive allocations (such as instantiating new objects) inside `renderNode`.

---

## 5. Next Steps

- Learn about adding interactive UI controls inside nodes in the [Property Widgets Guide](property-widgets.md).
- Learn about the theme token system in the [Themes Guide](themes.md).
