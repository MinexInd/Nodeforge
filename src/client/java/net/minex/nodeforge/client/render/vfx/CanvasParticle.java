package net.minex.nodeforge.client.render.vfx;

import net.minecraft.client.gui.DrawContext;

/**
 * High-performance 2D particle simulation entity for editor canvas visual effects.
 */
public class CanvasParticle {

    private double x;
    private double y;
    private double vx;
    private double vy;
    private double size;
    private int baseColor;
    private double maxLifetime;
    private double age;
    private double drag;
    private double gravity;

    public CanvasParticle(double x, double y, double vx, double vy, double size,
                          int baseColor, double maxLifetime, double drag, double gravity) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = Math.max(1.0, size);
        this.baseColor = baseColor;
        this.maxLifetime = Math.max(0.05, maxLifetime);
        this.age = 0.0;
        this.drag = Math.clamp(drag, 0.0, 1.0);
        this.gravity = gravity;
    }

    public static CanvasParticle spark(double x, double y, double vx, double vy, int color, double lifetime) {
        return new CanvasParticle(x, y, vx, vy, 2.0, color, lifetime, 0.92, 10.0);
    }

    public static CanvasParticle burst(double x, double y, double vx, double vy, int color, double size, double lifetime) {
        return new CanvasParticle(x, y, vx, vy, size, color, lifetime, 0.88, 0.0);
    }

    /** Steps particle physics forward by {@code deltaSeconds}. */
    public void step(double deltaSeconds) {
        if (!isAlive() || deltaSeconds <= 0.0) return;

        age += deltaSeconds;
        x += vx * deltaSeconds;
        y += vy * deltaSeconds;

        vy += gravity * deltaSeconds;

        double decay = Math.pow(drag, deltaSeconds * 60.0);
        vx *= decay;
        vy *= decay;
    }

    /** Returns {@code true} while the particle's age is less than its maximum lifetime. */
    public boolean isAlive() {
        return age < maxLifetime;
    }

    /** Returns normalized remaining lifetime in {@code [0.0, 1.0]}. */
    public double remainingLifeFraction() {
        return Math.clamp(1.0 - (age / maxLifetime), 0.0, 1.0);
    }

    /** Renders the particle to the DrawContext in canvas world space. */
    public void render(DrawContext context) {
        if (!isAlive() || context == null) return;

        double frac = remainingLifeFraction();
        int baseAlpha = (baseColor >> 24) & 0xFF;
        if (baseAlpha == 0) baseAlpha = 0xFF;
        int currentAlpha = (int) Math.round(baseAlpha * frac);
        if (currentAlpha <= 0) return;

        int renderColor = (currentAlpha << 24) | (baseColor & 0x00FFFFFF);

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        int halfS = (int) Math.max(1, Math.round(size / 2.0));

        context.fill(px - halfS, py - halfS, px + halfS, py + halfS, renderColor);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getBaseColor() { return baseColor; }
    public double getAge() { return age; }
    public double getMaxLifetime() { return maxLifetime; }
}
