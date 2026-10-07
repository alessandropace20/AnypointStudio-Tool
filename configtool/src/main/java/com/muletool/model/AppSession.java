package com.muletool.model;

import java.util.*;

/**
 * Holds the complete session state: current mode, list of entries, undo/redo stacks.
 * All UI panels read from and write to this object via the AppController.
 */
public class AppSession {

    private ConfigMode           mode = ConfigMode.GLOBAL;
    private final List<ConfigEntry> entries = new ArrayList<>();

    // Undo / Redo stacks — each snapshot is a deep-copy of entries
    private final Deque<List<ConfigEntry>> undoStack = new ArrayDeque<>();
    private final Deque<List<ConfigEntry>> redoStack = new ArrayDeque<>();

    private static final int MAX_HISTORY = 50;

    // ── Mode ──────────────────────────────────────────────────────────────

    public ConfigMode getMode()              { return mode; }
    public void setMode(ConfigMode mode)     { this.mode = mode; }

    // ── Entries ───────────────────────────────────────────────────────────

    public List<ConfigEntry> getEntries()    { return Collections.unmodifiableList(entries); }

    public void addEntry(ConfigEntry entry) {
        pushUndoSnapshot();
        entries.add(entry);
    }

    public void removeEntry(ConfigEntry entry) {
        pushUndoSnapshot();
        entries.remove(entry);
    }

    public void replaceEntry(ConfigEntry old, ConfigEntry updated) {
        pushUndoSnapshot();
        int idx = entries.indexOf(old);
        if (idx >= 0) entries.set(idx, updated);
    }

    public void clearAll() {
        pushUndoSnapshot();
        entries.clear();
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────

    private void pushUndoSnapshot() {
        if (undoStack.size() >= MAX_HISTORY) undoStack.pollFirst();
        undoStack.push(deepCopy(entries));
        redoStack.clear();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (!canUndo()) return;
        redoStack.push(deepCopy(entries));
        restore(undoStack.pop());
    }

    public void redo() {
        if (!canRedo()) return;
        undoStack.push(deepCopy(entries));
        restore(redoStack.pop());
    }

    private void restore(List<ConfigEntry> snapshot) {
        entries.clear();
        entries.addAll(snapshot);
    }

    private List<ConfigEntry> deepCopy(List<ConfigEntry> src) {
        List<ConfigEntry> copy = new ArrayList<>();
        for (ConfigEntry e : src) copy.add(new ConfigEntry(e));
        return copy;
    }

    // ── XML Assembly ─────────────────────────────────────────────────────

    /**
     * Returns the assembled XML for all entries in the current session.
     * Suitable for file export.
     */
    public String buildFullXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n");
        sb.append("      xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\"\n");
        sb.append("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core\n");
        sb.append("          http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n\n");
        for (ConfigEntry entry : entries) {
            sb.append(entry.getDefinition().id).append("<!-- snippet placeholder -->\n\n");
        }
        sb.append("</mule>\n");
        return sb.toString();
    }
}
