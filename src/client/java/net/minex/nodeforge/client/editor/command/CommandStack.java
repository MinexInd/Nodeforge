package net.minex.nodeforge.client.editor.command;

import java.util.*;

/**
 * Manages the undo and redo history stacks for editor commands.
 */
public class CommandStack {

    public static final int DEFAULT_MAX_HISTORY = 100;

    private final Deque<EditorCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditorCommand> redoStack = new ArrayDeque<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private int maxHistory;

    public CommandStack(int maxHistory) {
        if (maxHistory <= 0) {
            throw new IllegalArgumentException("maxHistory must be positive: " + maxHistory);
        }
        this.maxHistory = maxHistory;
    }

    public CommandStack() {
        this(DEFAULT_MAX_HISTORY);
    }

    /**
     * Executes the given command, pushes it onto the undo stack, and clears the redo stack.
     *
     * @param command the command to execute, must not be null
     */
    public void execute(EditorCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        command.execute();
        pushExecuted(command);
    }

    /**
     * Pushes an already-executed command onto the undo stack and clears the redo stack.
     *
     * @param command the executed command, must not be null
     */
    public void pushExecuted(EditorCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        undoStack.push(command);
        while (undoStack.size() > maxHistory) {
            undoStack.removeLast();
        }
        redoStack.clear();
        notifyChanged();
    }

    /**
     * Undoes the most recently executed command.
     *
     * @return {@code true} if a command was undone, or {@code false} if the undo stack was empty
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        EditorCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        notifyChanged();
        return true;
    }

    /**
     * Re-executes the most recently undone command.
     *
     * @return {@code true} if a command was redone, or {@code false} if the redo stack was empty
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        EditorCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        notifyChanged();
        return true;
    }

    /** Returns {@code true} if there is at least one command available to undo. */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /** Returns {@code true} if there is at least one command available to redo. */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** Returns the description of the next command to be undone, or empty string. */
    public String undoDescription() {
        return undoStack.isEmpty() ? "" : undoStack.peek().description();
    }

    /** Returns the description of the next command to be redone, or empty string. */
    public String redoDescription() {
        return redoStack.isEmpty() ? "" : redoStack.peek().description();
    }

    /** Returns the number of commands currently on the undo stack. */
    public int undoCount() {
        return undoStack.size();
    }

    /** Returns the number of commands currently on the redo stack. */
    public int redoCount() {
        return redoStack.size();
    }

    /** Returns the maximum undo history capacity. */
    public int maxHistory() {
        return maxHistory;
    }

    /**
     * Sets the maximum number of undo commands to keep in history.
     * Prunes excess commands immediately if current history exceeds the new limit.
     *
     * @param maxHistory the new maximum history size, must be positive
     */
    public void setMaxHistory(int maxHistory) {
        if (maxHistory <= 0) {
            throw new IllegalArgumentException("maxHistory must be positive: " + maxHistory);
        }
        this.maxHistory = maxHistory;
        while (undoStack.size() > this.maxHistory) {
            undoStack.removeLast();
        }
        while (redoStack.size() > this.maxHistory) {
            redoStack.removeLast();
        }
        notifyChanged();
    }

    /** Clears all undo and redo history. */
    public void clear() {
        if (!undoStack.isEmpty() || !redoStack.isEmpty()) {
            undoStack.clear();
            redoStack.clear();
            notifyChanged();
        }
    }

    /** Adds a listener invoked whenever the undo/redo stack state changes. */
    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /** Removes a change listener. */
    public void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception ignored) {
                // Isolate listener exceptions without swallowing VirtualMachineErrors
            }
        }
    }
}
