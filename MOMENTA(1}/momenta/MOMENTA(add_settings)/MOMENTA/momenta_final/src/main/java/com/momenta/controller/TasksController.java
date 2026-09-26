package com.momenta.controller;

import com.momenta.dao.SubtaskDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Subtask;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.RecurrenceUtil;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TasksController {

    @FXML private SidebarController sidebarController;

    @FXML private ComboBox<String> filterBox;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colCategory;
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
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colCategory.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) { setGraphic(null); return; }
                HBox chips = new HBox(4);
                for (String tag : value.split(",")) {
                    String t = tag.trim();
                    if (t.isEmpty()) continue;
                    Label chip = new Label(t);
                    chip.getStyleClass().add("category-chip");
                    chips.getChildren().add(chip);
                }
                setGraphic(chips);
            }
        });

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
                bar.setPrefWidth(90);
                setGraphic(new HBox(6, bar, new Label(value.intValue() + "%")));
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button subBtn = new Button("Subtasks");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.getStyleClass().add("secondary-button");
                subBtn.getStyleClass().add("secondary-button");
                deleteBtn.getStyleClass().add("danger-button");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                subBtn.setOnAction(e -> SubtaskDialogs.showSubtasksDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, editBtn, subBtn, deleteBtn));
            }
        });

        filterBox.setItems(FXCollections.observableArrayList(
                "All Tasks", "Pending", "In Progress", "Waiting", "Done", "High Priority (4-5)"));
        filterBox.setValue("All Tasks");
        filterBox.valueProperty().addListener((obs, o, n) -> applyFilter());

        categoryFilterBox.valueProperty().addListener((obs, o, n) -> applyFilter());

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
                    refreshCategoryFilterOptions();
                    applyFilter();
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load tasks: " + error.getMessage()).showAndWait()
        );
    }

    private void refreshCategoryFilterOptions() {
        String current = categoryFilterBox.getValue();
        Set<String> categories = new HashSet<>();
        for (Task t : allTasks) {
            if (t.getCategory() == null) continue;
            for (String tag : t.getCategory().split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) categories.add(trimmed);
            }
        }
        ObservableList<String> items = FXCollections.observableArrayList("All Categories");
        items.addAll(categories.stream().sorted().toList());
        categoryFilterBox.setItems(items);
        categoryFilterBox.setValue(current != null && items.contains(current) ? current : "All Categories");
    }

    private void applyFilter() {
        String statusFilter = filterBox.getValue();
        String categoryFilter = categoryFilterBox.getValue();

        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : allTasks) {
            boolean statusMatch = statusFilter == null || statusFilter.equals("All Tasks") || switch (statusFilter) {
                case "Pending" -> "PENDING".equals(t.getStatus());
                case "In Progress" -> "IN_PROGRESS".equals(t.getStatus());
                case "Waiting" -> "WAITING".equals(t.getStatus());
                case "Done" -> "DONE".equals(t.getStatus());
                case "High Priority (4-5)" -> t.getPriority() >= 4;
                default -> true;
            };
            boolean categoryMatch = categoryFilter == null || categoryFilter.equals("All Categories")
                    || (t.getCategory() != null && java.util.Arrays.stream(t.getCategory().split(","))
                            .map(String::trim).anyMatch(categoryFilter::equalsIgnoreCase));
            if (statusMatch && categoryMatch) filtered.add(t);
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
        boolean wasAlreadyDone = "DONE".equals(task.getStatus());
        Optional<Task> result = TaskDialogs.showEditTaskDialog(task);
        result.ifPresent(updated -> AsyncUtil.run(
                () -> {
                    TaskDAO dao = new TaskDAO();
                    dao.update(updated);
                    // Recurring Tasks: freshly completed + recurring -> queue up the next occurrence.
                    boolean justCompleted = "DONE".equals(updated.getStatus()) && !wasAlreadyDone;
                    if (justCompleted && RecurrenceUtil.isRecurring(updated)) {
                        dao.insert(RecurrenceUtil.buildNextOccurrence(updated));
                    }
                    return null;
                },
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
