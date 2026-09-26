package com.momenta.controller;

import com.momenta.model.Reminder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public class ReminderDialogs {

    public static Optional<Reminder> showAddReminderDialog(LocalDate defaultDate) {
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Add Reminder");
        dialog.getDialogPane().getStylesheets().add(
                ReminderDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Add Reminder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Reminder title");

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Note (optional)");
        noteArea.setPrefRowCount(2);

        DatePicker datePicker = new DatePicker(defaultDate == null ? LocalDate.now() : defaultDate);

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 9);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Title"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Note"), 0, 1);
        grid.add(noteArea, 1, 1);
        grid.add(new Label("Date"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Time"), 0, 3);
        grid.add(new javafx.scene.layout.HBox(6, hourSpinner, new Label(":"), minuteSpinner), 1, 3);

        dialog.getDialogPane().setContent(grid);
        titleField.requestFocus();

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(titleField.textProperty().isEmpty());

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) return null;
            String time = String.format("%02d:%02d", hourSpinner.getValue(), minuteSpinner.getValue());
            Reminder r = new Reminder();
            r.setTitle(titleField.getText().trim());
            r.setNote(noteArea.getText());
            r.setDate((datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue()).toString());
            r.setTime(time);
            return r;
        });

        return dialog.showAndWait();
    }
}
