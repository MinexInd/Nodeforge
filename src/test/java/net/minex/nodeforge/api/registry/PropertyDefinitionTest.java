package net.minex.nodeforge.api.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PropertyDefinition")
class PropertyDefinitionTest {

    enum OperationMode { ADD, SUBTRACT, MULTIPLY }

    @Test
    @DisplayName("creates and validates standard property types")
    void standardProperties() {
        // Boolean
        PropertyDefinition<Boolean> boolProp = PropertyDefinition.booleanProperty("active", "Active State", true);
        assertEquals("active", boolProp.key());
        assertEquals(Boolean.class, boolProp.valueClass());
        assertTrue(boolProp.defaultValue());
        assertTrue(boolProp.isValid(false));

        // Int
        PropertyDefinition<Integer> intProp = PropertyDefinition.intProperty("amount", "Amount", 10, 1, 100);
        assertEquals(10, intProp.defaultValue());
        assertTrue(intProp.isValid(50));
        assertFalse(intProp.isValid(0));
        assertFalse(intProp.isValid(101));

        // Double
        PropertyDefinition<Double> doubleProp = PropertyDefinition.doubleProperty("rate", "Rate", 1.5, 0.0, 10.0);
        assertEquals(1.5, doubleProp.defaultValue());
        assertTrue(doubleProp.isValid(9.9));
        assertFalse(doubleProp.isValid(-0.1));
        assertFalse(doubleProp.isValid(Double.NaN));

        // String
        PropertyDefinition<String> strProp = PropertyDefinition.stringProperty("formula", "Formula", "x + y");
        assertEquals("x + y", strProp.defaultValue());
        assertTrue(strProp.isValid("2 * z"));

        // Enum
        PropertyDefinition<OperationMode> enumProp = PropertyDefinition.enumProperty("mode", "Mode", OperationMode.class, OperationMode.ADD);
        assertEquals(OperationMode.ADD, enumProp.defaultValue());
        assertTrue(enumProp.isValid(OperationMode.MULTIPLY));
    }

    @Test
    @DisplayName("throws when default value fails validation predicate")
    void invalidDefaultValue() {
        assertThrows(IllegalArgumentException.class, () ->
                PropertyDefinition.intProperty("count", "Count", 500, 1, 100));

        assertThrows(IllegalArgumentException.class, () ->
                PropertyDefinition.doubleProperty("d", "D", -1.0, 0.0, 10.0));
    }

    @Test
    @DisplayName("equality and toString")
    void equalsAndToString() {
        PropertyDefinition<Integer> p1 = PropertyDefinition.intProperty("prop_a", "First", 5);
        PropertyDefinition<Integer> p2 = PropertyDefinition.intProperty("prop_a", "Second", 5);
        PropertyDefinition<String> p3 = PropertyDefinition.stringProperty("prop_a", "First", "val");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3, "Different valueClass should not be equal");
        assertEquals("PropertyDefinition[prop_a (Integer) default=5]", p1.toString());
    }
}
