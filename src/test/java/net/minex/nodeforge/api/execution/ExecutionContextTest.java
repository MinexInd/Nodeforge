package net.minex.nodeforge.api.execution;

import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExecutionContext & State")
class ExecutionContextTest {

    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = new ExecutionContext(10);
    }

    @Test
    @DisplayName("stores, retrieves, and removes runtime variables")
    void variables() {
        assertFalse(context.hasVariable("score"));
        assertNull(context.getVariable("score"));
        assertEquals(100, context.getVariable("score", 100));

        context.setVariable("score", 42);
        assertTrue(context.hasVariable("score"));
        assertEquals(42, context.getVariable("score"));
        assertEquals(42, context.getVariable("score", 0));
        assertEquals(42, context.getVariable("score", Integer.class, 0));

        // Mismatched type returns defaultValue instead of throwing ClassCastException
        assertEquals("fallback", context.getVariable("score", "fallback"));
        assertEquals("fallback", context.getVariable("score", String.class, "fallback"));

        assertEquals(42, context.removeVariable("score"));
        assertFalse(context.hasVariable("score"));
    }

    @Test
    @DisplayName("buffers input and output port values")
    void portBuffers() {
        NodeId n1 = NodeId.of("node1");
        PortId pOut = PortId.of("out");
        PortId pIn = PortId.of("in");

        assertNull(context.getOutputValue(n1, pOut));
        context.setOutputValue(n1, pOut, "hello");
        assertEquals("hello", context.getOutputValue(n1, pOut));

        context.setInputValue(n1, pIn, 123.45);
        assertEquals(123.45, context.getInputValue(n1, pIn));
    }

    @Test
    @DisplayName("enforces maximum step limit and detects cancellation")
    void stepLimitsAndCancellation() {
        assertFalse(context.isCancelled());
        context.cancel();
        assertTrue(context.isCancelled());

        ExecutionContext limited = new ExecutionContext(3);
        assertEquals(1, limited.incrementAndCheckSteps());
        assertEquals(2, limited.incrementAndCheckSteps());
        assertEquals(3, limited.incrementAndCheckSteps());
        assertThrows(IllegalStateException.class, limited::incrementAndCheckSteps);
    }
}
