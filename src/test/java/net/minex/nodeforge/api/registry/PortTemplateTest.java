package net.minex.nodeforge.api.registry;

import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortTemplate")
class PortTemplateTest {

    @Test
    @DisplayName("creates input and output templates")
    void templateCreation() {
        PortTemplate in = PortTemplate.input("in_a", "Operand A", BuiltinPortTypes.DOUBLE, true);
        assertEquals(PortId.of("in_a"), in.id());
        assertEquals("Operand A", in.displayName());
        assertEquals(PortDirection.INPUT, in.direction());
        assertSame(BuiltinPortTypes.DOUBLE, in.portType());
        assertTrue(in.required());
        assertTrue(in.isInput());
        assertFalse(in.isOutput());

        PortTemplate out = PortTemplate.output("out_res", "Result", BuiltinPortTypes.DOUBLE);
        assertEquals(PortId.of("out_res"), out.id());
        assertEquals("Result", out.displayName());
        assertEquals(PortDirection.OUTPUT, out.direction());
        assertFalse(out.required());
        assertTrue(out.isOutput());
        assertFalse(out.isInput());
    }

    @Test
    @DisplayName("instantiates active Port matching template properties")
    void instantiatePort() {
        PortTemplate template = PortTemplate.input("exec_in", "Trigger", BuiltinPortTypes.EXECUTION);
        Port port = template.instantiate();

        assertEquals(PortId.of("exec_in"), port.id());
        assertEquals("Trigger", port.name());
        assertEquals(PortDirection.INPUT, port.direction());
        assertEquals(BuiltinPortTypes.EXECUTION.id().value(), port.typeKey());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank displayName")
    void blankDisplayName(String blank) {
        assertThrows(IllegalArgumentException.class, () ->
                PortTemplate.input("id", blank, BuiltinPortTypes.INTEGER));
    }
}
