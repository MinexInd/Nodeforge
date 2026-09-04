package net.minex.nodeforge.client.render.vfx;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.render.BezierCurve;
import net.minex.nodeforge.core.id.ConnectionId;

import java.util.Objects;

/**
 * An energetic packet or signal traveling along a connection cable Bézier trajectory.
 */
public class CableImpulse {

    private final ConnectionId connectionId;
    private final int color;
    private final double duration;
    private final double radius;
    private double age;

    public CableImpulse(ConnectionId connectionId, int color, double duration, double radius) {
        this.connectionId = Objects.requireNonNull(connectionId, "connectionId must not be null");
        this.color = color;
        this.duration = Math.max(0.1, duration);
        this.radius = Math.max(1.5, radius);
        this.age = 0.0;
    }

    public static CableImpulse of(ConnectionId connectionId, int color) {
        return new CableImpulse(connectionId, color, 0.45, 3.5);
    }

    public void step(double deltaSeconds) {
        if (deltaSeconds > 0.0) {
            age += deltaSeconds;
        }
    }

    public boolean isAlive() {
        return age < duration;
    }

    public ConnectionId getConnectionId() {
        return connectionId;
    }

    public double progress() {
        return Math.clamp(age / duration, 0.0, 1.0);
    }

    /**
     * Renders the traveling pulse packet along the specified curve.
     */
    public void render(DrawContext context, BezierCurve curve) {
        if (!isAlive() || context == null || curve == null) return;

        double t = progress();
        Position head = curve.evaluate(t);

        int hx = (int) Math.round(head.x());
        int hy = (int) Math.round(head.y());
        int r = (int) Math.round(radius);

        // Core packet glow
        context.fill(hx - r - 1, hy - r - 1, hx + r + 1, hy + r + 1, (color & 0x00FFFFFF) | 0x88000000);
        context.fill(hx - r, hy - r, hx + r, hy + r, color | 0xFF000000);
        // Bright center dot
        context.fill(hx - 1, hy - 1, hx + 1, hy + 1, 0xFFFFFFFF);

        // Short trailing echo
        if (t > 0.06) {
            Position tail = curve.evaluate(t - 0.05);
            int tx = (int) Math.round(tail.x());
            int ty = (int) Math.round(tail.y());
            int tr = Math.max(1, r - 1);
            context.fill(tx - tr, ty - tr, tx + tr, ty + tr, (color & 0x00FFFFFF) | 0x55000000);
        }
    }
}
