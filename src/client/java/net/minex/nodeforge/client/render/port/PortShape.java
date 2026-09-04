package net.minex.nodeforge.client.render.port;

import net.minex.nodeforge.api.graph.Port;

import java.util.Collection;
import java.util.Objects;

/**
 * Geometric socket shape representing the semantic nature of a port connection.
 */
public enum PortShape {

    /** Standard circular socket for primitive values and data objects. */
    CIRCLE,

    /** Square socket representing collections, lists, arrays, or streams. */
    SQUARE,

    /** Triangular socket representing procedural execution control-flow (impulse/signal). */
    TRIANGLE,

    /** Diamond socket representing event callbacks, triggers, or delegates. */
    DIAMOND,

    /** Rounded capsule socket representing universal/generic wildcard connections. */
    CAPSULE;

    /**
     * Determines the recommended {@link PortShape} for a given {@link Port}.
     *
     * @param port the port to evaluate
     * @return the determined port shape
     */
    public static PortShape fromPort(Port port) {
        if (port == null) return CIRCLE;
        if (port.isExecution()) {
            return TRIANGLE;
        }
        String key = port.typeKey() != null ? port.typeKey().toLowerCase(java.util.Locale.ROOT) : "";
        if (key.contains("list") || key.contains("array") || key.contains("collection")) {
            return SQUARE;
        }
        if (port.portType() != null && port.portType().typeClass() != null) {
            Class<?> cls = port.portType().typeClass();
            if (cls.isArray() || Collection.class.isAssignableFrom(cls)) {
                return SQUARE;
            }
        }
        return CIRCLE;
    }
}
