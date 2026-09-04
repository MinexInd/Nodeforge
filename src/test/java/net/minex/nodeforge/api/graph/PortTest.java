package net.minex.nodeforge.api.graph;

import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Port")
class PortTest {

    @Test
    @DisplayName("creates port with constructor")
    void createsWithConstructor() {
        Port port = new Port(PortId.of("in1"), "Input 1", PortDirection.INPUT, "number");
        assertEquals(PortId.of("in1"), port.id());
        assertEquals("Input 1", port.name());
        assertEquals(PortDirection.INPUT, port.direction());
        assertEquals("number", port.typeKey());
        assertTrue(port.isInput());
        assertFalse(port.isOutput());
    }

    @Test
    @DisplayName("convenience factory methods")
    void factoryMethods() {
        Port input1 = Port.input(PortId.of("a"), "A", "string");
        Port input2 = Port.input("b", "B", "string");
        Port output1 = Port.output(PortId.of("x"), "X", "number");
        Port output2 = Port.output("y", "Y", "number");

        assertTrue(input1.isInput());
        assertTrue(input2.isInput());
        assertTrue(output1.isOutput());
        assertTrue(output2.isOutput());
        assertEquals(PortId.of("b"), input2.id());
        assertEquals(PortId.of("y"), output2.id());
    }

    @Test
    @DisplayName("null argument rejection")
    void nullArguments() {
        assertThrows(NullPointerException.class, () -> new Port(null, "Name", PortDirection.INPUT, "type"));
        assertThrows(NullPointerException.class, () -> new Port(PortId.of("p"), null, PortDirection.INPUT, "type"));
        assertThrows(NullPointerException.class, () -> new Port(PortId.of("p"), "Name", null, "type"));
        assertThrows(NullPointerException.class, () -> new Port(PortId.of("p"), "Name", PortDirection.INPUT, (String) null));
        assertThrows(NullPointerException.class, () -> new Port(PortId.of("p"), "Name", PortDirection.INPUT, (PortType<?>) null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank name and typeKey")
    void blankArguments(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new Port(PortId.of("p"), blank, PortDirection.INPUT, "type"));
        assertThrows(IllegalArgumentException.class, () -> new Port(PortId.of("p"), "Name", PortDirection.INPUT, blank));
    }

    @Test
    @DisplayName("equals and hashCode based on PortId and PortDirection")
    void equalsAndHashCode() {
        Port in1 = Port.input("port_a", "First Name", "typeA");
        Port in2 = Port.input("port_a", "Different Name", "typeB");
        Port out1 = Port.output("port_a", "First Name", "typeA");
        Port in3 = Port.input("port_b", "First Name", "typeA");

        // Same ID and same direction -> equal
        assertEquals(in1, in2);
        assertEquals(in1.hashCode(), in2.hashCode());

        // Same ID but different direction -> NOT equal
        assertNotEquals(in1, out1);
        assertNotEquals(in1.hashCode(), out1.hashCode());

        // Different ID -> NOT equal
        assertNotEquals(in1, in3);
    }

    @Test
    @DisplayName("toString format")
    void toStringFormat() {
        Port p = Port.input("exec", "Exec In", "flow");
        assertEquals("Port[exec INPUT flow]", p.toString());
    }
}
