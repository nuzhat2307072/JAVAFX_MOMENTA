package com.momenta.controller;

import com.momenta.dao.TaskDAO;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.NavigationManager;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private SidebarController sidebarController;

    @FXML private Label greetingLabel;
    @FXML private Label occupationLabel;
    @FXML private Label workloadWarningLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label highPriorityLabel;
    @FXML private Label dueTodayLabel;
    @FXML private Label recommendedTitleLabel;
    @FXML private Label recommendedPriorityLabel;
    @FXML private Label recommendedDeadlineLabel;
    @FXML private Label recommendedProgressLabel;
    @FXML private ListView<Task> savedTasksList;

    private Task recommended;

    @FXML
    public void initialize() {
        sidebarController.setActive("dashboard");

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            greetingLabel.setText(greeting() + ", " + user.getUsername());
            occupationLabel.setText(user.getOccupation());
        }

        savedTasksList.setCellFactory(list -> new TaskCell());
        loadTasks();
    }

    private String greeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private void loadTasks() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        AsyncUtil.run(
                () -> new TaskDAO().getAllByUser(user.getId()),
                (List<Task> tasks) -> {
                    ObservableList<Task> data = FXCollections.observableArrayList(tasks);
                    savedTasksList.setItems(data);
                    updateStats(tasks);
                },
                error -> recommendedTitleLabel.setText("Could not load tasks: " + error.getMessage())
        );
    }

    private void updateStats(List<Task> tasks) {
        int total = tasks.size();
        long highPriority = tasks.stream().filter(t -> t.getPriority() >= 4 && !"DONE".equals(t.getStatus())).count();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        long dueToday = tasks.stream().filter(t -> today.equals(t.getDeadline())).count();

        totalTasksLabel.setText(String.valueOf(total));
        highPriorityLabel.setText(String.valueOf(highPriority));
        dueTodayLabel.setText(String.valueOf(dueToday));

        if (total > 10 || highPriority >= 5) {
            workloadWarningLabel.setText("⚠ Your schedule contains a high number of active or high-priority tasks. Consider re-prioritizing. (This is a workload notice, not a medical diagnosis.)");
            workloadWarningLabel.setVisible(true);
            workloadWarningLabel.setManaged(true);
        } else {
            workloadWarningLabel.setVisible(false);
            workloadWarningLabel.setManaged(false);
        }

        recommended = recommend(tasks, today);
        if (recommended == null) {
            recommendedTitleLabel.setText("No tasks yet — add one to get started!");
            recommendedPriorityLabel.setText("Priority: -");
            recommendedDeadlineLabel.setText("Deadline: -");
            recommendedProgressLabel.setText("Progress: -");
        } else {
            recommendedTitleLabel.setText(recommended.getTitle());
            recommendedPriorityLabel.setText("Priority: " + recommended.getPriority() + "/5");
            recommendedDeadlineLabel.setText("Deadline: " + (recommended.getDeadline() == null || recommended.getDeadline().isEmpty() ? "None" : recommended.getDeadline()));
            recommendedProgressLabel.setText("Progress: " + recommended.getProgress() + "%");
        }
    }

    /**
     * Picks the task most deserving attention right now, based on the user's own
     * priority, whether it's overdue/due today, and how far along it is.
     * Does not override the user's chosen priority — only ranks among it.
     */
    private Task recommend(List<Task> tasks, String today) {
        return tasks.stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .max(Comparator
                        .comparing((Task t) -> isOverdueOrToday(t, today))
                        .thenComparingInt(Task::getPriority)
                        .thenComparingInt(t -> -t.getProgress()))
                .orElse(null);
    }

    private boolean isOverdueOrToday(Task t, String today) {
        String d = t.getDeadline();
        if (d == null || d.isEmpty()) return false;
        try {
            return !d.isEmpty() && d.compareTo(today) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void handleQuickAdd() {
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

    @FXML
    private void handleStartFocus() {
        NavigationManager.navigate("focus.fxml", "MOMENTA - Focus Mode");
    }

    /** Read-only summary row for a task in the dashboard's list. */
    private static class TaskCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setGraphic(null);
                return;
            }
            HBox row = new HBox(14);
            row.setStyle("-fx-padding: 8 4;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox titleBox = new VBox(2);
            Label title = new Label(task.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F2C4C;");
            Label deadline = new Label("Deadline: " + (task.getDeadline() == null || task.getDeadline().isEmpty() ? "None" : task.getDeadline()));
            deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            titleBox.getChildren().addAll(title, deadline);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label progress = new Label(task.getProgress() + "%");
            progress.setStyle("-fx-text-fill: #6B7280;");

            Label badge = new Label(task.getPriority() + "/5");
            badge.getStyleClass().addAll("badge", "badge-p" + task.getPriority());

            row.getChildren().addAll(titleBox, spacer, progress, badge);
            setGraphic(row);
        }
    }
}
