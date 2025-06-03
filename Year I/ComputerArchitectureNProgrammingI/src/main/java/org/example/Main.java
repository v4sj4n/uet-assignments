package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class Main extends Application {
    
    private RegisterSet registerSet;
    private TableView<RegisterRow> registerTable;
    private TextArea dumpArea;
    private Label statusLabel;
    
    private static final String BG_PRIMARY = "#1a1a1a";
    private static final String BG_SECONDARY = "#2d2d2d";
    private static final String BG_CARD = "#333333";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_SECONDARY = "#b0b0b0";
    private static final String ACCENT = "#007acc";
    private static final String SUCCESS = "#28a745";
    private static final String ERROR = "#dc3545";
    private static final String WARNING = "#ffc107";
    private static final String BORDER = "#404040";

    @Override
    public void start(Stage primaryStage) {
        int registerCount = askForRegisterCount();
        this.registerSet = new RegisterSet(registerCount);
        
        primaryStage.setTitle("CPU Register Simulator");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        
        Label headerLabel = new Label("CPU Register Simulator");
        headerLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);
        
        VBox leftPanel = createRegisterPanel();
        
        VBox rightPanel = createControlPanel();
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
        
        statusLabel = new Label("Ready - " + registerSet.size() + " registers initialized");
        statusLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-padding: 10px;");
        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: " + BG_SECONDARY + "; -fx-background-radius: 4px;");
        
        root.getChildren().addAll(headerLabel, mainContent, statusLabel);
        
        Scene scene = new Scene(root, 900, 600);
        
        try {
            scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        } catch (Exception e) {
            applyInlineComboBoxStyling(scene);
        }
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize display
        refreshRegisterTable();
        dumpRegisters();
    }
    
    private void applyInlineComboBoxStyling(Scene scene) {
        String css = """
            .combo-box-popup .list-view {
                -fx-background-color: %s;
            }
            .combo-box-popup .list-view .list-cell {
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-padding: 4px 8px;
            }
            .combo-box-popup .list-view .list-cell:hover {
                -fx-background-color: %s;
            }
            .combo-box-popup .list-view .list-cell:selected {
                -fx-background-color: %s;
            }
            """.formatted(BG_SECONDARY, BG_SECONDARY, TEXT_PRIMARY, ACCENT, ACCENT);
        
        scene.getRoot().setStyle(scene.getRoot().getStyle() + css);
    }
    
    private int askForRegisterCount() {
        TextInputDialog dialog = new TextInputDialog("8");
        dialog.setTitle("Register Setup");
        dialog.setHeaderText("How many registers?");
        dialog.setContentText("Enter number (1-32):");
        
        while (true) {
            var result = dialog.showAndWait();
            if (result.isEmpty()) {
                System.exit(0);
            }
            
            try {
                int count = Integer.parseInt(result.get().trim());
                if (count > 0 && count <= 32) {
                    return count;
                }
                showAlert("Invalid Range", "Please enter a number between 1 and 32.");
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        }
    }
    
    private VBox createRegisterPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(400);
        
        Label title = new Label("Registers");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        registerTable = new TableView<>();
        registerTable.setStyle(
            "-fx-background-color: " + BG_CARD + "; " +
            "-fx-control-inner-background: " + BG_CARD + "; " +
            "-fx-table-cell-border-color: " + BORDER + "; " +
            "-fx-border-color: " + BORDER + "; " +
            "-fx-border-radius: 8px;"
        );
        registerTable.setPrefHeight(300);
        
        TableColumn<RegisterRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(80);
        
        TableColumn<RegisterRow, String> hexCol = new TableColumn<>("Hex");
        hexCol.setCellValueFactory(new PropertyValueFactory<>("hexValue"));
        hexCol.setPrefWidth(120);
        
        TableColumn<RegisterRow, String> decCol = new TableColumn<>("Decimal");
        decCol.setCellValueFactory(new PropertyValueFactory<>("decValue"));
        decCol.setPrefWidth(100);
        
        TableColumn<RegisterRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        
        registerTable.getColumns().addAll(nameCol, hexCol, decCol, statusCol);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Dump area
        Label dumpTitle = new Label("Register Dump");
        dumpTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        dumpArea = new TextArea();
        dumpArea.setEditable(false);
        dumpArea.setPrefHeight(150);
        dumpArea.setStyle(
            "-fx-control-inner-background: " + BG_CARD + "; " +
            "-fx-text-fill: " + TEXT_PRIMARY + "; " +
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 11px; " +
            "-fx-border-color: " + BORDER + "; " +
            "-fx-border-radius: 8px;"
        );
        
        panel.getChildren().addAll(title, registerTable, dumpTitle, dumpArea);
        return panel;
    }
    
    private VBox createControlPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(350);
        
        VBox writeSection = createWriteSection();
        
        VBox opsSection = createOperationsSection();
        
        VBox controlSection = createControlButtons();
        
        panel.getChildren().addAll(writeSection, opsSection, controlSection);
        return panel;
    }
    
    private VBox createWriteSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8px;");
        
        Label title = new Label("Write Register");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField regField = createTextField("R0", 60);
        Label equalLabel = new Label("=");
        equalLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
        TextField valueField = createTextField("123", 100);
        
        Button writeBtn = createButton("Write", ACCENT);
        writeBtn.setOnAction(e -> performWrite(regField.getText(), valueField.getText(), regField, valueField));
        
        // Enter key support
        valueField.setOnAction(e -> performWrite(regField.getText(), valueField.getText(), regField, valueField));
        
        inputRow.getChildren().addAll(regField, equalLabel, valueField, writeBtn);
        section.getChildren().addAll(title, inputRow);
        return section;
    }
    
    private VBox createOperationsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8px;");
        
        Label title = new Label("Operations");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Math Operations: R1 + R2 → R3
        Label mathLabel = new Label("Math: Reg1 OP Reg2 → Dest");
        mathLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;");
        
        HBox mathFields = new HBox(5);
        mathFields.setAlignment(Pos.CENTER_LEFT);
        
        TextField mathSrc1 = createTextField("R0", 45);
        ComboBox<String> opSelector = new ComboBox<>();
        opSelector.getItems().addAll("+", "-", "×", "÷");
        opSelector.setValue("+");
        opSelector.setPrefWidth(50);
        
        // Fixed ComboBox styling
        opSelector.setStyle(
            "-fx-background-color: " + BG_SECONDARY + "; " +
            "-fx-text-fill: " + TEXT_PRIMARY + "; " +
            "-fx-border-color: " + BORDER + "; " +
            "-fx-border-radius: 4px;"
        );
        
        TextField mathSrc2 = createTextField("R1", 45);
        Label mathArrow = new Label("→");
        mathArrow.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
        TextField mathDest = createTextField("R2", 45);
        
        Button executeBtn = createButton("Execute", ACCENT);
        executeBtn.setOnAction(e -> {
            String operation = switch(opSelector.getValue()) {
                case "+" -> "add";
                case "-" -> "sub";  
                case "×" -> "mul";
                case "÷" -> "div";
                default -> "add";
            };
            performOperation(operation, mathSrc1.getText(), mathSrc2.getText(), mathDest.getText());
        });
        
        mathFields.getChildren().addAll(mathSrc1, opSelector, mathSrc2, mathArrow, mathDest, executeBtn);
        
        // Copy Operation: R1 → R2  
        Label copyLabel = new Label("Copy: Source → Dest");
        copyLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;");
        
        HBox copyFields = new HBox(5);
        copyFields.setAlignment(Pos.CENTER_LEFT);
        
        TextField copySrc = createTextField("R0", 60);
        Label copyArrow = new Label("→");
        copyArrow.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
        TextField copyDest = createTextField("R1", 60);
        
        Button copyBtn = createButton("Copy", TEXT_SECONDARY);
        copyBtn.setOnAction(e -> performCopy(copySrc.getText(), copyDest.getText()));
        
        copyFields.getChildren().addAll(copySrc, copyArrow, copyDest, copyBtn);
        
        section.getChildren().addAll(title, mathLabel, mathFields, copyLabel, copyFields);
        return section;
    } 
    
    private VBox createControlButtons() {
        VBox section = new VBox(8);
        
        Button refreshBtn = createButton("Refresh", ACCENT);
        refreshBtn.setPrefWidth(120);
        refreshBtn.setOnAction(e -> {
            refreshRegisterTable();
            dumpRegisters();
            showStatus("Display refreshed", SUCCESS);
        });
        
        Button clearBtn = createButton("Clear All", ERROR);
        clearBtn.setPrefWidth(120);
        clearBtn.setOnAction(e -> clearAllRegisters());
        
        section.getChildren().addAll(refreshBtn, clearBtn);
        return section;
    }
    
    private TextField createTextField(String prompt, int width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(
            "-fx-background-color: " + BG_SECONDARY + "; " +
            "-fx-text-fill: " + TEXT_PRIMARY + "; " +
            "-fx-border-color: " + BORDER + "; " +
            "-fx-border-radius: 4px; " +
            "-fx-padding: 6px;"
        );
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(field.getStyle().replace(BORDER, ACCENT));
            } else {
                field.setStyle(field.getStyle().replace(ACCENT, BORDER));
            }
        });
        
        return field;
    }
    
    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 6 12 6 12; " +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
        
        return button;
    }
    
    private void performWrite(String regName, String valueText, TextField regField, TextField valueField) {
        try {
            regName = regName.trim().toUpperCase();
            valueText = valueText.trim();
            
            if (regName.isEmpty() || valueText.isEmpty()) {
                showStatus("Please enter both register and value", WARNING);
                return;
            }
            
            int value = parseValue(valueText);
            registerSet.write(regName, value);
            
            refreshRegisterTable();
            dumpRegisters();
            
            showStatus(String.format("Written %s = %d", regName, value), SUCCESS);
            
            regField.clear();
            valueField.clear();
            regField.requestFocus();
            
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), ERROR);
        }
    }
    
    private void performOperation(String op, String reg1, String reg2, String dest) {
        try {
            reg1 = reg1.trim().toUpperCase();
            reg2 = reg2.trim().toUpperCase();
            dest = dest.trim().toUpperCase();
            
            if (reg1.isEmpty() || reg2.isEmpty() || dest.isEmpty()) {
                showStatus("Please fill all register fields", WARNING);
                return;
            }
            
            switch (op) {
                case "add" -> registerSet.add(reg1, reg2, dest);
                case "sub" -> registerSet.subtract(reg1, reg2, dest);
                case "mul" -> registerSet.multiply(reg1, reg2, dest);
                case "div" -> registerSet.divide(reg1, reg2, dest);
            }
            
            refreshRegisterTable();
            dumpRegisters();
            
            String symbol = switch (op) {
                case "add" -> "+";
                case "sub" -> "-";
                case "mul" -> "×";
                case "div" -> "÷";
                default -> "?";
            };
            
            showStatus(String.format("%s %s %s → %s", reg1, symbol, reg2, dest), SUCCESS);
            
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), ERROR);
        }
    }
    
    private void performCopy(String source, String dest) {
        try {
            source = source.trim().toUpperCase();
            dest = dest.trim().toUpperCase();
            
            if (source.isEmpty() || dest.isEmpty()) {
                showStatus("Please specify source and destination", WARNING);
                return;
            }
            
            registerSet.copy(source, dest);
            refreshRegisterTable();
            dumpRegisters();
            
            showStatus(String.format("Copied %s → %s", source, dest), SUCCESS);
            
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), ERROR);
        }
    }
    
    private int parseValue(String input) throws NumberFormatException {
        if (input.toLowerCase().startsWith("0x")) {
            return Integer.parseUnsignedInt(input.substring(2), 16);
        } else if (input.toLowerCase().startsWith("0b")) {
            return Integer.parseUnsignedInt(input.substring(2), 2);
        } else {
            return Integer.parseInt(input);
        }
    }
    
    private void refreshRegisterTable() {
        ObservableList<RegisterRow> data = FXCollections.observableArrayList();
        
        for (int i = 0; i < registerSet.size(); i++) {
            String regName = "R" + i;
            Register reg = registerSet.get(regName);
            
            String status = reg.isModified() ? "Modified" : "Clean";
            data.add(new RegisterRow(regName, reg.toHexString(), 
                reg.toDecimalString(), status));
        }
        
        registerTable.setItems(data);
    }
    
    private void dumpRegisters() {
        dumpArea.setText(registerSet.getDumpString());
    }
    
    private void clearAllRegisters() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Registers");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will reset all register values to 0.");
        
        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            registerSet.clearAll();
            refreshRegisterTable();
            dumpRegisters();
            showStatus("All registers cleared", SUCCESS);
        }
    }
    
    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle(
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 10px; " +
            "-fx-background-color: " + BG_SECONDARY + "; " +
            "-fx-background-radius: 4px;"
        );
        
        FadeTransition fade = new FadeTransition(Duration.seconds(3), statusLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setOnFinished(e -> {
            statusLabel.setText("Ready - " + registerSet.size() + " registers");
            statusLabel.setStyle(
                "-fx-text-fill: " + TEXT_SECONDARY + "; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 10px; " +
                "-fx-background-color: " + BG_SECONDARY + "; " +
                "-fx-background-radius: 4px;"
            );
        });
        fade.play();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table rows
    public static class RegisterRow {
        private final StringProperty name;
        private final StringProperty hexValue;
        private final StringProperty decValue;
        private final StringProperty status;
        
        public RegisterRow(String name, String hexValue, String decValue, String status) {
            this.name = new SimpleStringProperty(name);
            this.hexValue = new SimpleStringProperty(hexValue);
            this.decValue = new SimpleStringProperty(decValue);
            this.status = new SimpleStringProperty(status);
        }
        
        public String getName() { return name.get(); }
        public String getHexValue() { return hexValue.get(); }
        public String getDecValue() { return decValue.get(); }
        public String getStatus() { return status.get(); }
        
        public StringProperty nameProperty() { return name; }
        public StringProperty hexValueProperty() { return hexValue; }
        public StringProperty decValueProperty() { return decValue; }
        public StringProperty statusProperty() { return status; }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}