package net.minex.nodeforge.client.editor.selection;

import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages the current selection of nodes and connections in the graph editor.
 *
 * <p>Supports single-select, multi-select, toggle-select, and change event notifications.
 */
public class SelectionModel {

    private final Set<NodeId> selectedNodes = new LinkedHashSet<>();
    private final Set<ConnectionId> selectedConnections = new LinkedHashSet<>();
    private final Set<String> selectedCommentBoxes = new LinkedHashSet<>();
    private final List<Consumer<SelectionModel>> listeners = new ArrayList<>();

    /** Creates an empty selection model. */
    public SelectionModel() {
    }

    // ========== Node Selection ==========

    /** Selects a single node, adding it to the current selection. */
    public boolean selectNode(NodeId nodeId) {
        if (nodeId == null) return false;
        if (selectedNodes.add(nodeId)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Deselects a node. */
    public boolean deselectNode(NodeId nodeId) {
        if (nodeId == null) return false;
        if (selectedNodes.remove(nodeId)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Toggles the selection state of a node. */
    public void toggleNode(NodeId nodeId) {
        if (nodeId == null) return;
        if (selectedNodes.contains(nodeId)) {
            selectedNodes.remove(nodeId);
        } else {
            selectedNodes.add(nodeId);
        }
        notifyChanged();
    }

    /** Replaces the current selection with a single node. */
    public void setSingleNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        selectedNodes.clear();
        selectedConnections.clear();
        selectedNodes.add(nodeId);
        notifyChanged();
    }

    /** Replaces the selected nodes with the given collection. */
    public void setSelectedNodes(Collection<NodeId> nodes) {
        selectedNodes.clear();
        if (nodes != null) {
            for (NodeId id : nodes) {
                if (id != null) selectedNodes.add(id);
            }
        }
        notifyChanged();
    }

    /** Adds all given nodes to the selection. */
    public void selectAllNodes(Collection<NodeId> nodes) {
        if (nodes == null || nodes.isEmpty()) return;
        boolean changed = false;
        for (NodeId id : nodes) {
            if (id != null && selectedNodes.add(id)) {
                changed = true;
            }
        }
        if (changed) {
            notifyChanged();
        }
    }

    /** Returns {@code true} if the given node is selected. */
    public boolean isSelected(NodeId nodeId) {
        if (nodeId == null) return false;
        return selectedNodes.contains(nodeId);
    }

    /** Returns an unmodifiable set of all currently selected node IDs. */
    public Set<NodeId> selectedNodes() {
        return Collections.unmodifiableSet(selectedNodes);
    }

    /** Returns the number of selected nodes. */
    public int selectedNodeCount() {
        return selectedNodes.size();
    }

    // ========== Connection Selection ==========

    /** Selects a connection, adding it to the current selection. */
    public boolean selectConnection(ConnectionId connectionId) {
        if (connectionId == null) return false;
        if (selectedConnections.add(connectionId)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Deselects a connection. */
    public boolean deselectConnection(ConnectionId connectionId) {
        if (connectionId == null) return false;
        if (selectedConnections.remove(connectionId)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Toggles the selection state of a connection. */
    public void toggleConnection(ConnectionId connectionId) {
        if (connectionId == null) return;
        if (selectedConnections.contains(connectionId)) {
            selectedConnections.remove(connectionId);
        } else {
            selectedConnections.add(connectionId);
        }
        notifyChanged();
    }

    /** Replaces the current selection with a single connection. */
    public void setSingleConnection(ConnectionId connectionId) {
        Objects.requireNonNull(connectionId, "connectionId must not be null");
        selectedNodes.clear();
        selectedConnections.clear();
        selectedConnections.add(connectionId);
        notifyChanged();
    }

    /** Replaces the selected connections with the given collection. */
    public void setSelectedConnections(Collection<ConnectionId> connections) {
        selectedConnections.clear();
        if (connections != null) {
            for (ConnectionId id : connections) {
                if (id != null) selectedConnections.add(id);
            }
        }
        notifyChanged();
    }

    /** Returns {@code true} if the given connection is selected. */
    public boolean isSelected(ConnectionId connectionId) {
        if (connectionId == null) return false;
        return selectedConnections.contains(connectionId);
    }

    /** Returns an unmodifiable set of all currently selected connection IDs. */
    public Set<ConnectionId> selectedConnections() {
        return Collections.unmodifiableSet(selectedConnections);
    }

    /** Returns the number of selected connections. */
    public int selectedConnectionCount() {
        return selectedConnections.size();
    }

    // ========== Comment Box Selection ==========

    /** Selects a comment box by ID. */
    public boolean selectCommentBox(String id) {
        if (id == null) return false;
        if (selectedCommentBoxes.add(id)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Deselects a comment box by ID. */
    public boolean deselectCommentBox(String id) {
        if (id == null) return false;
        if (selectedCommentBoxes.remove(id)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    /** Toggles the selection state of a comment box. */
    public void toggleCommentBox(String id) {
        if (id == null) return;
        if (selectedCommentBoxes.contains(id)) {
            selectedCommentBoxes.remove(id);
        } else {
            selectedCommentBoxes.add(id);
        }
        notifyChanged();
    }

    /** Replaces the current selection with a single comment box. */
    public void setSingleCommentBox(String id) {
        Objects.requireNonNull(id, "id must not be null");
        selectedNodes.clear();
        selectedConnections.clear();
        selectedCommentBoxes.clear();
        selectedCommentBoxes.add(id);
        notifyChanged();
    }

    /** Returns {@code true} if the given comment box is selected. */
    public boolean isCommentBoxSelected(String id) {
        if (id == null) return false;
        return selectedCommentBoxes.contains(id);
    }

    /** Returns an unmodifiable set of all currently selected comment box IDs. */
    public Set<String> selectedCommentBoxes() {
        return Collections.unmodifiableSet(selectedCommentBoxes);
    }

    /** Returns the number of selected comment boxes. */
    public int selectedCommentBoxCount() {
        return selectedCommentBoxes.size();
    }

    // ========== General Selection Operations ==========

    /** Clears all selected nodes, connections, and comment boxes. */
    public void clearSelection() {
        if (!selectedNodes.isEmpty() || !selectedConnections.isEmpty() || !selectedCommentBoxes.isEmpty()) {
            selectedNodes.clear();
            selectedConnections.clear();
            selectedCommentBoxes.clear();
            notifyChanged();
        }
    }

    /** Returns {@code true} if nothing is currently selected. */
    public boolean isEmpty() {
        return selectedNodes.isEmpty() && selectedConnections.isEmpty() && selectedCommentBoxes.isEmpty();
    }

    /** Adds a listener invoked whenever selection changes. */
    public void addSelectionListener(Consumer<SelectionModel> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /** Removes a selection listener. */
    public void removeSelectionListener(Consumer<SelectionModel> listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        for (Consumer<SelectionModel> listener : listeners) {
            try {
                listener.accept(this);
            } catch (VirtualMachineError t) {
                throw t;
            } catch (Exception e) {
                // Isolate listener exceptions so other listeners and caller are not broken
            }
        }
    }
}
