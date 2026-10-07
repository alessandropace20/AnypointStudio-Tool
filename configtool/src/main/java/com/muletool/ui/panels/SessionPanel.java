package com.muletool.ui.panels;

import com.muletool.controller.AppController;
import com.muletool.model.ConfigEntry;
import com.muletool.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Small panel embedded at the bottom of the left ConfigFormPanel area
 * (or floatable). Shows the ordered list of ConfigEntries in the current session.
 *
 * Single-click  → sets selectedEntry in controller (highlights, no form change)
 * Double-click  → sets selectedEntry AND triggers form rendering
 */
public class SessionPanel extends JPanel implements AppController.ChangeListener {

    private final AppController ctrl;
    private final DefaultListModel<ConfigEntry> model = new DefaultListModel<>();
    private final JList<ConfigEntry>            list  = new JList<>(model);
    private final JLabel                        countLbl = new JLabel();

    public SessionPanel(AppController ctrl) {
        this.ctrl = ctrl;
        ctrl.addChangeListener(this);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_SIDEBAR);

        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setBackground(Theme.BG_SIDEBAR);
        header.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 1, 0, Theme.DIVIDER),
            new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel title = new JLabel("Sessione");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_SECONDARY);
        header.add(title, BorderLayout.WEST);

        countLbl.setFont(Theme.FONT_SMALL);
        countLbl.setForeground(Theme.TEXT_MUTED);
        header.add(countLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── List ──────────────────────────────────────────────────────────
        list.setBackground(Theme.BG_SIDEBAR);
        list.setSelectionBackground(Theme.BG_ROW_SEL);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(34);
        list.setCellRenderer(new SessionEntryRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(BorderFactory.createEmptyBorder());

        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                ConfigEntry sel = list.getSelectedValue();
                if (sel == null) return;
                if (e.getClickCount() == 2) {
                    ctrl.setSelectedEntry(sel);  // triggers form render via controller
                } else {
                    // single click: just set selection without triggering full form re-render
                    ctrl.setSelectedEntry(sel);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BG_SIDEBAR);
        scroll.getViewport().setBackground(Theme.BG_SIDEBAR);
        add(scroll, BorderLayout.CENTER);
    }

    @Override public void onSessionChanged() {
        model.clear();
        ctrl.getEntries().forEach(model::addElement);
        countLbl.setText(model.size() + " elementi");

        // Sync list selection with controller
        ConfigEntry sel = ctrl.getSelectedEntry();
        if (sel != null) {
            list.setSelectedValue(sel, true);
        } else {
            list.clearSelection();
        }
    }

    // ── Cell renderer ─────────────────────────────────────────────────────
    private static class SessionEntryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx,
                boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list, value, idx, isSelected, cellHasFocus);
            if (value instanceof ConfigEntry) {
                ConfigEntry e = (ConfigEntry) value;
                lbl.setText("  " + (idx + 1) + ".  " + e.getDisplayLabel());
                lbl.setFont(Theme.FONT_SMALL);
                boolean complete = e.isComplete();
                if (!isSelected) {
                    lbl.setBackground(Theme.BG_SIDEBAR);
                    lbl.setForeground(complete ? Theme.TEXT_PRIMARY : Theme.ACCENT_ORANGE);
                }
            }
            lbl.setBorder(new EmptyBorder(0, 4, 0, 4));
            return lbl;
        }
    }
}
