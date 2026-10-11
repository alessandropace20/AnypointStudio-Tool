package com.integrationtoolbox;

import com.integrationtoolbox.datatools.JsonService;
import com.integrationtoolbox.datatools.XmlService;
import com.integrationtoolbox.diff.DiffService;
import com.integrationtoolbox.library.SnippetLibraryService;
import com.integrationtoolbox.library.TibcoMuleLibraryService;
import com.integrationtoolbox.security.SecurityService;
import com.integrationtoolbox.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Integration Toolbox – main application entry point.
 * Implements VS Code-inspired layout: activity bar, side panel, workspace tabs, status bar, command palette.
 */
public class MainApp extends Application {

    // ─── Services ─────────────────────────────────────────────────────────────
    private final SecurityService securityService = new SecurityService();
    private final XmlService xmlService = new XmlService();
    private final JsonService jsonService = new JsonService();
    private final DiffService diffService = new DiffService();
    private final SnippetLibraryService snippetService = new SnippetLibraryService();
    private final TibcoMuleLibraryService tibcoMuleService = new TibcoMuleLibraryService();

    // ─── UI roots ─────────────────────────────────────────────────────────────
    private TabPane workspaceTabs;
    private Label statusLabel;
    private Stage primaryStage;

    // ─── Activity sections ────────────────────────────────────────────────────
    private static final String SEC_SECURITY   = "🔐  Security";
    private static final String SEC_DATA_TOOLS = "🛠  Data Tools";
    private static final String SEC_DIFF       = "⇄  Diff";
    private static final String SEC_DATAWEAVE  = "📚  DataWeave";
    private static final String SEC_TIBCO      = "⚙  TIBCO → Mule";
    private static final String SEC_SETTINGS   = "⚙  Settings";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Integration Toolbox");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        workspaceTabs = new TabPane();
        workspaceTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        // Show welcome tab on startup
        showWelcomeTab();

        // Activity bar (left)
        VBox activityBar = buildActivityBar();

        // Status bar (bottom)
        HBox statusBar = buildStatusBar();

        BorderPane root = new BorderPane();
        root.setLeft(activityBar);
        root.setCenter(workspaceTabs);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());

        // Keyboard shortcuts
        installKeyboardShortcuts(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Activity bar
    // ──────────────────────────────────────────────────────────────────────────

    private VBox buildActivityBar() {
        VBox bar = new VBox();
        bar.getStyleClass().add("activity-bar");
        bar.setSpacing(0);
        bar.setPrefWidth(180);

        String[] sections = {SEC_SECURITY, SEC_DATA_TOOLS, SEC_DIFF, SEC_DATAWEAVE, SEC_TIBCO};
        for (String section : sections) {
            Button btn = new Button(section);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.getStyleClass().add("activity-button");
            btn.setOnAction(e -> openSection(section));
            bar.getChildren().add(btn);
        }

        // Separator + settings at bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        bar.getChildren().add(spacer);

        Button settingsBtn = new Button(SEC_SETTINGS);
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.getStyleClass().add("activity-button");
        settingsBtn.setOnAction(e -> openSection(SEC_SETTINGS));
        bar.getChildren().add(settingsBtn);

        return bar;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Status bar
    // ──────────────────────────────────────────────────────────────────────────

    private HBox buildStatusBar() {
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        HBox bar = new HBox(statusLabel);
        bar.getStyleClass().add("status-bar");
        bar.setPadding(new Insets(2, 8, 2, 8));
        return bar;
    }

    public void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tab management
    // ──────────────────────────────────────────────────────────────────────────

    private void showWelcomeTab() {
        Tab tab = new Tab("Welcome");
        tab.setClosable(false);

        VBox content = new VBox(16);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Integration Toolbox");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label("A local-first toolbox for integration developers");
        subtitle.getStyleClass().add("welcome-subtitle");

        Label hint = new Label(
            "Select a section from the left activity bar or use Ctrl+Shift+P to open the command palette.\n\n" +
            "Features:\n" +
            "  🔐  Security — encrypt and decrypt API key values\n" +
            "  🛠  Data Tools — format, validate, and convert XML/JSON\n" +
            "  ⇄  Diff — structural comparison of XML/JSON documents\n" +
            "  📚  DataWeave — searchable snippet library\n" +
            "  ⚙  TIBCO → Mule — TIBCO BusinessWorks equivalence reference"
        );
        hint.getStyleClass().add("welcome-hint");
        hint.setWrapText(true);

        content.getChildren().addAll(title, subtitle, new Separator(), hint);
        tab.setContent(content);
        workspaceTabs.getTabs().add(tab);
    }

    private void openSection(String section) {
        // If a tab for this section is already open, select it
        for (Tab t : workspaceTabs.getTabs()) {
            if (section.equals(t.getUserData())) {
                workspaceTabs.getSelectionModel().select(t);
                return;
            }
        }
        // Otherwise create a new tab
        Tab tab = createTabForSection(section);
        if (tab != null) {
            tab.setUserData(section);
            workspaceTabs.getTabs().add(tab);
            workspaceTabs.getSelectionModel().select(tab);
        }
    }

    private Tab createTabForSection(String section) {
        return switch (section) {
            case SEC_SECURITY   -> SecurityPanel.createTab(securityService, this);
            case SEC_DATA_TOOLS -> DataToolsPanel.createTab(xmlService, jsonService, this);
            case SEC_DIFF       -> DiffPanel.createTab(diffService, this);
            case SEC_DATAWEAVE  -> DataWeavePanel.createTab(snippetService, this);
            case SEC_TIBCO      -> TibcoMulePanel.createTab(tibcoMuleService, this);
            case SEC_SETTINGS   -> SettingsPanel.createTab(this);
            default             -> null;
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Keyboard shortcuts
    // ──────────────────────────────────────────────────────────────────────────

    private void installKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN).match(e)) {
                openCommandPalette();
                e.consume();
            } else if (new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN).match(e)) {
                Tab selected = workspaceTabs.getSelectionModel().getSelectedItem();
                if (selected != null && selected.isClosable()) {
                    workspaceTabs.getTabs().remove(selected);
                }
                e.consume();
            } else if (new KeyCodeCombination(KeyCode.TAB, KeyCombination.CONTROL_DOWN).match(e)) {
                int size = workspaceTabs.getTabs().size();
                if (size > 1) {
                    int next = (workspaceTabs.getSelectionModel().getSelectedIndex() + 1) % size;
                    workspaceTabs.getSelectionModel().select(next);
                }
                e.consume();
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Command palette
    // ──────────────────────────────────────────────────────────────────────────

    private void openCommandPalette() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Command Palette");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());

        TextField searchField = new TextField();
        searchField.setPromptText("Type a command…");

        String[][] commands = {
            {"Open Security",               SEC_SECURITY},
            {"Open Data Tools",             SEC_DATA_TOOLS},
            {"Open Structural Diff",        SEC_DIFF},
            {"Open DataWeave Library",      SEC_DATAWEAVE},
            {"Open TIBCO → Mule Library",   SEC_TIBCO},
            {"Open Settings",               SEC_SETTINGS},
        };

        ListView<String[]> listView = new ListView<>();
        listView.getItems().setAll(commands);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item[0]);
            }
        });
        listView.setPrefHeight(220);

        searchField.textProperty().addListener((obs, old, val) -> {
            listView.getItems().clear();
            for (String[] cmd : commands) {
                if (cmd[0].toLowerCase().contains(val.toLowerCase())) {
                    listView.getItems().add(cmd);
                }
            }
        });

        VBox box = new VBox(8, searchField, listView);
        box.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Pressing Enter executes the selected command
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String[] selected = listView.getSelectionModel().getSelectedItem();
                if (selected == null && !listView.getItems().isEmpty()) {
                    selected = listView.getItems().get(0);
                }
                if (selected != null) {
                    dialog.close();
                    openSection(selected[1]);
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String[] selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    dialog.close();
                    openSection(selected[1]);
                }
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.equals(ButtonType.OK.toString())) {
                String[] selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) openSection(selected[1]);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Main
    // ──────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
