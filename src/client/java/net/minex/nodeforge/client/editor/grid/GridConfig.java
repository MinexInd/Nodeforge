package net.minex.nodeforge.client.editor.grid;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mutable configuration holding background grid styling, dimensions, and snapping settings.
 */
public class GridConfig {

    public static final double DEFAULT_SIZE = 16.0;
    public static final double DEFAULT_MAJOR_INTERVAL = 5.0;

    private GridStyle style = GridStyle.LINES;
    private double size = DEFAULT_SIZE;
    private boolean snapEnabled = true;
    private double majorInterval = DEFAULT_MAJOR_INTERVAL;
    private Consumer<GridConfig> onChange;

    public GridConfig(GridStyle style, double size, boolean snapEnabled) {
        this.style = Objects.requireNonNull(style, "style must not be null");
        this.size = size <= 0.0 ? DEFAULT_SIZE : size;
        this.snapEnabled = snapEnabled;
    }

    public GridConfig() {
        this(GridStyle.LINES, DEFAULT_SIZE, true);
    }

    public void setOnChange(Consumer<GridConfig> onChange) {
        this.onChange = onChange;
    }

    private void notifyChanged() {
        if (onChange != null) {
            try {
                onChange.accept(this);
            } catch (Exception ignored) {}
        }
    }

    public GridStyle getStyle() {
        return style;
    }

    public void setStyle(GridStyle style) {
        this.style = Objects.requireNonNull(style, "style must not be null");
        notifyChanged();
    }

    public void cycleStyle() {
        this.style = this.style.next();
        notifyChanged();
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size <= 0.0 ? DEFAULT_SIZE : size;
        notifyChanged();
    }

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        notifyChanged();
    }

    public boolean toggleSnap() {
        this.snapEnabled = !this.snapEnabled;
        notifyChanged();
        return this.snapEnabled;
    }

    public double getMajorInterval() {
        return majorInterval;
    }

    public void setMajorInterval(double majorInterval) {
        this.majorInterval = majorInterval <= 0.0 ? DEFAULT_MAJOR_INTERVAL : majorInterval;
        notifyChanged();
    }
}
