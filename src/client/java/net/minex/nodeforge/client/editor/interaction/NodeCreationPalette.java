package net.minex.nodeforge.client.editor.interaction;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import net.minex.nodeforge.client.render.theme.NodeTheme;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Searchable popup menu / palette for creating new nodes in the editor canvas.
 */
public class NodeCreationPalette {

    public static final int PALETTE_WIDTH = 200;
    public static final int ITEM_HEIGHT = 18;
    public static final int MAX_VISIBLE_ITEMS = 8;
    public static final int HEADER_HEIGHT = 24;

    private final NodeDefinitionRegistry registry;
    private boolean open = false;
    private String searchQuery = "";
    private int selectedIndex = 0;
    private double screenX;
    private double screenY;
    private Position spawnWorldPos = Position.ZERO;

    public NodeCreationPalette(NodeDefinitionRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public NodeCreationPalette() {
        this(NodeDefinitionRegistry.getInstance());
    }

    /** Opens the palette at the given screen and world coordinates. */
    public void open(double screenX, double screenY, Position spawnWorldPos) {
        this.open = true;
        this.screenX = screenX;
        this.screenY = screenY;
        this.spawnWorldPos = Objects.requireNonNull(spawnWorldPos, "spawnWorldPos must not be null");
        this.searchQuery = "";
        this.selectedIndex = 0;
    }

    /** Closes the palette. */
    public void close() {
        this.open = false;
        this.searchQuery = "";
        this.selectedIndex = 0;
    }

    /** Returns {@code true} if the palette is currently open. */
    public boolean isOpen() {
        return open;
    }

    /** Sets the current search filter query. */
    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query;
        this.selectedIndex = 0;
    }

    /** Appends a character to the search query. */
    public void appendSearchChar(char c) {
        this.searchQuery += c;
        this.selectedIndex = 0;
    }

    /** Removes the last character from the search query. */
    public void backspaceSearch() {
        if (!searchQuery.isEmpty()) {
            this.searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            this.selectedIndex = 0;
        }
    }

    /** Returns the current search query. */
    public String searchQuery() {
        return searchQuery;
    }

    private String selectedCategory = null;

    /** Sets the category filter, or {@code null} to show all categories. */
    public void setSelectedCategory(String category) {
        this.selectedCategory = (category == null || category.equalsIgnoreCase("all")) ? null : category;
        this.selectedIndex = 0;
    }

    /** Returns the currently active category filter, or {@code null} for all. */
    public String getSelectedCategory() {
        return selectedCategory;
    }

    /** Cycles through available categories. */
    public void cycleCategory() {
        List<String> categories = registry.allDefinitions().stream()
                .map(d -> d.category().displayName())
                .distinct()
                .sorted()
                .toList();
        if (categories.isEmpty()) return;

        if (selectedCategory == null) {
            selectedCategory = categories.get(0);
        } else {
            int idx = categories.indexOf(selectedCategory);
            if (idx == -1 || idx == categories.size() - 1) {
                selectedCategory = null; // back to ALL
            } else {
                selectedCategory = categories.get(idx + 1);
            }
        }
        this.selectedIndex = 0;
    }

    /** Returns all node definitions matching the current search query and category filter. */
    public List<NodeDefinition> filteredDefinitions() {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        return registry.allDefinitions().stream()
                .filter(def -> selectedCategory == null || def.category().displayName().equalsIgnoreCase(selectedCategory))
                .filter(def -> query.isEmpty()
                        || def.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || def.id().value().toLowerCase(Locale.ROOT).contains(query)
                        || def.description().toLowerCase(Locale.ROOT).contains(query)
                        || def.category().displayName().toLowerCase(Locale.ROOT).contains(query)
                        || def.inputPorts().stream().anyMatch(p -> p.portType().id().value().toLowerCase(Locale.ROOT).contains(query))
                        || def.outputPorts().stream().anyMatch(p -> p.portType().id().value().toLowerCase(Locale.ROOT).contains(query)))
                .sorted(Comparator.comparing((NodeDefinition d) -> d.category().order())
                        .thenComparing(NodeDefinition::displayName))
                .collect(Collectors.toList());
    }

    /** Moves the selection cursor up. */
    public void navigateUp() {
        List<NodeDefinition> items = filteredDefinitions();
        if (items.isEmpty()) return;
        selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
    }

    /** Moves the selection cursor down. */
    public void navigateDown() {
        List<NodeDefinition> items = filteredDefinitions();
        if (items.isEmpty()) return;
        selectedIndex = (selectedIndex + 1) % items.size();
    }

    /** Returns the currently selected definition, or {@code null} if none. */
    public NodeDefinition getSelectedDefinition() {
        List<NodeDefinition> items = filteredDefinitions();
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            return null;
        }
        return items.get(selectedIndex);
    }

    /**
     * Instantiates a new {@link Node} from the selected definition at the palette's spawn position.
     *
     * @return the new node, or {@code null} if nothing selected
     */
    public Node instantiateSelected() {
        NodeDefinition def = getSelectedDefinition();
        if (def == null) return null;
        return def.createNode(spawnWorldPos);
    }

    /** Returns the world coordinates where spawned nodes will be placed. */
    public Position spawnWorldPos() {
        return spawnWorldPos;
    }

    /**
     * Renders the node creation palette overlay.
     */
    public void render(DrawContext context, TextRenderer textRenderer, NodeTheme theme) {
        if (!open) return;
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(theme, "theme must not be null");

        List<NodeDefinition> items = filteredDefinitions();
        int visibleCount = Math.min(MAX_VISIBLE_ITEMS, Math.max(1, items.size()));
        int totalHeight = HEADER_HEIGHT + visibleCount * ITEM_HEIGHT + 4;

        int px = (int) Math.round(screenX);
        int py = (int) Math.round(screenY);

        // 1. Draw outer border and background
        context.fill(px - 1, py - 1, px + PALETTE_WIDTH + 1, py + totalHeight + 1, theme.nodeBorderColor());
        context.fill(px, py, px + PALETTE_WIDTH, py + totalHeight, theme.nodeBackgroundColor());

        // 2. Draw search bar header
        context.fill(px, py, px + PALETTE_WIDTH, py + HEADER_HEIGHT, theme.nodeHeaderColor());
        if (textRenderer != null) {
            String searchText = searchQuery.isEmpty() ? "Search nodes..." : searchQuery + "_";
            int searchColor = searchQuery.isEmpty() ? theme.textSecondaryColor() : theme.textColor();
            context.drawText(textRenderer, searchText, px + 8, py + 8, searchColor, false);
        }

        // 3. Draw items list
        int itemY = py + HEADER_HEIGHT + 2;
        if (items.isEmpty()) {
            if (textRenderer != null) {
                context.drawText(textRenderer, "No matching nodes", px + 8, itemY + 4, theme.textSecondaryColor(), false);
            }
        } else {
            for (int i = 0; i < Math.min(MAX_VISIBLE_ITEMS, items.size()); i++) {
                NodeDefinition def = items.get(i);
                boolean isSelected = i == selectedIndex;

                if (isSelected) {
                    context.fill(px + 2, itemY, px + PALETTE_WIDTH - 2, itemY + ITEM_HEIGHT, theme.selectionBoxFillColor());
                }

                if (textRenderer != null) {
                    int textColor = isSelected ? theme.nodeSelectedBorderColor() : theme.textColor();
                    context.drawText(textRenderer, def.displayName(), px + 8, itemY + 4, textColor, false);

                    String catName = def.category().displayName();
                    int catWidth = textRenderer.getWidth(catName);
                    context.drawText(textRenderer, catName, px + PALETTE_WIDTH - catWidth - 8, itemY + 4, theme.textSecondaryColor(), false);
                }

                itemY += ITEM_HEIGHT;
            }
        }
    }
}
