package net.minex.nodeforge.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.port.PortShape;
import net.minex.nodeforge.client.render.port.PortShapeRenderer;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.Objects;

/**
 * Renders port sockets and port labels on node cards.
 */
public class PortRenderer {

    private final PortTypeRegistry portTypeRegistry;

    public PortRenderer(PortTypeRegistry portTypeRegistry) {
        this.portTypeRegistry = Objects.requireNonNull(portTypeRegistry, "portTypeRegistry must not be null");
    }

    public PortRenderer() {
        this(PortTypeRegistry.getInstance());
    }

    /**
     * Renders all ports for the given node.
     */
    public void renderPorts(DrawContext context, Node node, TextRenderer textRenderer,
                            EditorState editorState, NodeTheme theme) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        for (Port port : node.ports().values()) {
            Position socketPos = PortLayout.getPortPosition(node, port);
            boolean isConnected = !editorState.graph().getConnectionsForPort(node.id(), port.id()).isEmpty();
            boolean isHovered = editorState.hoverState().isPortHovered(node.id(), port.id());

            PortType<?> type = portTypeRegistry.get(port.typeKey());
            int typeColor = type != null ? type.color() : theme.cableDefaultColor();

            int sx = (int) Math.round(socketPos.x());
            int sy = (int) Math.round(socketPos.y());
            int r = (int) Math.round(PortLayout.SOCKET_RADIUS);

            // 1. Determine PortShape and render styled socket
            PortShape shape = PortShape.fromPort(port);
            int borderColor = isHovered ? theme.nodeSelectedBorderColor() : theme.socketBorderColor();
            PortShapeRenderer.renderPortSocket(
                    context, shape, port.direction(),
                    sx, sy, r, typeColor, borderColor,
                    theme.nodeBackgroundColor(), isConnected, isHovered
            );

            // 4. Render port label if textRenderer provided
            if (textRenderer != null) {
                String name = port.name();
                int labelY = sy - 4;
                if (port.isInput()) {
                    context.drawText(textRenderer, name, sx + r + 4, labelY, theme.textSecondaryColor(), false);
                } else {
                    int textWidth = textRenderer.getWidth(name);
                    context.drawText(textRenderer, name, sx - r - 4 - textWidth, labelY, theme.textSecondaryColor(), false);
                }
            }
        }
    }
}
