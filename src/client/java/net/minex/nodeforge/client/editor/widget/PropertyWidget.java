package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

import java.util.function.Consumer;

/**
 * Generic, domain-agnostic interactive UI widget representing an editable property value.
 *
 * <p>Designed as a standalone library interface that can be embedded in node bodies,
 * side property inspectors, or custom configuration dialogs.
 *
 * @param <T> the value type managed by this widget
 */
public interface PropertyWidget<T> {

    /** Returns the current value held by this widget. */
    T getValue();

    /** Sets the value held by this widget. */
    void setValue(T value);

    /** Registers a callback invoked whenever the widget's value changes through user interaction. */
    void setOnChanged(Consumer<T> listener);

    /** Returns {@code true} if this widget currently has keyboard focus. */
    boolean isFocused();

    /** Sets whether this widget has keyboard focus. */
    void setFocused(boolean focused);

    /** Returns the preferred vertical height in pixels for this widget. */
    default int getPreferredHeight() {
        return 18;
    }

    /**
     * Renders the widget into the given 2D drawing context.
     *
     * @param context      drawing context
     * @param textRenderer Minecraft font renderer
     * @param x            top-left X screen coordinate
     * @param y            top-left Y screen coordinate
     * @param width        widget display width
     * @param height       widget display height
     * @param mouseX       current cursor X coordinate
     * @param mouseY       current cursor Y coordinate
     */
    void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int mouseX, int mouseY);

    /**
     * Handles mouse click input events.
     *
     * @param click  mouse click event
     * @param x      widget X
     * @param y      widget Y
     * @param width  widget width
     * @param height widget height
     * @return {@code true} if the event was consumed
     */
    default boolean mouseClicked(Click click, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handles mouse drag input events.
     *
     * @param click  mouse drag event
     * @param deltaX horizontal delta
     * @param deltaY vertical delta
     * @param x      widget X
     * @param y      widget Y
     * @param width  widget width
     * @param height widget height
     * @return {@code true} if the event was consumed
     */
    default boolean mouseDragged(Click click, double deltaX, double deltaY, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handles mouse release input events.
     *
     * @param click  mouse release event
     * @param x      widget X
     * @param y      widget Y
     * @param width  widget width
     * @param height widget height
     * @return {@code true} if the event was consumed
     */
    default boolean mouseReleased(Click click, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handles keyboard key press events when this widget has focus.
     *
     * @param input key input event
     * @return {@code true} if the event was consumed
     */
    default boolean keyPressed(KeyInput input) {
        return false;
    }

    /**
     * Handles character typed events when this widget has focus.
     *
     * @param input character typed event
     * @return {@code true} if the event was consumed
     */
    default boolean charTyped(CharInput input) {
        return false;
    }
}
