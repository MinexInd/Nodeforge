package net.minex.nodeforge.demo;

import net.minex.nodeforge.api.execution.ExecutionResult;
import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.NodeExecutorRegistry;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.plugin.NodeForgeContext;
import net.minex.nodeforge.api.plugin.NodeForgePlugin;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import net.minex.nodeforge.api.registry.NodeCategory;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.api.registry.NodeTypeId;
import net.minex.nodeforge.core.id.PortId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Living reference implementation of a {@link NodeForgePlugin}.
 *
 * <p>Demonstrates registering custom port types, node definitions, and execution handlers
 * using pure generic mathematics and vectors without any Minecraft-specific dependencies.
 */
public class DemoMathPlugin implements NodeForgePlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("DemoMathPlugin");

    public static final PortTypeId FLOAT_TYPE_ID = PortTypeId.of("demo:float");
    public static final PortTypeId VEC2_TYPE_ID = PortTypeId.of("demo:vector2");

    public static final NodeTypeId ADD_NODE = NodeTypeId.of("demo:add");
    public static final NodeTypeId MULTIPLY_NODE = NodeTypeId.of("demo:multiply");
    public static final NodeTypeId CLAMP_NODE = NodeTypeId.of("demo:clamp");
    public static final NodeTypeId VEC2_MAKE_NODE = NodeTypeId.of("demo:vec2_make");

    private static final PortType<Float> FLOAT_TYPE = PortType.builder(FLOAT_TYPE_ID, Float.class)
            .color(0xFF00E5FF)
            .build();

    private static final PortType<Vector2> VEC2_TYPE = PortType.builder(VEC2_TYPE_ID, Vector2.class)
            .color(0xFFFF4081)
            .build();

    @Override
    public String id() {
        return "DemoMathPlugin";
    }

    @Override
    public void registerPortTypes(PortTypeRegistry registry) {
        registry.register(FLOAT_TYPE);
        registry.register(VEC2_TYPE);
    }

    @Override
    public void registerNodeDefinitions(NodeDefinitionRegistry registry) {
        // demo:add
        registry.register(NodeDefinition.builder(ADD_NODE)
                .displayName("Add (Float)")
                .category(NodeCategory.MATH)
                .description("Computes the arithmetic sum of two floating-point values.")
                .inputPort("a", "A", FLOAT_TYPE)
                .inputPort("b", "B", FLOAT_TYPE)
                .outputPort("result", "Result", FLOAT_TYPE)
                .build());

        // demo:multiply
        registry.register(NodeDefinition.builder(MULTIPLY_NODE)
                .displayName("Multiply (Float)")
                .category(NodeCategory.MATH)
                .description("Computes the arithmetic product of two floating-point values.")
                .inputPort("a", "A", FLOAT_TYPE)
                .inputPort("b", "B", FLOAT_TYPE)
                .outputPort("result", "Result", FLOAT_TYPE)
                .build());

        // demo:clamp
        registry.register(NodeDefinition.builder(CLAMP_NODE)
                .displayName("Clamp (Float)")
                .category(NodeCategory.MATH)
                .description("Clamps a floating-point value between minimum and maximum bounds.")
                .inputPort("val", "Value", FLOAT_TYPE)
                .inputPort("min", "Min", FLOAT_TYPE)
                .inputPort("max", "Max", FLOAT_TYPE)
                .outputPort("result", "Result", FLOAT_TYPE)
                .build());

        // demo:vec2_make
        registry.register(NodeDefinition.builder(VEC2_MAKE_NODE)
                .displayName("Make Vector2")
                .category(NodeCategory.MATH)
                .description("Constructs a 2D vector from X and Y components.")
                .inputPort("x", "X", FLOAT_TYPE)
                .inputPort("y", "Y", FLOAT_TYPE)
                .outputPort("vec", "Vector", VEC2_TYPE)
                .build());
    }

    @Override
    public void registerExecutors(NodeExecutorRegistry registry) {
        // demo:add executor
        registry.register(ADD_NODE.value(), (node, context) -> {
            float a = getFloatInput(context, node, "a", 0f);
            float b = getFloatInput(context, node, "b", 0f);
            context.setOutputValue(node.id(), PortId.of("result"), a + b);
            return ExecutionResult.Success.of();
        });

        // demo:multiply executor
        registry.register(MULTIPLY_NODE.value(), (node, context) -> {
            float a = getFloatInput(context, node, "a", 1f);
            float b = getFloatInput(context, node, "b", 1f);
            context.setOutputValue(node.id(), PortId.of("result"), a * b);
            return ExecutionResult.Success.of();
        });

        // demo:clamp executor
        registry.register(CLAMP_NODE.value(), (node, context) -> {
            float val = getFloatInput(context, node, "val", 0f);
            float min = getFloatInput(context, node, "min", 0f);
            float max = getFloatInput(context, node, "max", 1f);
            float actualMin = Math.min(min, max);
            float actualMax = Math.max(min, max);
            context.setOutputValue(node.id(), PortId.of("result"), Math.clamp(val, actualMin, actualMax));
            return ExecutionResult.Success.of();
        });

        // demo:vec2_make executor
        registry.register(VEC2_MAKE_NODE.value(), (node, context) -> {
            float x = getFloatInput(context, node, "x", 0f);
            float y = getFloatInput(context, node, "y", 0f);
            context.setOutputValue(node.id(), PortId.of("vec"), new Vector2(x, y));
            return ExecutionResult.Success.of();
        });
    }

    private static float getFloatInput(ExecutionContext context, Node node, String portName, float defaultValue) {
        Object val = context.getInputValue(node.id(), PortId.of(portName));
        if (val instanceof Number n) {
            return n.floatValue();
        }
        return defaultValue;
    }

    @Override
    public void onInitialize(NodeForgeContext context) {
        LOGGER.info("DemoMathPlugin initialized successfully with 4 custom math nodes and 2 port types");
    }
}
