package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import com.integrationtoolbox.library.TibcoMuleEntry;
import com.integrationtoolbox.library.TibcoMuleLibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TIBCO BusinessWorks → MuleSoft equivalence library panel.
 */
public class TibcoMulePanel {

    public static Tab createTab(TibcoMuleLibraryService service, MainApp app) {
        Tab tab = new Tab("⚙ TIBCO → Mule");
        tab.setContent(buildContent(service, app));
        return tab;
    }

    private static BorderPane buildContent(TibcoMuleLibraryService service, MainApp app) {
        // ── Left panel ────────────────────────────────────────────────────────
        TextField searchField = new TextField();
        searchField.setPromptText("Search by TIBCO activity, MuleSoft component, category…");

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by category");
        categoryFilter.setMaxWidth(Double.MAX_VALUE);

        ObservableList<TibcoMuleEntry> shownEntries = FXCollections.observableArrayList();
        ListView<TibcoMuleEntry> listView = new ListView<>(shownEntries);
        listView.setPrefWidth(280);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TibcoMuleEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String approx = item.isApproximateMapping() ? " ≈" : "";
                setText(item.getTibcoActivity() + approx);
                setTooltip(new Tooltip("→ " + item.getMuleComponent()));
            }
        });

        Button newBtn = new Button("New");
        Button importBtn = new Button("Import…");
        Button exportBtn = new Button("Export…");
        HBox leftToolbar = new HBox(6, newBtn, importBtn, exportBtn);

        VBox leftPanel = new VBox(6, searchField, categoryFilter, leftToolbar, listView);
        leftPanel.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);

        // ── Right: detail form ────────────────────────────────────────────────
        TextField tibcoField = new TextField(); tibcoField.setPromptText("TIBCO activity or concept");
        TextField tibcoVersionField = new TextField(); tibcoVersionField.setPromptText("TIBCO version/context");
        TextField muleField = new TextField(); muleField.setPromptText("MuleSoft component or pattern");
        TextField catField = new TextField(); catField.setPromptText("Category");
        TextField tagsField = new TextField(); tagsField.setPromptText("Tags (comma-separated)");
        TextArea tibcoBehaviorArea = new TextArea(); tibcoBehaviorArea.setPromptText("Explanation of TIBCO behavior"); tibcoBehaviorArea.setPrefRowCount(3);
        TextArea muleSuggestArea = new TextArea(); muleSuggestArea.setPromptText("Suggested MuleSoft approach"); muleSuggestArea.setPrefRowCount(3);
        TextArea codeArea = new TextArea(); codeArea.setPromptText("Code or configuration example"); codeArea.getStyleClass().add("code-area"); codeArea.setPrefRowCount(6);
        TextArea semanticArea = new TextArea(); semanticArea.setPromptText("Semantic differences and migration caveats"); semanticArea.setPrefRowCount(3);
        TextArea limitArea = new TextArea(); limitArea.setPromptText("Limitations and non-one-to-one cases"); limitArea.setPrefRowCount(2);
        TextField docRefField = new TextField(); docRefField.setPromptText("Documentation reference URL");
        CheckBox approxCheck = new CheckBox("Approximate mapping (not one-to-one)");

        Button saveBtn = new Button("Save");
        Button deleteBtn = new Button("Delete");
        Button duplicateBtn = new Button("Duplicate");
        Label statusLbl = new Label();
        HBox detailToolbar = new HBox(8, saveBtn, deleteBtn, duplicateBtn, statusLbl);

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(6); form.setPadding(new Insets(10));
        form.getColumnConstraints().addAll(colC(150, false), colC(0, true));
        int r = 0;
        form.add(new Label("TIBCO Activity:"), 0, r); form.add(tibcoField, 1, r++);
        form.add(new Label("TIBCO Version:"), 0, r); form.add(tibcoVersionField, 1, r++);
        form.add(new Label("MuleSoft Component:"), 0, r); form.add(muleField, 1, r++);
        form.add(new Label("Category:"), 0, r); form.add(catField, 1, r++);
        form.add(new Label("Tags:"), 0, r); form.add(tagsField, 1, r++);
        form.add(new Label("TIBCO behavior:"), 0, r); form.add(tibcoBehaviorArea, 1, r++);
        form.add(new Label("Mule approach:"), 0, r); form.add(muleSuggestArea, 1, r++);
        form.add(new Label("Code example:"), 0, r); form.add(codeArea, 1, r++);
        form.add(new Label("Semantic diff:"), 0, r); form.add(semanticArea, 1, r++);
        form.add(new Label("Limitations:"), 0, r); form.add(limitArea, 1, r++);
        form.add(new Label("Docs ref:"), 0, r); form.add(docRefField, 1, r++);
        form.add(approxCheck, 1, r++);
        form.add(detailToolbar, 0, r++, 2, 1);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);

        // ── State ─────────────────────────────────────────────────────────────
        final TibcoMuleEntry[] current = {null};

        Runnable populateForm = () -> {
            TibcoMuleEntry e = current[0];
            boolean has = e != null;
            tibcoField.setText(has ? orEmpty(e.getTibcoActivity()) : "");
            tibcoVersionField.setText(has ? orEmpty(e.getTibcoVersionContext()) : "");
            muleField.setText(has ? orEmpty(e.getMuleComponent()) : "");
            catField.setText(has ? orEmpty(e.getCategory()) : "");
            tagsField.setText(has && e.getTags() != null ? String.join(", ", e.getTags()) : "");
            tibcoBehaviorArea.setText(has ? orEmpty(e.getTibcoBehaviorExplanation()) : "");
            muleSuggestArea.setText(has ? orEmpty(e.getMuleSuggestedApproach()) : "");
            codeArea.setText(has ? orEmpty(e.getCodeExample()) : "");
            semanticArea.setText(has ? orEmpty(e.getSemanticDifferences()) : "");
            limitArea.setText(has ? orEmpty(e.getLimitations()) : "");
            docRefField.setText(has ? orEmpty(e.getDocumentationRef()) : "");
            approxCheck.setSelected(has && e.isApproximateMapping());
            statusLbl.setText("");
        };

        Runnable refreshList = () -> {
            String q = searchField.getText();
            String cat = categoryFilter.getValue();
            List<TibcoMuleEntry> results = service.search(q);
            if (cat != null && !cat.isBlank() && !"(All)".equals(cat)) {
                results = results.stream().filter(e -> cat.equalsIgnoreCase(e.getCategory())).collect(Collectors.toList());
            }
            shownEntries.setAll(results);

            List<String> cats = new ArrayList<>();
            cats.add("(All)");
            cats.addAll(service.getCategories());
            String prev = categoryFilter.getValue();
            categoryFilter.getItems().setAll(cats);
            if (cats.contains(prev)) categoryFilter.setValue(prev);
        };

        refreshList.run();
        searchField.textProperty().addListener((obs, o, v) -> refreshList.run());
        categoryFilter.valueProperty().addListener((obs, o, v) -> refreshList.run());
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            current[0] = val;
            populateForm.run();
        });

        newBtn.setOnAction(e -> {
            TibcoMuleEntry entry = new TibcoMuleEntry();
            entry.setId(UUID.randomUUID().toString());
            current[0] = entry;
            populateForm.run();
            listView.getSelectionModel().clearSelection();
        });

        saveBtn.setOnAction(e -> {
            if (current[0] == null) return;
            TibcoMuleEntry entry = current[0];
            entry.setTibcoActivity(tibcoField.getText());
            entry.setTibcoVersionContext(tibcoVersionField.getText());
            entry.setMuleComponent(muleField.getText());
            entry.setCategory(catField.getText());
            entry.setTags(Arrays.stream(tagsField.getText().split(","))
                    .map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList()));
            entry.setTibcoBehaviorExplanation(tibcoBehaviorArea.getText());
            entry.setMuleSuggestedApproach(muleSuggestArea.getText());
            entry.setCodeExample(codeArea.getText());
            entry.setSemanticDifferences(semanticArea.getText());
            entry.setLimitations(limitArea.getText());
            entry.setDocumentationRef(docRefField.getText());
            entry.setApproximateMapping(approxCheck.isSelected());
            try {
                if (service.findById(entry.getId()).isPresent()) {
                    service.updateEntry(entry);
                } else {
                    service.addEntry(entry);
                }
                statusLbl.setText("✅ Saved.");
                app.setStatus("Entry saved.");
                refreshList.run();
            } catch (Exception ex) {
                statusLbl.setText("❌ " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            if (current[0] == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + current[0].getTibcoActivity() + "\"?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        service.deleteEntry(current[0].getId());
                        current[0] = null;
                        populateForm.run();
                        refreshList.run();
                        app.setStatus("Entry deleted.");
                    } catch (Exception ex) {
                        statusLbl.setText("❌ " + ex.getMessage());
                    }
                }
            });
        });

        duplicateBtn.setOnAction(e -> {
            if (current[0] == null) return;
            try {
                TibcoMuleEntry copy = service.duplicate(current[0].getId());
                current[0] = copy;
                refreshList.run();
                populateForm.run();
                statusLbl.setText("✅ Duplicated.");
            } catch (Exception ex) {
                statusLbl.setText("❌ " + ex.getMessage());
            }
        });

        importBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                try {
                    int count = service.importFromFile(file.getAbsolutePath());
                    refreshList.run();
                    new Alert(Alert.AlertType.INFORMATION, "Imported " + count + " entries (duplicates skipped).").showAndWait();
                    app.setStatus("Imported " + count + " entries.");
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Import failed: " + ex.getMessage()).showAndWait();
                }
            }
        });

        exportBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            fc.setInitialFileName("tibco-mule-export.json");
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
        return new BorderPane(split);
    }

    private static String orEmpty(String s) { return s != null ? s : ""; }

    private static ColumnConstraints colC(double min, boolean grow) {
        ColumnConstraints c = new ColumnConstraints();
        if (min > 0) c.setMinWidth(min);
        c.setHgrow(grow ? Priority.ALWAYS : Priority.NEVER);
        return c;
    }
}
