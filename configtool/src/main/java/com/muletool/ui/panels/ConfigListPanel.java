package com.muletool.ui.panels;

import com.muletool.controller.AppController;
import com.muletool.model.*;
import com.muletool.ui.Theme;
import com.muletool.ui.components.ComponentRow;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Top-left panel.
 * Shows the catalogue of available component types for the current mode.
 * Single-click → highlights; double-click → adds a new ConfigEntry to the session.
 */
public class ConfigListPanel extends JPanel implements AppController.ChangeListener {

    private final AppController ctrl;
    private final DefaultListModel<ComponentDef> listModel = new DefaultListModel<>();
    private final JList<ComponentDef>            list      = new JList<>(listModel);
    private final JLabel                         titleLbl  = new JLabel();

    public ConfigListPanel(AppController ctrl) {
        this.ctrl = ctrl;
        ctrl.addChangeListener(this);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_SIDEBAR);

        // ── Header ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 12, 8, 12));

        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);
        header.add(titleLbl, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── List ─────────────────────────────────────────────────────────
        list.setBackground(Theme.BG_SIDEBAR);
        list.setSelectionBackground(Theme.BG_ROW_SEL);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(52);
        list.setCellRenderer(new ComponentRow());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(BorderFactory.createEmptyBorder());

        // Double-click → add to session
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ComponentDef def = list.getSelectedValue();
                    if (def != null) ctrl.addEntry(def);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BG_SIDEBAR);
        scroll.getViewport().setBackground(Theme.BG_SIDEBAR);
        add(scroll, BorderLayout.CENTER);

        // ── Footer hint ───────────────────────────────────────────────────
        JLabel hint = new JLabel("  Doppio click per aggiungere");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 8, 6, 8));
        add(hint, BorderLayout.SOUTH);

        reload();
    }

    private void reload() {
        ConfigMode mode = ctrl.getMode();
        titleLbl.setText("Componenti " + mode.label);
        listModel.clear();
        List<ComponentDef> defs = mode == ConfigMode.GLOBAL
            ? GlobalComponents.getAll()
            : FlowComponents.getAll();
        defs.forEach(listModel::addElement);
    }

    @Override public void onSessionChanged() { reload(); }
}
