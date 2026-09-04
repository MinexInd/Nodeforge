package net.minex.nodeforge.client.render.port;

import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PortShape")
class PortShapeTest {

    @Test
    @DisplayName("execution ports resolve to TRIANGLE shape")
    void executionPortShape() {
        Port execIn = new Port(PortId.of("in"), "Exec In", net.minex.nodeforge.api.graph.PortDirection.INPUT, BuiltinPortTypes.EXECUTION);
        Port execOut = new Port(PortId.of("out"), "Exec Out", net.minex.nodeforge.api.graph.PortDirection.OUTPUT, BuiltinPortTypes.EXECUTION);

        assertEquals(PortShape.TRIANGLE, PortShape.fromPort(execIn));
        assertEquals(PortShape.TRIANGLE, PortShape.fromPort(execOut));
    }

    @Test
    @DisplayName("standard scalar data ports resolve to CIRCLE shape")
    void scalarDataPortShape() {
        Port strPort = new Port(PortId.of("p1"), "Text", net.minex.nodeforge.api.graph.PortDirection.INPUT, BuiltinPortTypes.STRING);
        Port intPort = new Port(PortId.of("p2"), "Count", net.minex.nodeforge.api.graph.PortDirection.OUTPUT, BuiltinPortTypes.INTEGER);
        Port boolPort = new Port(PortId.of("p3"), "Flag", net.minex.nodeforge.api.graph.PortDirection.INPUT, BuiltinPortTypes.BOOLEAN);

        assertEquals(PortShape.CIRCLE, PortShape.fromPort(strPort));
        assertEquals(PortShape.CIRCLE, PortShape.fromPort(intPort));
        assertEquals(PortShape.CIRCLE, PortShape.fromPort(boolPort));
    }

    @Test
    @DisplayName("collection and array ports resolve to SQUARE shape")
    void collectionPortShape() {
        PortType<List> listType = PortType.builder(PortTypeId.of("testmod:list_shape"), List.class).build();
        net.minex.nodeforge.api.port.PortTypeRegistry.getInstance().register(listType);
        Port listPort = new Port(PortId.of("list"), "Items", net.minex.nodeforge.api.graph.PortDirection.INPUT, listType);

        PortType<String[]> arrayType = PortType.builder(PortTypeId.of("testmod:array_shape"), String[].class).build();
        net.minex.nodeforge.api.port.PortTypeRegistry.getInstance().register(arrayType);
        Port arrayPort = new Port(PortId.of("arr"), "Array", net.minex.nodeforge.api.graph.PortDirection.OUTPUT, arrayType);

        assertEquals(PortShape.SQUARE, PortShape.fromPort(listPort));
        assertEquals(PortShape.SQUARE, PortShape.fromPort(arrayPort));
    }

    @Test
    @DisplayName("null port safely defaults to CIRCLE")
    void nullPortDefault() {
        assertEquals(PortShape.CIRCLE, PortShape.fromPort(null));
    }

    @Test
    @DisplayName("renderer safely ignores non-positive radius and null context")
    void invalidRadiusHandledGracefully() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> PortShapeRenderer.renderPortSocket(
                null, PortShape.TRIANGLE, net.minex.nodeforge.api.graph.PortDirection.OUTPUT,
                10, 10, 0, 0xFFFFFFFF, 0xFF000000, 0xFF222222, false, false));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> PortShapeRenderer.renderPortSocket(
                null, PortShape.TRIANGLE, net.minex.nodeforge.api.graph.PortDirection.OUTPUT,
                10, 10, -5, 0xFFFFFFFF, 0xFF000000, 0xFF222222, false, false));
    }
}
