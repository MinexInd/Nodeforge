package net.minex.nodeforge.api.execution;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runtime execution context providing variables, port input/output buffers,
 * step limits, and cancellation tokens for graph evaluation.
 */
public class ExecutionContext {

    public static final int DEFAULT_MAX_STEPS = 10_000;

    private final Map<String, Object> variables = new ConcurrentHashMap<>();
    private final Map<NodeId, Map<PortId, Object>> inputPortValues = new ConcurrentHashMap<>();
    private final Map<NodeId, Map<PortId, Object>> outputPortValues = new ConcurrentHashMap<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger stepCounter = new AtomicInteger(0);
    private final int maxSteps;
    private final List<String> traceLogs = new CopyOnWriteArrayList<>();

    public ExecutionContext(int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be positive: " + maxSteps);
        }
        this.maxSteps = maxSteps;
    }

    public ExecutionContext() {
        this(DEFAULT_MAX_STEPS);
    }

    // ========== Global Variables ==========

    /** Sets a runtime variable. */
    public void setVariable(String key, Object value) {
        Objects.requireNonNull(key, "variable key must not be null");
        if (value == null) {
            variables.remove(key);
        } else {
            variables.put(key, value);
        }
    }

    /** Retrieves a runtime variable, or {@code null} if not found. */
    public Object getVariable(String key) {
        Objects.requireNonNull(key, "variable key must not be null");
        return variables.get(key);
    }

    /** Retrieves a typed runtime variable with a fallback default value. */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        Object val = getVariable(key);
        if (val == null) return defaultValue;
        if (defaultValue != null && !defaultValue.getClass().isInstance(val)) {
            return defaultValue;
        }
        return (T) val;
    }

    /** Retrieves a typed runtime variable with an explicit class token and fallback. */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, Class<T> type, T defaultValue) {
        Objects.requireNonNull(type, "type class must not be null");
        Object val = getVariable(key);
        if (val == null || !type.isInstance(val)) {
            return defaultValue;
        }
        return (T) val;
    }

    /** Returns {@code true} if the variable is present in the context. */
    public boolean hasVariable(String key) {
        return key != null && variables.containsKey(key);
    }

    /** Removes a variable from the context. */
    public Object removeVariable(String key) {
        return key == null ? null : variables.remove(key);
    }

    /** Returns an unmodifiable snapshot of all current variables. */
    public Map<String, Object> variables() {
        return Collections.unmodifiableMap(new HashMap<>(variables));
    }

    // ========== Port Input / Output Buffers ==========

    /** Sets the evaluated value received on an input port, or clears it if value is null. */
    public void setInputValue(NodeId node, PortId port, Object value) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(port, "port must not be null");
        if (value == null) {
            Map<PortId, Object> nodeInputs = inputPortValues.get(node);
            if (nodeInputs != null) {
                nodeInputs.remove(port);
            }
        } else {
            inputPortValues.computeIfAbsent(node, k -> new ConcurrentHashMap<>()).put(port, value);
        }
    }

    /** Retrieves the value received on an input port. */
    public Object getInputValue(NodeId node, PortId port) {
        if (node == null || port == null) return null;
        Map<PortId, Object> nodeInputs = inputPortValues.get(node);
        return nodeInputs != null ? nodeInputs.get(port) : null;
    }

    /** Retrieves the value received on an input port by string name. */
    public Object getInputValue(NodeId node, String portName) {
        return getInputValue(node, PortId.of(portName));
    }

    /** Retrieves the value received on an input port of a {@link Node}. */
    public Object getInputValue(Node node, String portName) {
        Objects.requireNonNull(node, "node must not be null");
        return getInputValue(node.id(), portName);
    }

    /** Sets the value emitted by a node on an output port. */
    public void setOutputValue(NodeId node, PortId port, Object value) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(port, "port must not be null");
        if (value == null) {
            Map<PortId, Object> nodeOutputs = outputPortValues.get(node);
            if (nodeOutputs != null) nodeOutputs.remove(port);
        } else {
            outputPortValues.computeIfAbsent(node, k -> new ConcurrentHashMap<>()).put(port, value);
        }
    }

    /** Sets the value emitted by a node on an output port by string name. */
    public void setOutputValue(NodeId node, String portName, Object value) {
        setOutputValue(node, PortId.of(portName), value);
    }

    /** Sets the value emitted by a {@link Node} on an output port by string name. */
    public void setOutputValue(Node node, String portName, Object value) {
        Objects.requireNonNull(node, "node must not be null");
        setOutputValue(node.id(), portName, value);
    }

    /** Retrieves the value emitted on an output port. */
    public Object getOutputValue(NodeId node, PortId port) {
        if (node == null || port == null) return null;
        Map<PortId, Object> nodeOutputs = outputPortValues.get(node);
        return nodeOutputs != null ? nodeOutputs.get(port) : null;
    }

    /** Retrieves the value emitted on an output port by string name. */
    public Object getOutputValue(NodeId node, String portName) {
        return getOutputValue(node, PortId.of(portName));
    }

    /** Retrieves the value emitted on an output port of a {@link Node}. */
    public Object getOutputValue(Node node, String portName) {
        Objects.requireNonNull(node, "node must not be null");
        return getOutputValue(node.id(), portName);
    }

    /** Returns an unmodifiable snapshot of all emitted output values. */
    public Map<NodeId, Map<PortId, Object>> allOutputs() {
        Map<NodeId, Map<PortId, Object>> copy = new HashMap<>();
        for (Map.Entry<NodeId, Map<PortId, Object>> entry : outputPortValues.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    // ========== Cancellation & Step Limiting ==========

    /** Cancels execution of the graph. */
    public void cancel() {
        this.cancelled.set(true);
    }

    /** Returns {@code true} if execution has been cancelled. */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Increments the executed step counter and checks against the maximum step limit.
     *
     * @return the new step count
     * @throws IllegalStateException if the maximum allowed step limit is exceeded
     */
    public int incrementAndCheckSteps() {
        int steps = stepCounter.incrementAndGet();
        if (steps > maxSteps) {
            throw new IllegalStateException("Execution exceeded maximum allowed step limit (" + maxSteps + " steps)");
        }
        return steps;
    }

    /** Returns the total number of steps executed so far. */
    public int stepCount() {
        return stepCounter.get();
    }

    /** Returns the maximum step limit for this context. */
    public int maxSteps() {
        return maxSteps;
    }

    // ========== Tracing ==========

    /** Records a message in the execution trace log. */
    public void logTrace(String message) {
        if (message != null) {
            traceLogs.add(message);
        }
    }

    /** Returns an unmodifiable view of all recorded trace logs. */
    public List<String> traceLogs() {
        return Collections.unmodifiableList(traceLogs);
    }
}
