package net.minex.nodeforge.client.editor.widget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generic Property Widgets")
class PropertyWidgetTest {

    @Test
    @DisplayName("StringPropertyWidget updates values, notifies listeners, and applies filters")
    void stringWidget() {
        StringPropertyWidget widget = new StringPropertyWidget("initial");
        assertEquals("initial", widget.getValue());

        AtomicReference<String> changed = new AtomicReference<>();
        widget.setOnChanged(changed::set);

        widget.setValue("updated");
        assertEquals("updated", widget.getValue());
        assertEquals("updated", changed.get());

        // Test filter
        widget.setFilter(s -> s.length() <= 10);
        assertTrue(widget.getValue().length() <= 10);
    }

    @Test
    @DisplayName("NumericPropertyWidget clamps to bounds and steps")
    void numericWidget() {
        NumericPropertyWidget widget = new NumericPropertyWidget(10.0);
        widget.setBounds(0.0, 100.0);
        widget.setStep(5.0);

        assertEquals(10.0, widget.getValue(), 1e-6);

        widget.setValue(150.0); // Above max
        assertEquals(100.0, widget.getValue(), 1e-6);

        widget.setValue(-50.0); // Below min
        assertEquals(0.0, widget.getValue(), 1e-6);
    }

    @Test
    @DisplayName("SliderPropertyWidget clamps continuous ranges")
    void sliderWidget() {
        SliderPropertyWidget slider = new SliderPropertyWidget(0.5, 0.0, 1.0);
        assertEquals(0.5, slider.getValue(), 1e-6);

        slider.setValue(0.75);
        assertEquals(0.75, slider.getValue(), 1e-6);

        slider.setValue(2.0);
        assertEquals(1.0, slider.getValue(), 1e-6);
    }

    @Test
    @DisplayName("TogglePropertyWidget toggles boolean values")
    void toggleWidget() {
        TogglePropertyWidget toggle = new TogglePropertyWidget(false, "Enable Feature");
        assertFalse(toggle.getValue());

        AtomicReference<Boolean> changed = new AtomicReference<>();
        toggle.setOnChanged(changed::set);

        toggle.setValue(true);
        assertTrue(toggle.getValue());
        assertTrue(changed.get());
    }

    @Test
    @DisplayName("DropdownWidget manages options and selection")
    void dropdownWidget() {
        List<String> options = List.of("Option A", "Option B", "Option C");
        DropdownWidget<String> dropdown = new DropdownWidget<>(options, "Option A");

        assertEquals("Option A", dropdown.getValue());
        assertFalse(dropdown.isOpen());

        dropdown.setOpen(true);
        assertTrue(dropdown.isOpen());

        dropdown.setValue("Option C");
        assertEquals("Option C", dropdown.getValue());
    }

    @Test
    @DisplayName("ColorPickerWidget stores ARGB integers")
    void colorPickerWidget() {
        ColorPickerWidget picker = new ColorPickerWidget(0xFFE53935);
        assertEquals(0xFFE53935, picker.getValue());

        picker.setValue(0xFF43A047);
        assertEquals(0xFF43A047, picker.getValue());
    }

    @Test
    @DisplayName("Widgets clear focus on outside clicks")
    void widgetFocusManagement() {
        net.minecraft.client.gui.Click inside = new net.minecraft.client.gui.Click(50.0, 50.0, new net.minecraft.client.input.MouseInput(0, 0));
        net.minecraft.client.gui.Click outside = new net.minecraft.client.gui.Click(200.0, 200.0, new net.minecraft.client.input.MouseInput(0, 0));

        // StringPropertyWidget
        StringPropertyWidget strWidget = new StringPropertyWidget("hello");
        strWidget.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(strWidget.isFocused());
        strWidget.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(strWidget.isFocused());

        // NumericPropertyWidget
        NumericPropertyWidget numWidget = new NumericPropertyWidget(42.0);
        numWidget.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(numWidget.isFocused());
        numWidget.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(numWidget.isFocused());

        // Slider
        SliderPropertyWidget slider = new SliderPropertyWidget(0.5, 0.0, 1.0);
        slider.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(slider.isFocused());
        slider.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(slider.isFocused());

        // Toggle
        TogglePropertyWidget toggle = new TogglePropertyWidget(false, "Toggle");
        toggle.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(toggle.isFocused());
        toggle.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(toggle.isFocused());

        // Dropdown
        DropdownWidget<String> dropdown = new DropdownWidget<>(List.of("A", "B"), "A");
        dropdown.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(dropdown.isFocused());
        assertTrue(dropdown.isOpen());
        dropdown.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(dropdown.isFocused());
        assertFalse(dropdown.isOpen());

        // ColorPicker
        ColorPickerWidget picker = new ColorPickerWidget(0xFF000000);
        picker.mouseClicked(inside, 0, 40, 100, 20);
        assertTrue(picker.isFocused());
        assertTrue(picker.isOpen());
        picker.mouseClicked(outside, 0, 40, 100, 20);
        assertFalse(picker.isFocused());
        assertFalse(picker.isOpen());
    }
}
