package com.muletool.ui;

import com.muletool.controller.AppController;
import com.muletool.model.ConfigMode;
import com.muletool.ui.panels.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Main application window.
 *
 * Layout:
 *   ┌─────────────────────────────────────────────────────────┐
 *   │  MenuBar: File | Edit                                    │
 *   ├─────────────────────────────────────────────────────────┤
 *   │  TopBar: [● Global XML]  [● Flow XML]   mode toggle     │
 *   ├──────────────────────────┬──────────────────────────────┤
 *   │  ConfigListPanel (top-L) │                              │
 *   │  (available components)  │   PreviewPanel (right)       │
 *   ├──────────────────────────┤   XML tab + Graphic tab      │
 *   │  SessionPanel (mid-L)    │                              │
 *   │  (added entries)         │                              │
 *   ├──────────────────────────┤                              │
 *   │  ConfigFormPanel (bot-L) │                              │
 *   │  (field editor)          │                              │
 *   └──────────────────────────┴──────────────────────────────┘
 */
public class MainFrame extends JFrame implements AppController.ChangeListener {

    private final AppController ctrl = new AppController();

    // Mode toggle buttons
    private final JToggleButton globalBtn = new JToggleButton("Global XML");
    private final JToggleButton flowBtn   = new JToggleButton("Flow XML");
    private final ButtonGroup   modeGroup = new ButtonGroup();

    public MainFrame() {
        super("MuleXML Tool — Anypoint Studio 7 Config Generator");
        ctrl.addChangeListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1280, 780));
        setMinimumSize(new Dimension(900, 600));
        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Dark background for entire frame
        getContentPane().setBackground(Theme.BG_DARK);

        // ── Menu Bar ──────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── Root layout ───────────────────────────────────────────────────
        getContentPane().setLayout(new BorderLayout(0, 0));

        // ── Top bar (mode toggle) ─────────────────────────────────────────
        getContentPane().add(buildTopBar(), BorderLayout.NORTH);

        // ── Main split: left column | right preview ───────────────────────
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildLeftColumn(), new PreviewPanel(ctrl));
        mainSplit.setDividerLocation(330);
        mainSplit.setDividerSize(4);
        mainSplit.setBorder(null);
        mainSplit.setBackground(Theme.BG_DARK);
        getContentPane().add(mainSplit, BorderLayout.CENTER);
    }

    // ── Top bar ───────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.DIVIDER),
            new EmptyBorder(8, 0, 8, 0)
        ));

        styleToggle(globalBtn, true);
        styleToggle(flowBtn,   false);

        modeGroup.add(globalBtn);
        modeGroup.add(flowBtn);
        globalBtn.setSelected(true);

        globalBtn.addActionListener(e -> ctrl.setMode(ConfigMode.GLOBAL));
        flowBtn  .addActionListener(e -> ctrl.setMode(ConfigMode.FLOW));

        bar.add(globalBtn);
        bar.add(Box.createHorizontalStrut(2));
        bar.add(flowBtn);
        return bar;
    }

    private void styleToggle(JToggleButton btn, boolean left) {
        btn.setFont(Theme.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(Theme.BG_PANEL);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(Theme.ACCENT_BLUE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Theme.BG_PANEL);
                btn.setForeground(Theme.TEXT_SECONDARY);
            }
        });
    }

    // ── Left column: list + session + form ────────────────────────────────
    private JComponent buildLeftColumn() {
        // ConfigListPanel (top): available components catalogue
        ConfigListPanel listPanel = new ConfigListPanel(ctrl);

        // SessionPanel (middle): entries in this session
        SessionPanel sessionPanel = new SessionPanel(ctrl);
        sessionPanel.setPreferredSize(new Dimension(330, 120));
        sessionPanel.setMinimumSize(new Dimension(200, 80));

        // ConfigFormPanel (bottom): field editor
        ConfigFormPanel formPanel = new ConfigFormPanel(ctrl);

        // Stack with two splits
        JSplitPane topMid = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, sessionPanel);
        topMid.setDividerLocation(260);
        topMid.setDividerSize(4);
        topMid.setBorder(null);

        JSplitPane full = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topMid, formPanel);
        full.setDividerLocation(400);
        full.setDividerSize(4);
        full.setBorder(null);
        return full;
    }

    // ── Menu Bar ──────────────────────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(Theme.BG_PANEL);
        mb.setBorder(new MatteBorder(0, 0, 1, 0, Theme.DIVIDER));

        // ── File ──────────────────────────────────────────────────────────
        JMenu fileMenu = menu("File");

        JMenuItem newItem    = item("Nuova Sessione", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        JMenuItem openItem   = item("Apri Sessione…",  KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        JMenuItem saveItem   = item("Salva Sessione…", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        JMenuItem exportItem = item("Esporta XML…",    KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        JMenuItem exitItem   = new JMenuItem("Esci");
        styleItem(exitItem);

        newItem   .addActionListener(e -> ctrl.newSession(this));
        openItem  .addActionListener(e -> ctrl.openSession(this));
        saveItem  .addActionListener(e -> ctrl.saveSession(this));
        exportItem.addActionListener(e -> ctrl.exportXml(this));
        exitItem  .addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ── Edit ──────────────────────────────────────────────────────────
        JMenu editMenu = menu("Edit");

        JMenuItem undoItem    = item("Annulla",        KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        JMenuItem redoItem    = item("Ripristina",     KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
        JMenuItem copyXmlItem = item("Copia XML",      KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        undoItem   .addActionListener(e -> ctrl.undo());
        redoItem   .addActionListener(e -> ctrl.redo());
        copyXmlItem.addActionListener(e -> ctrl.copyXmlToClipboard(this));

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(copyXmlItem);

        mb.add(fileMenu);
        mb.add(editMenu);
        return mb;
    }

    private JMenu menu(String text) {
        JMenu m = new JMenu(text);
        m.setFont(Theme.FONT_LABEL);
        m.setForeground(Theme.TEXT_PRIMARY);
        m.setBackground(Theme.BG_PANEL);
        return m;
    }

    private JMenuItem item(String text, int key, int mod) {
        JMenuItem mi = new JMenuItem(text);
        mi.setAccelerator(KeyStroke.getKeyStroke(key, mod));
        styleItem(mi);
        return mi;
    }

    private void styleItem(JMenuItem mi) {
        mi.setFont(Theme.FONT_LABEL);
        mi.setBackground(Theme.BG_PANEL);
        mi.setForeground(Theme.TEXT_PRIMARY);
    }

    @Override public void onSessionChanged() {
        // Update undo/redo enabled state if needed in future
    }
}
