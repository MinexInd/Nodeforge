package net.minex.nodeforge.core.execution;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.NodeExecutor;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.core.id.PortId;

import java.util.Objects;

/**
 * Standard built-in logic executors for math, logic, branching flow, and data variables.
 */
public final class BuiltinExecutors {

    private BuiltinExecutors() {}

    /**
     * Registers all standard built-in node executors into the given registry.
     */
    public static void registerAll(NodeExecutorRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");

        // Math
        registry.register("math:add", BuiltinExecutors::executeAdd);
        registry.register("math:subtract", BuiltinExecutors::executeSubtract);
        registry.register("math:multiply", BuiltinExecutors::executeMultiply);
        registry.register("math:divide", BuiltinExecutors::executeDivide);

        // Logic
        registry.register("logic:and", BuiltinExecutors::executeAnd);
        registry.register("logic:or", BuiltinExecutors::executeOr);
        registry.register("logic:not", BuiltinExecutors::executeNot);
        registry.register("logic:compare", BuiltinExecutors::executeCompare);

        // Flow Control
        registry.register("flow:branch", BuiltinExecutors::executeBranch);

        // Data
        registry.register("data:constant", BuiltinExecutors::executeConstant);
        registry.register("data:get_variable", BuiltinExecutors::executeGetVariable);
        registry.register("data:set_variable", BuiltinExecutors::executeSetVariable);
    }

    // ========== Math Executors ==========

    private static ExecutionResult executeAdd(Node node, ExecutionContext ctx) {
        double a = getDoubleInput(node, ctx, "a", 0, 0.0);
        double b = getDoubleInput(node, ctx, "b", 1, 0.0);
        double sum = a + b;
        setOutputs(node, ctx, sum, "sum", "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeSubtract(Node node, ExecutionContext ctx) {
        double a = getDoubleInput(node, ctx, "a", 0, 0.0);
        double b = getDoubleInput(node, ctx, "b", 1, 0.0);
        double diff = a - b;
        setOutputs(node, ctx, diff, "diff", "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeMultiply(Node node, ExecutionContext ctx) {
        double a = getDoubleInput(node, ctx, "a", 0, 1.0);
        double b = getDoubleInput(node, ctx, "b", 1, 1.0);
        double prod = a * b;
        setOutputs(node, ctx, prod, "prod", "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeDivide(Node node, ExecutionContext ctx) {
        double a = getDoubleInput(node, ctx, "a", 0, 0.0);
        double b = getDoubleInput(node, ctx, "b", 1, 1.0);
        double quot = b != 0.0 ? a / b : 0.0;
        setOutputs(node, ctx, quot, "quot", "result", "out");
        return ExecutionResult.Success.of();
    }

    // ========== Logic Executors ==========

    private static ExecutionResult executeAnd(Node node, ExecutionContext ctx) {
        boolean a = getBooleanInput(node, ctx, "a", 0, false);
        boolean b = getBooleanInput(node, ctx, "b", 1, false);
        boolean res = a && b;
        setOutputs(node, ctx, res, "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeOr(Node node, ExecutionContext ctx) {
        boolean a = getBooleanInput(node, ctx, "a", 0, false);
        boolean b = getBooleanInput(node, ctx, "b", 1, false);
        boolean res = a || b;
        setOutputs(node, ctx, res, "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeNot(Node node, ExecutionContext ctx) {
        boolean in = getBooleanInput(node, ctx, "in", 0, false);
        boolean res = !in;
        setOutputs(node, ctx, res, "result", "out");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeCompare(Node node, ExecutionContext ctx) {
        double a = getDoubleInput(node, ctx, "a", 0, 0.0);
        double b = getDoubleInput(node, ctx, "b", 1, 0.0);
        String op = node.getMetadata("op");
        if (op == null) op = "==";

        boolean result = switch (op) {
            case ">" -> a > b;
            case "<" -> a < b;
            case ">=" -> a >= b;
            case "<=" -> a <= b;
            case "!=" -> Math.abs(a - b) > 1e-9;
            default -> Math.abs(a - b) <= 1e-9;
        };

        setOutputs(node, ctx, result, "result", "out");
        return ExecutionResult.Success.of();
    }

    // ========== Flow Control Executors ==========

    private static ExecutionResult executeBranch(Node node, ExecutionContext ctx) {
        boolean condition = getBooleanInput(node, ctx, "condition", 0, false);
        String outPort = condition ? "true_exec" : "false_exec";

        // Fallback to "true" or "false" if node port IDs are named without "_exec"
        if (!node.hasPort(PortId.of(outPort))) {
            outPort = condition ? "true" : "false";
        }

        return ExecutionResult.Success.of(outPort);
    }

    // ========== Data Variable Executors ==========

    private static ExecutionResult executeConstant(Node node, ExecutionContext ctx) {
        String valStr = node.getMetadata("value");
        Object outVal = valStr;

        if (valStr != null) {
            try {
                if (valStr.equalsIgnoreCase("true") || valStr.equalsIgnoreCase("false")) {
                    outVal = Boolean.parseBoolean(valStr);
                } else {
                    outVal = Double.parseDouble(valStr);
                }
            } catch (NumberFormatException ignored) {
                outVal = valStr;
            }
        }

        setOutputs(node, ctx, outVal, "value", "val", "out", "result");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeGetVariable(Node node, ExecutionContext ctx) {
        String varName = node.getMetadata("var_name");
        if (varName == null) {
            Object inName = ctx.getInputValue(node, "var_name");
            if (inName != null) varName = inName.toString();
        }

        Object val = varName != null ? ctx.getVariable(varName) : null;
        setOutputs(node, ctx, val, "value", "val", "out", "result");
        return ExecutionResult.Success.of();
    }

    private static ExecutionResult executeSetVariable(Node node, ExecutionContext ctx) {
        String varName = node.getMetadata("var_name");
        if (varName == null) {
            Object inName = ctx.getInputValue(node, "var_name");
            if (inName != null) varName = inName.toString();
        }

        Object val = ctx.getInputValue(node, "value");
        if (val == null) {
            val = ctx.getInputValue(node, "val");
        }
        if (varName != null && val != null) {
            ctx.setVariable(varName, val);
        }

        return node.hasPort(PortId.of("exec_out"))
                ? ExecutionResult.Success.of("exec_out")
                : ExecutionResult.Success.of();
    }

    // ========== Value Conversion & Port Helpers ==========

    private static double getDoubleInput(Node node, ExecutionContext ctx, String portName, int portIndex, double fallback) {
        Object val = ctx.getInputValue(node, portName);
        if (val == null) {
            java.util.List<net.minex.nodeforge.api.graph.Port> inPorts = node.inputPorts().stream()
                    .filter(net.minex.nodeforge.api.graph.Port::isData)
                    .toList();
            if (portIndex < inPorts.size()) {
                val = ctx.getInputValue(node.id(), inPorts.get(portIndex).id());
            }
        }
        return asDouble(val, fallback);
    }

    private static boolean getBooleanInput(Node node, ExecutionContext ctx, String portName, int portIndex, boolean fallback) {
        Object val = ctx.getInputValue(node, portName);
        if (val == null) {
            java.util.List<net.minex.nodeforge.api.graph.Port> inPorts = node.inputPorts().stream()
                    .filter(net.minex.nodeforge.api.graph.Port::isData)
                    .toList();
            if (portIndex < inPorts.size()) {
                val = ctx.getInputValue(node.id(), inPorts.get(portIndex).id());
            }
        }
        return asBoolean(val, fallback);
    }

    private static void setOutputs(Node node, ExecutionContext ctx, Object value, String... portNames) {
        for (String name : portNames) {
            ctx.setOutputValue(node, name, value);
        }
        for (net.minex.nodeforge.api.graph.Port p : node.outputPorts()) {
            if (p.isData()) {
                ctx.setOutputValue(node.id(), p.id(), value);
            }
        }
    }

    private static double asDouble(Object val, double fallback) {
        if (val instanceof Number n) {
            return n.doubleValue();
        } else if (val instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static boolean asBoolean(Object val, boolean fallback) {
        if (val instanceof Boolean b) {
            return b;
        } else if (val instanceof String s) {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
        } else if (val instanceof Number n) {
            return n.doubleValue() != 0.0;
        }
        return fallback;
    }
}
