package com.muletool.ui.components;

import com.muletool.model.ComponentDef;
import com.muletool.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom cell renderer for the component catalogue list (ConfigListPanel).
 * Renders each ComponentDef as a two-line row with a badge chip.
 */
public class ComponentRow extends JPanel implements ListCellRenderer<ComponentDef> {

    private final JLabel badgeLbl = new JLabel("", SwingConstants.CENTER);
    private final JLabel nameLbl  = new JLabel();
    private final JLabel descLbl  = new JLabel();

    public ComponentRow() {
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(6, 10, 6, 10));

        // Badge chip
        badgeLbl.setFont(Theme.FONT_BADGE);
        badgeLbl.setForeground(Color.WHITE);
        badgeLbl.setPreferredSize(new Dimension(38, 38));
        badgeLbl.setOpaque(true);
        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(badgeLbl, BorderLayout.WEST);

        // Text block
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 0));
        text.setOpaque(false);
        nameLbl.setFont(Theme.FONT_LABEL);
        descLbl.setFont(Theme.FONT_SMALL);
        text.add(nameLbl);
        text.add(descLbl);
        add(text, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ComponentDef> list,
                                                   ComponentDef def, int index,
                                                   boolean isSelected, boolean hasFocus) {
        nameLbl.setText(def.label);
        descLbl.setText(def.description);
        badgeLbl.setText(def.badge);

        // Chip colour by category
        Color chip = categoryColor(def.category);
        badgeLbl.setBackground(chip);

        if (isSelected) {
            setBackground(Theme.BG_ROW_SEL);
            nameLbl.setForeground(Color.WHITE);
            descLbl.setForeground(new Color(200, 220, 255));
        } else {
            setBackground(index % 2 == 0 ? Theme.BG_SIDEBAR : Theme.BG_PANEL);
            nameLbl.setForeground(Theme.TEXT_PRIMARY);
            descLbl.setForeground(Theme.TEXT_MUTED);
        }
        return this;
    }

    private Color categoryColor(ComponentDef.Category cat) {
        switch (cat) {
            case HTTP:     return new Color(0, 120, 215);
            case DATA:     return new Color(39, 174, 96);
            case SECURITY: return new Color(155, 89, 182);
            default:       return new Color(90, 100, 120);
        }
    }
}
