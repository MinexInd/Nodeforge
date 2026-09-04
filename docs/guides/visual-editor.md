# Visual Editor Screen

The `NodeEditorScreen` (`net.minex.nodeforge.client.editor.NodeEditorScreen`) is NodeForge's client-side graphical canvas for editing, wiring, and inspecting graphs interactively.

---

## 1. Opening the Editor

To launch the editor from a Minecraft client (e.g. from an item right-click, block interaction, keybind, or client command):

```java
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minex.nodeforge.client.editor.NodeEditorScreen;
import net.minex.nodeforge.core.graph.Graph;

public class EditorTrigger {

    public static void openScreen(Graph graph) {
        MinecraftClient.getInstance().setScreen(
                new NodeEditorScreen(Text.literal("NodeForge Canvas"), graph)
        );
    }
}
```

> [!CAUTION]
> **Client-Only Rule**:
> `NodeEditorScreen` and `MinecraftClient` reside strictly within the `src/client/java` source set. Never invoke this code from common or server packages.

---

## 2. Canvas Gestures & Navigation

- **Panning**: Drag on empty background space using Left-Click or Middle-Click.
- **Continuous Zooming**: Scroll the mouse wheel. The viewport zooms toward the exact world position beneath the cursor, preserving focal invariance. Zoom is clamped between $0.1\times$ and $3.0\times$.
- **Frame All (`A` or `F`)**: Automatically translates and scales the camera to center all existing nodes within the viewport.

---

## 3. Node Selection & Movement

- **Single Selection**: Left-click a node card to select it and bring it to the foreground.
- **Multi-Selection (Shift + Click)**: Hold `Shift` while clicking nodes to toggle them into the selection group.
- **Marquee Box Selection**: Left-click and drag on empty canvas space to draw a selection rectangle. All intersecting nodes are selected upon mouse release.
- **Group Dragging**: Dragging any selected node moves all selected nodes simultaneously, preserving relative offsets.

---

## 4. Cable Wiring Interaction

- **Creating Wires**: Click and drag from any port socket. An active Bézier spline follows the cursor.
- **Completing Wires**: Release the mouse button over a compatible socket. If valid, the connection snaps into place and registers in the `CommandStack`. If invalid, the wire retracts with a visual rejection effect.
- **Severing Wires**: Right-click directly on an existing Bézier cable to sever it.

---

## 5. Editor Features & Overlays

### 5.1 Interactive Minimap (`M`)
A bird's-eye radar view of the entire graph rendered in the corner of the screen. Left-click or drag inside the minimap viewport rectangle to instantly pan the main camera.

### 5.2 Diagnostics HUD (`H` / `F3`)
Displays live performance telemetry:
- Node count and connection count
- Current zoom factor and camera world coordinates $(X, Y)$
- Frame render duration and active theme name

### 5.3 Background Grid & Snapping (`G`)
Renders dot, line, or cross patterns behind nodes. Toggling grid snap (`G`) quantizes node positions to discrete grid steps (default 16 pixels).

### 5.4 Node Creation Palette (Right-Click or `Space`)
Opens a searchable popup catalog. Features fuzzy searching across:
- Node display name and description
- Category groupings
- Compatible input/output port types

---

## 6. Complete Keyboard Shortcuts Matrix

| Keybind | Action | Description |
| :--- | :--- | :--- |
| `Ctrl + Z` | **Undo** | Reverts the last command on the `CommandStack`. |
| `Ctrl + Y` | **Redo** | Re-applies the next command on the `CommandStack`. |
| `Ctrl + C` | **Copy** | Copies selected nodes and their internal connecting wires to clipboard. |
| `Ctrl + V` | **Paste** | Pastes clipboard nodes with a slight offset, creating new unique IDs. |
| `Ctrl + D` | **Duplicate** | Duplicates selected nodes in place. |
| `Delete` / `Backspace` | **Delete** | Removes selected nodes and connections from the graph. |
| `C` | **Comment Box** | Creates a titled visual grouping box enclosing the selected nodes. |
| `M` | **Toggle Minimap** | Shows or hides the bird's-eye canvas radar. |
| `G` | **Toggle Snap** | Toggles discrete grid quantization. |
| `T` | **Cycle Theme** | Switches dynamically between the 6 built-in color themes. |
| `H` / `F3` | **Toggle HUD** | Toggles the diagnostics telemetry overlay. |
| `V` | **Toggle Motion** | Toggles reduced-motion mode for accessibility compliance. |
| `A` / `F` | **Frame All** | Centers the camera to frame all nodes in the viewport. |

---

## 7. Next Steps

- Learn how to define custom node archetypes in the [Custom Nodes Guide](../customization/custom-nodes.md).
- Learn how to create custom node visual cards in the [Custom Node Renderers Guide](../customization/node-renderers.md).
