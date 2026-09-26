package com.momenta.controller;

import com.momenta.model.FinanceEntry;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public class FinanceDialogs {

    public static Optional<FinanceEntry> showAddEntryDialog() {
        Dialog<FinanceEntry> dialog = new Dialog<>();
        dialog.setTitle("Add Finance Entry");
        dialog.getDialogPane().getStylesheets().add(
                FinanceDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("INCOME", "EXPENSE"));
        typeBox.setValue("EXPENSE");

        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
                "Salary", "Freelance", "Food", "Transport", "Office", "Software", "Equipment", "Bills", "Other"));
        categoryBox.setValue("Other");
        categoryBox.setEditable(true);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Type"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Category"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Amount"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Date"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        amountField.textProperty().addListener((obs, oldV, newV) ->
                saveButton.setDisable(!newV.matches("\\d+(\\.\\d+)?")));

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) return null;
            FinanceEntry entry = new FinanceEntry();
            entry.setType(typeBox.getValue());
            entry.setCategory(categoryBox.getValue());
            entry.setAmount(Double.parseDouble(amountField.getText()));
            entry.setDate((datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue()).toString());
            return entry;
        });

        return dialog.showAndWait();
    }
}
