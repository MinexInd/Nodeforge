package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Generic draggable slider widget for continuous or stepped numeric ranges.
 */
public class SliderPropertyWidget implements PropertyWidget<Double> {

    private double value;
    private double min = 0.0;
    private double max = 1.0;
    private double step = 0.01;
    private boolean dragging = false;
    private boolean focused = false;
    private Consumer<Double> onChanged;

    public SliderPropertyWidget(double initialValue, double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min (" + min + ") must be strictly less than max (" + max + ")");
        }
        this.min = min;
        this.max = max;
        this.value = clamp(initialValue);
    }

    public SliderPropertyWidget() {
        this(0.5, 0.0, 1.0);
    }

    public SliderPropertyWidget setStep(double step) {
        if (step > 0) this.step = step;
        return this;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Double value) {
        double clamped = clamp(value != null ? value : min);
        if (Double.compare(this.value, clamped) != 0) {
            this.value = clamped;
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
        this.focused = focused;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        // Background track
        int trackHeight = 4;
        int trackY = y + (height - trackHeight) / 2;
        context.fill(x, trackY, x + width, trackY + trackHeight, 0xFF181A20);
        context.drawStrokedRectangle(x, trackY, width, trackHeight, 0xFF31353D);

        // Filled track
        double ratio = (value - min) / (max - min);
        int filledWidth = (int) Math.round(ratio * width);
        if (filledWidth > 0) {
            context.fill(x, trackY, x + filledWidth, trackY + trackHeight, 0xFF4A90E2);
        }

        // Thumb handle
        int thumbWidth = 8;
        int thumbHeight = 12;
        int thumbX = Math.max(x, Math.min(x + width - thumbWidth, x + (int) Math.round(ratio * (width - thumbWidth))));
        int thumbY = y + (height - thumbHeight) / 2;

        int thumbColor = (dragging || (hovered && focused)) ? 0xFFFFFFFF : (hovered ? 0xFFCCCCCC : 0xFFAAAAAA);
        context.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbHeight, thumbColor);
        context.drawStrokedRectangle(thumbX, thumbY, thumbWidth, thumbHeight, 0xFF22262E);

        // Value text overlay
        String label = String.format(Locale.ROOT, "%.2f", value);
        int textWidth = textRenderer.getWidth(label);
        int textX = x + width - textWidth - 2;
        int textY = y - textRenderer.fontHeight + 2;
        if (textY < y) textY = y + (height - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, label, textX, textY, 0xFFAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        boolean inside = click.x() >= x && click.x() <= x + width && click.y() >= y && click.y() <= y + height;
        setFocused(inside);
        if (inside) {
            this.dragging = true;
            updateFromMouseX(click.x(), x, width);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY, int x, int y, int width, int height) {
        if (dragging) {
            updateFromMouseX(click.x(), x, width);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click, int x, int y, int width, int height) {
        if (dragging) {
            this.dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;
        int key = input.key();
        if (key == GLFW.GLFW_KEY_LEFT) {
            setValue(value - step);
            return true;
        } else if (key == GLFW.GLFW_KEY_RIGHT) {
            setValue(value + step);
            return true;
        }
        return false;
    }

    private void updateFromMouseX(double mouseX, int x, int width) {
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - x) / (double) width));
        double rawVal = min + ratio * (max - min);
        // Quantize to step
        double stepped = Math.round(rawVal / step) * step;
        setValue(stepped);
    }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }
}
