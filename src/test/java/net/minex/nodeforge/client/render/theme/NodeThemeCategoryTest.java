package net.minex.nodeforge.client.render.theme;

import net.minex.nodeforge.api.registry.NodeCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NodeTheme Category & Extended Tokens")
class NodeThemeCategoryTest {

    @Test
    @DisplayName("resolves category header accent colors by NodeCategory")
    void categoryHeaderColors() {
        NodeTheme theme = NodeTheme.DARK;

        assertEquals(theme.categoryActionColor(), theme.getCategoryHeaderColor(NodeCategory.ACTION));
        assertEquals(theme.categoryFlowColor(), theme.getCategoryHeaderColor(NodeCategory.FLOW_CONTROL));
        assertEquals(theme.categoryLogicColor(), theme.getCategoryHeaderColor(NodeCategory.LOGIC));
        assertEquals(theme.categoryLogicColor(), theme.getCategoryHeaderColor(NodeCategory.CONDITION));
        assertEquals(theme.categoryMathColor(), theme.getCategoryHeaderColor(NodeCategory.MATH));
        assertEquals(theme.categoryMathColor(), theme.getCategoryHeaderColor(NodeCategory.DATA));
        assertEquals(theme.categoryEventColor(), theme.getCategoryHeaderColor(NodeCategory.EVENT));
        assertEquals(theme.categoryCustomColor(), theme.getCategoryHeaderColor(NodeCategory.MISC));
        assertEquals(theme.nodeHeaderColor(), theme.getCategoryHeaderColor((NodeCategory) null));
    }

    @Test
    @DisplayName("resolves category header accent colors by string key")
    void categoryStringLookup() {
        NodeTheme theme = NodeTheme.DARK;

        assertEquals(theme.categoryActionColor(), theme.getCategoryHeaderColor("action"));
        assertEquals(theme.categoryActionColor(), theme.getCategoryHeaderColor("nodeforge:action"));
        assertEquals(theme.categoryFlowColor(), theme.getCategoryHeaderColor("flow_control"));
        assertEquals(theme.categoryFlowColor(), theme.getCategoryHeaderColor("nodeforge:flow_control"));
        assertEquals(theme.categoryLogicColor(), theme.getCategoryHeaderColor("logic"));
        assertEquals(theme.categoryMathColor(), theme.getCategoryHeaderColor("Math"));
        assertEquals(theme.categoryEventColor(), theme.getCategoryHeaderColor("EVENT"));

        // Verify zero false positives for words containing category substrings
        assertEquals(theme.categoryCustomColor(), theme.getCategoryHeaderColor("lifecycle"));
        assertEquals(theme.categoryCustomColor(), theme.getCategoryHeaderColor("factory"));
        assertEquals(theme.categoryCustomColor(), theme.getCategoryHeaderColor("actionable"));
        assertEquals(theme.categoryCustomColor(), theme.getCategoryHeaderColor("unknown_custom"));
        assertEquals(theme.nodeHeaderColor(), theme.getCategoryHeaderColor((String) null));
        assertEquals(theme.nodeHeaderColor(), theme.getCategoryHeaderColor("   "));
    }

    @Test
    @DisplayName("builder customizes extended tokens and presets")
    void builderCustomization() {
        NodeTheme custom = NodeTheme.builder()
                .commentBorderColor(0xFF112233)
                .menuBackgroundColor(0xFF445566)
                .categoryActionColor(0xFFFF0000)
                .cableLineWidth(3.0f)
                .highContrast(true)
                .build();

        assertEquals(0xFF112233, custom.commentBorderColor());
        assertEquals(0xFF445566, custom.menuBackgroundColor());
        assertEquals(0xFFFF0000, custom.categoryActionColor());
        assertEquals(3.0f, custom.cableLineWidth());
        assertTrue(custom.highContrast());
    }

    @Test
    @DisplayName("high contrast preset enforces accessibility standards")
    void highContrastPreset() {
        NodeTheme hc = NodeTheme.HIGH_CONTRAST;
        assertTrue(hc.highContrast());
        assertTrue(hc.cableLineWidth() >= 3.0f);
        assertEquals(0xFF000000, hc.backgroundColor());
        assertEquals(0xFFFFFFFF, hc.nodeBorderColor());
    }

    @Test
    @DisplayName("backward compatibility constructor preserves default extended tokens")
    void backwardCompatibilityConstructor() {
        NodeTheme legacy = new NodeTheme(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
        );

        assertEquals(0, legacy.backgroundColor());
        assertEquals(16, legacy.socketBorderColor());
        assertFalse(legacy.highContrast());
        assertEquals(2.0f, legacy.cableLineWidth());
        assertNotEquals(0, legacy.commentHeaderColor());
        assertNotEquals(0, legacy.menuBackgroundColor());
    }
}
