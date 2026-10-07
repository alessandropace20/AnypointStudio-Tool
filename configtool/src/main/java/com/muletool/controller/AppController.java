package com.muletool.controller;

import com.muletool.generator.XmlGenerator;
import com.muletool.model.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Central application controller.
 *
 * Responsibilities:
 *  - owns the AppSession (single source of truth)
 *  - exposes coarse-grained operations that UI panels call (addEntry, removeEntry, etc.)
 *  - fires change notifications to registered listeners
 *  - handles File I/O and clipboard
 *
 * UI panels must NOT mutate the session directly; they call AppController methods.
 */
public class AppController {

    // ── State ──────────────────────────────────────────────────────────────
    private final AppSession session = new AppSession();
    private ConfigEntry      selectedEntry = null;

    // ── Listeners ─────────────────────────────────────────────────────────
    public interface ChangeListener { void onSessionChanged(); }
    private final List<ChangeListener> listeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) { listeners.add(l); }
    private void fire() { listeners.forEach(ChangeListener::onSessionChanged); }

    // ── Mode ───────────────────────────────────────────────────────────────
    public ConfigMode getMode()               { return session.getMode(); }
    public void setMode(ConfigMode m)         { session.setMode(m); selectedEntry = null; fire(); }

    // ── Entry CRUD ─────────────────────────────────────────────────────────
    public List<ConfigEntry> getEntries()     { return session.getEntries(); }

    public void addEntry(ComponentDef def) {
        ConfigEntry e = new ConfigEntry(def);
        session.addEntry(e);
        selectedEntry = e;
        fire();
    }

    public void removeEntry(ConfigEntry entry) {
        if (entry == null) return;
        session.removeEntry(entry);
        if (selectedEntry == entry) selectedEntry = null;
        fire();
    }

    public void updateEntryField(ConfigEntry entry, String key, String value) {
        entry.setValue(key, value);
        fire();
    }

    // ── Selection ─────────────────────────────────────────────────────────
    public ConfigEntry getSelectedEntry()        { return selectedEntry; }
    public void setSelectedEntry(ConfigEntry e)  { selectedEntry = e; fire(); }

    // ── Undo / Redo ────────────────────────────────────────────────────────
    public boolean canUndo()  { return session.canUndo(); }
    public boolean canRedo()  { return session.canRedo(); }
    public void undo()        { session.undo(); selectedEntry = null; fire(); }
    public void redo()        { session.redo(); selectedEntry = null; fire(); }

    // ── XML ────────────────────────────────────────────────────────────────
    /** Returns the XML snippet for a single entry. */
    public String getXmlSnippet(ConfigEntry entry) {
        return entry == null ? "" : XmlGenerator.generate(entry);
    }

    /** Returns the full XML document for all entries. */
    public String buildFullXml() {
        return XmlGenerator.buildDocument(session.getEntries());
    }

    /** Copies the full XML document to the system clipboard. */
    public void copyXmlToClipboard(Component parent) {
        String xml = buildFullXml();
        java.awt.datatransfer.StringSelection sel =
            new java.awt.datatransfer.StringSelection(xml);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(parent, "XML copiato negli appunti.",
            "Copia XML", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Copies only the selected snippet to clipboard. */
    public void copySnippetToClipboard(Component parent) {
        if (selectedEntry == null) return;
        String xml = getXmlSnippet(selectedEntry);
        java.awt.datatransfer.StringSelection sel =
            new java.awt.datatransfer.StringSelection(xml);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(parent, "Snippet copiato negli appunti.",
            "Copia Snippet", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── File I/O ───────────────────────────────────────────────────────────
    public void newSession(Component parent) {
        int r = JOptionPane.showConfirmDialog(parent,
            "Creare una nuova sessione? Le modifiche non salvate saranno perse.",
            "Nuova Sessione", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            session.clearAll();
            selectedEntry = null;
            fire();
        }
    }

    public void exportXml(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Esporta XML");
        fc.setSelectedFile(new File(session.getMode().defaultFileName));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                w.write(buildFullXml());
                JOptionPane.showMessageDialog(parent, "File esportato:\n" + f.getAbsolutePath(),
                    "Esporta XML", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Errore durante il salvataggio:\n" + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveSession(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salva Sessione (.mxs)");
        fc.setSelectedFile(new File("session.mxs"));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                // Serialize entries as simple field maps
                List<String[]> data = new ArrayList<>();
                for (ConfigEntry e : session.getEntries()) {
                    data.add(new String[]{
                        e.getDefinition().mode.name(),
                        e.getDefinition().id,
                        mapToString(e.getValues())
                    });
                }
                oos.writeObject(data);
                JOptionPane.showMessageDialog(parent, "Sessione salvata:\n" + f.getAbsolutePath(),
                    "Salva", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Errore:\n" + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void openSession(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Apri Sessione (.mxs)");
        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))) {
                List<String[]> data = (List<String[]>) ois.readObject();
                session.clearAll();
                for (String[] row : data) {
                    ConfigMode m = ConfigMode.valueOf(row[0]);
                    String id = row[1];
                    ComponentDef def = m == ConfigMode.GLOBAL
                        ? com.muletool.model.GlobalComponents.findById(id)
                        : com.muletool.model.FlowComponents.findById(id);
                    if (def == null) continue;
                    ConfigEntry e = new ConfigEntry(def);
                    stringToMap(row[2]).forEach(e::setValue);
                    session.addEntry(e);
                }
                selectedEntry = null;
                fire();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Errore apertura:\n" + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Serialization helpers ──────────────────────────────────────────────
    private String mapToString(java.util.Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(k.replace("=","\\=").replace(";","\\;"))
              .append("=")
              .append((v == null ? "" : v).replace("=","\\=").replace(";","\\;"))
              .append(";");
        });
        return sb.toString();
    }

    private java.util.Map<String, String> stringToMap(String s) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        String[] parts = s.split("(?<!\\\\);");
        for (String part : parts) {
            int eq = part.indexOf('=');
            if (eq < 0) continue;
            String k = part.substring(0, eq).replace("\\=","=").replace("\\;",";");
            String v = part.substring(eq + 1).replace("\\=","=").replace("\\;",";");
            m.put(k, v);
        }
        return m;
    }
}
