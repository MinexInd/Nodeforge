package net.minex.nodeforge.client.render;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.editor.state.InteractionState;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.core.graph.Graph;

import net.minex.nodeforge.client.render.cable.CableStyle;

import java.util.List;
import java.util.Objects;

/**
 * Renders connection cables between ports with type-based colors, configurable geometries, and interaction highlights.
 */
public class CableRenderer {

    private final PortTypeRegistry portTypeRegistry;
    private CableStyle cableStyle = CableStyle.CUBIC_BEZIER;

    public CableRenderer(PortTypeRegistry portTypeRegistry, CableStyle cableStyle) {
        this.portTypeRegistry = Objects.requireNonNull(portTypeRegistry, "portTypeRegistry must not be null");
        this.cableStyle = Objects.requireNonNull(cableStyle, "cableStyle must not be null");
    }

    public CableRenderer(PortTypeRegistry portTypeRegistry) {
        this(portTypeRegistry, CableStyle.CUBIC_BEZIER);
    }

    public CableRenderer() {
        this(PortTypeRegistry.getInstance(), CableStyle.CUBIC_BEZIER);
    }

    /** Returns the active connection cable routing style. */
    public CableStyle getCableStyle() {
        return cableStyle;
    }

    /** Sets the active connection cable routing style. */
    public void setCableStyle(CableStyle cableStyle) {
        this.cableStyle = Objects.requireNonNull(cableStyle, "cableStyle must not be null");
    }

    /**
     * Renders all connections in the graph and active interactive drag cable.
     */
    public void render(DrawContext context, EditorState editorState, NodeTheme theme) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(editorState, "editorState must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        Graph graph = editorState.graph();

        // 1. Render all existing connections
        for (Connection conn : graph.getConnections()) {
            Node srcNode = graph.getNode(conn.fromNode());
            Node dstNode = graph.getNode(conn.toNode());
            if (srcNode == null || dstNode == null) continue;

            Port srcPort = srcNode.getPort(conn.fromPort());
            Port dstPort = dstNode.getPort(conn.toPort());
            if (srcPort == null || dstPort == null) continue;

            Position start = PortLayout.getPortPosition(srcNode, srcPort);
            Position end = PortLayout.getPortPosition(dstNode, dstPort);

            List<Position> path = cableStyle.generatePath(start, srcPort.direction(), end, dstPort.direction(), 24);

            // Determine cable color
            PortType<?> type = portTypeRegistry.get(srcPort.typeKey());
            int baseColor = type != null ? type.color() : theme.cableDefaultColor();

            boolean isSelected = editorState.selection().isSelected(conn.id());
            boolean isHovered = editorState.hoverState().isConnectionHovered(conn.id());

            double baseWidth = theme.cableLineWidth();
            if (isSelected) {
                // Draw thicker glowing background for selection
                renderPath(context, path, theme.cableSelectedColor(), baseWidth + 2.0);
            } else if (isHovered) {
                renderPath(context, path, theme.cableHoverColor(), baseWidth + 1.5);
            }

            renderPath(context, path, baseColor, baseWidth);
        }

        // 2. Render active dragging connection cable
        if (editorState.interactionState() instanceof InteractionState.ConnectingCable connecting) {
            Node srcNode = graph.getNode(connecting.sourceNode());
            if (srcNode != null) {
                Port srcPort = srcNode.getPort(connecting.sourcePort());
                if (srcPort != null) {
                    Position start = PortLayout.getPortPosition(srcNode, srcPort);
                    Position end = connecting.currentWorldPos();

                    List<Position> dragPath = cableStyle.generatePath(
                            start, connecting.sourceDirection(), end, connecting.sourceDirection().opposite(), 24);

                    int cableColor = connecting.portType() != null ? connecting.portType().color() : theme.cableDefaultColor();
                    renderPath(context, dragPath, cableColor, theme.cableLineWidth() + 0.5);
                }
            }
        }
    }

    private void renderPath(DrawContext context, List<Position> points, int color, double thickness) {
        if (points == null || points.size() < 2) return;
        int halfThick = (int) Math.max(1.0, Math.round(thickness / 2.0));

        for (int i = 0; i < points.size() - 1; i++) {
            Position a = points.get(i);
            Position b = points.get(i + 1);

            int x1 = (int) Math.round(a.x());
            int y1 = (int) Math.round(a.y());
            int x2 = (int) Math.round(b.x());
            int y2 = (int) Math.round(b.y());

            int minX = Math.min(x1, x2) - halfThick;
            int maxX = Math.max(x1, x2) + halfThick;
            int minY = Math.min(y1, y2) - halfThick;
            int maxY = Math.max(y1, y2) + halfThick;

            context.fill(minX, minY, Math.max(minX + 1, maxX), Math.max(minY + 1, maxY), color);
        }
    }
}
