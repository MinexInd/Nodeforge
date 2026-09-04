package net.minex.nodeforge.api.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortType & PortTypeId")
class PortTypeTest {

    @Test
    @DisplayName("creates PortTypeId with value and namespace")
    void portTypeIdCreation() {
        PortTypeId id1 = PortTypeId.of("nodeforge:custom");
        assertEquals("nodeforge:custom", id1.value());
        assertEquals("PortTypeId[nodeforge:custom]", id1.toString());

        PortTypeId id2 = PortTypeId.of("testmod", "skill");
        assertEquals("testmod:skill", id2.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects blank PortTypeId values")
    void portTypeIdBlank(String blank) {
        assertThrows(IllegalArgumentException.class, () -> PortTypeId.of(blank));
        assertThrows(IllegalArgumentException.class, () -> PortTypeId.of("ns", blank));
        assertThrows(IllegalArgumentException.class, () -> PortTypeId.of(blank, "path"));
    }

    @Test
    @DisplayName("builds data PortType with custom attributes")
    void buildDataPortType() {
        PortTypeId id = PortTypeId.of("mod:player");
        PortType<CharSequence> type = PortType.builder(id, CharSequence.class)
                .displayName("Player Character")
                .color(0xFF336699)
                .build();

        assertEquals(id, type.id());
        assertEquals(CharSequence.class, type.typeClass());
        assertEquals("Player Character", type.displayName());
        assertEquals(0xFF336699, type.color());
        assertFalse(type.isExecution());
        assertEquals("PortType[mod:player (Player Character)]", type.toString());
    }

    @Test
    @DisplayName("creates execution PortType")
    void executionPortType() {
        PortType<Void> exec = PortType.execution(PortTypeId.of("custom:exec"), "Flow", 0xFFFFFFFF);
        assertTrue(exec.isExecution());
        assertEquals(Void.class, exec.typeClass());
        assertEquals("Flow", exec.displayName());
    }

    @Test
    @DisplayName("evaluates isAssignableTo hierarchy")
    void isAssignableTo() {
        PortType<Number> numberType = PortType.builder("test:number", Number.class).build();
        PortType<Integer> integerType = PortType.builder("test:integer", Integer.class).build();
        PortType<String> stringType = PortType.builder("test:string", String.class).build();
        PortType<Void> exec1 = PortType.execution(PortTypeId.of("test:e1"), "E1", 0xFF);
        PortType<Void> exec2 = PortType.execution(PortTypeId.of("test:e2"), "E2", 0xFF);

        // Integer is assignable to Number
        assertTrue(integerType.isAssignableTo(numberType));
        // Number is NOT assignable to Integer
        assertFalse(numberType.isAssignableTo(integerType));
        // String is NOT assignable to Number
        assertFalse(stringType.isAssignableTo(numberType));

        // Execution types
        assertTrue(exec1.isAssignableTo(exec2));
        assertFalse(exec1.isAssignableTo(numberType));
        assertFalse(numberType.isAssignableTo(exec1));
    }

    @Test
    @DisplayName("equals and hashCode based on definition attributes")
    void equalsAndHashCode() {
        PortType<String> t1 = PortType.builder("type:a", String.class).displayName("A").build();
        PortType<String> t2 = PortType.builder("type:a", String.class).displayName("A").build();
        PortType<String> t3 = PortType.builder("type:a", String.class).displayName("Different").build();
        PortType<Integer> t4 = PortType.builder("type:b", Integer.class).build();

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3);
        assertNotEquals(t1, t4);
    }
}
