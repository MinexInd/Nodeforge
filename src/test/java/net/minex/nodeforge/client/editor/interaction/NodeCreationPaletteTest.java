package net.minex.nodeforge.client.editor.interaction;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.registry.NodeCategory;
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.api.registry.NodeDefinitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeCreationPalette & Filtering")
class NodeCreationPaletteTest {

    private NodeDefinitionRegistry registry;
    private NodeCreationPalette palette;

    @BeforeEach
    void setUp() {
        registry = new NodeDefinitionRegistry();
        palette = new NodeCreationPalette(registry);

        registry.register(NodeDefinition.builder("math:add")
                .displayName("Add Numbers")
                .category(NodeCategory.MATH)
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("sum", "Sum", BuiltinPortTypes.DOUBLE)
                .build());

        registry.register(NodeDefinition.builder("math:multiply")
                .displayName("Multiply")
                .category(NodeCategory.MATH)
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("prod", "Product", BuiltinPortTypes.DOUBLE)
                .build());

        registry.register(NodeDefinition.builder("combat:heal")
                .displayName("Heal Player")
                .description("Restores health points")
                .category(NodeCategory.ACTION)
                .inputPort("exec", "In", BuiltinPortTypes.EXECUTION)
                .build());
    }

    @Test
    @DisplayName("opens and closes palette state")
    void openClose() {
        assertFalse(palette.isOpen());

        palette.open(100, 200, new Position(50, 75));
        assertTrue(palette.isOpen());
        assertEquals(new Position(50, 75), palette.spawnWorldPos());

        palette.close();
        assertFalse(palette.isOpen());
    }

    @Test
    @DisplayName("filters definitions by name, id, category, and description")
    void searchFiltering() {
        assertEquals(3, palette.filteredDefinitions().size());

        palette.setSearchQuery("heal");
        List<NodeDefinition> healMatches = palette.filteredDefinitions();
        assertEquals(1, healMatches.size());
        assertEquals("combat:heal", healMatches.get(0).id().value());

        palette.setSearchQuery("math");
        List<NodeDefinition> mathMatches = palette.filteredDefinitions();
        assertEquals(2, mathMatches.size());

        palette.setSearchQuery("health points");
        assertEquals(1, palette.filteredDefinitions().size());
    }

    @Test
    @DisplayName("navigates selection with up and down cursor wrapping")
    void navigation() {
        palette.setSearchQuery("");
        assertSame(palette.filteredDefinitions().get(0), palette.getSelectedDefinition());

        palette.navigateDown();
        assertSame(palette.filteredDefinitions().get(1), palette.getSelectedDefinition());

        palette.navigateUp();
        assertSame(palette.filteredDefinitions().get(0), palette.getSelectedDefinition());

        palette.navigateUp(); // Wrap to end
        int lastIndex = palette.filteredDefinitions().size() - 1;
        assertSame(palette.filteredDefinitions().get(lastIndex), palette.getSelectedDefinition());
    }

    @Test
    @DisplayName("instantiates node at palette spawn position")
    void instantiateSelected() {
        palette.open(100, 100, new Position(300, 450));
        palette.setSearchQuery("heal");

        Node node = palette.instantiateSelected();
        assertNotNull(node);
        assertEquals("combat:heal", node.typeKey());
        assertEquals(new Position(300, 450), node.position());
    }
}
