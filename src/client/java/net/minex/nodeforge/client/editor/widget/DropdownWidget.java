package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic single-selection dropdown popup menu widget.
 *
 * @param <T> option item type
 */
public class DropdownWidget<T> implements PropertyWidget<T> {

    private final List<T> options = new ArrayList<>();
    private Function<T, String> labelProvider = String::valueOf;
    private T selectedValue;
    private boolean open = false;
    private boolean focused = false;
    private Consumer<T> onChanged;

    public DropdownWidget(List<T> options, T initialValue) {
        if (options != null) {
            this.options.addAll(options);
        }
        this.selectedValue = initialValue != null ? initialValue : (!this.options.isEmpty() ? this.options.get(0) : null);
    }

    public DropdownWidget(List<T> options) {
        this(options, null);
    }

    public DropdownWidget<T> setLabelProvider(Function<T, String> labelProvider) {
        this.labelProvider = labelProvider != null ? labelProvider : String::valueOf;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public T getValue() {
        return selectedValue;
    }

    @Override
    public void setValue(T value) {
        if (!Objects.equals(this.selectedValue, value)) {
            this.selectedValue = value;
            if (onChanged != null) {
                onChanged.accept(this.selectedValue);
            }
        }
    }

    @Override
    public void setOnChanged(Consumer<T> listener) {
        this.onChanged = listener;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused) {
            this.open = false;
        }
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        // Main header bar
        int bg = focused ? 0xFF22262E : (hovered ? 0xFF1E2128 : 0xFF181A20);
        context.fill(x, y, x + width, y + height, bg);
        context.drawStrokedRectangle(x, y, width, height, (focused || open) ? 0xFF4A90E2 : (hovered ? 0xFF4A4E58 : 0xFF31353D));

        // Selected label
        String text = selectedValue != null ? labelProvider.apply(selectedValue) : "Select...";
        int textY = y + (height - textRenderer.fontHeight) / 2 + 1;
        context.drawText(textRenderer, text, x + 6, textY, 0xFFEEEEEE, false);

        // Arrow indicator (▼ / ▲)
        String arrow = open ? "▲" : "▼";
        context.drawText(textRenderer, arrow, x + width - 12, textY, 0xFFAAAAAA, false);

        // Open popup overlay list
        if (open && !options.isEmpty()) {
            int itemHeight = 16;
            int popupHeight = options.size() * itemHeight;
            int popupY = y + height + 2;

            // Background shadow/box
            context.fill(x, popupY, x + width, popupY + popupHeight, 0xFA181A20);
            context.drawStrokedRectangle(x, popupY, width, popupHeight, 0xFF4A90E2);

            for (int i = 0; i < options.size(); i++) {
                T opt = options.get(i);
                int optY = popupY + i * itemHeight;
                boolean itemHovered = mouseX >= x && mouseX <= x + width && mouseY >= optY && mouseY < optY + itemHeight;
                boolean isSelected = Objects.equals(opt, selectedValue);

                if (itemHovered || isSelected) {
                    context.fill(x + 1, optY, x + width - 1, optY + itemHeight, isSelected ? 0xFF264F78 : 0xFF2A2E38);
                }

                String optText = labelProvider.apply(opt);
                context.drawText(textRenderer, optText, x + 6, optY + (itemHeight - textRenderer.fontHeight) / 2 + 1,
                        isSelected ? 0xFFFFFFFF : 0xFFCCCCCC, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        boolean headerClicked = click.x() >= x && click.x() <= x + width && click.y() >= y && click.y() <= y + height;

        if (headerClicked) {
            this.open = !this.open;
            setFocused(true);
            return true;
        }

        // Check popup items
        if (open && !options.isEmpty()) {
            int itemHeight = 16;
            int popupY = y + height + 2;
            int popupHeight = options.size() * itemHeight;

            if (click.x() >= x && click.x() <= x + width && click.y() >= popupY && click.y() <= popupY + popupHeight) {
                int index = (int) ((click.y() - popupY) / itemHeight);
                if (index >= 0 && index < options.size()) {
                    setValue(options.get(index));
                    this.open = false;
                    setFocused(false);
                    return true;
                }
            }
        }

        if (open) {
            this.open = false;
        }
        setFocused(false);
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.open = false;
            setFocused(false);
            return true;
        }
        return false;
    }
}
