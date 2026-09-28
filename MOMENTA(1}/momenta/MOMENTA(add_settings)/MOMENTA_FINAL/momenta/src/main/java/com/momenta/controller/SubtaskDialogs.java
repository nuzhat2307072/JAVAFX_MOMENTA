package com.momenta.controller;

import com.momenta.dao.SubtaskDAO;
import com.momenta.model.Subtask;
import com.momenta.model.Task;
import com.momenta.util.AsyncUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class SubtaskDialogs {

    public static void showSubtasksDialog(Task task) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Subtasks — " + task.getTitle());
        dialog.getDialogPane().getStylesheets().add(
                SubtaskDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setPrefWidth(360);

        VBox listBox = new VBox(6);
        Button addButton = new Button("+ Add Subtask");
        addButton.getStyleClass().add("secondary-button");

        Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> AsyncUtil.run(
                () -> new SubtaskDAO().getAllByTask(task.getId()),
                (List<Subtask> subtasks) -> {
                    listBox.getChildren().clear();
                    if (subtasks.isEmpty()) {
                        Label empty = new Label("No subtasks yet.");
                        empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
                        listBox.getChildren().add(empty);
                    }
                    for (Subtask s : subtasks) {
                        CheckBox cb = new CheckBox(s.getTitle());
                        cb.setSelected(s.isDone());
                        cb.selectedProperty().addListener((obs, oldV, newV) -> AsyncUtil.run(
                                () -> { new SubtaskDAO().toggleDone(s.getId(), newV); return null; },
                                v -> { }, error -> { }));

                        Button removeBtn = new Button("✕");
                        removeBtn.getStyleClass().add("danger-button");
                        removeBtn.setOnAction(e -> AsyncUtil.run(
                                () -> { new SubtaskDAO().delete(s.getId()); return null; },
                                v -> refreshHolder[0].run(),
                                error -> { }));

                        HBox row = new HBox(8, cb, removeBtn);
                        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        listBox.getChildren().add(row);
                    }
                },
                error -> { }
        );

        addButton.setOnAction(e -> {
            TextInputDialog input = new TextInputDialog();
            input.setTitle("Add Subtask");
            input.setHeaderText(null);
            input.setContentText("Subtask:");
            Optional<String> result = input.showAndWait();
            result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> AsyncUtil.run(
                    () -> new SubtaskDAO().insert(new Subtask(0, task.getId(), text.trim(), false)),
                    saved -> refreshHolder[0].run(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not add subtask: " + error.getMessage()).showAndWait()
            ));
        });

        root.getChildren().addAll(listBox, addButton);
        dialog.getDialogPane().setContent(root);

        refreshHolder[0].run();
        dialog.showAndWait();
    }
}
