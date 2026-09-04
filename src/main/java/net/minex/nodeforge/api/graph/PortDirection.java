package net.minex.nodeforge.api.graph;

/**
 * The direction of a port on a node.
 *
 * <p>Connections flow from {@link #OUTPUT} ports to {@link #INPUT} ports.
 */
public enum PortDirection {

    /** An input port — receives data or execution flow. */
    INPUT,

    /** An output port — sends data or execution flow. */
    OUTPUT;

    /**
     * Returns the opposite direction.
     *
     * @return {@link #INPUT} if this is {@link #OUTPUT}, and vice versa
     */
    public PortDirection opposite() {
        return this == INPUT ? OUTPUT : INPUT;
    }
}
