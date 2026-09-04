package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Generic numeric input widget with minimum, maximum bounds, step increment/decrement,
 * and direct numeric keyboard entry.
 */
public class NumericPropertyWidget implements PropertyWidget<Double> {

    private double value;
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;
    private double step = 1.0;
    private int decimalPlaces = 2;
    private boolean focused = false;
    private String textBuffer;
    private Consumer<Double> onChanged;

    public NumericPropertyWidget(double initialValue) {
        this.value = initialValue;
        this.textBuffer = formatValue(initialValue);
    }

    public NumericPropertyWidget() {
        this(0.0);
    }

    public NumericPropertyWidget setBounds(double min, double max) {
        this.min = min;
        this.max = max;
        setValue(clamp(value));
        return this;
    }

    public NumericPropertyWidget setStep(double step) {
        if (step > 0) this.step = step;
        return this;
    }

    public NumericPropertyWidget setDecimalPlaces(int places) {
        this.decimalPlaces = Math.max(0, places);
        this.textBuffer = formatValue(value);
        return this;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Double value) {
        double clamped = clamp(value != null ? value : 0.0);
        if (Double.compare(this.value, clamped) != 0) {
            this.value = clamped;
            this.textBuffer = formatValue(clamped);
            if (onChanged != null) {
                onChanged.accept(this.value);
            }
        }
    }

    @Override
    public void setOnChanged(Consumer<Double> listener) {
        this.onChanged = listener;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        if (this.focused && !focused) {
            // Commit buffer
            commitTextBuffer();
        }
        this.focused = focused;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        int bg = focused ? 0xFF22262E : (hovered ? 0xFF1E2128 : 0xFF181A20);
        context.fill(x, y, x + width, y + height, bg);

        int border = focused ? 0xFF4A90E2 : (hovered ? 0xFF4A4E58 : 0xFF31353D);
        context.drawStrokedRectangle(x, y, width, height, border);

        int textY = y + (height - textRenderer.fontHeight) / 2 + 1;
        String display = focused ? textBuffer : formatValue(value);
        context.drawText(textRenderer, display, x + 4, textY, 0xFFEEEEEE, false);
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        boolean inside = click.x() >= x && click.x() <= x + width && click.y() >= y && click.y() <= y + height;
        setFocused(inside);
        return inside;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;

        int key = input.key();
        if (key == GLFW.GLFW_KEY_UP) {
            setValue(clamp(value + step));
            return true;
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            setValue(clamp(value - step));
            return true;
        } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!textBuffer.isEmpty()) {
                textBuffer = textBuffer.substring(0, textBuffer.length() - 1);
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
            setFocused(false);
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) return false;

        char c = (char) input.codepoint();
        if (Character.isDigit(c) || c == '-' || c == '.') {
            textBuffer += c;
            return true;
        }
        return false;
    }

    private void commitTextBuffer() {
        try {
            double parsed = Double.parseDouble(textBuffer);
            setValue(parsed);
        } catch (NumberFormatException e) {
            this.textBuffer = formatValue(value);
        }
    }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }

    private String formatValue(double v) {
        if (decimalPlaces == 0 || (v == Math.floor(v) && !Double.isInfinite(v))) {
            return String.valueOf((long) v);
        }
        return String.format(java.util.Locale.ROOT, "%." + decimalPlaces + "f", v);
    }
}
