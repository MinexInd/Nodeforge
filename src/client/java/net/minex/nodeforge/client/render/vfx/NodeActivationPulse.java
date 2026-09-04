package net.minex.nodeforge.client.render.vfx;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.client.animation.Easing;
import net.minex.nodeforge.core.id.NodeId;

import java.util.Objects;

/**
 * Animated expanding ripple pulse surrounding a node upon activation or trigger.
 */
public class NodeActivationPulse {

    private final NodeId nodeId;
    private final int color;
    private final double duration;
    private final double maxExpansion;
    private double age;

    public NodeActivationPulse(NodeId nodeId, int color, double duration, double maxExpansion) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId must not be null");
        this.color = color;
        this.duration = Math.max(0.1, duration);
        this.maxExpansion = Math.max(2.0, maxExpansion);
        this.age = 0.0;
    }

    public static NodeActivationPulse of(NodeId nodeId, int color) {
        return new NodeActivationPulse(nodeId, color, 0.45, 14.0);
    }

    public void step(double deltaSeconds) {
        if (deltaSeconds > 0.0) {
            age += deltaSeconds;
        }
    }

    public boolean isAlive() {
        return age < duration;
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    /**
     * Renders the expanding ripple pulse around the given node.
     */
    public void render(DrawContext context, Node node) {
        if (!isAlive() || context == null || node == null) return;

        double progress = Math.clamp(age / duration, 0.0, 1.0);
        double expansion = maxExpansion * Easing.QUAD_OUT.apply(progress);
        double alphaFrac = 1.0 - Easing.QUAD_OUT.apply(progress);

        int baseAlpha = (color >> 24) & 0xFF;
        if (baseAlpha == 0) baseAlpha = 0xFF;
        int currentAlpha = (int) Math.round(baseAlpha * alphaFrac);
        if (currentAlpha <= 0) return;

        int renderColor = (currentAlpha << 24) | (color & 0x00FFFFFF);

        int x = (int) Math.round(node.position().x() - expansion);
        int y = (int) Math.round(node.position().y() - expansion);
        int w = (int) Math.round(node.size().width() + expansion * 2.0);
        int h = (int) Math.round(node.size().height() + expansion * 2.0);

        context.drawStrokedRectangle(x, y, w, h, renderColor);
        // Double-stroke for vivid glow
        if (w > 2 && h > 2) {
            context.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, (renderColor & 0x00FFFFFF) | ((currentAlpha / 2) << 24));
        }
    }
}
