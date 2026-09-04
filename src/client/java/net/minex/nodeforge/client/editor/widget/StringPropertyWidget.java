package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Generic single-line text input widget with cursor positioning, character filtering,
 * and placeholder text support.
 */
public class StringPropertyWidget implements PropertyWidget<String> {

    private String value;
    private String placeholder = "";
    private boolean focused = false;
    private int cursorPosition = 0;
    private Predicate<String> filter = s -> true;
    private Consumer<String> onChanged;

    public StringPropertyWidget(String initialValue) {
        this.value = initialValue != null ? initialValue : "";
        this.cursorPosition = this.value.length();
    }

    public StringPropertyWidget() {
        this("");
    }

    public StringPropertyWidget setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        return this;
    }

    public StringPropertyWidget setFilter(Predicate<String> filter) {
        this.filter = filter != null ? filter : s -> true;
        return this;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        String safe = value != null ? value : "";
        if (!Objects.equals(this.value, safe)) {
            this.value = safe;
            this.cursorPosition = Math.min(cursorPosition, safe.length());
            if (onChanged != null) {
                onChanged.accept(this.value);
            }
        }
    }

    @Override
    public void setOnChanged(Consumer<String> listener) {
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

        // Background
        int bg = focused ? 0xFF22262E : (hovered ? 0xFF1E2128 : 0xFF181A20);
        context.fill(x, y, x + width, y + height, bg);

        // Border
        int border = focused ? 0xFF4A90E2 : (hovered ? 0xFF4A4E58 : 0xFF31353D);
        context.drawStrokedRectangle(x, y, width, height, border);

        // Text or Placeholder
        int textY = y + (height - textRenderer.fontHeight) / 2 + 1;
        int textX = x + 4;

        if (value.isEmpty() && !placeholder.isEmpty() && !focused) {
            context.drawText(textRenderer, placeholder, textX, textY, 0xFF666666, false);
        } else {
            String displayText = value;
            context.drawText(textRenderer, displayText, textX, textY, 0xFFEEEEEE, false);

            // Blinking cursor when focused
            if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
                String sub = value.substring(0, Math.min(cursorPosition, value.length()));
                int cursorX = textX + textRenderer.getWidth(sub);
                context.fill(cursorX, textY - 1, cursorX + 1, textY + textRenderer.fontHeight, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        boolean inside = click.x() >= x && click.x() <= x + width && click.y() >= y && click.y() <= y + height;
        setFocused(inside);
        if (inside) {
            cursorPosition = value.length();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;

        int key = input.key();
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorPosition > 0 && !value.isEmpty()) {
                String newVal = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                if (filter.test(newVal)) {
                    cursorPosition--;
                    setValue(newVal);
                }
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_DELETE) {
            if (cursorPosition < value.length()) {
                String newVal = value.substring(0, cursorPosition) + value.substring(cursorPosition + 1);
                if (filter.test(newVal)) {
                    setValue(newVal);
                }
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_LEFT) {
            if (cursorPosition > 0) cursorPosition--;
            return true;
        } else if (key == GLFW.GLFW_KEY_RIGHT) {
            if (cursorPosition < value.length()) cursorPosition++;
            return true;
        } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
            setFocused(false);
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) return false;

        String inserted = input.asString();
        String newVal = value.substring(0, cursorPosition) + inserted + value.substring(cursorPosition);
        if (filter.test(newVal)) {
            cursorPosition += inserted.length();
            setValue(newVal);
            return true;
        }

        return false;
    }
}
