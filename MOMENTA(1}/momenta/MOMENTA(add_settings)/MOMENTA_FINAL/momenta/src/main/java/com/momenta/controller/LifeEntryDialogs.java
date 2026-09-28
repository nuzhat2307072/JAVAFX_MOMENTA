package com.momenta.controller;

import com.momenta.model.LifeEntry;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

/** One reusable "add entry" form shared by every Life Areas module (Learning, Career, ...). */
public class LifeEntryDialogs {

    public static Optional<LifeEntry> showAddDialog(String dialogTitle, String titlePrompt,
                                                      String subtitlePrompt, boolean showDate,
                                                      String notesPrompt) {
        Dialog<LifeEntry> dialog = new Dialog<>();
        dialog.setTitle(dialogTitle);
        dialog.getDialogPane().getStylesheets().add(
                LifeEntryDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText(titlePrompt);

        TextField subtitleField = new TextField();
        subtitleField.setPromptText(subtitlePrompt);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        TextArea notesArea = new TextArea();
        notesArea.setPromptText(notesPrompt);
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        int row = 0;
        grid.add(new Label(titlePrompt), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label(subtitlePrompt), 0, row);
        grid.add(subtitleField, 1, row++);
        if (showDate) {
            grid.add(new Label("Date"), 0, row);
            grid.add(datePicker, 1, row++);
        }
        grid.add(new Label(notesPrompt), 0, row);
        grid.add(notesArea, 1, row);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((obs, oldV, newV) ->
                saveButton.setDisable(newV.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) return null;
            LifeEntry entry = new LifeEntry();
            entry.setTitle(titleField.getText().trim());
            entry.setSubtitle(subtitleField.getText() == null ? "" : subtitleField.getText().trim());
            entry.setEntryDate(showDate && datePicker.getValue() != null ? datePicker.getValue().toString() : "");
            entry.setNotes(notesArea.getText() == null ? "" : notesArea.getText().trim());
            entry.setProgress(0);
            entry.setDone(false);
            return entry;
        });

        return dialog.showAndWait();
    }
}
