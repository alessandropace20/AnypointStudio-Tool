package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import com.integrationtoolbox.datatools.JsonService;
import com.integrationtoolbox.datatools.XmlService;
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
 * Data Tools panel: format, validate, and convert XML/JSON documents.
 * Sub-tabs: XML Tools, JSON Tools, XML↔JSON Convert, XSD Validate, JSON Schema Validate.
 */
public class DataToolsPanel {

    public static Tab createTab(XmlService xmlService, JsonService jsonService, MainApp app) {
        Tab tab = new Tab("🛠 Data Tools");
        TabPane subTabs = new TabPane();
        subTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        subTabs.getTabs().addAll(
                buildXmlTab(xmlService, app),
                buildJsonTab(jsonService, app),
                buildConvertTab(xmlService, jsonService, app),
                buildXsdTab(xmlService, app),
                buildJsonSchemaTab(jsonService, app)
        );
        tab.setContent(subTabs);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // XML Tools tab
    // ──────────────────────────────────────────────────────────────────────────

    private static Tab buildXmlTab(XmlService xmlService, MainApp app) {
        Tab tab = new Tab("XML");
        TextArea inputArea = newCodeArea("Paste or type XML here…");
        TextArea outputArea = newCodeArea(null);
        outputArea.setEditable(false);
        Label statusLbl = new Label();
        statusLbl.setWrapText(true);

        Button formatBtn = new Button("Format");
        Button validateBtn = new Button("Validate");
        Button openBtn = new Button("Open file…");
        Button copyBtn = new Button("Copy output");
        Button clearBtn = new Button("Clear");

        formatBtn.setOnAction(e -> runAsync(() -> xmlService.format(inputArea.getText()),
                outputArea, statusLbl, app, "XML formatted."));
        validateBtn.setOnAction(e -> {
            List<String> errors = xmlService.validate(inputArea.getText());
            if (errors.isEmpty()) {
                outputArea.setText("✅ Valid XML");
                statusLbl.setText("Valid.");
                app.setStatus("XML is valid.");
            } else {
                outputArea.setText(String.join("\n", errors));
                statusLbl.setText("❌ Validation failed.");
                app.setStatus("XML validation errors.");
            }
        });
        openBtn.setOnAction(e -> openFile(inputArea, app));
        copyBtn.setOnAction(e -> copyToClipboard(outputArea.getText(), statusLbl));
        clearBtn.setOnAction(e -> { inputArea.clear(); outputArea.clear(); statusLbl.setText(""); });

        HBox buttons = new HBox(8, formatBtn, validateBtn, openBtn, copyBtn, clearBtn);
        buttons.setPadding(new Insets(6));

        SplitPane split = new SplitPane(wrapInScroll(inputArea), wrapInScroll(outputArea));
        split.setDividerPositions(0.5);
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox root = new VBox(buttons, split, statusLbl);
        VBox.setVgrow(split, Priority.ALWAYS);
        root.setPadding(new Insets(8));
        tab.setContent(root);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // JSON Tools tab
    // ──────────────────────────────────────────────────────────────────────────

    private static Tab buildJsonTab(JsonService jsonService, MainApp app) {
        Tab tab = new Tab("JSON");
        TextArea inputArea = newCodeArea("Paste or type JSON here…");
        TextArea outputArea = newCodeArea(null);
        outputArea.setEditable(false);
        Label statusLbl = new Label();
        statusLbl.setWrapText(true);

        Button formatBtn = new Button("Format");
        Button validateBtn = new Button("Validate");
        Button openBtn = new Button("Open file…");
        Button copyBtn = new Button("Copy output");
        Button clearBtn = new Button("Clear");

        formatBtn.setOnAction(e -> runAsync(() -> jsonService.format(inputArea.getText()),
                outputArea, statusLbl, app, "JSON formatted."));
        validateBtn.setOnAction(e -> {
            List<String> errors = jsonService.validate(inputArea.getText());
            if (errors.isEmpty()) {
                outputArea.setText("✅ Valid JSON");
                statusLbl.setText("Valid.");
                app.setStatus("JSON is valid.");
            } else {
                outputArea.setText(String.join("\n", errors));
                statusLbl.setText("❌ Validation failed.");
                app.setStatus("JSON validation errors.");
            }
        });
        openBtn.setOnAction(e -> openFile(inputArea, app));
        copyBtn.setOnAction(e -> copyToClipboard(outputArea.getText(), statusLbl));
        clearBtn.setOnAction(e -> { inputArea.clear(); outputArea.clear(); statusLbl.setText(""); });

        HBox buttons = new HBox(8, formatBtn, validateBtn, openBtn, copyBtn, clearBtn);
        buttons.setPadding(new Insets(6));

        SplitPane split = new SplitPane(wrapInScroll(inputArea), wrapInScroll(outputArea));
        split.setDividerPositions(0.5);
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox root = new VBox(buttons, split, statusLbl);
        VBox.setVgrow(split, Priority.ALWAYS);
        root.setPadding(new Insets(8));
        tab.setContent(root);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Convert tab
    // ──────────────────────────────────────────────────────────────────────────

    private static Tab buildConvertTab(XmlService xmlService, JsonService jsonService, MainApp app) {
        Tab tab = new Tab("Convert");
        TextArea inputArea = newCodeArea("Paste XML or JSON here…");
        TextArea outputArea = newCodeArea(null);
        outputArea.setEditable(false);
        Label statusLbl = new Label();
        statusLbl.setWrapText(true);

        CheckBox prettyCheck = new CheckBox("Pretty-print");
        prettyCheck.setSelected(true);
        TextField rootField = new TextField("root");
        Label rootLabel = new Label("XML root element:");

        Button xmlToJsonBtn = new Button("XML → JSON");
        Button jsonToXmlBtn = new Button("JSON → XML");
        Button copyBtn = new Button("Copy output");
        Button clearBtn = new Button("Clear");

        xmlToJsonBtn.setOnAction(e -> runAsync(
                () -> xmlService.toJson(inputArea.getText(), prettyCheck.isSelected()),
                outputArea, statusLbl, app, "Converted XML → JSON."));
        jsonToXmlBtn.setOnAction(e -> runAsync(
                () -> jsonService.toXml(inputArea.getText(), rootField.getText().isBlank() ? "root" : rootField.getText()),
                outputArea, statusLbl, app, "Converted JSON → XML."));
        copyBtn.setOnAction(e -> copyToClipboard(outputArea.getText(), statusLbl));
        clearBtn.setOnAction(e -> { inputArea.clear(); outputArea.clear(); statusLbl.setText(""); });

        HBox buttons = new HBox(8, xmlToJsonBtn, jsonToXmlBtn, prettyCheck, rootLabel, rootField, copyBtn, clearBtn);
        buttons.setPadding(new Insets(6));

        SplitPane split = new SplitPane(wrapInScroll(inputArea), wrapInScroll(outputArea));
        split.setDividerPositions(0.5);
        VBox.setVgrow(split, Priority.ALWAYS);

        Label warnLabel = new Label("⚠ Conversions between XML and JSON may be lossy. XML attributes, mixed content, and namespace semantics may not round-trip perfectly.");
        warnLabel.setWrapText(true);
        warnLabel.getStyleClass().add("warning-label");

        VBox root = new VBox(buttons, warnLabel, split, statusLbl);
        VBox.setVgrow(split, Priority.ALWAYS);
        root.setPadding(new Insets(8));
        tab.setContent(root);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // XSD Validation tab
    // ──────────────────────────────────────────────────────────────────────────

    private static Tab buildXsdTab(XmlService xmlService, MainApp app) {
        Tab tab = new Tab("XSD Validate");
        TextArea xmlArea = newCodeArea("Paste XML here…");
        TextArea xsdArea = newCodeArea("Paste XSD schema here…");
        TextArea resultArea = newCodeArea(null);
        resultArea.setEditable(false);
        Label statusLbl = new Label();

        Button validateBtn = new Button("Validate XML against XSD");
        Button openXmlBtn = new Button("Open XML…");
        Button openXsdBtn = new Button("Open XSD…");
        Button clearBtn = new Button("Clear");

        validateBtn.setOnAction(e -> {
            try {
                List<String> errors = xmlService.validateAgainstXsd(xmlArea.getText(), xsdArea.getText());
                if (errors.isEmpty()) {
                    resultArea.setText("✅ XML is valid against the provided XSD.");
                    statusLbl.setText("Validation passed.");
                    app.setStatus("XSD validation passed.");
                } else {
                    resultArea.setText("❌ Validation errors:\n" + String.join("\n", errors));
                    statusLbl.setText("Validation failed: " + errors.size() + " error(s).");
                    app.setStatus("XSD validation failed.");
                }
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
                statusLbl.setText("Error during validation.");
            }
        });
        openXmlBtn.setOnAction(e -> openFile(xmlArea, app));
        openXsdBtn.setOnAction(e -> openFile(xsdArea, app));
        clearBtn.setOnAction(e -> { xmlArea.clear(); xsdArea.clear(); resultArea.clear(); statusLbl.setText(""); });

        HBox buttons = new HBox(8, validateBtn, openXmlBtn, openXsdBtn, clearBtn);
        buttons.setPadding(new Insets(6));

        SplitPane topSplit = new SplitPane(wrapInScroll(xmlArea), wrapInScroll(xsdArea));
        topSplit.setDividerPositions(0.5);
        SplitPane mainSplit = new SplitPane(topSplit, wrapInScroll(resultArea));
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.setDividerPositions(0.65);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        VBox root = new VBox(buttons, mainSplit, statusLbl);
        root.setPadding(new Insets(8));
        tab.setContent(root);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // JSON Schema Validation tab
    // ──────────────────────────────────────────────────────────────────────────

    private static Tab buildJsonSchemaTab(JsonService jsonService, MainApp app) {
        Tab tab = new Tab("JSON Schema");
        TextArea jsonArea = newCodeArea("Paste JSON document here…");
        TextArea schemaArea = newCodeArea("Paste JSON Schema here…");
        TextArea resultArea = newCodeArea(null);
        resultArea.setEditable(false);
        Label statusLbl = new Label();

        Button validateBtn = new Button("Validate JSON against Schema");
        Button openJsonBtn = new Button("Open JSON…");
        Button openSchemaBtn = new Button("Open Schema…");
        Button clearBtn = new Button("Clear");

        validateBtn.setOnAction(e -> {
            List<String> errors = jsonService.validateAgainstSchema(jsonArea.getText(), schemaArea.getText());
            if (errors.isEmpty()) {
                resultArea.setText("✅ JSON is valid against the provided schema.");
                statusLbl.setText("Validation passed.");
                app.setStatus("JSON Schema validation passed.");
            } else {
                resultArea.setText("❌ Validation errors:\n" + String.join("\n", errors));
                statusLbl.setText("Validation failed: " + errors.size() + " error(s).");
                app.setStatus("JSON Schema validation failed.");
            }
        });
        openJsonBtn.setOnAction(e -> openFile(jsonArea, app));
        openSchemaBtn.setOnAction(e -> openFile(schemaArea, app));
        clearBtn.setOnAction(e -> { jsonArea.clear(); schemaArea.clear(); resultArea.clear(); statusLbl.setText(""); });

        HBox buttons = new HBox(8, validateBtn, openJsonBtn, openSchemaBtn, clearBtn);
        buttons.setPadding(new Insets(6));

        SplitPane topSplit = new SplitPane(wrapInScroll(jsonArea), wrapInScroll(schemaArea));
        topSplit.setDividerPositions(0.5);
        SplitPane mainSplit = new SplitPane(topSplit, wrapInScroll(resultArea));
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.setDividerPositions(0.65);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        VBox root = new VBox(buttons, mainSplit, statusLbl);
        root.setPadding(new Insets(8));
        tab.setContent(root);
        return tab;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private static TextArea newCodeArea(String prompt) {
        TextArea ta = new TextArea();
        ta.getStyleClass().add("code-area");
        ta.setWrapText(false);
        if (prompt != null) ta.setPromptText(prompt);
        return ta;
    }

    private static ScrollPane wrapInScroll(TextArea area) {
        ScrollPane sp = new ScrollPane(area);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        return sp;
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static void runAsync(ThrowingSupplier<String> action, TextArea outputArea,
                                  Label statusLbl, MainApp app, String successMsg) {
        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception { return action.get(); }
        };
        task.setOnSucceeded(e -> {
            outputArea.setText(task.getValue());
            statusLbl.setText("✅ " + successMsg);
            app.setStatus(successMsg);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            outputArea.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
            statusLbl.setText("❌ Error.");
            app.setStatus("Error.");
        });
        new Thread(task, "DataTools-Worker").start();
    }

    private static void copyToClipboard(String text, Label statusLbl) {
        if (text != null && !text.isBlank()) {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(text);
            Clipboard.getSystemClipboard().setContent(cc);
            statusLbl.setText("📋 Copied to clipboard.");
        }
    }

    private static void openFile(TextArea target, MainApp app) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open file");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML / JSON / XSD", "*.xml", "*.json", "*.xsd"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                target.setText(content);
                app.setStatus("Opened: " + file.getName());
            } catch (Exception ex) {
                target.setText("Error reading file: " + ex.getMessage());
            }
        }
    }
}
