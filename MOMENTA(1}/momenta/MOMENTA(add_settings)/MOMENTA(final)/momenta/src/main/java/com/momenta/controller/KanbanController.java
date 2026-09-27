package com.momenta.controller;

import com.momenta.dao.TaskDAO;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.RecurrenceUtil;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.List;

public class KanbanController {

    @FXML private SidebarController sidebarController;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox waitingColumn;
    @FXML private VBox doneColumn;
    @FXML private VBox todoCards;
    @FXML private VBox inProgressCards;
    @FXML private VBox waitingCards;
    @FXML private VBox doneCards;

    private List<Task> allTasks;

    @FXML
    public void initialize() {
        sidebarController.setActive("kanban");
        setupDropTarget(todoColumn, "PENDING");
        setupDropTarget(inProgressColumn, "IN_PROGRESS");
        setupDropTarget(waitingColumn, "WAITING");
        setupDropTarget(doneColumn, "DONE");
        loadTasks();
    }

    private void loadTasks() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new TaskDAO().getAllByUser(user.getId()),
                (List<Task> tasks) -> {
                    allTasks = tasks;
                    render();
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load tasks: " + error.getMessage()).showAndWait()
        );
    }

    private void render() {
        todoCards.getChildren().clear();
        inProgressCards.getChildren().clear();
        waitingCards.getChildren().clear();
        doneCards.getChildren().clear();
        for (Task t : allTasks) {
            VBox card = buildCard(t);
            switch (t.getStatus()) {
                case "IN_PROGRESS" -> inProgressCards.getChildren().add(card);
                case "WAITING" -> waitingCards.getChildren().add(card);
                case "DONE" -> doneCards.getChildren().add(card);
                default -> todoCards.getChildren().add(card);
            }
        }
    }

    private VBox buildCard(Task task) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kanban-card");

        Label title = new Label(task.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F2C4C;");

        javafx.scene.layout.HBox meta = new javafx.scene.layout.HBox(6);
        Label priority = new Label(task.getPriority() + "/5");
        priority.getStyleClass().addAll("badge", "badge-p" + task.getPriority());
        meta.getChildren().add(priority);
        if (task.getCategory() != null && !task.getCategory().isBlank()) {
            String firstTag = task.getCategory().split(",")[0].trim();
            Label cat = new Label(firstTag);
            cat.getStyleClass().add(com.momenta.util.ChipColors.styleClassFor(firstTag));
            meta.getChildren().add(cat);
        }

        card.getChildren().addAll(title, meta);
        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            Label deadline = new Label("Due " + task.getDeadline());
            deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            card.getChildren().add(deadline);
        }

        card.setOnDragDetected(e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId()));
            db.setContent(content);
            e.consume();
        });

        return card;
    }

    private void setupDropTarget(VBox column, String targetStatus) {
        column.setOnDragOver(e -> {
            if (e.getGestureSource() != column && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        column.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) column.getStyleClass().add("kanban-column-dragover");
            e.consume();
        });
        column.setOnDragExited(e -> {
            column.getStyleClass().remove("kanban-column-dragover");
            e.consume();
        });
        column.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int taskId = Integer.parseInt(db.getString());
                moveTask(taskId, targetStatus);
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private void moveTask(int taskId, String targetStatus) {
        Task task = allTasks.stream().filter(t -> t.getId() == taskId).findFirst().orElse(null);
        if (task == null) return;
        boolean wasAlreadyDone = "DONE".equals(task.getStatus());
        boolean movingToDone = "DONE".equals(targetStatus);

        AsyncUtil.run(
                () -> {
                    TaskDAO dao = new TaskDAO();
                    dao.updateStatus(taskId, targetStatus);
                    if (movingToDone && !wasAlreadyDone && RecurrenceUtil.isRecurring(task)) {
                        Task completedCopy = task;
                        completedCopy.setStatus("DONE");
                        dao.insert(RecurrenceUtil.buildNextOccurrence(completedCopy));
                    }
                    return null;
                },
                v -> loadTasks(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not move task: " + error.getMessage()).showAndWait()
        );
    }
}
