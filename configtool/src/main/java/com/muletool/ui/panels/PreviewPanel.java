package com.muletool.ui.panels;

import com.muletool.controller.AppController;
import com.muletool.generator.GraphicPreviewRenderer;
import com.muletool.model.ConfigEntry;
import com.muletool.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Right-side panel with two tabs:
 *  1. XML Preview  — live XML text for the selected entry (or full document)
 *  2. Graphic View — Anypoint-style card (single entry) or flow diagram (all entries)
 */
public class PreviewPanel extends JPanel implements AppController.ChangeListener {

    private final AppController ctrl;

    // XML tab
    private final JTextArea xmlArea   = new JTextArea();
    private final JCheckBox fullDocCb = new JCheckBox("Documento completo");

    // Graphic tab
    private final JPanel graphicHolder = new JPanel(new BorderLayout());

    // Tab switcher
    private final JTabbedPane tabs = new JTabbedPane();

    public PreviewPanel(AppController ctrl) {
        this.ctrl = ctrl;
        ctrl.addChangeListener(this);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.DIVIDER),
            new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel title = new JLabel("Anteprima");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_SECONDARY);
        header.add(title, BorderLayout.WEST);

        // Full-doc checkbox
        fullDocCb.setFont(Theme.FONT_SMALL);
        fullDocCb.setForeground(Theme.TEXT_SECONDARY);
        fullDocCb.setBackground(Theme.BG_PANEL);
        fullDocCb.setSelected(false);
        fullDocCb.addActionListener(e -> refreshXml());
        header.add(fullDocCb, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── XML Area ──────────────────────────────────────────────────────
        xmlArea.setEditable(false);
        xmlArea.setFont(Theme.FONT_MONO);
        xmlArea.setBackground(new Color(22, 26, 32));
        xmlArea.setForeground(new Color(180, 210, 255));
        xmlArea.setCaretColor(Theme.TEXT_PRIMARY);
        xmlArea.setLineWrap(false);
        xmlArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        xmlArea.setText("<!-- seleziona un componente per vedere l'anteprima XML -->");

        JScrollPane xmlScroll = new JScrollPane(xmlArea);
        xmlScroll.setBorder(BorderFactory.createEmptyBorder());
        xmlScroll.setBackground(new Color(22, 26, 32));
        xmlScroll.getViewport().setBackground(new Color(22, 26, 32));
        xmlScroll.getVerticalScrollBar().setUnitIncrement(14);

        // ── Graphic Panel ─────────────────────────────────────────────────
        graphicHolder.setBackground(Theme.BG_DARK);
        JScrollPane graphicScroll = new JScrollPane(graphicHolder);
        graphicScroll.setBorder(BorderFactory.createEmptyBorder());
        graphicScroll.getViewport().setBackground(Theme.BG_DARK);
        graphicScroll.getVerticalScrollBar().setUnitIncrement(14);

        // ── Tabs ──────────────────────────────────────────────────────────
        tabs.setBackground(Theme.BG_PANEL);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.FONT_LABEL);
        tabs.addTab("XML",      xmlScroll);
        tabs.addTab("Grafico",  graphicScroll);
        tabs.addChangeListener(e -> refreshGraphic());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Refresh helpers ───────────────────────────────────────────────────

    private void refreshXml() {
        ConfigEntry sel = ctrl.getSelectedEntry();
        if (fullDocCb.isSelected()) {
            xmlArea.setText(ctrl.buildFullXml());
        } else if (sel != null) {
            xmlArea.setText(ctrl.getXmlSnippet(sel));
        } else {
            xmlArea.setText("<!-- seleziona un componente per vedere l'anteprima XML -->");
        }
        xmlArea.setCaretPosition(0);
    }

    private void refreshGraphic() {
        graphicHolder.removeAll();
        ConfigEntry sel = ctrl.getSelectedEntry();
        JPanel card;
        if (sel != null) {
            // Single card view
            card = GraphicPreviewRenderer.buildCard(sel);
        } else if (!ctrl.getEntries().isEmpty()) {
            // Flow diagram of all entries
            card = GraphicPreviewRenderer.buildFlowDiagram(ctrl.getEntries());
        } else {
            card = GraphicPreviewRenderer.emptyCard();
        }
        graphicHolder.add(card, BorderLayout.NORTH);
        graphicHolder.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        graphicHolder.revalidate();
        graphicHolder.repaint();
    }

    // ── ChangeListener ────────────────────────────────────────────────────
    @Override public void onSessionChanged() {
        refreshXml();
        if (tabs.getSelectedIndex() == 1) refreshGraphic();
    }
}
