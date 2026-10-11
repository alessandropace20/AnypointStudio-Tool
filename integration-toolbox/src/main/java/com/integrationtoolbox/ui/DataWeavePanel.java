package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import com.integrationtoolbox.library.DataWeaveSnippet;
import com.integrationtoolbox.library.SnippetLibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataWeave snippet library panel: search, browse, create, edit, delete, import, export.
 */
public class DataWeavePanel {

    public static Tab createTab(SnippetLibraryService service, MainApp app) {
        Tab tab = new Tab("📚 DataWeave");
        tab.setContent(buildContent(service, app));
        return tab;
    }

    private static BorderPane buildContent(SnippetLibraryService service, MainApp app) {
        // ── Left side: search + list ──────────────────────────────────────────
        TextField searchField = new TextField();
        searchField.setPromptText("Search snippets (Ctrl+Shift+F)…");

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by category");
        categoryFilter.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(categoryFilter, Priority.ALWAYS);

        ObservableList<DataWeaveSnippet> shownSnippets = FXCollections.observableArrayList();
        ListView<DataWeaveSnippet> listView = new ListView<>(shownSnippets);
        listView.setPrefWidth(280);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(DataWeaveSnippet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                String star = item.isFavorite() ? "⭐ " : "";
                setText(star + item.getTitle());
            }
        });

        Button newBtn = new Button("New");
        Button importBtn = new Button("Import…");
        Button exportBtn = new Button("Export…");
        HBox leftToolbar = new HBox(6, newBtn, importBtn, exportBtn);

        VBox leftPanel = new VBox(6, searchField, categoryFilter, leftToolbar, listView);
        leftPanel.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);
        leftPanel.setPrefWidth(300);

        // ── Right side: snippet detail / editor ──────────────────────────────
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField tagsField = new TextField();
        tagsField.setPromptText("Tags (comma-separated)");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Short description");
        descArea.setPrefRowCount(2);
        TextArea codeArea = new TextArea();
        codeArea.setPromptText("DataWeave code");
        codeArea.getStyleClass().add("code-area");
        codeArea.setPrefRowCount(10);
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Example input");
        inputArea.setPrefRowCount(4);
        inputArea.getStyleClass().add("code-area");
        TextArea outputArea = new TextArea();
        outputArea.setPromptText("Expected output");
        outputArea.setPrefRowCount(4);
        outputArea.getStyleClass().add("code-area");
        TextField compatField = new TextField();
        compatField.setPromptText("Compatibility notes");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setPrefRowCount(2);
        TextField urlField = new TextField();
        urlField.setPromptText("Source/reference URL");
        CheckBox favoriteCheck = new CheckBox("Favorite");

        Button saveBtn = new Button("Save");
        Button deleteBtn = new Button("Delete");
        Button duplicateBtn = new Button("Duplicate");
        Button copyCodeBtn = new Button("Copy code");
        Label detailStatus = new Label();
        HBox detailToolbar = new HBox(8, saveBtn, deleteBtn, duplicateBtn, copyCodeBtn, detailStatus);
        HBox.setHgrow(detailStatus, Priority.ALWAYS);

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(6); form.setPadding(new Insets(10));
        form.getColumnConstraints().addAll(
                colC(120, false), colC(0, true)
        );
        int r = 0;
        form.add(new Label("Title:"), 0, r); form.add(titleField, 1, r++);
        form.add(new Label("Category:"), 0, r); form.add(categoryField, 1, r++);
        form.add(new Label("Tags:"), 0, r); form.add(tagsField, 1, r++);
        form.add(new Label("Description:"), 0, r); form.add(descArea, 1, r++);
        form.add(new Label("Code:"), 0, r); form.add(codeArea, 1, r++);
        form.add(new Label("Example input:"), 0, r); form.add(inputArea, 1, r++);
        form.add(new Label("Expected output:"), 0, r); form.add(outputArea, 1, r++);
        form.add(new Label("Compatibility:"), 0, r); form.add(compatField, 1, r++);
        form.add(new Label("Notes:"), 0, r); form.add(notesArea, 1, r++);
        form.add(new Label("Source URL:"), 0, r); form.add(urlField, 1, r++);
        form.add(favoriteCheck, 1, r++);
        form.add(detailToolbar, 0, r++, 2, 1);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);

        // ── State: current selection ──────────────────────────────────────────
        final DataWeaveSnippet[] currentSnippet = {null};

        Runnable populateForm = () -> {
            DataWeaveSnippet s = currentSnippet[0];
            boolean hasSnippet = s != null;
            titleField.setText(hasSnippet ? orEmpty(s.getTitle()) : "");
            categoryField.setText(hasSnippet ? orEmpty(s.getCategory()) : "");
            tagsField.setText(hasSnippet && s.getTags() != null ? String.join(", ", s.getTags()) : "");
            descArea.setText(hasSnippet ? orEmpty(s.getDescription()) : "");
            codeArea.setText(hasSnippet ? orEmpty(s.getCode()) : "");
            inputArea.setText(hasSnippet ? orEmpty(s.getExampleInput()) : "");
            outputArea.setText(hasSnippet ? orEmpty(s.getExpectedOutput()) : "");
            compatField.setText(hasSnippet ? orEmpty(s.getCompatibilityNotes()) : "");
            notesArea.setText(hasSnippet ? orEmpty(s.getNotes()) : "");
            urlField.setText(hasSnippet ? orEmpty(s.getSourceUrl()) : "");
            favoriteCheck.setSelected(hasSnippet && s.isFavorite());
            detailStatus.setText("");
        };

        Runnable refreshList = () -> {
            String q = searchField.getText();
            String cat = categoryFilter.getValue();
            List<DataWeaveSnippet> results = service.search(q);
            if (cat != null && !cat.isBlank() && !"(All)".equals(cat)) {
                results = results.stream().filter(s -> cat.equalsIgnoreCase(s.getCategory())).collect(Collectors.toList());
            }
            shownSnippets.setAll(results);

            // Refresh categories
            List<String> cats = new ArrayList<>();
            cats.add("(All)");
            cats.addAll(service.getCategories());
            String prev = categoryFilter.getValue();
            categoryFilter.getItems().setAll(cats);
            if (cats.contains(prev)) categoryFilter.setValue(prev);
        };

        // Initial population
        refreshList.run();

        // Search & filter listeners
        searchField.textProperty().addListener((obs, o, v) -> refreshList.run());
        categoryFilter.valueProperty().addListener((obs, o, v) -> refreshList.run());

        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            currentSnippet[0] = val;
            populateForm.run();
        });

        // New snippet
        newBtn.setOnAction(e -> {
            DataWeaveSnippet s = new DataWeaveSnippet();
            s.setId(UUID.randomUUID().toString());
            currentSnippet[0] = s;
            populateForm.run();
            listView.getSelectionModel().clearSelection();
            titleField.requestFocus();
        });

        // Save snippet
        saveBtn.setOnAction(e -> {
            if (currentSnippet[0] == null) return;
            DataWeaveSnippet s = currentSnippet[0];
            s.setTitle(titleField.getText());
            s.setCategory(categoryField.getText());
            s.setTags(Arrays.stream(tagsField.getText().split(","))
                    .map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList()));
            s.setDescription(descArea.getText());
            s.setCode(codeArea.getText());
            s.setExampleInput(inputArea.getText());
            s.setExpectedOutput(outputArea.getText());
            s.setCompatibilityNotes(compatField.getText());
            s.setNotes(notesArea.getText());
            s.setSourceUrl(urlField.getText());
            s.setFavorite(favoriteCheck.isSelected());
            try {
                if (service.findById(s.getId()).isPresent()) {
                    service.updateSnippet(s);
                } else {
                    service.addSnippet(s);
                }
                detailStatus.setText("✅ Saved.");
                app.setStatus("Snippet saved.");
                refreshList.run();
            } catch (Exception ex) {
                detailStatus.setText("❌ " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            if (currentSnippet[0] == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete snippet \"" + currentSnippet[0].getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        service.deleteSnippet(currentSnippet[0].getId());
                        currentSnippet[0] = null;
                        populateForm.run();
                        refreshList.run();
                        app.setStatus("Snippet deleted.");
                    } catch (Exception ex) {
                        detailStatus.setText("❌ " + ex.getMessage());
                    }
                }
            });
        });

        duplicateBtn.setOnAction(e -> {
            if (currentSnippet[0] == null) return;
            try {
                DataWeaveSnippet copy = service.duplicate(currentSnippet[0].getId());
                currentSnippet[0] = copy;
                refreshList.run();
                populateForm.run();
                detailStatus.setText("✅ Duplicated.");
            } catch (Exception ex) {
                detailStatus.setText("❌ " + ex.getMessage());
            }
        });

        copyCodeBtn.setOnAction(e -> {
            String code = codeArea.getText();
            if (!code.isBlank()) {
                ClipboardContent cc = new ClipboardContent();
                cc.putString(code);
                Clipboard.getSystemClipboard().setContent(cc);
                detailStatus.setText("📋 Code copied.");
            }
        });

        // Import
        importBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Import snippets");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                try {
                    int count = service.importFromFile(file.getAbsolutePath());
                    refreshList.run();
                    app.setStatus("Imported " + count + " snippets.");
                    new Alert(Alert.AlertType.INFORMATION, "Imported " + count + " snippet(s) (duplicates skipped).").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Import failed: " + ex.getMessage()).showAndWait();
                }
            }
        });

        // Export
        exportBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export snippets");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            fc.setInitialFileName("dw-snippets-export.json");
            File file = fc.showSaveDialog(null);
            if (file != null) {
                try {
                    service.exportToFile(file.getAbsolutePath());
                    app.setStatus("Exported to " + file.getName());
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
                }
            }
        });

        SplitPane split = new SplitPane(leftPanel, formScroll);
        split.setDividerPositions(0.28);

        BorderPane root = new BorderPane(split);
        return root;
    }

    private static String orEmpty(String s) { return s != null ? s : ""; }

    private static ColumnConstraints colC(double min, boolean grow) {
        ColumnConstraints c = new ColumnConstraints();
        if (min > 0) c.setMinWidth(min);
        c.setHgrow(grow ? Priority.ALWAYS : Priority.NEVER);
        return c;
    }
}
