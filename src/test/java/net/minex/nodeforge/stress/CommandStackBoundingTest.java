package net.minex.nodeforge.stress;

import net.minex.nodeforge.client.editor.command.CommandStack;
import net.minex.nodeforge.client.editor.command.EditorCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates memory bounding, capacity limits, and dynamic pruning of CommandStack.
 */
class CommandStackBoundingTest {

    private record IncrementCommand(AtomicInteger counter, String desc) implements EditorCommand {
        @Override
        public void execute() {
            counter.incrementAndGet();
        }

        @Override
        public void undo() {
            counter.decrementAndGet();
        }

        @Override
        public String description() {
            return desc;
        }
    }

    @Test
    @DisplayName("10,000 commands executed on CommandStack strictly adhere to bounded maxHistory")
    void testCommandStackMemoryBounding() {
        CommandStack stack = new CommandStack(100);
        assertEquals(100, stack.maxHistory());

        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 10_000; i++) {
            stack.execute(new IncrementCommand(counter, "Command " + i));
        }

        assertEquals(10_000, counter.get());
        assertEquals(100, stack.undoCount(), "Undo stack size must be strictly clamped to maxHistory");
        assertEquals("Command 9999", stack.undoDescription());

        // Can only undo 100 times
        for (int i = 0; i < 100; i++) {
            assertTrue(stack.canUndo());
            stack.undo();
        }
        assertFalse(stack.canUndo());
        assertEquals(0, stack.undoCount());
        assertEquals(100, stack.redoCount());
        assertEquals(10_000 - 100, counter.get());
    }

    @Test
    @DisplayName("Dynamic setMaxHistory immediately truncates excess undo and redo entries")
    void testDynamicMaxHistoryPruning() {
        CommandStack stack = new CommandStack(50);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            stack.execute(new IncrementCommand(counter, "Cmd " + i));
        }
        assertEquals(50, stack.undoCount());

        // Undo 20 items so redo stack has 20 items
        for (int i = 0; i < 20; i++) {
            stack.undo();
        }
        assertEquals(30, stack.undoCount());
        assertEquals(20, stack.redoCount());

        // Shrink max history to 10
        stack.setMaxHistory(10);
        assertEquals(10, stack.maxHistory());
        assertEquals(10, stack.undoCount(), "Undo stack must be truncated to 10");
        assertEquals(10, stack.redoCount(), "Redo stack must be truncated to 10");

        // Invalid maxHistory throws
        assertThrows(IllegalArgumentException.class, () -> stack.setMaxHistory(0));
        assertThrows(IllegalArgumentException.class, () -> stack.setMaxHistory(-5));
    }
}
