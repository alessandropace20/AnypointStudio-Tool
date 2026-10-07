package com.muletool.ui.panels;

import com.muletool.controller.AppController;
import com.muletool.model.*;
import com.muletool.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bottom-left panel.
 * Renders the form fields for the currently selected ConfigEntry.
 * Changes propagate to the controller in real-time (live preview).
 */
public class ConfigFormPanel extends JPanel implements AppController.ChangeListener {

    private final AppController ctrl;

    // Widgets keyed by field key
    private final Map<String, JComponent> widgets = new LinkedHashMap<>();

    private final JPanel   formArea   = new JPanel(new GridBagLayout());
    private final JLabel   titleLbl   = new JLabel("Pannello Configurazione");
    private final JButton  removeBtn  = new JButton("Rimuovi");
    private final JButton  copyBtn    = new JButton("Copia XML");
    private final JScrollPane scroll;

    private ConfigEntry currentEntry = null;
    private boolean     suppressFire = false;

    public ConfigFormPanel(AppController ctrl) {
        this.ctrl = ctrl;
        ctrl.addChangeListener(this);
        formArea.setBackground(Theme.BG_PANEL);
        scroll = new JScrollPane(formArea);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);

        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 1, 0, Theme.DIVIDER),
            new EmptyBorder(8, 12, 8, 12)
        ));

        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);
        header.add(titleLbl, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        styleSmallBtn(copyBtn,   Theme.ACCENT_BLUE);
        styleSmallBtn(removeBtn, new Color(192, 57, 43));
        copyBtn.addActionListener(e -> ctrl.copySnippetToClipboard(this));
        removeBtn.addActionListener(e -> { if (currentEntry != null) ctrl.removeEntry(currentEntry); });
        btnPanel.add(copyBtn);
        btnPanel.add(removeBtn);
        header.add(btnPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Form ──────────────────────────────────────────────────────────
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BG_PANEL);
        scroll.getViewport().setBackground(Theme.BG_PANEL);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        showPlaceholder();
    }

    // ── Render form for an entry ──────────────────────────────────────────
    private void renderForm(ConfigEntry entry) {
        suppressFire = true;
        widgets.clear();
        formArea.removeAll();
        formArea.setBackground(Theme.BG_PANEL);

        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = 0;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(6, 14, 2, 8);
        lc.fill = GridBagConstraints.NONE;

        GridBagConstraints wc = new GridBagConstraints();
        wc.gridx = 1; wc.gridy = 0;
        wc.anchor = GridBagConstraints.NORTHWEST;
        wc.insets = new Insets(6, 0, 2, 14);
        wc.fill = GridBagConstraints.HORIZONTAL;
        wc.weightx = 1.0;

        int row = 0;
        for (FieldDef f : entry.getDefinition().fields) {
            lc.gridy = row; wc.gridy = row;

            JLabel lbl = new JLabel(f.label + (f.required ? " *" : ""));
            lbl.setFont(Theme.FONT_SMALL);
            lbl.setForeground(f.required ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            if (!f.tooltip.isEmpty()) lbl.setToolTipText(f.tooltip);
            formArea.add(lbl, lc);

            JComponent widget = createWidget(f, entry.getValue(f.key));
            if (!f.tooltip.isEmpty()) widget.setToolTipText(f.tooltip);
            widgets.put(f.key, widget);
            formArea.add(widget, wc);
            row++;
        }

        // Filler
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 0; fc.gridy = row; fc.gridwidth = 2;
        fc.weighty = 1.0; fc.fill = GridBagConstraints.VERTICAL;
        formArea.add(Box.createVerticalGlue(), fc);

        formArea.revalidate();
        formArea.repaint();
        suppressFire = false;
    }

    private void showPlaceholder() {
        widgets.clear();
        formArea.removeAll();
        formArea.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("Doppio click su un componente per configurarlo", SwingConstants.CENTER);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_MUTED);
        formArea.add(lbl, BorderLayout.CENTER);
        formArea.revalidate();
        formArea.repaint();
        titleLbl.setText("Pannello Configurazione");
        removeBtn.setEnabled(false);
        copyBtn.setEnabled(false);
    }

    // ── Widget factory ────────────────────────────────────────────────────
    private JComponent createWidget(FieldDef f, String currentVal) {
        switch (f.type) {
            case COMBO: {
                JComboBox<String> cb = new JComboBox<>(f.options);
                cb.setBackground(Theme.BG_DARK);
                cb.setForeground(Theme.TEXT_PRIMARY);
                cb.setFont(Theme.FONT_LABEL);
                if (currentVal != null && !currentVal.isEmpty()) cb.setSelectedItem(currentVal);
                cb.addActionListener(e -> {
                    if (!suppressFire && currentEntry != null)
                        ctrl.updateEntryField(currentEntry, f.key, (String) cb.getSelectedItem());
                });
                return cb;
            }
            case PASSWORD: {
                JPasswordField pf = new JPasswordField(currentVal);
                styleField(pf, f.placeholder);
                pf.getDocument().addDocumentListener(liveListener(f.key, () ->
                    new String(pf.getPassword())));
                return pf;
            }
            case SPINNER: {
                int init = 0;
                try { init = Integer.parseInt(currentVal.isEmpty() ? f.placeholder : currentVal); }
                catch (NumberFormatException ignored) {}
                JSpinner sp = new JSpinner(new SpinnerNumberModel(init, 0, Integer.MAX_VALUE, 1));
                sp.setBackground(Theme.BG_DARK);
                sp.setForeground(Theme.TEXT_PRIMARY);
                ((JSpinner.DefaultEditor)sp.getEditor()).getTextField().setBackground(Theme.BG_DARK);
                ((JSpinner.DefaultEditor)sp.getEditor()).getTextField().setForeground(Theme.TEXT_PRIMARY);
                ((JSpinner.DefaultEditor)sp.getEditor()).getTextField().setCaretColor(Theme.TEXT_PRIMARY);
                sp.addChangeListener(e -> {
                    if (!suppressFire && currentEntry != null)
                        ctrl.updateEntryField(currentEntry, f.key, String.valueOf(sp.getValue()));
                });
                return sp;
            }
            case TEXTAREA: {
                JTextArea ta = new JTextArea(currentVal, 3, 20);
                ta.setBackground(Theme.BG_DARK);
                ta.setForeground(Theme.TEXT_PRIMARY);
                ta.setCaretColor(Theme.TEXT_PRIMARY);
                ta.setFont(Theme.FONT_MONO);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                if (currentVal.isEmpty()) ta.setText(f.placeholder);
                ta.getDocument().addDocumentListener(liveListener(f.key, ta::getText));
                JScrollPane sp = new JScrollPane(ta);
                sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
                sp.setPreferredSize(new Dimension(200, 70));
                return sp;
            }
            case CHECKBOX: {
                JCheckBox cb = new JCheckBox();
                cb.setSelected("true".equals(currentVal));
                cb.setBackground(Theme.BG_PANEL);
                cb.setForeground(Theme.TEXT_PRIMARY);
                cb.addActionListener(e -> {
                    if (!suppressFire && currentEntry != null)
                        ctrl.updateEntryField(currentEntry, f.key, cb.isSelected() ? "true" : "false");
                });
                return cb;
            }
            default: { // TEXT
                JTextField tf = new JTextField(currentVal);
                styleField(tf, f.placeholder);
                tf.getDocument().addDocumentListener(liveListener(f.key, tf::getText));
                return tf;
            }
        }
    }

    private void styleField(JTextField tf, String placeholder) {
        tf.setBackground(Theme.BG_DARK);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setCaretColor(Theme.TEXT_PRIMARY);
        tf.setFont(Theme.FONT_LABEL);
        tf.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(3, 6, 3, 6)
        ));
    }

    private DocumentListener liveListener(String key, java.util.function.Supplier<String> getter) {
        return new DocumentListener() {
            private void upd() {
                if (!suppressFire && currentEntry != null)
                    ctrl.updateEntryField(currentEntry, key, getter.get());
            }
            public void insertUpdate(DocumentEvent e)  { upd(); }
            public void removeUpdate(DocumentEvent e)  { upd(); }
            public void changedUpdate(DocumentEvent e) { upd(); }
        };
    }

    private void styleSmallBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.FONT_SMALL);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(4, 10, 4, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ── ChangeListener ────────────────────────────────────────────────────
    @Override public void onSessionChanged() {
        ConfigEntry sel = ctrl.getSelectedEntry();
        if (sel == null) {
            currentEntry = null;
            showPlaceholder();
        } else if (sel != currentEntry) {
            currentEntry = sel;
            formArea.setLayout(new GridBagLayout());
            renderForm(sel);
            titleLbl.setText(sel.getDefinition().label);
            removeBtn.setEnabled(true);
            copyBtn.setEnabled(true);
        }
        // else same entry → fields updated in-place by liveListener, no full re-render needed
    }
}
