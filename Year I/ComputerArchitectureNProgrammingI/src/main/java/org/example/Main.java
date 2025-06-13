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
import javafx.scene.layout.Priority; // MODIFICATION: Import Priority
import javafx.scene.layout.Region;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.util.Callback;
import javafx.scene.Parent;

public class Main extends Application {

    private RegisterSet registerSet;
    private TableView<RegisterRow> registerTable;
    private Label statusLabel;
    private DatabaseManager dbManager;

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
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label headerLabel = new Label("CPU Register Simulator");
        headerLabel.getStyleClass().add("label");
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-padding: 0 0 16px 0;");

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(16, 0, 16, 0));

        VBox leftPanel = createRegisterPanel();
        VBox rightPanel = createControlPanel();

        // MODIFICATION: Set Hgrow policy to allow the right panel to expand
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        leftPanel.setPadding(new Insets(10));
        rightPanel.setPadding(new Insets(10));

        mainContent.getChildren().addAll(leftPanel, rightPanel);

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("label");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-padding: 14px; -fx-background-radius: 8px;");

        root.getChildren().addAll(headerLabel, mainContent, statusLabel);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/org/example/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshRegisterTable();

        String initialStatus = stateToLoad.isPresent() && stateToLoad.get() > 0
            ? "Successfully loaded saved state."
            : "New session started.";
        showStatus(initialStatus, "success");
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

    @SuppressWarnings({"unchecked", "deprecation"})
    private VBox createRegisterPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(400); // MODIFICATION: Set a preferred width for the left panel
        panel.setMinWidth(350);  // MODIFICATION: Set a minimum width
        panel.setPadding(new Insets(18));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().add("card");

        Label title = new Label("Registers");
        title.getStyleClass().addAll("label", "title-label");

        registerTable = new TableView<>();
        registerTable.setPrefHeight(300);
        VBox.setVgrow(registerTable, Priority.ALWAYS); // MODIFICATION: Allow table to grow vertically

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
        registerTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        panel.getChildren().addAll(title, registerTable);
        return panel;
    }

    // MODIFICATION: Main control panel setup
    private VBox createControlPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0)); // MODIFICATION: Padding is now handled by individual cards
        panel.setAlignment(Pos.TOP_CENTER);

        VBox writeSection = createWriteSection();
        VBox opsSection = createOperationsSection();
        VBox controlSection = createControlButtons();

        panel.getChildren().addAll(writeSection, opsSection, controlSection);
        return panel;
    }

    // MODIFICATION: Cleaned up control buttons section
    private VBox createControlButtons() {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(14));
        section.getStyleClass().add("card");

        Button clearBtn = createButton("Clear All", "button-error");
        clearBtn.setPrefWidth(120);
        clearBtn.setOnAction(e -> clearAllRegisters());

        section.getChildren().addAll(clearBtn);
        return section;
    }

    // MODIFICATION: Adjusted layout for better alignment
    private VBox createWriteSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(18));
        section.setAlignment(Pos.CENTER_LEFT);
        section.getStyleClass().add("card");

        Label title = new Label("Write Register");
        title.getStyleClass().addAll("label", "title-label");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 0 0 8px 0;");

        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(5, 0, 0, 0));

        ComboBox<String> regSelector = createRegisterComboBox();
        regSelector.setPrefWidth(90); // MODIFICATION: Standardized width

        Label equalLabel = new Label("=");
        equalLabel.getStyleClass().add("label");
        equalLabel.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 14px;");

        TextField valueField = createTextField("123", 100);
        HBox.setHgrow(valueField, Priority.ALWAYS); // MODIFICATION: Allow value field to grow

        Button writeBtn = createButton("Write", "button-accent");
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

    // MODIFICATION: Reworked layout for perfect alignment
    private VBox createOperationsSection() {
        VBox section = new VBox(15); // MODIFICATION: Increased spacing
        section.setPadding(new Insets(18));
        section.setAlignment(Pos.CENTER_LEFT);
        section.getStyleClass().add("card");

        Label title = new Label("Operations");
        title.getStyleClass().addAll("label", "title-label");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 0 0 8px 0;");

        // --- Math Operations ---
        Label mathLabel = new Label("Math: Reg1 OP Reg2 = Dest");
        mathLabel.getStyleClass().add("label");
        mathLabel.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 11px;");

        HBox mathFields = new HBox(10); // MODIFICATION: Standardized spacing
        mathFields.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> mathSrc1 = createRegisterComboBox();
        ComboBox<String> mathSrc2 = createRegisterComboBox();
        ComboBox<String> mathDest = createRegisterComboBox();
        if (registerSet.size() > 2) {
            mathSrc1.setValue("R0");
            mathSrc2.setValue("R1");
            mathDest.setValue("R2");
        }

        ComboBox<Operation> opSelector = new ComboBox<>();
        opSelector.getItems().addAll(
            new Operation("add", "+"),
            new Operation("sub", "-"),
            new Operation("mul", "×"),
            new Operation("div", "÷")
        );
        opSelector.setValue(opSelector.getItems().get(0));
        opSelector.setPrefWidth(70); // MODIFICATION: Adjusted width
        opSelector.getStyleClass().add("combo-box");
        Callback<ListView<Operation>, ListCell<Operation>> opFactory = createStyledCellFactory();
        opSelector.setCellFactory(opFactory);
        opSelector.setButtonCell(opFactory.call(null));

        Label mathArrow = new Label("=");
        mathArrow.getStyleClass().add("label");
        mathArrow.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 14px;");

        // MODIFICATION: Create a spacer to push the button to the right
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button executeBtn = createButton("Execute", "button-accent");
        executeBtn.setOnAction(e -> {
            Operation op = opSelector.getValue();
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
                mathSrc1, opSelector, mathSrc2, mathArrow, mathDest, spacer1, executeBtn
            );

        // --- Copy Operation ---
        Label copyLabel = new Label("Copy: Source → Dest");
        copyLabel.getStyleClass().add("label");
        copyLabel.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 11px;");

        HBox copyFields = new HBox(10); // MODIFICATION: Standardized spacing
        copyFields.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> copySrc = createRegisterComboBox();
        ComboBox<String> copyDest = createRegisterComboBox();
        if (registerSet.size() > 1) {
            copySrc.setValue("R0");
            copyDest.setValue("R1");
        }

        Label copyArrow = new Label("→");
        copyArrow.getStyleClass().add("label");
        copyArrow.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 14px;");

        // MODIFICATION: Create another spacer
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button copyBtn = createButton("Copy", "button-accent");
        copyBtn.setOnAction(e ->
            performCopy(copySrc.getValue(), copyDest.getValue())
        );

        copyFields.getChildren().addAll(copySrc, copyArrow, copyDest, spacer2, copyBtn);

        section
            .getChildren()
            .addAll(title, mathLabel, mathFields, new Separator(), copyLabel, copyFields); // MODIFICATION: Added Separator
        return section;
    }

    // --- UI Helper Methods ---

    private TextField createTextField(String prompt, int width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        // MODIFICATION: Let HGrow manage width instead of a fixed preferred width
        // field.setPrefWidth(width);
        field.getStyleClass().add("text-field");
        return field;
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        return button;
    }

    private <T> Callback<ListView<T>, ListCell<T>> createStyledCellFactory() {
        return lv -> {
            ListCell<T> cell = new ListCell<>() {
                @Override protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item == null ? null : item.toString());
                }
            };
            cell.getStyleClass().add("label");
            return cell;
        };
    }

    private ComboBox<String> createRegisterComboBox() {
        ObservableList<String> names = FXCollections.observableArrayList();
        for (int i = 0; i < registerSet.size(); i++) {
            names.add("R" + i);
        }
        ComboBox<String> cb = new ComboBox<>(names);
        if (!names.isEmpty()) cb.setValue(names.get(0));
        cb.setPrefWidth(90); // MODIFICATION: Standardized width
        cb.getStyleClass().add("combo-box");
        Callback<ListView<String>, ListCell<String>> factory = createStyledCellFactory();
        cb.setCellFactory(factory);
        cb.setButtonCell(factory.call(null));
        return cb;
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
            showStatus("Please select a register and enter a value", "warning");
            return;
        }

        try {
            int value = parseValue(valueText.trim());
            registerSet.write(regName, value);

            refreshRegisterTable();

            showStatus(
                String.format("Written %s = %d", regName, value),
                "success"
            );

            valueField.clear(); // Only clear the value field
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), "error");
        }
    }

    private record Operation(String name, String symbol) {
        @Override
        public String toString() { return symbol; }
    }

    private void performOperation(
        Operation op,
        String reg1,
        String reg2,
        String dest
    ) {
        if (op == null || reg1 == null || reg2 == null || dest == null) {
            showStatus("Please select all registers for the operation", "warning");
            return;
        }
        try {
            switch (op.name()) {
                case "add" -> registerSet.add(reg1, reg2, dest);
                case "sub" -> registerSet.subtract(reg1, reg2, dest);
                case "mul" -> registerSet.multiply(reg1, reg2, dest);
                case "div" -> registerSet.divide(reg1, reg2, dest);
            }

            refreshRegisterTable();
            showStatus(
                String.format("%s %s %s → %s", reg1, op.symbol(), reg2, dest),
                "success"
            );
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), "error");
        }
    }

    private void performCopy(String source, String dest) {
        if (source == null || dest == null) {
            showStatus(
                "Please select source and destination registers",
                "warning"
            );
            return;
        }
        try {
            registerSet.copy(source, dest);
            refreshRegisterTable();
            showStatus(String.format("Copied %s → %s", source, dest), "success");
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), "error");
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

    private void clearAllRegisters() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Registers");
        alert.setHeaderText("Are you sure you want to proceed?");
        alert.setContentText("This will reset all register values to 0.");

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            registerSet.clearAll();
            refreshRegisterTable();
            showStatus("All registers have been cleared.", "success");
        }
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning");
        String cssClass = switch (type) {
            case "success" -> "status-success";
            case "error" -> "status-error";
            case "warning" -> "status-warning";
            default -> "status-success";
        };
        statusLabel.getStyleClass().add(cssClass);

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
            statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning");
            statusLabel.getStyleClass().add("status-success");
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