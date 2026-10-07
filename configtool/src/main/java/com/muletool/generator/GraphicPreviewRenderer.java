package com.muletool.generator;

import com.muletool.model.ConfigEntry;
import com.muletool.model.ConfigMode;
import com.muletool.model.FieldDef;
import com.muletool.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Map;

/**
 * Builds a JPanel that visually represents a ConfigEntry as an Anypoint-style card.
 *
 * The card shows:
 *  - coloured icon chip (namespace + icon text)
 *  - component title
 *  - key field-value pairs as label rows
 *  - a status chip (complete / incomplete)
 *
 * This class is stateless; call buildCard() for each render cycle.
 */
public class GraphicPreviewRenderer {

    /** Returns a fully rendered card panel for the given entry, or an empty panel if null. */
    public static JPanel buildCard(ConfigEntry entry) {
        if (entry == null) return emptyCard();

        GraphicLabel gl = GraphicLabel.of(entry.getDefinition().id);
        Map<String, String> vals = entry.getValues();

        // Outer card
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(0, 0));
        card.setBackground(Theme.BG_PANEL);
        card.setBorder(new CompoundBorder(
            new MatteBorder(1, 3, 1, 1, gl.borderColor),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // ── Header row ───────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);

        // Icon chip
        JLabel chip = new JLabel(gl.iconText, SwingConstants.CENTER);
        chip.setFont(Theme.FONT_BADGE);
        chip.setForeground(Color.WHITE);
        chip.setBackground(gl.chipColor);
        chip.setOpaque(true);
        chip.setPreferredSize(new Dimension(38, 22));
        chip.setBorder(BorderFactory.createLineBorder(gl.chipColor.darker(), 1));
        header.add(chip);

        // Namespace label
        if (!gl.namespace.isEmpty()) {
            JLabel ns = new JLabel(gl.namespace);
            ns.setFont(Theme.FONT_SMALL);
            ns.setForeground(Theme.TEXT_MUTED);
            header.add(ns);
        }

        // Title
        JLabel title = new JLabel(gl.title);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title);

        // Status chip
        boolean complete = entry.isComplete();
        JLabel status = new JLabel(complete ? "✔ Complete" : "⚠ Incomplete");
        status.setFont(Theme.FONT_SMALL);
        status.setForeground(complete ? new Color(39,174,96) : new Color(230,126,34));
        header.add(Box.createHorizontalStrut(8));
        header.add(status);

        card.add(header, BorderLayout.NORTH);

        // ── Fields grid ──────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 2, 0, 0));

        GridBagConstraints kc = new GridBagConstraints();
        kc.gridx = 0; kc.gridy = 0; kc.anchor = GridBagConstraints.NORTHWEST;
        kc.insets = new Insets(2, 0, 2, 12); kc.fill = GridBagConstraints.NONE;

        GridBagConstraints vc = new GridBagConstraints();
        vc.gridx = 1; vc.gridy = 0; vc.anchor = GridBagConstraints.NORTHWEST;
        vc.insets = new Insets(2, 0, 2, 0);
        vc.fill = GridBagConstraints.HORIZONTAL; vc.weightx = 1.0;

        int row = 0;
        for (FieldDef f : entry.getDefinition().fields) {
            String val = vals.getOrDefault(f.key, "").trim();
            if (f.type == FieldDef.FieldType.PASSWORD) val = val.isEmpty() ? "" : "••••••••";
            if (val.isEmpty() && !f.required) continue; // skip optional blank fields

            JLabel keyLbl = new JLabel(f.label + ":");
            keyLbl.setFont(Theme.FONT_SMALL);
            keyLbl.setForeground(Theme.TEXT_SECONDARY);

            String display = val.isEmpty() ? "(not set)" : val;
            // truncate long values for readability
            if (display.length() > 60) display = display.substring(0, 57) + "…";
            JLabel valLbl = new JLabel(display);
            valLbl.setFont(Theme.FONT_SMALL);
            valLbl.setForeground(val.isEmpty() ? Theme.TEXT_MUTED : Theme.TEXT_PRIMARY);

            kc.gridy = row; vc.gridy = row;
            grid.add(keyLbl, kc);
            grid.add(valLbl, vc);
            row++;
        }

        // Add a filler row to push content up
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 0; fc.gridy = row; fc.gridwidth = 2;
        fc.weighty = 1.0; fc.fill = GridBagConstraints.VERTICAL;
        grid.add(Box.createVerticalGlue(), fc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /** Builds a placeholder panel shown when no entry is selected. */
    public static JPanel emptyCard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_PANEL);
        JLabel lbl = new JLabel("Seleziona una configurazione", SwingConstants.CENTER);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds a flow diagram panel that chains all entries in the session visually.
     * Used by the graphic tab when mode == FLOW.
     */
    public static JPanel buildFlowDiagram(java.util.List<ConfigEntry> entries) {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(Theme.BG_DARK);
        outer.setBorder(new EmptyBorder(12, 20, 12, 20));

        if (entries.isEmpty()) {
            JLabel lbl = new JLabel("Nessuna activity configurata", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_LABEL);
            lbl.setForeground(Theme.TEXT_MUTED);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            outer.add(lbl);
            return outer;
        }

        for (int i = 0; i < entries.size(); i++) {
            outer.add(buildFlowNode(entries.get(i)));
            if (i < entries.size() - 1) {
                outer.add(buildArrow());
            }
        }
        outer.add(Box.createVerticalGlue());
        return outer;
    }

    private static JPanel buildFlowNode(ConfigEntry entry) {
        GraphicLabel gl = GraphicLabel.of(entry.getDefinition().id);
        JPanel node = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        node.setBackground(Theme.BG_PANEL);
        node.setBorder(new CompoundBorder(
            new MatteBorder(1, 3, 1, 1, gl.borderColor),
            new EmptyBorder(4, 8, 4, 8)
        ));
        node.setMaximumSize(new Dimension(500, 48));
        node.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel chip = new JLabel(gl.iconText, SwingConstants.CENTER);
        chip.setFont(Theme.FONT_BADGE);
        chip.setForeground(Color.WHITE);
        chip.setBackground(gl.chipColor);
        chip.setOpaque(true);
        chip.setPreferredSize(new Dimension(34, 18));

        JLabel name = new JLabel(entry.getDisplayLabel());
        name.setFont(Theme.FONT_LABEL);
        name.setForeground(Theme.TEXT_PRIMARY);

        node.add(chip);
        node.add(name);
        return node;
    }

    private static JLabel buildArrow() {
        JLabel arrow = new JLabel("↓", SwingConstants.CENTER);
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        arrow.setForeground(Theme.TEXT_MUTED);
        arrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        return arrow;
    }
}
