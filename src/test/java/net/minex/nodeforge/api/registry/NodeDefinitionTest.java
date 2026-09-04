package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.graph.Size;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeDefinition")
class NodeDefinitionTest {

    @Test
    @DisplayName("builds complete NodeDefinition")
    void buildDefinition() {
        NodeTypeId typeId = NodeTypeId.of("testmod:heal");
        NodeDefinition def = NodeDefinition.builder(typeId)
                .displayName("Heal Player")
                .description("Restores health to target entity")
                .category(NodeCategory.ACTION)
                .icon("textures/gui/icons/heal.png")
                .defaultSize(180, 90)
                .inputPort("exec_in", "In", BuiltinPortTypes.EXECUTION)
                .inputPort("amount", "Amount", BuiltinPortTypes.FLOAT, true)
                .outputPort("exec_out", "Out", BuiltinPortTypes.EXECUTION)
                .property(PropertyDefinition.doubleProperty("heal_amount", "Heal Amount", 4.0))
                .property(PropertyDefinition.booleanProperty("particles", "Spawn Particles", true))
                .build();

        assertEquals(typeId, def.id());
        assertEquals("Heal Player", def.displayName());
        assertEquals("Restores health to target entity", def.description());
        assertEquals(NodeCategory.ACTION, def.category());
        assertEquals("textures/gui/icons/heal.png", def.iconPath());
        assertEquals(new Size(180, 90), def.defaultSize());

        assertEquals(2, def.inputPorts().size());
        assertEquals(1, def.outputPorts().size());
        assertEquals(2, def.properties().size());
        assertNotNull(def.lifecycleHooks());
    }

    @Test
    @DisplayName("instantiates configured Node instance via createNode")
    void createNode() {
        NodeDefinition def = NodeDefinition.builder("test:math_add")
                .displayName("Add Numbers")
                .defaultSize(150, 75)
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("sum", "Sum", BuiltinPortTypes.DOUBLE)
                .property(PropertyDefinition.stringProperty("precision", "Precision", "0.01"))
                .build();

        NodeId nodeId = NodeId.of("add_instance_1");
        Position pos = new Position(100, 200);

        Node node = def.createNode(nodeId, pos);

        assertEquals(nodeId, node.id());
        assertEquals("test:math_add", node.typeKey());
        assertEquals("Add Numbers", node.displayName());
        assertEquals(pos, node.position());
        assertEquals(new Size(150, 75), node.size());

        assertEquals(3, node.portCount());
        assertTrue(node.hasPort(PortId.of("a")));
        assertTrue(node.hasPort(PortId.of("b")));
        assertTrue(node.hasPort(PortId.of("sum")));

        assertEquals("0.01", node.getMetadata("precision"));
    }

    @Test
    @DisplayName("convenience createNode overloads")
    void createNodeOverloads() {
        NodeDefinition def = NodeDefinition.builder("test:simple")
                .outputPort("out", "Out", BuiltinPortTypes.STRING)
                .build();

        Node n1 = def.createNode(NodeId.of("n1"));
        assertEquals(Position.ZERO, n1.position());

        Node n2 = def.createNode(new Position(50, 50));
        assertNotNull(n2.id());
        assertEquals(new Position(50, 50), n2.position());

        Node n3 = def.createNode();
        assertNotNull(n3.id());
        assertEquals(Position.ZERO, n3.position());
    }

    @Test
    @DisplayName("rejects duplicate port IDs and duplicate property keys")
    void duplicateRejection() {
        NodeDefinition.Builder builder = NodeDefinition.builder("test:dup")
                .inputPort("port_1", "P1", BuiltinPortTypes.INTEGER);

        assertThrows(IllegalArgumentException.class, () ->
                builder.outputPort("port_1", "P1 Duplicate", BuiltinPortTypes.INTEGER));

        builder.property(PropertyDefinition.booleanProperty("flag", "Flag", true));
        assertThrows(IllegalArgumentException.class, () ->
                builder.property(PropertyDefinition.stringProperty("flag", "Flag String", "val")));
    }

    @Test
    @DisplayName("rejects builder reuse after build()")
    void rejectsBuilderReuse() {
        NodeDefinition.Builder builder = NodeDefinition.builder("test:single_use")
                .displayName("Single Use")
                .inputPort("in", "In", BuiltinPortTypes.STRING);

        NodeDefinition def = builder.build();
        assertNotNull(def);

        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.displayName("New Name"));
        assertThrows(IllegalStateException.class, () -> builder.description("New Desc"));
        assertThrows(IllegalStateException.class, () -> builder.category(NodeCategory.ACTION));
        assertThrows(IllegalStateException.class, () -> builder.icon("path"));
        assertThrows(IllegalStateException.class, () -> builder.inputPort("in2", "In2", BuiltinPortTypes.STRING));
        assertThrows(IllegalStateException.class, () -> builder.outputPort("out2", "Out2", BuiltinPortTypes.STRING));
        assertThrows(IllegalStateException.class, () -> builder.property(PropertyDefinition.booleanProperty("p", "P", true)));
        assertThrows(IllegalStateException.class, () -> builder.validationRule((node, graph) -> Collections.emptyList()));
        assertThrows(IllegalStateException.class, () -> builder.lifecycleHooks(NodeLifecycleHooks.EMPTY));
        assertThrows(IllegalStateException.class, () -> builder.defaultSize(100, 100));
    }
}
