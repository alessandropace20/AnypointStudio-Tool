package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import com.integrationtoolbox.diff.DiffService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Structural Diff panel: compare two XML or JSON documents.
 */
public class DiffPanel {

    public static Tab createTab(DiffService diffService, MainApp app) {
        Tab tab = new Tab("⇄ Diff");

        // Mode selector
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton jsonMode = new RadioButton("JSON");
        jsonMode.setToggleGroup(modeGroup);
        jsonMode.setSelected(true);
        RadioButton xmlMode = new RadioButton("XML");
        xmlMode.setToggleGroup(modeGroup);

        CheckBox normalizeWs = new CheckBox("Normalize whitespace");
        CheckBox ignoreArrayOrder = new CheckBox("Ignore JSON array order");

        HBox modeBar = new HBox(12, new Label("Mode:"), jsonMode, xmlMode, normalizeWs, ignoreArrayOrder);
        modeBar.setPadding(new Insets(6));

        // Listen to mode change
        modeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            boolean isJson = val == jsonMode;
            ignoreArrayOrder.setDisable(!isJson);
        });

        // Left and right editors
        TextArea leftArea = newCodeArea("Left document…");
        TextArea rightArea = newCodeArea("Right document…");
        Label leftLabel = new Label("Left");
        Label rightLabel = new Label("Right");

        Button openLeftBtn = new Button("Open…");
        Button openRightBtn = new Button("Open…");
        openLeftBtn.setOnAction(e -> openFile(leftArea, app));
        openRightBtn.setOnAction(e -> openFile(rightArea, app));

        VBox leftPane = new VBox(4, new HBox(8, leftLabel, openLeftBtn), wrapScroll(leftArea));
        VBox.setVgrow(wrapScroll(leftArea), Priority.ALWAYS);
        VBox rightPane = new VBox(4, new HBox(8, rightLabel, openRightBtn), wrapScroll(rightArea));

        // We need to re-create since wrapScroll is called twice — store refs
        ScrollPane leftScroll = wrapScroll(leftArea);
        ScrollPane rightScroll = wrapScroll(rightArea);
        leftPane = new VBox(4, new HBox(8, leftLabel, openLeftBtn), leftScroll);
        rightPane = new VBox(4, new HBox(8, rightLabel, openRightBtn), rightScroll);
        VBox.setVgrow(leftScroll, Priority.ALWAYS);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);

        SplitPane editorSplit = new SplitPane(leftPane, rightPane);
        editorSplit.setDividerPositions(0.5);

        // Results pane
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(false);
        resultArea.getStyleClass().add("code-area");
        resultArea.setPromptText("Diff results will appear here…");

        ListView<String> diffList = new ListView<>();
        diffList.setPlaceholder(new Label("No differences found or diff not yet run."));

        Label summaryLabel = new Label();
        summaryLabel.setWrapText(true);

        Button compareBtn = new Button("Compare");
        Button copyBtn = new Button("Copy report");
        Button clearBtn = new Button("Clear all");
        HBox toolbar = new HBox(8, compareBtn, copyBtn, clearBtn, summaryLabel);
        toolbar.setPadding(new Insets(6));
        HBox.setHgrow(summaryLabel, Priority.ALWAYS);

        compareBtn.setOnAction(e -> {
            String left = leftArea.getText();
            String right = rightArea.getText();
            if (left.isBlank() || right.isBlank()) {
                summaryLabel.setText("⚠ Both documents must have content.");
                return;
            }
            boolean isJson = modeGroup.getSelectedToggle() == jsonMode;
            boolean normalize = normalizeWs.isSelected();
            boolean ignoreOrder = ignoreArrayOrder.isSelected();

            Task<DiffService.DiffResult> task = new Task<>() {
                @Override protected DiffService.DiffResult call() throws Exception {
                    return isJson
                            ? diffService.diffJson(left, right, ignoreOrder)
                            : diffService.diffXml(left, right, normalize);
                }
            };
            task.setOnSucceeded(ev -> {
                DiffService.DiffResult result = task.getValue();
                summaryLabel.setText(result.getSummary());
                resultArea.setText(result.getFullReport());
                diffList.getItems().clear();
                List<DiffService.DiffItem> items = result.getItems();
                for (DiffService.DiffItem item : items) {
                    diffList.getItems().add(item.toString());
                }
                app.setStatus("Diff complete: " + result.getSummary());
            });
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                summaryLabel.setText("❌ Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
                resultArea.setText("Error during diff: " + ex.getMessage());
                app.setStatus("Diff error.");
            });
            new Thread(task, "Diff-Worker").start();
        });

        copyBtn.setOnAction(e -> {
            String text = resultArea.getText();
            if (!text.isBlank()) {
                ClipboardContent cc = new ClipboardContent();
                cc.putString(text);
                Clipboard.getSystemClipboard().setContent(cc);
                summaryLabel.setText("📋 Report copied.");
            }
        });

        clearBtn.setOnAction(e -> {
            leftArea.clear(); rightArea.clear();
            resultArea.clear(); diffList.getItems().clear();
            summaryLabel.setText("");
        });

        TabPane resultTabs = new TabPane();
        resultTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab reportTab = new Tab("Full report", new ScrollPane(resultArea) {{ setFitToWidth(true); setFitToHeight(true); }});
        Tab listTab = new Tab("Diff items", diffList);
        resultTabs.getTabs().addAll(reportTab, listTab);
        resultTabs.setPrefHeight(220);

        SplitPane mainSplit = new SplitPane(editorSplit, resultTabs);
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.setDividerPositions(0.60);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        VBox root = new VBox(modeBar, toolbar, mainSplit);
        root.setPadding(new Insets(4));
        tab.setContent(root);
        return tab;
    }

    private static TextArea newCodeArea(String prompt) {
        TextArea ta = new TextArea();
        ta.getStyleClass().add("code-area");
        ta.setWrapText(false);
        if (prompt != null) ta.setPromptText(prompt);
        return ta;
    }

    private static ScrollPane wrapScroll(TextArea ta) {
        ScrollPane sp = new ScrollPane(ta);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    private static void openFile(TextArea target, MainApp app) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML / JSON", "*.xml", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            try {
                target.setText(Files.readString(file.toPath()));
                app.setStatus("Opened: " + file.getName());
            } catch (Exception ex) {
                target.setText("Error: " + ex.getMessage());
            }
        }
    }
}
