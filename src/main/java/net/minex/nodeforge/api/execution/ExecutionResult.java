package net.minex.nodeforge.api.execution;

import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;
import java.util.Optional;

/**
 * Result returned by a {@link NodeExecutor} after executing an individual node.
 */
public sealed interface ExecutionResult permits ExecutionResult.Success, ExecutionResult.Failure, ExecutionResult.Halt {

    /**
     * Successful node execution.
     *
     * @param nextFlowPort optional output port ID representing the next execution flow branch to pulse
     */
    record Success(Optional<PortId> nextFlowPort) implements ExecutionResult {
        public Success {
            Objects.requireNonNull(nextFlowPort, "nextFlowPort must not be null");
        }

        /** Creates a success result with no further execution flow pulse. */
        public static Success of() {
            return new Success(Optional.empty());
        }

        /** Creates a success result pulsing the specified output execution port. */
        public static Success of(PortId flowPort) {
            Objects.requireNonNull(flowPort, "flowPort must not be null");
            return new Success(Optional.of(flowPort));
        }

        /** Creates a success result pulsing the specified output execution port by name. */
        public static Success of(String flowPortName) {
            Objects.requireNonNull(flowPortName, "flowPortName must not be null");
            return of(PortId.of(flowPortName));
        }
    }

    /**
     * Node execution failed due to an error.
     *
     * @param errorMessage descriptive failure reason
     * @param cause        optional root cause exception
     */
    record Failure(String errorMessage, Optional<Throwable> cause) implements ExecutionResult {
        public Failure {
            Objects.requireNonNull(errorMessage, "errorMessage must not be null");
            Objects.requireNonNull(cause, "cause must not be null");
        }

        public static Failure of(String message) {
            return new Failure(message, Optional.empty());
        }

        public static Failure of(String message, Throwable cause) {
            return new Failure(message, Optional.ofNullable(cause));
        }
    }

    /**
     * Explicit request to halt graph execution gracefully.
     *
     * @param reason explanation for the halt
     */
    record Halt(String reason) implements ExecutionResult {
        public Halt {
            Objects.requireNonNull(reason, "reason must not be null");
        }

        public static Halt of(String reason) {
            return new Halt(reason);
        }
    }
}
