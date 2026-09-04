package net.minex.nodeforge.client.editor.command;

/**
 * Common interface for reversible operations executed within the graph editor.
 */
public interface EditorCommand {

    /**
     * Executes (or re-executes) this command.
     */
    void execute();

    /**
     * Reverses the changes made by this command.
     */
    void undo();

    /**
     * Returns a brief human-readable description of this command (for undo/redo menus and tooltips).
     */
    String description();
}
