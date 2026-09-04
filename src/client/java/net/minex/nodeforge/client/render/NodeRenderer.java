package net.minex.nodeforge.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import net.minex.nodeforge.client.render.node.CustomNodeRenderer;
import net.minex.nodeforge.client.render.node.NodeRendererRegistry;

import java.util.Objects;

/**
 * Renders individual node cards, header strips, titles, borders, and delegates to custom node renderers.
 */
public class NodeRenderer {

    private final PortRenderer portRenderer;
    private final NodeRendererRegistry registry;

    public NodeRenderer(PortRenderer portRenderer, NodeRendererRegistry registry) {
        this.portRenderer = Objects.requireNonNull(portRenderer, "portRenderer must not be null");
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public NodeRenderer(PortRenderer portRenderer) {
        this(portRenderer, NodeRendererRegistry.getInstance());
    }

    public NodeRenderer() {
        this(new PortRenderer(), NodeRendererRegistry.getInstance());
    }

    /**
     * Renders a single node card and its ports.
     */
    public void renderNode(DrawContext context, Node node, TextRenderer textRenderer,
                           EditorState editorState, NodeTheme theme) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        boolean isSelected = editorState != null && editorState.selection().isSelected(node.id());
        boolean isHovered = editorState != null && editorState.hoverState().isNodeHovered(node.id());

        // Check for custom node renderer
        CustomNodeRenderer custom = registry.get(node.typeKey());
        if (custom != null) {
            custom.renderNode(context, textRenderer, node, editorState, theme, isSelected, isHovered);
            return;
        }

        // Default node card rendering
        int x = (int) Math.round(node.position().x());
        int y = (int) Math.round(node.position().y());
        int w = (int) Math.round(node.size().width());
        int h = (int) Math.round(node.size().height());
        int headerH = (int) Math.round(PortLayout.HEADER_HEIGHT);

        // 0. Draw subtle ambient drop-shadow (unless high-contrast)
        if (!theme.highContrast() && (theme.shadowColor() & 0xFF000000) != 0) {
            context.fill(x + 2, y + 2, x + w + 4, y + h + 4, theme.shadowColor());
        }

        // 1. Draw outer selection glow / border
        int borderWidth = isSelected ? 2 : 1;
        int borderColor = isSelected ? theme.nodeSelectedBorderColor() :
                (isHovered ? theme.nodeHoverBorderColor() : theme.nodeBorderColor());

        context.fill(x - borderWidth, y - borderWidth, x + w + borderWidth, y + h + borderWidth, borderColor);

        // 2. Draw node card body
        context.fill(x, y, x + w, y + h, theme.nodeBackgroundColor());

        // 3. Draw header bar with category accent tint
        int headerColor = theme.nodeHeaderColor();
        var def = net.minex.nodeforge.api.registry.NodeDefinitionRegistry.getInstance().get(node.typeKey());
        if (def != null && def.category() != null) {
            headerColor = theme.getCategoryHeaderColor(def.category());
        } else {
            String categoryMeta = node.getMetadata("category");
            if (categoryMeta != null) {
                headerColor = theme.getCategoryHeaderColor(categoryMeta);
            }
        }
        context.fill(x, y, x + w, y + headerH, headerColor);

        // 4. Header separator line
        context.fill(x, y + headerH - 1, x + w, y + headerH, theme.nodeBorderColor());

        // 5. Draw node icon, title, and validation badge in header
        if (textRenderer != null) {
            int titleX = x + 8;

            // Render node icon if registered or specified in metadata
            net.minex.nodeforge.client.render.icon.NodeIcon icon =
                    net.minex.nodeforge.client.render.icon.NodeIconRegistry.getInstance().get(node.typeKey());
            if (icon == null) {
                String iconMeta = node.getMetadata("icon");
                if (iconMeta != null && !iconMeta.isBlank()) {
                    icon = new net.minex.nodeforge.client.render.icon.NodeIcon.Text(iconMeta, theme.textColor());
                }
            }
            if (icon != null) {
                int iconSize = 12;
                int iconY = y + (headerH - iconSize) / 2;
                icon.render(context, textRenderer, x + 6, iconY, iconSize);
                titleX = x + 6 + iconSize + 4;
            }

            // Draw title text
            String title = node.displayName();
            context.drawText(textRenderer, title, titleX, y + 8, theme.textColor(), false);

            // Render validation badge overlay if present
            String errorMsg = node.getMetadata("error");
            String severityStr = node.getMetadata("validation_severity");
            if (errorMsg != null || severityStr != null) {
                net.minex.nodeforge.core.validation.ValidationSeverity severity =
                        net.minex.nodeforge.core.validation.ValidationSeverity.ERROR;
                if (severityStr != null) {
                    try {
                        severity = net.minex.nodeforge.core.validation.ValidationSeverity.valueOf(severityStr.toUpperCase(java.util.Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {}
                }

                int badgeSize = net.minex.nodeforge.client.render.overlay.ValidationBadgeRenderer.DEFAULT_BADGE_SIZE;
                int badgeX = x + w - badgeSize - 6;
                int badgeY = y + (headerH - badgeSize) / 2;
                net.minex.nodeforge.client.render.overlay.ValidationBadgeRenderer.renderBadge(
                        context, textRenderer, severity, badgeX, badgeY, badgeSize);
            }
        }

        // 6. Render ports
        portRenderer.renderPorts(context, node, textRenderer, editorState, theme);
    }
}
