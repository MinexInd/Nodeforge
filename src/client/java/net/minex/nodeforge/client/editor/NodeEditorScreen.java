package net.minex.nodeforge.client.editor;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.camera.GridSnap;
import net.minex.nodeforge.client.editor.command.*;
import net.minex.nodeforge.client.editor.interaction.NodeCreationPalette;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.editor.state.HoverState;
import net.minex.nodeforge.client.editor.state.InteractionState;
import net.minex.nodeforge.client.render.BezierCurve;
import net.minex.nodeforge.client.render.GraphCanvasRenderer;
import net.minex.nodeforge.client.render.PortLayout;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;
import org.lwjgl.glfw.GLFW;

import net.minex.nodeforge.api.graph.CommentBox;
import net.minex.nodeforge.client.editor.grid.GridConfig;
import net.minex.nodeforge.client.editor.hud.GraphHudOverlay;
import net.minex.nodeforge.client.editor.menu.ContextMenu;
import net.minex.nodeforge.client.editor.menu.ContextMenuFactory;
import net.minex.nodeforge.client.editor.minimap.MinimapRenderer;
import net.minex.nodeforge.client.editor.widget.*;
import net.minex.nodeforge.client.render.overlay.GraphTooltipRenderer;

import java.util.*;

/**
 * Interactive Minecraft GUI Screen for editing NodeForge node graphs.
 */
public class NodeEditorScreen extends Screen {

    private static final int INSPECTOR_WIDTH = 150;

    private final EditorState editorState;
    private final CommandStack commandStack;
    private NodeTheme theme;
    private net.minex.nodeforge.client.render.theme.ThemeId currentThemeId = net.minex.nodeforge.client.render.theme.ThemeId.DARK;
    private final GraphCanvasRenderer canvasRenderer;
    private final NodeCreationPalette palette;

    private final ContextMenu contextMenu = new ContextMenu();
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();
    private final GraphHudOverlay hudOverlay = new GraphHudOverlay();
    private final GridConfig gridConfig;
    private final List<Node> clipboard = new ArrayList<>();

    private final Map<String, PropertyWidget<?>> inspectorWidgets = new LinkedHashMap<>();
    private NodeId inspectedNodeId = null;

    private double lastMouseX;
    private double lastMouseY;
    private long lastRenderTimeNanos = 0L;

    public NodeEditorScreen(Text title, EditorState editorState, CommandStack commandStack, NodeTheme theme) {
        super(title);
        this.editorState = Objects.requireNonNull(editorState, "editorState must not be null");
        this.commandStack = Objects.requireNonNull(commandStack, "commandStack must not be null");
        this.theme = Objects.requireNonNull(theme, "theme must not be null");
        this.canvasRenderer = new GraphCanvasRenderer();
        this.palette = new NodeCreationPalette();
        this.gridConfig = canvasRenderer.getGridRenderer().getConfig();
        this.gridConfig.setOnChange(cfg -> editorState.setGridSnap(new GridSnap(cfg.getSize(), cfg.isSnapEnabled())));
        this.editorState.setGridSnap(new GridSnap(gridConfig.getSize(), gridConfig.isSnapEnabled()));
    }

    public NodeEditorScreen(Text title, Graph graph) {
        this(title, new EditorState(graph), new CommandStack(), NodeTheme.DARK);
    }

    public EditorState getEditorState() {
        return editorState;
    }

    public CommandStack getCommandStack() {
        return commandStack;
    }

    public NodeTheme getTheme() {
        return theme;
    }

    public void setTheme(NodeTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme must not be null");
    }

    public void setTheme(net.minex.nodeforge.client.render.theme.ThemeId id) {
        if (id == null) return;
        NodeTheme newTheme = net.minex.nodeforge.client.render.theme.ThemeRegistry.getInstance().get(id);
        if (newTheme != null) {
            this.currentThemeId = id;
            this.theme = newTheme;
        }
    }

    public void cycleTheme() {
        this.currentThemeId = net.minex.nodeforge.client.render.theme.ThemeRegistry.getInstance().cycleNext(currentThemeId);
        setTheme(currentThemeId);
    }

    public net.minex.nodeforge.client.render.theme.ThemeId getCurrentThemeId() {
        return currentThemeId;
    }

    public NodeCreationPalette getPalette() {
        return palette;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public MinimapRenderer getMinimapRenderer() {
        return minimapRenderer;
    }

    public GraphHudOverlay getHudOverlay() {
        return hudOverlay;
    }

    public GridConfig getGridConfig() {
        return gridConfig;
    }

    // ========== Rendering ==========

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        // 0. Step active visual effects and particle simulations with real frame delta
        long now = System.nanoTime();
        double deltaSeconds = (lastRenderTimeNanos > 0L) ? (now - lastRenderTimeNanos) / 1_000_000_000.0 : 0.016;
        lastRenderTimeNanos = now;
        deltaSeconds = Math.clamp(deltaSeconds, 0.001, 0.1);
        net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().tick(deltaSeconds);

        // 1. Update hover targets under cursor
        if (!palette.isOpen() && !contextMenu.isOpen() && editorState.interactionState() instanceof InteractionState.Idle) {
            updateHoverState(mouseX, mouseY);
        }

        // 2. Render Graph Canvas (Grid, Comments, Cables, Nodes, Sockets, Marquee)
        canvasRenderer.render(context, textRenderer, editorState, theme, width, height);

        // 3. Render Node Creation Palette Popup if open
        if (palette.isOpen()) {
            palette.render(context, textRenderer, theme);
        }

        // 4. Render Property Inspector Panel if a node is selected
        renderPropertyInspector(context, mouseX, mouseY);

        // 5. Render Minimap
        minimapRenderer.render(context, textRenderer, editorState, theme, width, height, mouseX, mouseY);

        // 6. Render Graph HUD Diagnostics Overlay
        hudOverlay.render(context, textRenderer, editorState, gridConfig, theme, width, height);

        // 7. Render Top/Bottom Status Bar
        renderStatusBar(context);

        // 8. Render Floating Tooltip
        renderHoverTooltip(context, mouseX, mouseY);

        // 9. Render Context Menu (top layer)
        if (contextMenu.isOpen()) {
            contextMenu.render(context, textRenderer, theme, mouseX, mouseY, width, height);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private Optional<NodeId> primarySelectedNode() {
        return editorState.selection().selectedNodes().stream().findFirst();
    }

    private void renderPropertyInspector(DrawContext context, int mouseX, int mouseY) {
        Optional<NodeId> selectedId = primarySelectedNode();
        if (selectedId.isEmpty()) {
            inspectedNodeId = null;
            inspectorWidgets.clear();
            return;
        }

        Node node = editorState.graph().getNode(selectedId.get());
        if (node == null) return;
        updateInspectorWidgets(node);

        int panelX = width - INSPECTOR_WIDTH - 8;
        int panelY = 24;
        int panelH = Math.min(height - 52, Math.max(60, inspectorWidgets.size() * 32 + 24));

        // Panel background & border
        context.fill(panelX, panelY, panelX + INSPECTOR_WIDTH, panelY + panelH, 0xEE181A20);
        context.drawStrokedRectangle(panelX, panelY, INSPECTOR_WIDTH, panelH, theme.nodeBorderColor());

        // Header
        context.fill(panelX, panelY, panelX + INSPECTOR_WIDTH, panelY + 16, 0xFF22262E);
        context.drawText(textRenderer, "PROPERTIES", panelX + 6, panelY + 4, theme.textColor(), false);
        context.fill(panelX, panelY + 16, panelX + INSPECTOR_WIDTH, panelY + 17, theme.nodeBorderColor());

        // Render widgets
        int currentY = panelY + 22;
        for (Map.Entry<String, PropertyWidget<?>> entry : inspectorWidgets.entrySet()) {
            context.drawText(textRenderer, entry.getKey(), panelX + 6, currentY, theme.textSecondaryColor(), false);
            currentY += textRenderer.fontHeight + 2;

            PropertyWidget<?> widget = entry.getValue();
            int widgetH = widget.getPreferredHeight();
            widget.render(context, textRenderer, panelX + 6, currentY, INSPECTOR_WIDTH - 12, widgetH, mouseX, mouseY);
            currentY += widgetH + 6;
        }
    }

    private void updateInspectorWidgets(Node node) {
        if (node == null) {
            inspectorWidgets.clear();
            inspectedNodeId = null;
            return;
        }
        if (Objects.equals(inspectedNodeId, node.id())) {
            return;
        }
        inspectedNodeId = node.id();
        inspectorWidgets.clear();

        // 1. Title/Name widget
        StringPropertyWidget nameWidget = new StringPropertyWidget(node.displayName());
        nameWidget.setOnChanged(node::setDisplayName);
        inspectorWidgets.put("Display Name", nameWidget);

        // 2. Bound NodeDefinition properties (if registered)
        Optional.ofNullable(net.minex.nodeforge.api.registry.NodeDefinitionRegistry.getInstance().get(node.typeKey())).ifPresent(def -> {
            for (net.minex.nodeforge.api.registry.PropertyDefinition<?> propDef : def.properties().values()) {
                String key = propDef.key();
                String val = node.getMetadata(key);
                if (val == null) {
                    val = String.valueOf(propDef.defaultValue());
                }

                if (propDef.valueClass().isEnum()) {
                    List<String> constants = Arrays.stream(propDef.valueClass().getEnumConstants())
                            .map(Object::toString)
                            .toList();
                    DropdownWidget<String> dropdown = new DropdownWidget<>(constants, val);
                    dropdown.setOnChanged(choice -> node.setMetadata(key, choice));
                    inspectorWidgets.put(propDef.displayName(), dropdown);
                } else if (Boolean.class.isAssignableFrom(propDef.valueClass())) {
                    TogglePropertyWidget toggle = new TogglePropertyWidget(Boolean.parseBoolean(val), propDef.displayName());
                    toggle.setOnChanged(b -> node.setMetadata(key, String.valueOf(b)));
                    inspectorWidgets.put(propDef.displayName(), toggle);
                } else if (Number.class.isAssignableFrom(propDef.valueClass())) {
                    double numVal = 0.0;
                    try { numVal = Double.parseDouble(val); } catch (NumberFormatException ignored) {}
                    if (key.toLowerCase(Locale.ROOT).contains("slider") || key.toLowerCase(Locale.ROOT).contains("ratio")) {
                        SliderPropertyWidget slider = new SliderPropertyWidget(numVal, 0.0, 1.0);
                        slider.setOnChanged(d -> node.setMetadata(key, String.valueOf(d)));
                        inspectorWidgets.put(propDef.displayName(), slider);
                    } else {
                        NumericPropertyWidget numWidget = new NumericPropertyWidget(numVal);
                        numWidget.setOnChanged(d -> node.setMetadata(key, String.valueOf(d)));
                        inspectorWidgets.put(propDef.displayName(), numWidget);
                    }
                }
            }
        });

        // 3. Dynamic metadata widgets
        for (Map.Entry<String, String> entry : node.metadata().entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (inspectorWidgets.containsKey(key)) continue;

            if (key.equalsIgnoreCase("active") || val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
                TogglePropertyWidget toggle = new TogglePropertyWidget(Boolean.parseBoolean(val), key);
                toggle.setOnChanged(b -> node.setMetadata(key, String.valueOf(b)));
                inspectorWidgets.put(key, toggle);
            } else if (key.equalsIgnoreCase("color") || val.startsWith("#") || val.startsWith("0x")) {
                int parsedColor = 0xFF4A90E2;
                try {
                    parsedColor = (int) Long.parseLong(val.replace("#", "").replace("0x", ""), 16);
                } catch (NumberFormatException ignored) {}
                ColorPickerWidget colorWidget = new ColorPickerWidget(parsedColor);
                colorWidget.setOnChanged(c -> node.setMetadata(key, String.format(Locale.ROOT, "#%08X", c)));
                inspectorWidgets.put(key, colorWidget);
            } else if (val.contains(",") || key.toLowerCase(Locale.ROOT).contains("mode") || key.toLowerCase(Locale.ROOT).contains("choice")) {
                List<String> options = val.contains(",") ? Arrays.asList(val.split("\\s*,\\s*")) : List.of(val);
                DropdownWidget<String> dropdown = new DropdownWidget<>(options, options.get(0));
                dropdown.setOnChanged(c -> node.setMetadata(key, c));
                inspectorWidgets.put(key, dropdown);
            } else if (key.toLowerCase(Locale.ROOT).contains("slider") || key.toLowerCase(Locale.ROOT).contains("ratio") || key.toLowerCase(Locale.ROOT).contains("factor")) {
                double num = 0.5;
                try { num = Double.parseDouble(val); } catch (NumberFormatException ignored) {}
                SliderPropertyWidget slider = new SliderPropertyWidget(num, 0.0, 1.0);
                slider.setOnChanged(d -> node.setMetadata(key, String.valueOf(d)));
                inspectorWidgets.put(key, slider);
            } else {
                try {
                    double num = Double.parseDouble(val);
                    NumericPropertyWidget numWidget = new NumericPropertyWidget(num);
                    numWidget.setOnChanged(d -> node.setMetadata(key, String.valueOf(d)));
                    inspectorWidgets.put(key, numWidget);
                } catch (NumberFormatException e) {
                    StringPropertyWidget strWidget = new StringPropertyWidget(val);
                    strWidget.setOnChanged(s -> node.setMetadata(key, s));
                    inspectorWidgets.put(key, strWidget);
                }
            }
        }
    }

    private void renderHoverTooltip(DrawContext context, int mouseX, int mouseY) {
        if (palette.isOpen()) return;

        HoverState hover = editorState.hoverState();
        if (hover.hoveredPort() != null && hover.hoveredNode() != null) {
            Node node = editorState.graph().getNode(hover.hoveredNode());
            if (node != null) {
                Port port = node.getPort(hover.hoveredPort());
                if (port != null) {
                    List<String> lines = List.of(
                            port.name(),
                            "Type: " + port.typeKey(),
                            "Direction: " + port.direction().name()
                    );
                    GraphTooltipRenderer.renderTooltip(context, textRenderer, lines, mouseX, mouseY, width, height);
                }
            }
        } else if (hover.hoveredNode() != null) {
            Node node = editorState.graph().getNode(hover.hoveredNode());
            if (node != null) {
                String error = node.getMetadata("error");
                if (error != null) {
                    List<String> lines = List.of("Validation Status", error);
                    GraphTooltipRenderer.renderTooltip(context, textRenderer, lines, mouseX, mouseY, width, height);
                }
            }
        }
    }

    private void renderStatusBar(DrawContext context) {
        int barHeight = 20;
        int y = height - barHeight;
        context.fill(0, y, width, height, 0xDD111116);
        context.fill(0, y, width, y + 1, theme.nodeBorderColor());

        String stats = "Nodes: " + editorState.graph().nodeCount() +
                " | Cables: " + editorState.graph().connectionCount() +
                " | Comments: " + editorState.graph().commentBoxCount() +
                " | Zoom: " + (int) Math.round(editorState.camera().zoom() * 100) + "%" +
                (commandStack.canUndo() ? " | Undo: " + commandStack.undoDescription() : "") +
                (commandStack.canRedo() ? " | Redo: " + commandStack.redoDescription() : "");

        context.drawText(textRenderer, stats, 10, y + 6, theme.textSecondaryColor(), false);

        String shortcuts = "[R-Click] Menu | [T] Theme | [V] " + net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().getConfig().getModeName() +
                " | [Del] Del | [Ctrl+D] Dup | [C] Box | [M] Map | [G] Snap | [H] HUD";
        int shortcutWidth = textRenderer.getWidth(shortcuts);
        context.drawText(textRenderer, shortcuts, width - shortcutWidth - 10, y + 6, theme.textSecondaryColor(), false);
    }

    private void updateHoverState(double mouseX, double mouseY) {
        Camera camera = editorState.camera();
        Position worldPos = camera.screenToWorld(mouseX, mouseY, width, height);
        Graph graph = editorState.graph();

        // 1. Check port hover
        for (Node node : graph.getNodes()) {
            Optional<Port> hitPort = PortLayout.getPortAt(node, worldPos);
            if (hitPort.isPresent()) {
                editorState.setHoverState(HoverState.port(node.id(), hitPort.get().id()));
                return;
            }
        }

        // 2. Check node body hover
        for (Node node : graph.getNodes()) {
            BoundingBox box = BoundingBox.fromPositionAndSize(node.position(), node.size());
            if (box.contains(worldPos)) {
                editorState.setHoverState(HoverState.node(node.id()));
                return;
            }
        }

        // 3. Check connection hover
        for (Connection conn : graph.getConnections()) {
            Node src = graph.getNode(conn.fromNode());
            Node dst = graph.getNode(conn.toNode());
            if (src != null && dst != null) {
                Port pSrc = src.getPort(conn.fromPort());
                Port pDst = dst.getPort(conn.toPort());
                if (pSrc != null && pDst != null) {
                    Position start = PortLayout.getPortPosition(src, pSrc);
                    Position end = PortLayout.getPortPosition(dst, pDst);
                    BezierCurve curve = BezierCurve.fromEndpoints(start, pSrc.direction(), end, pDst.direction());
                    if (curve.distanceToPoint(worldPos) <= 6.0) {
                        editorState.setHoverState(HoverState.connection(conn.id()));
                        return;
                    }
                }
            }
        }

        // 4. Check comment box hover
        for (CommentBox box : graph.getCommentBoxes()) {
            if (box.contains(worldPos)) {
                editorState.setHoverState(HoverState.commentBox(box.id()));
                return;
            }
        }

        editorState.setHoverState(HoverState.NONE);
    }

    // ========== Mouse Event Handlers ==========

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // If context menu is open, handle context menu click first
        if (contextMenu.isOpen()) {
            if (contextMenu.mouseClicked(click, width, height)) {
                return true;
            }
        }

        // If minimap clicked, handle minimap pan
        if (minimapRenderer.mouseClicked(click, editorState, width, height)) {
            return true;
        }

        // If palette open, handle palette selection
        if (palette.isOpen()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                Node created = palette.instantiateSelected();
                if (created != null) {
                    commandStack.execute(new AddNodeCommand(editorState.graph(), created));
                }
                palette.close();
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                palette.close();
                return true;
            }
        }

        // If property inspector is open, check if clicked inside inspector panel
        if (primarySelectedNode().isPresent() && !inspectorWidgets.isEmpty()) {
            int panelX = width - INSPECTOR_WIDTH - 8;
            int panelY = 24;
            int panelH = Math.min(height - 52, Math.max(60, inspectorWidgets.size() * 32 + 24));
            if (mouseX >= panelX && mouseX <= panelX + INSPECTOR_WIDTH && mouseY >= panelY && mouseY <= panelY + panelH) {
                int currentY = panelY + 22;
                for (PropertyWidget<?> widget : inspectorWidgets.values()) {
                    currentY += textRenderer.fontHeight + 2;
                    int widgetH = widget.getPreferredHeight();
                    if (widget.mouseClicked(click, panelX + 6, currentY, INSPECTOR_WIDTH - 12, widgetH)) {
                        return true;
                    }
                    currentY += widgetH + 6;
                }
                return true;
            } else {
                for (PropertyWidget<?> widget : inspectorWidgets.values()) {
                    widget.setFocused(false);
                }
            }
        }

        Camera camera = editorState.camera();
        Position worldPos = camera.screenToWorld(mouseX, mouseY, width, height);
        Graph graph = editorState.graph();
        boolean shiftHeld = click.hasShift();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // 1. Check Port Click -> Start Cable Drag
            for (Node node : graph.getNodes()) {
                Optional<Port> hitPort = PortLayout.getPortAt(node, worldPos);
                if (hitPort.isPresent()) {
                    Port port = hitPort.get();
                    editorState.startConnecting(node.id(), port.id(), port.direction(), port.portType(), worldPos);
                    return true;
                }
            }

            // 2. Check Node Click -> Select / Drag
            for (Node node : graph.getNodes()) {
                BoundingBox box = BoundingBox.fromPositionAndSize(node.position(), node.size());
                if (box.contains(worldPos)) {
                    if (shiftHeld) {
                        editorState.selection().toggleNode(node.id());
                    } else {
                        if (!editorState.selection().isSelected(node.id())) {
                            editorState.selection().setSingleNode(node.id());
                        }
                    }
                    editorState.startDraggingNodes(editorState.selection().selectedNodes(), mouseX, mouseY);
                    return true;
                }
            }

            // 2.5 Check Comment Box Click -> Select Comment Box and drag enclosed nodes
            for (CommentBox box : graph.getCommentBoxes()) {
                if (box.isHeaderHit(worldPos) || box.contains(worldPos)) {
                    if (shiftHeld) {
                        editorState.selection().toggleCommentBox(box.id());
                    } else {
                        editorState.selection().setSingleCommentBox(box.id());
                    }
                    List<NodeId> enclosed = graph.getNodes().stream()
                            .filter(box::encloses)
                            .map(Node::id)
                            .toList();
                    if (!enclosed.isEmpty()) {
                        editorState.selection().setSelectedNodes(enclosed);
                        editorState.startDraggingNodes(enclosed, mouseX, mouseY);
                    }
                    return true;
                }
            }

            // 3. Check Connection Click -> Select
            for (Connection conn : graph.getConnections()) {
                Node src = graph.getNode(conn.fromNode());
                Node dst = graph.getNode(conn.toNode());
                if (src != null && dst != null) {
                    Port pSrc = src.getPort(conn.fromPort());
                    Port pDst = dst.getPort(conn.toPort());
                    if (pSrc != null && pDst != null) {
                        Position start = PortLayout.getPortPosition(src, pSrc);
                        Position end = PortLayout.getPortPosition(dst, pDst);
                        BezierCurve curve = BezierCurve.fromEndpoints(start, pSrc.direction(), end, pDst.direction());
                        if (curve.distanceToPoint(worldPos) <= 6.0) {
                            if (shiftHeld) {
                                editorState.selection().toggleConnection(conn.id());
                            } else {
                                editorState.selection().setSingleConnection(conn.id());
                            }
                            return true;
                        }
                    }
                }
            }

            // 4. Clicked on Empty Space -> Marquee Box Selection
            if (!shiftHeld) {
                editorState.selection().clearSelection();
            }
            editorState.startBoxSelecting(mouseX, mouseY);
            return true;

        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            // Middle click -> Pan
            editorState.startPanning(mouseX, mouseY);
            return true;

        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            // Right click -> Open Context Menu or cancel gesture
            if (editorState.interactionState() instanceof InteractionState.Idle) {
                HoverState hover = editorState.hoverState();
                if (hover.hoveredNode() != null) {
                    contextMenu.open(mouseX, mouseY, ContextMenuFactory.createNodeMenu(editorState, commandStack, hover.hoveredNode()));
                } else if (hover.hoveredConnection() != null) {
                    contextMenu.open(mouseX, mouseY, ContextMenuFactory.createConnectionMenu(editorState, commandStack, hover.hoveredConnection()));
                } else {
                    contextMenu.open(mouseX, mouseY, ContextMenuFactory.createCanvasMenu(editorState, commandStack, palette, mouseX, mouseY, worldPos, width, height, this::setTheme));
                }
            } else {
                editorState.finishGesture();
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();

        if (minimapRenderer.mouseDragged(click, editorState, width, height)) {
            return true;
        }

        InteractionState state = editorState.interactionState();

        if (state instanceof InteractionState.Panning) {
            editorState.updatePanning(mouseX, mouseY);
            return true;
        } else if (state instanceof InteractionState.DraggingNodes) {
            editorState.updateDraggingNodes(mouseX, mouseY);
            return true;
        } else if (state instanceof InteractionState.ConnectingCable) {
            Position worldPos = editorState.camera().screenToWorld(mouseX, mouseY, width, height);
            editorState.updateConnecting(worldPos);
            return true;
        } else if (state instanceof InteractionState.BoxSelecting) {
            editorState.updateBoxSelecting(mouseX, mouseY);
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        InteractionState state = editorState.interactionState();
        Graph graph = editorState.graph();

        if (state instanceof InteractionState.DraggingNodes drag) {
            // Check if nodes actually moved and commit MoveNodesCommand
            Map<NodeId, Position> finalPositions = new HashMap<>();
            boolean moved = false;
            for (Map.Entry<NodeId, Position> entry : drag.initialPositions().entrySet()) {
                Node node = graph.getNode(entry.getKey());
                if (node != null) {
                    finalPositions.put(node.id(), node.position());
                    if (!node.position().equals(entry.getValue())) {
                        moved = true;
                    }
                }
            }
            if (moved) {
                commandStack.pushExecuted(new MoveNodesCommand(graph, drag.initialPositions(), finalPositions));
            }
            editorState.finishGesture();
            return true;

        } else if (state instanceof InteractionState.ConnectingCable connecting) {
            // Check if dropped on a compatible input port
            Position worldPos = editorState.camera().screenToWorld(mouseX, mouseY, width, height);
            for (Node targetNode : graph.getNodes()) {
                Optional<Port> hitPort = PortLayout.getPortAt(targetNode, worldPos);
                if (hitPort.isPresent()) {
                    Port targetPort = hitPort.get();
                    if (targetPort.isInput()) {
                        NodeId fromNode = connecting.sourceNode();
                        PortId fromPort = connecting.sourcePort();
                        NodeId toNode = targetNode.id();
                        PortId toPort = targetPort.id();

                        try {
                            commandStack.execute(new ConnectCommand(graph, fromNode, fromPort, toNode, toPort));
                            Position sparkPos = PortLayout.getPortPosition(targetNode, targetPort);
                            int sparkColor = connecting.portType() != null ? connecting.portType().color() : theme.cableDefaultColor();
                            net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().spawnConnectionSpark(sparkPos, sparkColor);
                        } catch (Exception ignored) {
                            // Incompatible or invalid connection
                        }
                    }
                    break;
                }
            }
            editorState.finishGesture();
            return true;

        } else if (state instanceof InteractionState.BoxSelecting box) {
            // Select all nodes intersecting the box
            double minSX = Math.min(box.startScreenX(), box.currentScreenX());
            double maxSX = Math.max(box.startScreenX(), box.currentScreenX());
            double minSY = Math.min(box.startScreenY(), box.currentScreenY());
            double maxSY = Math.max(box.startScreenY(), box.currentScreenY());

            Camera camera = editorState.camera();
            Position worldTL = camera.screenToWorld(minSX, minSY, width, height);
            Position worldBR = camera.screenToWorld(maxSX, maxSY, width, height);
            BoundingBox selectBox = BoundingBox.fromCorners(worldTL, worldBR);

            List<NodeId> hitNodes = new ArrayList<>();
            for (Node node : graph.getNodes()) {
                BoundingBox nodeBox = BoundingBox.fromPositionAndSize(node.position(), node.size());
                if (selectBox.intersects(nodeBox)) {
                    hitNodes.add(node.id());
                }
            }
            editorState.selection().selectAllNodes(hitNodes);
            editorState.finishGesture();
            return true;

        } else if (state instanceof InteractionState.Panning) {
            editorState.finishGesture();
            return true;
        }

        minimapRenderer.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0.0) {
            double zoomFactor = verticalAmount > 0 ? 1.15 : 0.85;
            editorState.camera().zoomAt(mouseX, mouseY, zoomFactor, width, height);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ========== Keyboard Shortcuts ==========

    @Override
    public boolean keyPressed(KeyInput input) {
        // Forward key events to focused inspector widget first
        for (PropertyWidget<?> widget : inspectorWidgets.values()) {
            if (widget.isFocused() && widget.keyPressed(input)) {
                return true;
            }
        }

        int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (contextMenu.isOpen()) {
                contextMenu.close();
                return true;
            }
        }

        if (palette.isOpen()) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                palette.cycleCategory();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                palette.navigateUp();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                palette.navigateDown();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                Node created = palette.instantiateSelected();
                if (created != null) {
                    commandStack.execute(new AddNodeCommand(editorState.graph(), created));
                    net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().pulseNode(created.id(), theme.nodeSelectedBorderColor());
                }
                palette.close();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                palette.backspaceSearch();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                palette.close();
                return true;
            }
            return true;
        }

        boolean ctrl = input.hasCtrlOrCmd();
        boolean shift = input.hasShift();

        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            if (shift) {
                commandStack.redo();
            } else {
                commandStack.undo();
            }
            return true;
        } else if (ctrl && keyCode == GLFW.GLFW_KEY_Y) {
            commandStack.redo();
            return true;
        } else if (ctrl && keyCode == GLFW.GLFW_KEY_D) {
            duplicateSelectedNodes();
            return true;
        } else if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
            copySelectedNodes();
            return true;
        } else if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
            pasteNodes();
            return true;
        } else if (!ctrl && keyCode == GLFW.GLFW_KEY_C) {
            wrapSelectionInComment();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_M) {
            minimapRenderer.toggleVisible();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_G) {
            gridConfig.toggleSnap();
            editorState.setGridSnap(new net.minex.nodeforge.client.editor.camera.GridSnap(gridConfig.getSize(), gridConfig.isSnapEnabled()));
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_H || keyCode == GLFW.GLFW_KEY_F3) {
            hudOverlay.toggleVisible();
            return true;
        } else if (!ctrl && keyCode == GLFW.GLFW_KEY_T) {
            cycleTheme();
            return true;
        } else if (!ctrl && keyCode == GLFW.GLFW_KEY_V) {
            net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().getConfig().cycleMode();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!editorState.selection().isEmpty()) {
                commandStack.execute(new DeleteSelectionCommand(
                        editorState.graph(),
                        editorState.selection().selectedNodes(),
                        editorState.selection().selectedConnections()
                ));
                editorState.selection().clearSelection();
                return true;
            }
        } else if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
            List<NodeId> allIds = editorState.graph().getNodes().stream().map(Node::id).toList();
            editorState.selection().selectAllNodes(allIds);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_F) {
            editorState.frameAll(width, height, 50.0);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_TAB) {
            Position worldPos = editorState.camera().screenToWorld(lastMouseX, lastMouseY, width, height);
            palette.open(lastMouseX, lastMouseY, worldPos);
            return true;
        }

        return super.keyPressed(input);
    }

    private void duplicateSelectedNodes() {
        Set<NodeId> selected = editorState.selection().selectedNodes();
        if (selected.isEmpty()) return;

        List<NodeId> newIds = new ArrayList<>();
        for (NodeId id : selected) {
            Node node = editorState.graph().getNode(id);
            if (node != null) {
                Node clone = node.copy(NodeId.random(), node.position().offset(30.0, 30.0));
                commandStack.execute(new AddNodeCommand(editorState.graph(), clone));
                newIds.add(clone.id());
                net.minex.nodeforge.client.render.vfx.VfxManager.getInstance().pulseNode(clone.id(), theme.nodeSelectedBorderColor());
            }
        }
        editorState.selection().setSelectedNodes(newIds);
    }

    private void copySelectedNodes() {
        clipboard.clear();
        for (NodeId id : editorState.selection().selectedNodes()) {
            Node node = editorState.graph().getNode(id);
            if (node != null) {
                clipboard.add(node);
            }
        }
    }

    private void pasteNodes() {
        if (clipboard.isEmpty()) return;
        List<NodeId> pasted = new ArrayList<>();
        for (Node node : clipboard) {
            Node clone = node.copy(NodeId.random(), node.position().offset(40.0, 40.0));
            commandStack.execute(new AddNodeCommand(editorState.graph(), clone));
            pasted.add(clone.id());
        }
        editorState.selection().setSelectedNodes(pasted);
    }

    private void wrapSelectionInComment() {
        List<Node> selectedNodes = editorState.selection().selectedNodes().stream()
                .map(id -> editorState.graph().getNode(id))
                .filter(Objects::nonNull)
                .toList();
        String id = "comment_" + UUID.randomUUID().toString().substring(0, 8);
        CommentBox box = CommentBox.aroundNodes(id, "Group", selectedNodes, 20.0);
        editorState.graph().addCommentBox(box);
    }

    @Override
    public boolean charTyped(CharInput input) {
        for (PropertyWidget<?> widget : inspectorWidgets.values()) {
            if (widget.isFocused() && widget.charTyped(input)) {
                return true;
            }
        }
        if (palette.isOpen()) {
            palette.appendSearchChar((char) input.codepoint());
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
