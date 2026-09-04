package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import net.minex.nodeforge.core.validation.GraphValidator;
import net.minex.nodeforge.core.validation.ValidationError;
import net.minex.nodeforge.core.validation.ValidationSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeValidationRule & Definition Validation")
class NodeValidationRuleTest {

    @Test
    @DisplayName("validates required input port presence on nodes")
    void requiredInputPortValidation() {
        NodeDefinitionRegistry registry = new NodeDefinitionRegistry();

        NodeDefinition def = NodeDefinition.builder("test:operation")
                .inputPort("req_in", "Required Input", BuiltinPortTypes.DOUBLE, true)
                .inputPort("opt_in", "Optional Input", BuiltinPortTypes.DOUBLE, false)
                .outputPort("out", "Out", BuiltinPortTypes.DOUBLE)
                .build();
        registry.register(def);

        Graph graph = new Graph("req_test");
        Node node = def.createNode(NodeId.of("op_1"));
        graph.addNode(node);

        // 1. Without required connection -> validation error
        List<ValidationError> errors = GraphValidator.validate(graph, registry);
        assertEquals(1, errors.size());
        ValidationError err = errors.get(0);
        assertEquals(ValidationSeverity.ERROR, err.severity());
        assertTrue(err.message().contains("Required input port 'req_in' has no incoming connection"));

        // 2. Add source node and connect to req_in -> validation passes
        Node srcNode = Node.builder(NodeId.of("src"), "test:src")
                .outputPort("val_out", "Val", BuiltinPortTypes.DOUBLE)
                .build();
        graph.addNode(srcNode);
        graph.connect(NodeId.of("src"), PortId.of("val_out"), NodeId.of("op_1"), PortId.of("req_in"));

        List<ValidationError> errorsAfter = GraphValidator.validate(graph, registry);
        assertTrue(errorsAfter.isEmpty());
    }

    @Test
    @DisplayName("executes custom NodeValidationRule registered on NodeDefinition")
    void customNodeValidationRule() {
        NodeDefinitionRegistry registry = new NodeDefinitionRegistry();

        NodeValidationRule rule = (node, graph) -> {
            String thresholdStr = node.getMetadata("threshold");
            if (thresholdStr != null && Integer.parseInt(thresholdStr) < 0) {
                return List.of(ValidationError.nodeError(
                        ValidationSeverity.ERROR, "Threshold must not be negative: " + thresholdStr, node.id()));
            }
            return Collections.emptyList();
        };

        NodeDefinition def = NodeDefinition.builder("test:threshold_node")
                .property(PropertyDefinition.intProperty("threshold", "Threshold", 10))
                .validationRule(rule)
                .build();
        registry.register(def);

        Graph graph = new Graph("rule_test");
        Node node = def.createNode(NodeId.of("thresh_1"));
        graph.addNode(node);

        // Default value is 10 -> valid
        assertTrue(GraphValidator.validate(graph, registry).isEmpty());

        // Set invalid metadata -> custom validation error detected
        node.setMetadata("threshold", "-5");
        List<ValidationError> errors = GraphValidator.validate(graph, registry);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).message().contains("Threshold must not be negative: -5"));
    }
}
