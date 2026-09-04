package net.minex.nodeforge.demo;

/**
 * Demo 2D vector record showcasing custom composite data types on NodeForge ports.
 */
public record Vector2(float x, float y) {

    public static final Vector2 ZERO = new Vector2(0f, 0f);

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
}
