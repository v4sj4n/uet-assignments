package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Main extends Application {

    private RegisterSet registerSet;
    private TableView<RegisterRow> registerTable;
    private TextArea dumpArea;
    private Label statusLabel;
    private DatabaseManager dbManager;

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
        this.dbManager = new DatabaseManager();

        Optional<Integer> stateToLoad = askToLoadState();

        if (stateToLoad.isPresent() && stateToLoad.get() == -1) {
            System.exit(0);
        }

        if (stateToLoad.isPresent() && stateToLoad.get() > 0) {
            this.registerSet = new RegisterSet(32);
            dbManager.loadState(stateToLoad.get(), this.registerSet);
        } else {
            int registerCount = askForRegisterCount();
            this.registerSet = new RegisterSet(registerCount);
        }

        // --- UI Building ---
        primaryStage.setTitle("CPU Register Simulator");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // Add auto-save handler for when the window is closed
        primaryStage.setOnCloseRequest(this::handleAppClose);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_PRIMARY + ";");

        Label headerLabel = new Label("CPU Register Simulator");
        headerLabel.setStyle(
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; -fx-font-size: 24px; -fx-font-weight: bold;"
        );

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);

        VBox leftPanel = createRegisterPanel();
        VBox rightPanel = createControlPanel();

        mainContent.getChildren().addAll(leftPanel, rightPanel);

        statusLabel = new Label("Ready");
        statusLabel.setStyle(
            "-fx-text-fill: " +
            TEXT_SECONDARY +
            "; -fx-font-size: 12px; -fx-padding: 10px;"
        );
        statusLabel.setStyle(
            statusLabel.getStyle() +
            "-fx-background-color: " +
            BG_SECONDARY +
            "; -fx-background-radius: 4px;"
        );

        root.getChildren().addAll(headerLabel, mainContent, statusLabel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshRegisterTable();
        dumpRegisters();

        String initialStatus = stateToLoad.isPresent() && stateToLoad.get() > 0
            ? "Successfully loaded saved state."
            : "New session started.";
        showStatus(initialStatus, SUCCESS);
    }

    /**
     * Handles the application close request, prompting the user to save their session.
     */
    private void handleAppClose(WindowEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText(
            "Do you want to save your current session before exiting?"
        );

        ButtonType saveButton = new ButtonType("Save and Exit");
        ButtonType exitButton = new ButtonType("Exit Without Saving");
        ButtonType cancelButton = new ButtonType(
            "Cancel",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert.getButtonTypes().setAll(saveButton, exitButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                // Auto-generate a name and save
                String autoName =
                    "Auto-Save " +
                    LocalDateTime.now()
                        .format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                dbManager.saveState(autoName, registerSet);
                // Allow the application to close
            } else if (result.get() == cancelButton) {
                // Prevent the application from closing
                event.consume();
            }
            // If "Exit Without Saving" is clicked, do nothing and let it close.
        }
    }

    private Optional<Integer> askToLoadState() {
        List<String> savedStates = dbManager.getSavedStates();
        if (savedStates.isEmpty()) {
            return Optional.of(0);
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load State");
        alert.setHeaderText("Previous states were found.");
        alert.setContentText(
            "Would you like to load a saved state or start a new session?"
        );

        ButtonType loadButton = new ButtonType("Load State");
        ButtonType newSessionButton = new ButtonType("New Session");
        ButtonType cancelButton = new ButtonType(
            "Cancel",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert
            .getButtonTypes()
            .setAll(loadButton, newSessionButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == loadButton) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(
                savedStates.get(0),
                savedStates
            );
            dialog.setTitle("Select State");
            dialog.setHeaderText("Choose a state to load:");
            dialog.setContentText("Available states:");

            Optional<String> choice = dialog.showAndWait();
            if (choice.isPresent()) {
                int stateId = Integer.parseInt(choice.get().split(":")[0]);
                return Optional.of(stateId);
            }
        } else if (result.isPresent() && result.get() == newSessionButton) {
            return Optional.of(0);
        }

        return Optional.of(-1);
    }

    private int askForRegisterCount() {
        TextInputDialog dialog = new TextInputDialog("8");
        dialog.setTitle("Register Setup");
        dialog.setHeaderText("How many registers do you need?");
        dialog.setContentText("Enter a number (1-32):");

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
                showAlert(
                    "Invalid Range",
                    "Please enter a number between 1 and 32."
                );
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        }
    }

    // --- UI Creation Methods ---

    private VBox createRegisterPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(400);

        Label title = new Label("Registers");
        title.setStyle(
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; -fx-font-size: 16px; -fx-font-weight: bold;"
        );

        registerTable = new TableView<>();
        registerTable.setStyle(
            "-fx-background-color: " +
            BG_CARD +
            "; " +
            "-fx-control-inner-background: " +
            BG_CARD +
            "; " +
            "-fx-table-cell-border-color: " +
            BORDER +
            "; " +
            "-fx-border-color: " +
            BORDER +
            "; " +
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

        TableColumn<RegisterRow, String> statusCol = new TableColumn<>(
            "Status"
        );
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);

        registerTable.getColumns().addAll(nameCol, hexCol, decCol, statusCol);
        registerTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        Label dumpTitle = new Label("Register Dump");
        dumpTitle.setStyle(
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; -fx-font-size: 14px; -fx-font-weight: bold;"
        );

        dumpArea = new TextArea();
        dumpArea.setEditable(false);
        dumpArea.setPrefHeight(150);
        dumpArea.setStyle(
            "-fx-control-inner-background: " +
            BG_CARD +
            "; " +
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; " +
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 11px; " +
            "-fx-border-color: " +
            BORDER +
            "; " +
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

    /**
     * MODIFIED: "Save State" button is removed.
     */
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

    /**
     * MODIFIED: Uses a ComboBox for register selection.
     */
    private VBox createWriteSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " +
            BG_CARD +
            "; -fx-border-color: " +
            BORDER +
            "; -fx-border-radius: 8px;"
        );

        Label title = new Label("Write Register");
        title.setStyle(
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; -fx-font-size: 14px; -fx-font-weight: bold;"
        );

        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> regSelector = createRegisterComboBox();
        regSelector.setPrefWidth(75);

        Label equalLabel = new Label("=");
        equalLabel.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;"
        );
        TextField valueField = createTextField("123", 100);

        Button writeBtn = createButton("Write", ACCENT);
        // Pass the ComboBox value directly to the handler
        writeBtn.setOnAction(e ->
            performWrite(
                regSelector.getValue(),
                valueField.getText(),
                valueField
            )
        );

        valueField.setOnAction(e ->
            performWrite(
                regSelector.getValue(),
                valueField.getText(),
                valueField
            )
        );

        inputRow
            .getChildren()
            .addAll(regSelector, equalLabel, valueField, writeBtn);
        section.getChildren().addAll(title, inputRow);
        return section;
    }

    /**
     * MODIFIED: Uses ComboBoxes for all register selections.
     */
    private VBox createOperationsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " +
            BG_CARD +
            "; -fx-border-color: " +
            BORDER +
            "; -fx-border-radius: 8px;"
        );

        Label title = new Label("Operations");
        title.setStyle(
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; -fx-font-size: 14px; -fx-font-weight: bold;"
        );

        // --- Math Operations ---
        Label mathLabel = new Label("Math: Reg1 OP Reg2 → Dest");
        mathLabel.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;"
        );

        HBox mathFields = new HBox(5);
        mathFields.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> mathSrc1 = createRegisterComboBox();
        ComboBox<String> mathSrc2 = createRegisterComboBox();
        ComboBox<String> mathDest = createRegisterComboBox();
        // Set default values to avoid nulls
        if (registerSet.size() > 2) {
            mathSrc1.setValue("R0");
            mathSrc2.setValue("R1");
            mathDest.setValue("R2");
        }

        ComboBox<String> opSelector = new ComboBox<>();
        opSelector.getItems().addAll("+", "-", "×", "÷");
        opSelector.setValue("+");
        opSelector.setPrefWidth(50);

        // Enhanced styling for operation selector
        opSelector.setStyle(
            "-fx-background-color: " +
            BG_SECONDARY +
            "; " +
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; " +
            "-fx-border-color: " +
            BORDER +
            "; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;"
        );

        // Set cell factory for dropdown items
        opSelector.setCellFactory(listView ->
            new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-background-color: " +
                            BG_SECONDARY +
                            "; " +
                            "-fx-text-fill: " +
                            TEXT_PRIMARY +
                            "; " +
                            "-fx-padding: 4px 8px;"
                        );
                    }
                }
            }
        );

        // Set button cell for selected value
        opSelector.setButtonCell(
            new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: " +
                            TEXT_PRIMARY +
                            "; " +
                            "-fx-padding: 4px 8px;"
                        );
                    }
                }
            }
        );

        Label mathArrow = new Label("→");
        mathArrow.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;"
        );

        Button executeBtn = createButton("Execute", ACCENT);
        executeBtn.setOnAction(e -> {
            String op =
                switch (opSelector.getValue()) {
                    case "+" -> "add";
                    case "-" -> "sub";
                    case "×" -> "mul";
                    case "÷" -> "div";
                    default -> "add";
                };
            performOperation(
                op,
                mathSrc1.getValue(),
                mathSrc2.getValue(),
                mathDest.getValue()
            );
        });

        mathFields
            .getChildren()
            .addAll(
                mathSrc1,
                opSelector,
                mathSrc2,
                mathArrow,
                mathDest,
                executeBtn
            );

        // --- Copy Operation ---
        Label copyLabel = new Label("Copy: Source → Dest");
        copyLabel.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;"
        );

        HBox copyFields = new HBox(5);
        copyFields.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> copySrc = createRegisterComboBox();
        ComboBox<String> copyDest = createRegisterComboBox();
        if (registerSet.size() > 1) {
            copySrc.setValue("R0");
            copyDest.setValue("R1");
        }

        Label copyArrow = new Label("→");
        copyArrow.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;"
        );

        Button copyBtn = createButton("Copy", TEXT_SECONDARY);
        copyBtn.setOnAction(e ->
            performCopy(copySrc.getValue(), copyDest.getValue())
        );

        copyFields.getChildren().addAll(copySrc, copyArrow, copyDest, copyBtn);

        section
            .getChildren()
            .addAll(title, mathLabel, mathFields, copyLabel, copyFields);
        return section;
    }

    // --- UI Helper Methods ---

    private TextField createTextField(String prompt, int width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(
            "-fx-background-color: " +
            BG_SECONDARY +
            "; " +
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; " +
            "-fx-border-color: " +
            BORDER +
            "; " +
            "-fx-border-radius: 4px; " +
            "-fx-padding: 6px;"
        );

        field
            .focusedProperty()
            .addListener((obs, oldVal, newVal) -> {
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
            "-fx-background-color: " +
            color +
            "; " +
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

    /**
     * FIXED: Helper method to create a styled ComboBox for register selection with proper text visibility.
     */
    private ComboBox<String> createRegisterComboBox() {
        ObservableList<String> registerNames =
            FXCollections.observableArrayList();
        for (int i = 0; i < registerSet.size(); i++) {
            registerNames.add("R" + i);
        }
        ComboBox<String> comboBox = new ComboBox<>(registerNames);
        if (!registerNames.isEmpty()) {
            comboBox.setValue(registerNames.get(0));
        }
        comboBox.setPrefWidth(75);

        // Enhanced styling with proper text color and cell styling
        comboBox.setStyle(
            "-fx-background-color: " +
            BG_SECONDARY +
            "; " +
            "-fx-text-fill: " +
            TEXT_PRIMARY +
            "; " +
            "-fx-border-color: " +
            BORDER +
            "; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;"
        );

        // Set cell factory to ensure dropdown items are visible
        comboBox.setCellFactory(listView ->
            new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-background-color: " +
                            BG_SECONDARY +
                            "; " +
                            "-fx-text-fill: " +
                            TEXT_PRIMARY +
                            "; " +
                            "-fx-padding: 4px 8px;"
                        );
                    }
                }
            }
        );

        // Set button cell to ensure selected value is visible
        comboBox.setButtonCell(
            new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: " +
                            TEXT_PRIMARY +
                            "; " +
                            "-fx-padding: 4px 8px;"
                        );
                    }
                }
            }
        );

        return comboBox;
    }

    // --- Action Handler Methods ---

    /**
     * MODIFIED: Takes the value field to clear it, but not the register field (which is now a combo box).
     */
    private void performWrite(
        String regName,
        String valueText,
        TextField valueField
    ) {
        if (
            regName == null ||
            regName.isEmpty() ||
            valueText == null ||
            valueText.isEmpty()
        ) {
            showStatus("Please select a register and enter a value", WARNING);
            return;
        }

        try {
            int value = parseValue(valueText.trim());
            registerSet.write(regName, value);

            refreshRegisterTable();
            dumpRegisters();

            showStatus(
                String.format("Written %s = %d", regName, value),
                SUCCESS
            );

            valueField.clear(); // Only clear the value field
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), ERROR);
        }
    }

    private void performOperation(
        String op,
        String reg1,
        String reg2,
        String dest
    ) {
        if (reg1 == null || reg2 == null || dest == null) {
            showStatus(
                "Please select all registers for the operation",
                WARNING
            );
            return;
        }
        try {
            switch (op) {
                case "add" -> registerSet.add(reg1, reg2, dest);
                case "sub" -> registerSet.subtract(reg1, reg2, dest);
                case "mul" -> registerSet.multiply(reg1, reg2, dest);
                case "div" -> registerSet.divide(reg1, reg2, dest);
            }

            refreshRegisterTable();
            dumpRegisters();

            String symbol =
                switch (op) {
                    case "add" -> "+";
                    case "sub" -> "-";
                    case "mul" -> "×";
                    case "div" -> "÷";
                    default -> "?";
                };

            showStatus(
                String.format("%s %s %s → %s", reg1, symbol, reg2, dest),
                SUCCESS
            );
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), ERROR);
        }
    }

    private void performCopy(String source, String dest) {
        if (source == null || dest == null) {
            showStatus(
                "Please select source and destination registers",
                WARNING
            );
            return;
        }
        try {
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

    // --- UI Update and Utility Methods ---

    private void refreshRegisterTable() {
        ObservableList<RegisterRow> data = FXCollections.observableArrayList();

        for (int i = 0; i < registerSet.size(); i++) {
            String regName = "R" + i;
            if (registerSet.hasRegister(regName)) {
                Register reg = registerSet.get(regName);
                String status = reg.isModified() ? "Modified" : "Clean";
                data.add(
                    new RegisterRow(
                        regName,
                        reg.toHexString(),
                        reg.toDecimalString(),
                        status
                    )
                );
            }
        }

        registerTable.setItems(data);
    }

    private void dumpRegisters() {
        dumpArea.setText(registerSet.getDumpString());
    }

    private void clearAllRegisters() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Registers");
        alert.setHeaderText("Are you sure you want to proceed?");
        alert.setContentText("This will reset all register values to 0.");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            registerSet.clearAll();
            refreshRegisterTable();
            dumpRegisters();
            showStatus("All registers have been cleared.", SUCCESS);
        }
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle(
            "-fx-text-fill: " +
            color +
            "; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 10px; " +
            "-fx-background-color: " +
            BG_SECONDARY +
            "; " +
            "-fx-background-radius: 4px;"
        );

        FadeTransition fade = new FadeTransition(
            Duration.seconds(4),
            statusLabel
        );
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setOnFinished(e -> {
            statusLabel.setText(
                "Ready - " + registerSet.size() + " registers initialized"
            );
            statusLabel.setStyle(
                "-fx-text-fill: " +
                TEXT_SECONDARY +
                "; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 10px; " +
                "-fx-background-color: " +
                BG_SECONDARY +
                "; " +
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

    public static class RegisterRow {

        private final StringProperty name;
        private final StringProperty hexValue;
        private final StringProperty decValue;
        private final StringProperty status;

        public RegisterRow(
            String name,
            String hexValue,
            String decValue,
            String status
        ) {
            this.name = new SimpleStringProperty(name);
            this.hexValue = new SimpleStringProperty(hexValue);
            this.decValue = new SimpleStringProperty(decValue);
            this.status = new SimpleStringProperty(status);
        }

        public String getName() {
            return name.get();
        }

        public String getHexValue() {
            return hexValue.get();
        }

        public String getDecValue() {
            return decValue.get();
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty hexValueProperty() {
            return hexValue;
        }

        public StringProperty decValueProperty() {
            return decValue;
        }

        public StringProperty statusProperty() {
            return status;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
