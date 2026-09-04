package net.minex.nodeforge.api.execution;

/**
 * Thrown when an illegal cyclic dependency is detected during topological sorting or graph evaluation.
 */
public class GraphCycleException extends RuntimeException {

    public GraphCycleException(String message) {
        super(message);
    }

    public GraphCycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
