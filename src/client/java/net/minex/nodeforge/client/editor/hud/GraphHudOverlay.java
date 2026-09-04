package net.minex.nodeforge.client.editor.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.execution.GraphCycleException;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.grid.GridConfig;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.core.execution.TopologicalSorter;
import net.minex.nodeforge.core.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Diagnostics and topology HUD overlay displaying real-time graph metrics, camera coordinates, and DAG status.
 */
public class GraphHudOverlay {

    private boolean visible = false;
    private long lastCycleCheckTimeMs = 0L;
    private int lastNodeCount = -1;
    private int lastConnectionCount = -1;
    private boolean cachedHasCycles = false;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean toggleVisible() {
        this.visible = !this.visible;
        return this.visible;
    }

    public void render(DrawContext context, TextRenderer textRenderer, EditorState state,
                       GridConfig gridConfig, NodeTheme theme, int screenW, int screenH) {
        if (!visible || textRenderer == null) return;

        Graph graph = state.graph();
        BoundingBox viewBounds = state.camera().getVisibleWorldBounds(screenW, screenH);

        long visibleNodeCount = graph.getNodes().stream()
                .filter(n -> viewBounds.intersects(BoundingBox.fromPositionAndSize(n.position(), n.size())))
                .count();

        long now = System.currentTimeMillis();
        if (now - lastCycleCheckTimeMs > 500L
                || graph.nodeCount() != lastNodeCount
                || graph.connectionCount() != lastConnectionCount) {
            lastCycleCheckTimeMs = now;
            lastNodeCount = graph.nodeCount();
            lastConnectionCount = graph.connectionCount();
            try {
                TopologicalSorter.sort(graph);
                cachedHasCycles = false;
            } catch (GraphCycleException e) {
                cachedHasCycles = true;
            }
        }
        boolean hasCycles = cachedHasCycles;

        List<String> lines = new ArrayList<>();
        lines.add("§bNodeForge Graph Diagnostics [F3 / H]");
        lines.add(String.format(Locale.ROOT, "Nodes: %d (Visible: %d) | Cables: %d | Comments: %d",
                graph.nodeCount(), visibleNodeCount, graph.connectionCount(), graph.commentBoxCount()));
        lines.add(String.format(Locale.ROOT, "Selected: %d nodes, %d cables",
                state.selection().selectedNodeCount(), state.selection().selectedConnectionCount()));
        lines.add(String.format(Locale.ROOT, "Camera: (%.1f, %.1f) | Zoom: %d%%",
                state.camera().pan().x(), state.camera().pan().y(),
                (int) Math.round(state.camera().zoom() * 100)));
        lines.add(String.format(Locale.ROOT, "Grid: %s (%.0f px) | Snap: %s",
                gridConfig.getStyle().name(), gridConfig.getSize(),
                gridConfig.isSnapEnabled() ? "ON" : "OFF"));
        lines.add("Topology: " + (hasCycles ? "§cContains Cycles" : "§aValid DAG (Acyclic)"));

        int padding = 6;
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(line));
        }

        int boxW = maxWidth + padding * 2;
        int boxH = lines.size() * (textRenderer.fontHeight + 2) + padding * 2;
        int x = 8;
        int y = 24;

        context.fill(x, y, x + boxW, y + boxH, 0xEE12141A);
        context.drawStrokedRectangle(x, y, boxW, boxH, 0xFF3A3E48);

        for (int i = 0; i < lines.size(); i++) {
            int lineY = y + padding + i * (textRenderer.fontHeight + 2) + 1;
            context.drawText(textRenderer, lines.get(i), x + padding, lineY, 0xFFE0E0E0, false);
        }
    }
}
