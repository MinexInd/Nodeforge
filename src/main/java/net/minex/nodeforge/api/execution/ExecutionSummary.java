package net.minex.nodeforge.api.execution;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable report summarizing the outcome and performance metrics of a graph evaluation run.
 *
 * @param isSuccess          {@code true} if execution completed without error or cancellation
 * @param isCancelled        {@code true} if execution was halted via cancellation token
 * @param stepsExecuted      total number of nodes/steps executed
 * @param executionTimeNanos total duration in nanoseconds
 * @param errorMessage       optional failure message
 * @param finalVariables     unmodifiable snapshot of variables after execution
 */
public record ExecutionSummary(
        boolean isSuccess,
        boolean isCancelled,
        int stepsExecuted,
        long executionTimeNanos,
        Optional<String> errorMessage,
        Map<String, Object> finalVariables
) {

    public ExecutionSummary {
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        finalVariables = finalVariables != null ? Collections.unmodifiableMap(finalVariables) : Collections.emptyMap();
    }

    public static ExecutionSummary success(int steps, long durationNanos, Map<String, Object> variables) {
        return new ExecutionSummary(true, false, steps, durationNanos, Optional.empty(), variables);
    }

    public static ExecutionSummary failure(String error, int steps, long durationNanos, Map<String, Object> variables) {
        return new ExecutionSummary(false, false, steps, durationNanos, Optional.of(error), variables);
    }

    public static ExecutionSummary cancelled(int steps, long durationNanos, Map<String, Object> variables) {
        return new ExecutionSummary(false, true, steps, durationNanos, Optional.of("Execution was cancelled"), variables);
    }

    /** Returns execution duration in milliseconds. */
    public double durationMillis() {
        return executionTimeNanos / 1_000_000.0;
    }
}
