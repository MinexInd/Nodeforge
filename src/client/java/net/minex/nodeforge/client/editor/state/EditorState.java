package net.minex.nodeforge.client.editor.state;

import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.PortDirection;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.client.editor.camera.BoundingBox;
import net.minex.nodeforge.client.editor.camera.Camera;
import net.minex.nodeforge.client.editor.camera.GridSnap;
import net.minex.nodeforge.client.editor.selection.SelectionModel;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

import java.util.*;

/**
 * Unified state model and coordinator for an active graph editor canvas.
 *
 * <p>Encapsulates the {@link Graph}, {@link Camera}, {@link SelectionModel},
 * active {@link InteractionState} gesture, {@link HoverState}, and {@link GridSnap}.
 */
public class EditorState {

    private final Graph graph;
    private final Camera camera;
    private final SelectionModel selectionModel;
    private InteractionState interactionState = InteractionState.Idle.INSTANCE;
    private HoverState hoverState = HoverState.NONE;
    private GridSnap gridSnap = GridSnap.DEFAULT;
    private boolean readOnly = false;

    /**
     * Creates an editor state for the given graph with default camera and selection models.
     *
     * @param graph the graph to edit, must not be null
     */
    public EditorState(Graph graph) {
        this(graph, new Camera(), new SelectionModel());
    }

    /**
     * Creates an editor state with explicit camera and selection models.
     */
    public EditorState(Graph graph, Camera camera, SelectionModel selectionModel) {
        this.graph = Objects.requireNonNull(graph, "Graph must not be null");
        this.camera = Objects.requireNonNull(camera, "Camera must not be null");
        this.selectionModel = Objects.requireNonNull(selectionModel, "SelectionModel must not be null");
    }

    // ========== Core Models ==========

    /** Returns the underlying graph being edited. */
    public Graph graph() {
        return graph;
    }

    /** Returns the 2D camera viewport model. */
    public Camera camera() {
        return camera;
    }

    /** Returns the selection model. */
    public SelectionModel selection() {
        return selectionModel;
    }

    /** Returns the current interaction gesture state. */
    public InteractionState interactionState() {
        return interactionState;
    }

    /** Sets the current interaction gesture state. */
    public void setInteractionState(InteractionState state) {
        this.interactionState = Objects.requireNonNull(state, "interactionState must not be null");
    }

    /** Returns the current hover target state. */
    public HoverState hoverState() {
        return hoverState;
    }

    /** Sets the current hover target state. */
    public void setHoverState(HoverState hoverState) {
        this.hoverState = Objects.requireNonNull(hoverState, "hoverState must not be null");
    }

    /** Returns the current grid snapping configuration. */
    public GridSnap gridSnap() {
        return gridSnap;
    }

    /** Sets the grid snapping configuration. */
    public void setGridSnap(GridSnap gridSnap) {
        this.gridSnap = Objects.requireNonNull(gridSnap, "gridSnap must not be null");
    }

    /** Returns {@code true} if the editor is in read-only mode. */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Sets whether the editor is in read-only mode. */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    // ========== Gesture Helpers ==========

    /** Begins a canvas panning gesture. */
    public void startPanning(double screenX, double screenY) {
        this.interactionState = new InteractionState.Panning(camera.pan(), screenX, screenY);
    }

    /** Updates camera pan during a panning gesture. */
    public void updatePanning(double screenX, double screenY) {
        if (interactionState instanceof InteractionState.Panning panState) {
            double deltaScreenX = screenX - panState.startScreenX();
            double deltaScreenY = screenY - panState.startScreenY();

            // Convert screen delta to world delta based on current zoom
            double deltaWorldX = deltaScreenX / camera.zoom();
            double deltaWorldY = deltaScreenY / camera.zoom();

            camera.setPan(panState.startPan().x() - deltaWorldX, panState.startPan().y() - deltaWorldY);
        }
    }

    /** Begins dragging a collection of selected nodes. */
    public void startDraggingNodes(Collection<NodeId> nodeIds, double startScreenX, double startScreenY) {
        if (readOnly || nodeIds == null || nodeIds.isEmpty()) return;

        Map<NodeId, Position> initialPositions = new HashMap<>();
        for (NodeId id : nodeIds) {
            Node node = graph.getNode(id);
            if (node != null) {
                initialPositions.put(id, node.position());
            }
        }
        if (!initialPositions.isEmpty()) {
            this.interactionState = new InteractionState.DraggingNodes(initialPositions, startScreenX, startScreenY);
        }
    }

    /** Updates positions of dragged nodes relative to drag origin and grid snap. */
    public void updateDraggingNodes(double currentScreenX, double currentScreenY) {
        if (readOnly) return;
        if (interactionState instanceof InteractionState.DraggingNodes drag) {
            double deltaScreenX = currentScreenX - drag.startScreenX();
            double deltaScreenY = currentScreenY - drag.startScreenY();

            double deltaWorldX = deltaScreenX / camera.zoom();
            double deltaWorldY = deltaScreenY / camera.zoom();

            for (Map.Entry<NodeId, Position> entry : drag.initialPositions().entrySet()) {
                NodeId id = entry.getKey();
                Position initialPos = entry.getValue();

                double targetX = initialPos.x() + deltaWorldX;
                double targetY = initialPos.y() + deltaWorldY;

                Position targetPos = new Position(targetX, targetY);
                Position finalPos = gridSnap.snap(targetPos);

                Node node = graph.getNode(id);
                if (node != null) {
                    node.setPosition(finalPos);
                }
            }
        }
    }

    /** Begins dragging a connection cable from a source port. */
    public void startConnecting(NodeId sourceNode, PortId sourcePort, PortDirection direction, PortType<?> portType, Position worldPos) {
        if (readOnly) return;
        this.interactionState = new InteractionState.ConnectingCable(sourceNode, sourcePort, direction, portType, worldPos);
    }

    /** Updates target world position of a dragged connection cable. */
    public void updateConnecting(Position currentWorldPos) {
        if (readOnly) return;
        if (interactionState instanceof InteractionState.ConnectingCable conn) {
            this.interactionState = new InteractionState.ConnectingCable(
                    conn.sourceNode(), conn.sourcePort(), conn.sourceDirection(), conn.portType(), currentWorldPos);
        }
    }

    /** Begins a marquee box selection gesture. */
    public void startBoxSelecting(double screenX, double screenY) {
        this.interactionState = new InteractionState.BoxSelecting(screenX, screenY, screenX, screenY);
    }

    /** Updates the current bounds of a marquee box selection. */
    public void updateBoxSelecting(double currentScreenX, double currentScreenY) {
        if (interactionState instanceof InteractionState.BoxSelecting box) {
            this.interactionState = new InteractionState.BoxSelecting(box.startScreenX(), box.startScreenY(), currentScreenX, currentScreenY);
        }
    }

    /** Resets the interaction state back to {@link InteractionState.Idle}. */
    public void finishGesture() {
        this.interactionState = InteractionState.Idle.INSTANCE;
    }

    // ========== Frame All & Centering ==========

    /**
     * Centers the camera viewport on all nodes in the graph and adjusts zoom to fit.
     *
     * @param viewportWidth  viewport width in pixels
     * @param viewportHeight viewport height in pixels
     * @param padding        screen pixel padding around bounding box
     */
    public void frameAll(double viewportWidth, double viewportHeight, double padding) {
        Collection<Node> nodes = graph.getNodes();
        if (nodes.isEmpty()) {
            camera.reset();
            return;
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Node node : nodes) {
            Position pos = node.position();
            double w = node.size().width();
            double h = node.size().height();

            double nodeRight = Double.isFinite(pos.x() + w) ? pos.x() + w : pos.x();
            double nodeBottom = Double.isFinite(pos.y() + h) ? pos.y() + h : pos.y();

            minX = Math.min(minX, pos.x());
            minY = Math.min(minY, pos.y());
            maxX = Math.max(maxX, nodeRight);
            maxY = Math.max(maxY, nodeBottom);
        }

        if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            camera.reset();
            return;
        }

        BoundingBox bounds = new BoundingBox(minX, minY, maxX, maxY);
        camera.setPan(bounds.center());

        // Calculate optimal zoom to fit graph within viewport with padding
        double availableW = Math.max(10.0, viewportWidth - padding * 2.0);
        double availableH = Math.max(10.0, viewportHeight - padding * 2.0);

        double zoomX = availableW / Math.max(1.0, bounds.width());
        double zoomY = availableH / Math.max(1.0, bounds.height());
        double fitZoom = Math.min(zoomX, zoomY);

        camera.setZoom(Math.min(1.0, fitZoom));
    }
}
