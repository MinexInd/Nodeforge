# Property Widgets

Property widgets are interactive UI controls embedded directly within node bodies or sidebar inspectors, allowing users to configure node properties without wiring input sockets.

---

## 1. Built-in Property Widgets

NodeForge includes six production-ready property widgets in package `net.minex.nodeforge.client.editor.widget`:

| Widget Class | Value Type | Interactive Controls |
| :--- | :--- | :--- |
| **`StringPropertyWidget`** | `String` | Single-line text input field with cursor positioning and backspace. |
| **`NumericPropertyWidget`** | `Double` | Validated numeric input field accepting decimal and negative numbers. |
| **`SliderPropertyWidget`** | `Double` | Draggable horizontal slider bounded between minimum and maximum limits. |
| **`TogglePropertyWidget`** | `Boolean` | Interactive on/off switch with smooth animated thumb transitions. |
| **`DropdownWidget<T>`** | `T` | Selectable popup dropdown list supporting arbitrary item collections. |
| **`ColorPickerWidget`** | `Integer` | Hexadecimal ARGB color selector with live preview swatch. |

---

## 2. Using Built-in Widgets

Each widget implements `PropertyWidget<T>`:

```java
import net.minex.nodeforge.client.editor.widget.SliderPropertyWidget;

// Create a slider bounded between 0.0 and 100.0 with step 1.0
SliderPropertyWidget speedSlider = new SliderPropertyWidget(
        "Movement Speed",   // Label text
        25.0,               // Initial value
        0.0,                // Minimum value
        100.0,              // Maximum value
        1.0                 // Step increment
);

// Listen for user adjustments
speedSlider.setOnChanged(newValue -> {
    System.out.println("Updated speed: " + newValue);
});
```

---

## 3. Implementing a Custom `PropertyWidget`

To create a bespoke UI control (such as a Minecraft item selector or 2D curve editor), implement `PropertyWidget<T>`:

```java
package com.example.mymod.client.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.client.editor.widget.PropertyWidget;

import java.util.function.Consumer;

public class DifficultyToggleWidget implements PropertyWidget<String> {

    private String currentDifficulty = "NORMAL";
    private Consumer<String> changeListener;
    private boolean focused = false;

    @Override
    public String getValue() {
        return currentDifficulty;
    }

    @Override
    public void setValue(String value) {
        this.currentDifficulty = value != null ? value : "NORMAL";
    }

    @Override
    public void setOnChanged(Consumer<String> listener) {
        this.changeListener = listener;
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
    public int getPreferredHeight() {
        return 18;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, 
                       int x, int y, int width, int height, int mouseX, int mouseY) {
        int bgColor = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height 
                ? 0xFF3A4A5A : 0xFF2A3A4A;
        
        // Draw background button
        context.fill(x, y, x + width, y + height, bgColor);
        context.drawBorder(x, y, width, height, 0xFF5A6A7A);

        // Draw label
        String label = "Difficulty: " + currentDifficulty;
        context.drawText(textRenderer, label, x + 6, y + 5, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(Click click, int x, int y, int width, int height) {
        // Toggle on click
        if (currentDifficulty.equals("NORMAL")) {
            currentDifficulty = "HARD";
        } else {
            currentDifficulty = "NORMAL";
        }
        
        if (changeListener != null) {
            changeListener.accept(currentDifficulty);
        }
        return true;
    }
}
```

---

## 4. Next Steps

- Learn how themes customize widget colors and typography in the [Themes Guide](themes.md).
- Learn about the layered rendering pipeline in the [Rendering & VFX Guide](rendering.md).
