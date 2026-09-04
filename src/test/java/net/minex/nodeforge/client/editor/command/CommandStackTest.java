package net.minex.nodeforge.client.editor.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandStack & History")
class CommandStackTest {

    private CommandStack stack;

    @BeforeEach
    void setUp() {
        stack = new CommandStack(5);
    }

    private static class TestCommand implements EditorCommand {
        private final String desc;
        int execCount = 0;
        int undoCount = 0;

        TestCommand(String desc) { this.desc = desc; }

        @Override public void execute() { execCount++; }
        @Override public void undo() { undoCount++; }
        @Override public String description() { return desc; }
    }

    @Test
    @DisplayName("executes command and manages undo / redo lifecycle")
    void executeUndoRedo() {
        TestCommand cmd1 = new TestCommand("Cmd 1");
        TestCommand cmd2 = new TestCommand("Cmd 2");

        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());

        stack.execute(cmd1);
        assertEquals(1, cmd1.execCount);
        assertTrue(stack.canUndo());
        assertFalse(stack.canRedo());
        assertEquals("Cmd 1", stack.undoDescription());

        stack.execute(cmd2);
        assertEquals(1, cmd2.execCount);
        assertEquals(2, stack.undoCount());
        assertEquals("Cmd 2", stack.undoDescription());

        // Undo cmd2
        assertTrue(stack.undo());
        assertEquals(1, cmd2.undoCount);
        assertEquals(1, stack.undoCount());
        assertEquals(1, stack.redoCount());
        assertTrue(stack.canRedo());
        assertEquals("Cmd 2", stack.redoDescription());

        // Redo cmd2
        assertTrue(stack.redo());
        assertEquals(2, cmd2.execCount);
        assertEquals(2, stack.undoCount());
        assertEquals(0, stack.redoCount());
    }

    @Test
    @DisplayName("enforces max history limit")
    void maxHistoryEnforcement() {
        for (int i = 0; i < 10; i++) {
            stack.execute(new TestCommand("Cmd " + i));
        }

        // Capacity is 5
        assertEquals(5, stack.undoCount());
        assertEquals("Cmd 9", stack.undoDescription());
    }

    @Test
    @DisplayName("notifies change listeners and isolates exceptions")
    void changeListeners() {
        AtomicInteger eventCount = new AtomicInteger(0);

        // Add buggy listener
        stack.addChangeListener(() -> { throw new RuntimeException("boom"); });
        // Add normal listener
        stack.addChangeListener(eventCount::incrementAndGet);

        stack.execute(new TestCommand("Cmd"));
        assertEquals(1, eventCount.get());

        stack.undo();
        assertEquals(2, eventCount.get());

        stack.redo();
        assertEquals(3, eventCount.get());

        stack.clear();
        assertEquals(4, eventCount.get());
    }
}
