package net.minex.nodeforge.client.editor.menu;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * An individual entry in a floating {@link ContextMenu}.
 */
public final class MenuItem {

    private final String label;
    private final String shortcut;
    private final Runnable action;
    private final BooleanSupplier enabledSupplier;
    private final boolean separator;
    private final List<MenuItem> subItems;

    private MenuItem(String label, String shortcut, Runnable action, BooleanSupplier enabledSupplier,
                     boolean separator, List<MenuItem> subItems) {
        this.label = label != null ? label : "";
        this.shortcut = shortcut != null ? shortcut : "";
        this.action = action;
        this.enabledSupplier = enabledSupplier != null ? enabledSupplier : () -> true;
        this.separator = separator;
        this.subItems = subItems != null ? List.copyOf(subItems) : Collections.emptyList();
    }

    /** Creates an executable menu action. */
    public static MenuItem action(String label, Runnable action) {
        return new MenuItem(label, null, action, () -> true, false, null);
    }

    /** Creates an executable menu action with a keyboard shortcut hint. */
    public static MenuItem action(String label, String shortcut, Runnable action) {
        return new MenuItem(label, shortcut, action, () -> true, false, null);
    }

    /** Creates an executable menu action with an enabled condition. */
    public static MenuItem action(String label, BooleanSupplier enabledSupplier, Runnable action) {
        return new MenuItem(label, null, action, enabledSupplier, false, null);
    }

    /** Creates an executable menu action with shortcut and enabled condition. */
    public static MenuItem action(String label, String shortcut, BooleanSupplier enabledSupplier, Runnable action) {
        return new MenuItem(label, shortcut, action, enabledSupplier, false, null);
    }

    /** Creates a visual divider line separator. */
    public static MenuItem separator() {
        return new MenuItem("", null, null, () -> false, true, null);
    }

    /** Creates a cascading submenu. */
    public static MenuItem submenu(String label, List<MenuItem> subItems) {
        return new MenuItem(label, null, null, () -> true, false, subItems);
    }

    public String label() {
        return label;
    }

    public String shortcut() {
        return shortcut;
    }

    public Runnable action() {
        return action;
    }

    public boolean isEnabled() {
        return enabledSupplier.getAsBoolean();
    }

    public boolean isSeparator() {
        return separator;
    }

    public boolean hasSubItems() {
        return !subItems.isEmpty();
    }

    public List<MenuItem> subItems() {
        return subItems;
    }
}
