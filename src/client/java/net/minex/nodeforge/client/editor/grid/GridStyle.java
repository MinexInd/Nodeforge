package net.minex.nodeforge.client.editor.grid;

/**
 * Visual styles for rendering the infinite background grid.
 */
public enum GridStyle {
    LINES,
    DOTS,
    CROSSES;

    /** Cycles to the next grid style in sequential order. */
    public GridStyle next() {
        GridStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
