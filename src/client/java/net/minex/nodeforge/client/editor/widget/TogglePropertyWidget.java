package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Generic interactive toggle switch / checkbox widget for boolean properties.
 */
public class TogglePropertyWidget implements PropertyWidget<Boolean> {

    private boolean value;
    private String label = "";
    private boolean focused = false;
    private Consumer<Boolean> onChanged;

    public TogglePropertyWidget(boolean initialValue, String label) {
        this.value = initialValue;
        this.label = label != null ? label : "";
    }

    public TogglePropertyWidget(boolean initialValue) {
        this(initialValue, "");
    }

    public TogglePropertyWidget() {
        this(false, "");
    }

    public TogglePropertyWidget setLabel(String label) {
        this.label = label != null ? label : "";
        return this;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        boolean val = Boolean.TRUE.equals(value);
        if (this.value != val) {
            this.value = val;
            if (onChanged != null) {
                onChanged.accept(this.value);
            }
        }
    }

    @Override
    public void setOnChanged(Consumer<Boolean> listener) {
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

        int switchWidth = 26;
        int switchHeight = 14;
        int switchY = y + (height - switchHeight) / 2;

        // Background pill
        int bg = value ? 0xFF2E7D32 : (hovered ? 0xFF31353D : 0xFF22262E);
        context.fill(x, switchY, x + switchWidth, switchY + switchHeight, bg);
        context.drawStrokedRectangle(x, switchY, switchWidth, switchHeight, focused ? 0xFF4A90E2 : 0xFF4A4E58);

        // Thumb handle
        int thumbSize = 10;
        int thumbX = value ? (x + switchWidth - thumbSize - 2) : (x + 2);
        int thumbY = switchY + 2;
        context.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, 0xFFFFFFFF);

        // Label text
        if (!label.isEmpty()) {
            int textX = x + switchWidth + 6;
            int textY = y + (height - textRenderer.fontHeight) / 2 + 1;
            context.drawText(textRenderer, label, textX, textY, 0xFFEEEEEE, false);
        }
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        boolean inside = click.x() >= x && click.x() <= x + width && click.y() >= y && click.y() <= y + height;
        setFocused(inside);
        if (inside) {
            setValue(!value);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;
        if (input.key() == GLFW.GLFW_KEY_SPACE || input.key() == GLFW.GLFW_KEY_ENTER) {
            setValue(!value);
            return true;
        }
        return false;
    }
}
