package com.integrationtoolbox.ui;

import com.integrationtoolbox.MainApp;
import com.integrationtoolbox.security.SecurityService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Security panel: encrypt and decrypt API key values.
 * No secrets are ever placed in logs or persistent storage by this panel.
 */
public class SecurityPanel {

    public static Tab createTab(SecurityService service, MainApp app) {
        Tab tab = new Tab("🔐 Security");
        tab.setContent(buildContent(service, app));
        return tab;
    }

    private static VBox buildContent(SecurityService service, MainApp app) {
        // Mode toggle
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton encryptBtn = new RadioButton("Encrypt");
        encryptBtn.setToggleGroup(modeGroup);
        encryptBtn.setSelected(true);
        RadioButton decryptBtn = new RadioButton("Decrypt");
        decryptBtn.setToggleGroup(modeGroup);

        HBox modeBar = new HBox(16, encryptBtn, decryptBtn);
        modeBar.setAlignment(Pos.CENTER_LEFT);

        // Labels change depending on mode
        Label inputLabel = new Label("Plaintext value:");
        Label inputHint = new Label("(value is masked — click 👁 to reveal)");
        inputHint.setStyle("-fx-text-fill: #888;");

        // Secret input field (masked by default)
        PasswordField secretField = new PasswordField();
        secretField.setPromptText("Enter the value…");
        TextField secretVisible = new TextField();
        secretVisible.setPromptText("Enter the value…");
        secretVisible.setVisible(false);
        secretVisible.setManaged(false);
        // Sync the two fields
        secretField.textProperty().bindBidirectional(secretVisible.textProperty());

        ToggleButton revealBtn = new ToggleButton("👁");
        revealBtn.setTooltip(new Tooltip("Show/hide value"));
        revealBtn.selectedProperty().addListener((obs, old, val) -> {
            secretField.setVisible(!val);
            secretField.setManaged(!val);
            secretVisible.setVisible(val);
            secretVisible.setManaged(val);
        });

        HBox inputRow = new HBox(8, secretField, secretVisible, revealBtn);
        HBox.setHgrow(secretField, Priority.ALWAYS);
        HBox.setHgrow(secretVisible, Priority.ALWAYS);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        // Password field
        Label pwdLabel = new Label("Password:");
        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("Enter password…");
        PasswordField pwdConfirmField = new PasswordField();
        pwdConfirmField.setPromptText("Confirm password (encrypt only)…");
        Label pwdConfirmLabel = new Label("Confirm password:");

        // Output area
        Label outputLabel = new Label("Result (ciphertext):");
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Output will appear here…");
        outputArea.setPrefRowCount(6);
        outputArea.setWrapText(true);
        outputArea.getStyleClass().add("code-area");

        // Action / clear / copy buttons
        Button actionBtn = new Button("Encrypt");
        Button clearBtn = new Button("Clear all");
        Button copyBtn = new Button("Copy result");
        HBox btnRow = new HBox(8, actionBtn, clearBtn, copyBtn);

        // Status label
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-inline");

        // Mode change updates UI labels
        modeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            boolean isEncrypt = val == encryptBtn;
            inputLabel.setText(isEncrypt ? "Plaintext value:" : "Encrypted payload:");
            inputHint.setText(isEncrypt ? "(value is masked — click 👁 to reveal)" : "");
            pwdConfirmLabel.setVisible(isEncrypt);
            pwdConfirmLabel.setManaged(isEncrypt);
            pwdConfirmField.setVisible(isEncrypt);
            pwdConfirmField.setManaged(isEncrypt);
            outputLabel.setText(isEncrypt ? "Result (ciphertext):" : "Decrypted value:");
            actionBtn.setText(isEncrypt ? "Encrypt" : "Decrypt");
        });

        // Action button handler
        actionBtn.setOnAction(e -> {
            statusLabel.setText("");
            outputArea.clear();
            String inputText = secretField.getText();
            char[] pwd = pwdField.getText().toCharArray();
            try {
                if (modeGroup.getSelectedToggle() == encryptBtn) {
                    // Encryption
                    if (inputText.isBlank()) { statusLabel.setText("⚠ Enter a value to encrypt."); return; }
                    if (pwd.length == 0) { statusLabel.setText("⚠ Enter a password."); return; }
                    char[] confirm = pwdConfirmField.getText().toCharArray();
                    if (!java.util.Arrays.equals(pwd, confirm)) {
                        statusLabel.setText("⚠ Passwords do not match.");
                        return;
                    }
                    String encrypted = service.encryptValue(inputText, pwd);
                    outputArea.setText(encrypted);
                    statusLabel.setText("✅ Encrypted successfully.");
                    app.setStatus("Encryption complete.");
                } else {
                    // Decryption
                    if (inputText.isBlank()) { statusLabel.setText("⚠ Enter an encrypted payload."); return; }
                    if (pwd.length == 0) { statusLabel.setText("⚠ Enter a password."); return; }
                    String decrypted = service.decryptValue(inputText, pwd);
                    outputArea.setText(decrypted);
                    statusLabel.setText("✅ Decrypted successfully.");
                    app.setStatus("Decryption complete.");
                }
            } catch (IllegalArgumentException ex) {
                statusLabel.setText("⚠ " + ex.getMessage());
            } catch (Exception ex) {
                statusLabel.setText("❌ Operation failed: Incorrect password or malformed payload.");
                app.setStatus("Error during crypto operation.");
            } finally {
                java.util.Arrays.fill(pwd, '\0');
            }
        });

        clearBtn.setOnAction(e -> {
            secretField.clear();
            pwdField.clear();
            pwdConfirmField.clear();
            outputArea.clear();
            statusLabel.setText("");
        });

        copyBtn.setOnAction(e -> {
            String text = outputArea.getText();
            if (!text.isBlank()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
                statusLabel.setText("📋 Copied to clipboard.");
            }
        });

        // Build layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.getColumnConstraints().addAll(
                colConstraint(150, false),
                colConstraint(0, true)
        );

        int row = 0;
        grid.add(new Label("Mode:"), 0, row);
        grid.add(modeBar, 1, row++);
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(inputLabel, 0, row);
        grid.add(inputRow, 1, row++);
        grid.add(inputHint, 1, row++);
        grid.add(pwdLabel, 0, row);
        grid.add(pwdField, 1, row++);
        grid.add(pwdConfirmLabel, 0, row);
        grid.add(pwdConfirmField, 1, row++);
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(outputLabel, 0, row);
        grid.add(outputArea, 1, row++);
        grid.add(btnRow, 1, row++);
        grid.add(statusLabel, 0, row, 2, 1);

        VBox root = new VBox(grid);
        root.getStyleClass().add("panel-root");
        return root;
    }

    private static ColumnConstraints colConstraint(double minWidth, boolean grow) {
        ColumnConstraints c = new ColumnConstraints();
        if (minWidth > 0) c.setMinWidth(minWidth);
        c.setHgrow(grow ? Priority.ALWAYS : Priority.NEVER);
        return c;
    }
}
