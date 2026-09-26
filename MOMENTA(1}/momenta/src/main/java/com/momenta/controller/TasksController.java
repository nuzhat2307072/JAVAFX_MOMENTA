package com.momenta.controller;

import com.momenta.dao.TaskDAO;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class TasksController {

    @FXML private SidebarController sidebarController;

    @FXML private ComboBox<String> filterBox;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colDeadline;
    @FXML private TableColumn<Task, Number> colPriority;
    @FXML private TableColumn<Task, Number> colProgress;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, Void> colActions;

    private final ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        sidebarController.setActive("tasks");

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                Label badge = new Label(value.intValue() + "/5");
                badge.getStyleClass().addAll("badge", "badge-p" + value.intValue());
                setGraphic(badge);
            }
        });

        colProgress.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                ProgressBar bar = new ProgressBar(value.doubleValue() / 100.0);
                bar.setPrefWidth(100);
                setGraphic(new HBox(6, bar, new Label(value.intValue() + "%")));
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.getStyleClass().add("secondary-button");
                deleteBtn.getStyleClass().add("danger-button");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, editBtn, deleteBtn));
            }
        });

        filterBox.setItems(FXCollections.observableArrayList(
                "All Tasks", "Pending", "In Progress", "Done", "High Priority (4-5)"));
        filterBox.setValue("All Tasks");
        filterBox.valueProperty().addListener((obs, o, n) -> applyFilter());

        taskTable.setItems(allTasks);
        loadTasks();
    }

    private void loadTasks() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new TaskDAO().getAllByUser(user.getId()),
                (List<Task> tasks) -> {
                    allTasks.setAll(tasks);
                    applyFilter();
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load tasks: " + error.getMessage()).showAndWait()
        );
    }

    private void applyFilter() {
        String filter = filterBox.getValue();
        if (filter == null || filter.equals("All Tasks")) {
            taskTable.setItems(allTasks);
            return;
        }
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : allTasks) {
            boolean match = switch (filter) {
                case "Pending" -> "PENDING".equals(t.getStatus());
                case "In Progress" -> "IN_PROGRESS".equals(t.getStatus());
                case "Done" -> "DONE".equals(t.getStatus());
                case "High Priority (4-5)" -> t.getPriority() >= 4;
                default -> true;
            };
            if (match) filtered.add(t);
        }
        taskTable.setItems(filtered);
    }

    @FXML
    private void handleAddTask() {
        Optional<Task> result = TaskDialogs.showAddTaskDialog();
        result.ifPresent(task -> {
            User user = SessionManager.getCurrentUser();
            task.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new TaskDAO().insert(task),
                    saved -> loadTasks(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not save task: " + error.getMessage()).showAndWait()
            );
        });
    }

    private void handleEdit(Task task) {
        Optional<Task> result = TaskDialogs.showEditTaskDialog(task);
        result.ifPresent(updated -> AsyncUtil.run(
                () -> { new TaskDAO().update(updated); return null; },
                v -> loadTasks(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not update task: " + error.getMessage()).showAndWait()
        ));
    }

    private void handleDelete(Task task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + task.getTitle() + "\"? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AsyncUtil.run(
                        () -> { new TaskDAO().delete(task.getId()); return null; },
                        v -> loadTasks(),
                        error -> new Alert(Alert.AlertType.ERROR, "Could not delete task: " + error.getMessage()).showAndWait()
                );
            }
        });
    }
}
