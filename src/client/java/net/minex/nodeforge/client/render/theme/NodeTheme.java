package net.minex.nodeforge.client.render.theme;

import net.minex.nodeforge.api.registry.NodeCategory;

import java.util.Locale;
import java.util.Objects;

/**
 * Styling and color theme tokens for the NodeForge graph canvas editor.
 *
 * <p>All colors are stored as 32-bit ARGB integers (e.g. {@code 0xFF1E1E24}).
 */
public record NodeTheme(
        int backgroundColor,
        int gridMinorColor,
        int gridMajorColor,
        int nodeBackgroundColor,
        int nodeHeaderColor,
        int nodeBorderColor,
        int nodeSelectedBorderColor,
        int nodeHoverBorderColor,
        int textColor,
        int textSecondaryColor,
        int selectionBoxFillColor,
        int selectionBoxBorderColor,
        int cableDefaultColor,
        int cableSelectedColor,
        int cableHoverColor,
        int cableExecutionColor,
        int socketBorderColor,

        // Extended theme tokens
        int commentBorderColor,
        int commentHeaderColor,
        int commentBodyColor,
        int menuBackgroundColor,
        int menuBorderColor,
        int menuHoverColor,
        int menuTextColor,
        int categoryActionColor,
        int categoryFlowColor,
        int categoryLogicColor,
        int categoryMathColor,
        int categoryEventColor,
        int categoryCustomColor,
        int shadowColor,
        boolean highContrast,
        float cableLineWidth
) {

    /**
     * Backward-compatible 17-parameter constructor supplying standard defaults for extended visual tokens.
     */
    public NodeTheme(
            int backgroundColor, int gridMinorColor, int gridMajorColor,
            int nodeBackgroundColor, int nodeHeaderColor, int nodeBorderColor,
            int nodeSelectedBorderColor, int nodeHoverBorderColor,
            int textColor, int textSecondaryColor,
            int selectionBoxFillColor, int selectionBoxBorderColor,
            int cableDefaultColor, int cableSelectedColor, int cableHoverColor,
            int cableExecutionColor, int socketBorderColor
    ) {
        this(
                backgroundColor, gridMinorColor, gridMajorColor,
                nodeBackgroundColor, nodeHeaderColor, nodeBorderColor,
                nodeSelectedBorderColor, nodeHoverBorderColor,
                textColor, textSecondaryColor,
                selectionBoxFillColor, selectionBoxBorderColor,
                cableDefaultColor, cableSelectedColor, cableHoverColor,
                cableExecutionColor, socketBorderColor,
                0x884A4E58, // commentBorderColor
                0xDD2A2D36, // commentHeaderColor
                0x22181A20, // commentBodyColor
                0xEE1E2129, // menuBackgroundColor
                0xFF3B3F4A, // menuBorderColor
                0xFF2A2E39, // menuHoverColor
                0xFFEEEEEE, // menuTextColor
                0xFFE53935, // categoryActionColor
                0xFFFFA000, // categoryFlowColor
                0xFF43A047, // categoryLogicColor
                0xFF1E88E5, // categoryMathColor
                0xFF8E24AA, // categoryEventColor
                0xFF546E7A, // categoryCustomColor
                0x55000000, // shadowColor
                false,      // highContrast
                2.0f        // cableLineWidth
        );
    }

    // ========== Built-in Standard Themes ==========

    /** Sleek modern dark mode theme (Default). */
    public static final NodeTheme DARK = new NodeTheme(
            0xFF18181F, // background
            0x18FFFFFF, // grid minor
            0x35FFFFFF, // grid major
            0xF022222B, // node body
            0xFF2B2B36, // node header
            0xFF3B3B4A, // node border
            0xFFFFB300, // node selected border (Gold)
            0xFF6C6C85, // node hover border
            0xFFFFFFFF, // text primary
            0xFFA0A0B0, // text secondary
            0x334A90E2, // selection box fill
            0xAA4A90E2, // selection box border
            0xFF8A8A9E, // cable default
            0xFFFFD54F, // cable selected
            0xFFFFFFFF, // cable hover
            0xFFEEEEEE, // cable execution
            0xFF121216, // socket border
            0x884A4E58, // commentBorderColor
            0xDD2A2D36, // commentHeaderColor
            0x22181A20, // commentBodyColor
            0xEE1E2129, // menuBackgroundColor
            0xFF3B3F4A, // menuBorderColor
            0xFF2A2E39, // menuHoverColor
            0xFFEEEEEE, // menuTextColor
            0xFFE53935, // categoryActionColor
            0xFFFFA000, // categoryFlowColor
            0xFF43A047, // categoryLogicColor
            0xFF1E88E5, // categoryMathColor
            0xFF8E24AA, // categoryEventColor
            0xFF546E7A, // categoryCustomColor
            0x55000000, // shadowColor
            false,      // highContrast
            2.0f        // cableLineWidth
    );

    /** Light theme. */
    public static final NodeTheme LIGHT = new NodeTheme(
            0xFFEBEBF0, // background
            0x20000000, // grid minor
            0x45000000, // grid major
            0xF5FFFFFF, // node body
            0xFFE0E0EB, // node header
            0xFFCCCCCC, // node border
            0xFF1976D2, // node selected border
            0xFF888899, // node hover border
            0xFF111115, // text primary
            0xFF555566, // text secondary
            0x331976D2, // selection box fill
            0xAA1976D2, // selection box border
            0xFF666677, // cable default
            0xFF1976D2, // cable selected
            0xFF000000, // cable hover
            0xFF333333, // cable execution
            0xFFFFFFFF, // socket border
            0x88AAAAAA, // commentBorderColor
            0xDDE0E0E8, // commentHeaderColor
            0x22CCCCCC, // commentBodyColor
            0xEEF8F9FA, // menuBackgroundColor
            0xFFCCCCCC, // menuBorderColor
            0xFFE8ECEF, // menuHoverColor
            0xFF111115, // menuTextColor
            0xFFD32F2F, // categoryActionColor
            0xFFF57C00, // categoryFlowColor
            0xFF388E3C, // categoryLogicColor
            0xFF1976D2, // categoryMathColor
            0xFF7B1FA2, // categoryEventColor
            0xFF607D8B, // categoryCustomColor
            0x33000000, // shadowColor
            false,      // highContrast
            2.0f        // cableLineWidth
    );

    /** Midnight deep navy theme. */
    public static final NodeTheme MIDNIGHT = new NodeTheme(
            0xFF0E131F, // background
            0x184A90E2, // grid minor
            0x354A90E2, // grid major
            0xF0151C2C, // node body
            0xFF1E283D, // node header
            0xFF2B3852, // node border
            0xFF00E5FF, // node selected border (Neon Cyan)
            0xFF4A90E2, // node hover border
            0xFFFFFFFF, // text primary
            0xFF8EA4C8, // text secondary
            0x3300E5FF, // selection box fill
            0xAA00E5FF, // selection box border
            0xFF6D84A8, // cable default
            0xFF00E5FF, // cable selected
            0xFFFFFFFF, // cable hover
            0xFFE0F7FA, // cable execution
            0xFF0B0E17, // socket border
            0x882B3852, // commentBorderColor
            0xDD1E283D, // commentHeaderColor
            0x220E131F, // commentBodyColor
            0xEE151C2C, // menuBackgroundColor
            0xFF2B3852, // menuBorderColor
            0xFF1E283D, // menuHoverColor
            0xFFE0F7FA, // menuTextColor
            0xFFE91E63, // categoryActionColor
            0xFFFF9800, // categoryFlowColor
            0xFF00E676, // categoryLogicColor
            0xFF00B0FF, // categoryMathColor
            0xFF9C27B0, // categoryEventColor
            0xFF78909C, // categoryCustomColor
            0x66000000, // shadowColor
            false,      // highContrast
            2.0f        // cableLineWidth
    );

    /** Vibrant Cyberpunk / Neon theme. */
    public static final NodeTheme CYBERPUNK = new NodeTheme(
            0xFF120C1F, // background
            0x20FF007F, // grid minor
            0x4000F0FF, // grid major
            0xF01D1333, // node body
            0xFF2A1B4A, // node header
            0xFF4A2D7D, // node border
            0xFF00F0FF, // node selected border (Cyber Cyan)
            0xFFFF007F, // node hover border (Neon Pink)
            0xFFFFFFFF, // text primary
            0xFFD1B3FF, // text secondary
            0x44FF007F, // selection box fill
            0xCCFF007F, // selection box border
            0xFFB072FF, // cable default
            0xFF00F0FF, // cable selected
            0xFFFFE600, // cable hover (Electric Yellow)
            0xFFFF007F, // cable execution
            0xFF0A0612, // socket border
            0x884A2D7D, // commentBorderColor
            0xDD2A1B4A, // commentHeaderColor
            0x22120C1F, // commentBodyColor
            0xEE1D1333, // menuBackgroundColor
            0xFF4A2D7D, // menuBorderColor
            0xFF2A1B4A, // menuHoverColor
            0xFF00F0FF, // menuTextColor
            0xFFFF0055, // categoryActionColor
            0xFFFF7700, // categoryFlowColor
            0xFF00FF66, // categoryLogicColor
            0xFF00E5FF, // categoryMathColor
            0xFFD500F9, // categoryEventColor
            0xFF8E8EA0, // categoryCustomColor
            0x77000000, // shadowColor
            false,      // highContrast
            2.2f        // cableLineWidth
    );

    /** High contrast theme for enhanced accessibility (WCAG AAA compliant). */
    public static final NodeTheme HIGH_CONTRAST = new NodeTheme(
            0xFF000000, // background
            0x35FFFFFF, // grid minor
            0x75FFFFFF, // grid major
            0xFF000000, // node body
            0xFF222222, // node header
            0xFFFFFFFF, // node border (Thick White)
            0xFFFFEE00, // node selected border (Vivid Yellow)
            0xFF00E5FF, // node hover border
            0xFFFFFFFF, // text primary
            0xFFEEEEEE, // text secondary
            0x55FFFF00, // selection box fill
            0xFFFFFF00, // selection box border
            0xFFFFFFFF, // cable default
            0xFFFFEE00, // cable selected
            0xFF00E5FF, // cable hover
            0xFFFFFFFF, // cable execution
            0xFFFFFFFF, // socket border
            0xFFFFFFFF, // commentBorderColor
            0xFF333333, // commentHeaderColor
            0x33FFFFFF, // commentBodyColor
            0xFF000000, // menuBackgroundColor
            0xFFFFFFFF, // menuBorderColor
            0xFF333333, // menuHoverColor
            0xFFFFFFFF, // menuTextColor
            0xFFFF2222, // categoryActionColor
            0xFFFF8800, // categoryFlowColor
            0xFF00FF00, // categoryLogicColor
            0xFF00AAFF, // categoryMathColor
            0xFFFF00FF, // categoryEventColor
            0xFFAAAAAA, // categoryCustomColor
            0x00000000, // shadowColor
            true,       // highContrast
            3.5f        // cableLineWidth (extra thick)
    );

    /** Minecraft vanilla aesthetic theme. */
    public static final NodeTheme MINECRAFT_DARK = new NodeTheme(
            0xFF1E1E1E, // background
            0x20555555, // grid minor
            0x40888888, // grid major
            0xEE2A2A2A, // node body
            0xFF383838, // node header
            0xFF4A4A4A, // node border
            0xFFFFAA00, // node selected border (Vanilla Gold)
            0xFF777777, // node hover border
            0xFFFFFFFF, // text primary
            0xFFAAAAAA, // text secondary
            0x44FFAA00, // selection box fill
            0xCCFFAA00, // selection box border
            0xFFAAAAAA, // cable default
            0xFFFFAA00, // cable selected
            0xFFFFFFFF, // cable hover
            0xFFFFFFFF, // cable execution
            0xFF101010, // socket border
            0x884A4A4A, // commentBorderColor
            0xDD383838, // commentHeaderColor
            0x221E1E1E, // commentBodyColor
            0xEE2A2A2A, // menuBackgroundColor
            0xFF4A4A4A, // menuBorderColor
            0xFF383838, // menuHoverColor
            0xFFFFAA00, // menuTextColor
            0xFFFF5555, // categoryActionColor
            0xFFFFAA00, // categoryFlowColor
            0xFF55FF55, // categoryLogicColor
            0xFF55FFFF, // categoryMathColor
            0xFFFF55FF, // categoryEventColor
            0xFFAAAAAA, // categoryCustomColor
            0x55000000, // shadowColor
            false,      // highContrast
            2.0f        // cableLineWidth
    );

    /** Returns the category header accent color for the specified {@link NodeCategory}. */
    public int getCategoryHeaderColor(NodeCategory category) {
        if (category == null) return nodeHeaderColor;
        String id = category.id();
        if (id.equals(NodeCategory.ACTION.id())) return categoryActionColor;
        if (id.equals(NodeCategory.FLOW_CONTROL.id())) return categoryFlowColor;
        if (id.equals(NodeCategory.LOGIC.id()) || id.equals(NodeCategory.CONDITION.id())) return categoryLogicColor;
        if (id.equals(NodeCategory.MATH.id()) || id.equals(NodeCategory.DATA.id())) return categoryMathColor;
        if (id.equals(NodeCategory.EVENT.id())) return categoryEventColor;
        return categoryCustomColor;
    }

    /** Returns the category header accent color for a category key string. */
    public int getCategoryHeaderColor(String categoryKey) {
        if (categoryKey == null || categoryKey.isBlank()) return nodeHeaderColor;
        String lower = categoryKey.toLowerCase(Locale.ROOT).trim();
        int colonIdx = lower.lastIndexOf(':');
        String path = (colonIdx != -1) ? lower.substring(colonIdx + 1) : lower;

        return switch (path) {
            case "action" -> categoryActionColor;
            case "flow", "flow_control" -> categoryFlowColor;
            case "logic", "condition" -> categoryLogicColor;
            case "math", "data" -> categoryMathColor;
            case "event" -> categoryEventColor;
            default -> categoryCustomColor;
        };
    }

    /** Creates a builder initialized with default dark theme tokens. */
    public static Builder builder() {
        return new Builder(DARK);
    }

    /** Builder for constructing custom {@link NodeTheme} configurations. */
    public static final class Builder {
        private int backgroundColor;
        private int gridMinorColor;
        private int gridMajorColor;
        private int nodeBackgroundColor;
        private int nodeHeaderColor;
        private int nodeBorderColor;
        private int nodeSelectedBorderColor;
        private int nodeHoverBorderColor;
        private int textColor;
        private int textSecondaryColor;
        private int selectionBoxFillColor;
        private int selectionBoxBorderColor;
        private int cableDefaultColor;
        private int cableSelectedColor;
        private int cableHoverColor;
        private int cableExecutionColor;
        private int socketBorderColor;
        private int commentBorderColor;
        private int commentHeaderColor;
        private int commentBodyColor;
        private int menuBackgroundColor;
        private int menuBorderColor;
        private int menuHoverColor;
        private int menuTextColor;
        private int categoryActionColor;
        private int categoryFlowColor;
        private int categoryLogicColor;
        private int categoryMathColor;
        private int categoryEventColor;
        private int categoryCustomColor;
        private int shadowColor;
        private boolean highContrast;
        private float cableLineWidth;

        public Builder(NodeTheme base) {
            Objects.requireNonNull(base, "base theme must not be null");
            this.backgroundColor = base.backgroundColor;
            this.gridMinorColor = base.gridMinorColor;
            this.gridMajorColor = base.gridMajorColor;
            this.nodeBackgroundColor = base.nodeBackgroundColor;
            this.nodeHeaderColor = base.nodeHeaderColor;
            this.nodeBorderColor = base.nodeBorderColor;
            this.nodeSelectedBorderColor = base.nodeSelectedBorderColor;
            this.nodeHoverBorderColor = base.nodeHoverBorderColor;
            this.textColor = base.textColor;
            this.textSecondaryColor = base.textSecondaryColor;
            this.selectionBoxFillColor = base.selectionBoxFillColor;
            this.selectionBoxBorderColor = base.selectionBoxBorderColor;
            this.cableDefaultColor = base.cableDefaultColor;
            this.cableSelectedColor = base.cableSelectedColor;
            this.cableHoverColor = base.cableHoverColor;
            this.cableExecutionColor = base.cableExecutionColor;
            this.socketBorderColor = base.socketBorderColor;
            this.commentBorderColor = base.commentBorderColor;
            this.commentHeaderColor = base.commentHeaderColor;
            this.commentBodyColor = base.commentBodyColor;
            this.menuBackgroundColor = base.menuBackgroundColor;
            this.menuBorderColor = base.menuBorderColor;
            this.menuHoverColor = base.menuHoverColor;
            this.menuTextColor = base.menuTextColor;
            this.categoryActionColor = base.categoryActionColor;
            this.categoryFlowColor = base.categoryFlowColor;
            this.categoryLogicColor = base.categoryLogicColor;
            this.categoryMathColor = base.categoryMathColor;
            this.categoryEventColor = base.categoryEventColor;
            this.categoryCustomColor = base.categoryCustomColor;
            this.shadowColor = base.shadowColor;
            this.highContrast = base.highContrast;
            this.cableLineWidth = base.cableLineWidth;
        }

        public Builder backgroundColor(int color) { this.backgroundColor = color; return this; }
        public Builder gridMinorColor(int color) { this.gridMinorColor = color; return this; }
        public Builder gridMajorColor(int color) { this.gridMajorColor = color; return this; }
        public Builder nodeBackgroundColor(int color) { this.nodeBackgroundColor = color; return this; }
        public Builder nodeHeaderColor(int color) { this.nodeHeaderColor = color; return this; }
        public Builder nodeBorderColor(int color) { this.nodeBorderColor = color; return this; }
        public Builder nodeSelectedBorderColor(int color) { this.nodeSelectedBorderColor = color; return this; }
        public Builder nodeHoverBorderColor(int color) { this.nodeHoverBorderColor = color; return this; }
        public Builder textColor(int color) { this.textColor = color; return this; }
        public Builder textSecondaryColor(int color) { this.textSecondaryColor = color; return this; }
        public Builder selectionBoxFillColor(int color) { this.selectionBoxFillColor = color; return this; }
        public Builder selectionBoxBorderColor(int color) { this.selectionBoxBorderColor = color; return this; }
        public Builder cableDefaultColor(int color) { this.cableDefaultColor = color; return this; }
        public Builder cableSelectedColor(int color) { this.cableSelectedColor = color; return this; }
        public Builder cableHoverColor(int color) { this.cableHoverColor = color; return this; }
        public Builder cableExecutionColor(int color) { this.cableExecutionColor = color; return this; }
        public Builder socketBorderColor(int color) { this.socketBorderColor = color; return this; }
        public Builder commentBorderColor(int color) { this.commentBorderColor = color; return this; }
        public Builder commentHeaderColor(int color) { this.commentHeaderColor = color; return this; }
        public Builder commentBodyColor(int color) { this.commentBodyColor = color; return this; }
        public Builder menuBackgroundColor(int color) { this.menuBackgroundColor = color; return this; }
        public Builder menuBorderColor(int color) { this.menuBorderColor = color; return this; }
        public Builder menuHoverColor(int color) { this.menuHoverColor = color; return this; }
        public Builder menuTextColor(int color) { this.menuTextColor = color; return this; }
        public Builder categoryActionColor(int color) { this.categoryActionColor = color; return this; }
        public Builder categoryFlowColor(int color) { this.categoryFlowColor = color; return this; }
        public Builder categoryLogicColor(int color) { this.categoryLogicColor = color; return this; }
        public Builder categoryMathColor(int color) { this.categoryMathColor = color; return this; }
        public Builder categoryEventColor(int color) { this.categoryEventColor = color; return this; }
        public Builder categoryCustomColor(int color) { this.categoryCustomColor = color; return this; }
        public Builder shadowColor(int color) { this.shadowColor = color; return this; }
        public Builder highContrast(boolean highContrast) { this.highContrast = highContrast; return this; }
        public Builder cableLineWidth(float width) { this.cableLineWidth = Math.max(1.0f, width); return this; }

        public NodeTheme build() {
            return new NodeTheme(
                    backgroundColor, gridMinorColor, gridMajorColor,
                    nodeBackgroundColor, nodeHeaderColor, nodeBorderColor,
                    nodeSelectedBorderColor, nodeHoverBorderColor,
                    textColor, textSecondaryColor,
                    selectionBoxFillColor, selectionBoxBorderColor,
                    cableDefaultColor, cableSelectedColor, cableHoverColor,
                    cableExecutionColor, socketBorderColor,
                    commentBorderColor, commentHeaderColor, commentBodyColor,
                    menuBackgroundColor, menuBorderColor, menuHoverColor, menuTextColor,
                    categoryActionColor, categoryFlowColor, categoryLogicColor,
                    categoryMathColor, categoryEventColor, categoryCustomColor,
                    shadowColor, highContrast, cableLineWidth
            );
        }
    }
}
