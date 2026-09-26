package com.momenta.controller;

import com.momenta.model.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/** Small self-contained dialogs for creating / editing a Task. */
public class TaskDialogs {

    public static Optional<Task> showAddTaskDialog() {
        return showDialog(null);
    }

    public static Optional<Task> showEditTaskDialog(Task existing) {
        return showDialog(existing);
    }

    private static Optional<Task> showDialog(Task existing) {
        boolean editing = existing != null;

        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit Task" : "Add New Task");
        dialog.getDialogPane().getStylesheets().add(
                TaskDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType(editing ? "Save" : "Add Task", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(editing ? existing.getTitle() : "");
        titleField.setPromptText("Task title");

        TextArea descriptionArea = new TextArea(editing ? existing.getDescription() : "");
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);

        DatePicker deadlinePicker = new DatePicker();
        if (editing && existing.getDeadline() != null && !existing.getDeadline().isEmpty()) {
            try {
                deadlinePicker.setValue(java.time.LocalDate.parse(existing.getDeadline()));
            } catch (Exception ignored) { }
        }

        ComboBox<Integer> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(1, 2, 3, 4, 5);
        priorityBox.setValue(editing ? existing.getPriority() : 3);

        Slider progressSlider = new Slider(0, 100, editing ? existing.getProgress() : 0);
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        Label progressValueLabel = new Label(((int) progressSlider.getValue()) + "%");
        progressSlider.valueProperty().addListener((obs, oldV, newV) ->
                progressValueLabel.setText(newV.intValue() + "%"));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("PENDING", "IN_PROGRESS", "DONE");
        statusBox.setValue(editing ? existing.getStatus() : "PENDING");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Title"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Deadline"), 0, 2);
        grid.add(deadlinePicker, 1, 2);
        grid.add(new Label("Priority (you decide)"), 0, 3);
        grid.add(priorityBox, 1, 3);
        grid.add(new Label("Progress"), 0, 4);
        grid.add(new javafx.scene.layout.HBox(10, progressSlider, progressValueLabel), 1, 4);
        grid.add(new Label("Status"), 0, 5);
        grid.add(statusBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        titleField.requestFocus();

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(titleField.textProperty().isEmpty());

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) return null;
            Task task = editing ? existing : new Task();
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionArea.getText());
            task.setDeadline(deadlinePicker.getValue() == null ? "" : deadlinePicker.getValue().toString());
            task.setPriority(priorityBox.getValue());
            task.setProgress((int) progressSlider.getValue());
            task.setStatus(statusBox.getValue());
            return task;
        });

        return dialog.showAndWait();
    }
}
