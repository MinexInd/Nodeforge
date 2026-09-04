package net.minex.nodeforge.client.editor.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Composite command grouping multiple editor commands into a single atomic undo/redo unit.
 */
public class CompoundCommand implements EditorCommand {

    private final String description;
    private final List<EditorCommand> commands;

    public CompoundCommand(String description, List<EditorCommand> commands) {
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.commands = List.copyOf(Objects.requireNonNull(commands, "commands must not be null"));
    }

    public CompoundCommand(String description) {
        this(description, new ArrayList<>());
    }

    @Override
    public void execute() {
        for (EditorCommand command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        // Undo in reverse order
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String description() {
        return description;
    }

    public List<EditorCommand> getCommands() {
        return commands;
    }
}
