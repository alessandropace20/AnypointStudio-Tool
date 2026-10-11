package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Settings panel: theme selection and application preferences.
 */
public class SettingsPanel {

    public static Tab createTab(MainApp app) {
        Tab tab = new Tab("⚙ Settings");
        tab.setContent(buildContent(app));
        return tab;
    }

    private static VBox buildContent(MainApp app) {
        Label title = new Label("Settings");
        title.getStyleClass().add("settings-title");

        Label themeLabel = new Label("Theme:");
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton darkBtn = new RadioButton("Dark (default)");
        darkBtn.setToggleGroup(themeGroup);
        darkBtn.setSelected(true);
        RadioButton lightBtn = new RadioButton("Light");
        lightBtn.setToggleGroup(themeGroup);

        HBox themeRow = new HBox(12, themeLabel, darkBtn, lightBtn);

        Label storageLabel = new Label("Library storage location:");
        String storageDir = System.getProperty("user.home") + "/.integrationtoolbox/";
        TextField storageField = new TextField(storageDir);
        storageField.setEditable(false);
        storageField.setMaxWidth(Double.MAX_VALUE);

        Label versionLabel = new Label("Integration Toolbox v0.1.0");
        versionLabel.getStyleClass().add("version-label");

        Label privacyNote = new Label(
            "All data is stored locally. No telemetry, analytics, or network calls are made by default. " +
            "Secrets are never logged or persisted."
        );
        privacyNote.setWrapText(true);
        privacyNote.getStyleClass().add("privacy-note");

        Label shortcutsTitle = new Label("Keyboard shortcuts:");
        GridPane shortcuts = new GridPane();
        shortcuts.setHgap(20); shortcuts.setVgap(4);
        String[][] keys = {
            {"Ctrl+Shift+P", "Open command palette"},
            {"Ctrl+W",       "Close active tab"},
            {"Ctrl+Tab",     "Next tab"},
            {"Ctrl+F",       "Find in active view"},
            {"Escape",       "Close palette / dismiss dialog"},
        };
        for (int i = 0; i < keys.length; i++) {
            Label kl = new Label(keys[i][0]); kl.getStyleClass().add("shortcut-key");
            shortcuts.add(kl, 0, i);
            shortcuts.add(new Label(keys[i][1]), 1, i);
        }

        VBox root = new VBox(12,
            title,
            new Separator(),
            themeRow,
            new Separator(),
            storageLabel,
            storageField,
            new Separator(),
            shortcutsTitle,
            shortcuts,
            new Separator(),
            privacyNote,
            versionLabel
        );
        root.setPadding(new Insets(20));
        root.getStyleClass().add("panel-root");
        return root;
    }
}
