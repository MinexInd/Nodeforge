package net.minex.nodeforge.client.editor.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Generic color picker widget displaying an ARGB preview swatch, hex text code,
 * and an interactive popup palette.
 */
public class ColorPickerWidget implements PropertyWidget<Integer> {

    private static final int[] PRESET_PALETTE = {
            0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1,
            0xFF3949AB, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B,
            0xFF43A047, 0xFF7CB342, 0xFFFDD835, 0xFFFB8C00,
            0xFFF4511E, 0xFF6D4C41, 0xFF757575, 0xFFFFFFFF
    };

    private int value;
    private boolean open = false;
    private boolean focused = false;
    private Consumer<Integer> onChanged;

    public ColorPickerWidget(int initialArgb) {
        this.value = initialArgb;
    }

    public ColorPickerWidget() {
        this(0xFF4A90E2);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        int argb = value != null ? value : 0xFFFFFFFF;
        if (this.value != argb) {
            this.value = argb;
            if (onChanged != null) {
                onChanged.accept(this.value);
            }
        }
    }

    @Override
    public void setOnChanged(Consumer<Integer> listener) {
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

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        // Background
        int bg = focused ? 0xFF22262E : (hovered ? 0xFF1E2128 : 0xFF181A20);
        context.fill(x, y, x + width, y + height, bg);
        context.drawStrokedRectangle(x, y, width, height, (focused || open) ? 0xFF4A90E2 : (hovered ? 0xFF4A4E58 : 0xFF31353D));

        // Swatch box
        int swatchSize = 12;
        int swatchX = x + 3;
        int swatchY = y + (height - swatchSize) / 2;
        context.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, value);
        context.drawStrokedRectangle(swatchX, swatchY, swatchSize, swatchSize, 0xFFFFFFFF);

        // Hex string
        String hex = String.format(Locale.ROOT, "#%08X", value);
        int textY = y + (height - textRenderer.fontHeight) / 2 + 1;
        context.drawText(textRenderer, hex, swatchX + swatchSize + 5, textY, 0xFFEEEEEE, false);

        // Open palette popup
        if (open) {
            int cols = 4;
            int cellSize = 14;
            int pad = 4;
            int popupWidth = cols * cellSize + pad * 2;
            int popupHeight = (PRESET_PALETTE.length / cols) * cellSize + pad * 2;
            int popupY = y + height + 2;

            context.fill(x, popupY, x + popupWidth, popupY + popupHeight, 0xFA181A20);
            context.drawStrokedRectangle(x, popupY, popupWidth, popupHeight, 0xFF4A90E2);

            for (int i = 0; i < PRESET_PALETTE.length; i++) {
                int col = i % cols;
                int row = i / cols;
                int cellX = x + pad + col * cellSize;
                int cellY = popupY + pad + row * cellSize;
                int color = PRESET_PALETTE[i];

                context.fill(cellX + 1, cellY + 1, cellX + cellSize - 1, cellY + cellSize - 1, color);
                if (color == value) {
                    context.drawStrokedRectangle(cellX, cellY, cellSize, cellSize, 0xFFFFFFFF);
                }
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

        if (open) {
            int cols = 4;
            int cellSize = 14;
            int pad = 4;
            int popupWidth = cols * cellSize + pad * 2;
            int popupHeight = (PRESET_PALETTE.length / cols) * cellSize + pad * 2;
            int popupY = y + height + 2;

            if (click.x() >= x && click.x() <= x + popupWidth && click.y() >= popupY && click.y() <= popupY + popupHeight) {
                int relX = (int) (click.x() - x - pad);
                int relY = (int) (click.y() - popupY - pad);
                if (relX >= 0 && relY >= 0) {
                    int col = relX / cellSize;
                    int row = relY / cellSize;
                    int index = row * cols + col;
                    if (index >= 0 && index < PRESET_PALETTE.length) {
                        setValue(PRESET_PALETTE[index]);
                        this.open = false;
                        setFocused(false);
                        return true;
                    }
                }
            }
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
