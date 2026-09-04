package net.minex.nodeforge.core.port;

import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeCompatibilityEngine")
class TypeCompatibilityTest {

    @AfterEach
    void tearDown() {
        TypeCompatibilityEngine.clearCustomCompatibility();
    }

    @Test
    @DisplayName("exact type match is compatible")
    void exactTypeMatch() {
        Port out = Port.output("out", "Out", BuiltinPortTypes.STRING);
        Port in = Port.input("in", "In", BuiltinPortTypes.STRING);

        TypeCompatibilityEngine.TypeCheckResult result = TypeCompatibilityEngine.checkCompatibility(out, in);
        assertTrue(result.isCompatible());
    }

    @Test
    @DisplayName("execution isolation: execution only connects to execution")
    void executionIsolation() {
        Port execOut = Port.output("e_out", "Exec Out", BuiltinPortTypes.EXECUTION);
        Port execIn = Port.input("e_in", "Exec In", BuiltinPortTypes.EXECUTION);
        Port dataIn = Port.input("d_in", "Data In", BuiltinPortTypes.INTEGER);
        Port dataOut = Port.output("d_out", "Data Out", BuiltinPortTypes.INTEGER);

        // Exec -> Exec: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(execOut, execIn).isCompatible());

        // Exec -> Data: Incompatible
        TypeCompatibilityEngine.TypeCheckResult r1 = TypeCompatibilityEngine.checkCompatibility(execOut, dataIn);
        assertFalse(r1.isCompatible());
        assertTrue(r1.reason().contains("execution flow port"));

        // Data -> Exec: Incompatible
        TypeCompatibilityEngine.TypeCheckResult r2 = TypeCompatibilityEngine.checkCompatibility(dataOut, execIn);
        assertFalse(r2.isCompatible());
        assertTrue(r2.reason().contains("execution flow port"));
    }

    @Test
    @DisplayName("wildcard ANY connects to and from any data port")
    void wildcardAny() {
        Port anyOut = Port.output("any_out", "Any Out", BuiltinPortTypes.ANY);
        Port anyIn = Port.input("any_in", "Any In", BuiltinPortTypes.ANY);
        Port strIn = Port.input("str_in", "String In", BuiltinPortTypes.STRING);
        Port intOut = Port.output("int_out", "Int Out", BuiltinPortTypes.INTEGER);
        Port execIn = Port.input("exec_in", "Exec In", BuiltinPortTypes.EXECUTION);

        // ANY -> String: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(anyOut, strIn).isCompatible());

        // Integer -> ANY: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(intOut, anyIn).isCompatible());

        // ANY -> ANY: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(anyOut, anyIn).isCompatible());

        // ANY -> Execution: Incompatible
        assertFalse(TypeCompatibilityEngine.checkCompatibility(anyOut, execIn).isCompatible());
    }

    @Test
    @DisplayName("class hierarchy assignability (subclass -> superclass)")
    void classAssignability() {
        PortType<CharSequence> superType = PortType.builder("test:char_seq", CharSequence.class).build();
        PortType<String> subType = PortType.builder("test:str", String.class).build();

        PortTypeRegistry.getInstance().register(superType);
        PortTypeRegistry.getInstance().register(subType);

        Port subOut = Port.output("sub_out", "Sub Out", subType);
        Port superIn = Port.input("super_in", "Super In", superType);
        Port superOut = Port.output("super_out", "Super Out", superType);
        Port subIn = Port.input("sub_in", "Sub In", subType);

        // String -> CharSequence (subclass -> superclass): OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(subOut, superIn).isCompatible());

        // CharSequence -> String (superclass -> subclass): Incompatible without converter
        assertFalse(TypeCompatibilityEngine.checkCompatibility(superOut, subIn).isCompatible());
    }

    @Test
    @DisplayName("implicit numeric widening conversions")
    void numericWidening() {
        Port intOut = Port.output("i_out", "Int", BuiltinPortTypes.INTEGER);
        Port longIn = Port.input("l_in", "Long", BuiltinPortTypes.LONG);
        Port floatIn = Port.input("f_in", "Float", BuiltinPortTypes.FLOAT);
        Port doubleIn = Port.input("d_in", "Double", BuiltinPortTypes.DOUBLE);

        Port floatOut = Port.output("f_out", "Float", BuiltinPortTypes.FLOAT);
        Port longOut = Port.output("l_out", "Long", BuiltinPortTypes.LONG);
        Port doubleOut = Port.output("d_out", "Double", BuiltinPortTypes.DOUBLE);

        Port intIn = Port.input("i_in", "Int", BuiltinPortTypes.INTEGER);

        // Int -> Long, Float, Double: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(intOut, longIn).isCompatible());
        assertTrue(TypeCompatibilityEngine.checkCompatibility(intOut, floatIn).isCompatible());
        assertTrue(TypeCompatibilityEngine.checkCompatibility(intOut, doubleIn).isCompatible());

        // Float -> Double: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(floatOut, doubleIn).isCompatible());

        // Long -> Double: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(longOut, doubleIn).isCompatible());

        // Narrowing: Double -> Int (Incompatible)
        assertFalse(TypeCompatibilityEngine.checkCompatibility(doubleOut, intIn).isCompatible());

        // Narrowing: Float -> Int (Incompatible)
        assertFalse(TypeCompatibilityEngine.checkCompatibility(floatOut, intIn).isCompatible());
    }

    @Test
    @DisplayName("custom compatibility rules")
    void customCompatibilityRules() {
        PortTypeId idA = PortTypeId.of("custom:type_a");
        PortTypeId idB = PortTypeId.of("custom:type_b");

        PortType<String> typeA = PortType.builder(idA, String.class).build();
        PortType<Integer> typeB = PortType.builder(idB, Integer.class).build();

        PortTypeRegistry.getInstance().register(typeA);
        PortTypeRegistry.getInstance().register(typeB);

        Port outA = Port.output("out_a", "A", typeA);
        Port inB = Port.input("in_b", "B", typeB);

        // Initially incompatible
        assertFalse(TypeCompatibilityEngine.checkCompatibility(outA, inB).isCompatible());

        // Register custom rule
        TypeCompatibilityEngine.registerCustomCompatibility(idA, idB);
        assertTrue(TypeCompatibilityEngine.checkCompatibility(outA, inB).isCompatible());

        // Reverse is not automatically compatible (rules are directional)
        Port outB = Port.output("out_b", "B", typeB);
        Port inA = Port.input("in_a", "A", typeA);
        assertFalse(TypeCompatibilityEngine.checkCompatibility(outB, inA).isCompatible());

        // Symmetric rule registration allows both directions
        TypeCompatibilityEngine.registerSymmetricCompatibility(idA, idB);
        assertTrue(TypeCompatibilityEngine.checkCompatibility(outA, inB).isCompatible());
        assertTrue(TypeCompatibilityEngine.checkCompatibility(outB, inA).isCompatible());
    }

    @Test
    @DisplayName("unregistered type keys fallback to string exact matching")
    void unregisteredTypeKeys() {
        Port out1 = Port.output("p1", "P1", "unregistered_key_1");
        Port in1 = Port.input("p2", "P2", "unregistered_key_1");
        Port in2 = Port.input("p3", "P3", "unregistered_key_2");

        // Same string: OK
        assertTrue(TypeCompatibilityEngine.checkCompatibility(out1, in1).isCompatible());

        // Different string: Incompatible
        TypeCompatibilityEngine.TypeCheckResult result = TypeCompatibilityEngine.checkCompatibility(out1, in2);
        assertFalse(result.isCompatible());
        assertTrue(result.reason().contains("Unregistered port types are only compatible by exact string match"));
    }
}
