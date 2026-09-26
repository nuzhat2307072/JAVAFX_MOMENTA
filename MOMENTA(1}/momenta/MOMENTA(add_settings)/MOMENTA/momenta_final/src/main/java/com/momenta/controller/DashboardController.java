package com.momenta.controller;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.GoalDAO;
import com.momenta.dao.ReminderDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Goal;
import com.momenta.model.Reminder;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController {

    @FXML private SidebarController sidebarController;

    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;
    @FXML private Label streakLabel;
    @FXML private Label workloadWarningLabel;

    @FXML private Label overdueLabel;
    @FXML private Label dueTodayLabel;
    @FXML private Label completedTodayLabel;
    @FXML private Label focusedTodayLabel;

    @FXML private Label doNowTitleLabel;
    @FXML private Label doNowReasonLabel;
    @FXML private Button doNowFocusButton;
    @FXML private Label doNextTitleLabel;
    @FXML private Label doNextReasonLabel;
    @FXML private Label quickWinTitleLabel;
    @FXML private Label quickWinReasonLabel;

    @FXML private VBox scheduleBox;
    @FXML private VBox categoryBreakdownBox;
    @FXML private FlowPane achievementsBox;
    @FXML private ListView<Task> savedTasksList;

    private static class DashboardData {
        List<Task> tasks;
        List<Reminder> todayReminders;
        int focusMinutesToday;
        int focusStreak;
        int totalFocusMinutes;
        int goalsCompleted;
    }

    @FXML
    public void initialize() {
        sidebarController.setActive("dashboard");

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            greetingLabel.setText(greeting() + ", " + user.getUsername() + " 👋");
        }
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));

        savedTasksList.setCellFactory(list -> new TaskCell());
        loadDashboard();
    }

    private String greeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private void loadDashboard() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        int userId = user.getId();
        String today = LocalDate.now().toString();

        AsyncUtil.run(
                () -> {
                    DashboardData data = new DashboardData();
                    data.tasks = new TaskDAO().getAllByUser(userId);
                    data.todayReminders = new ReminderDAO().getByDate(userId, today);
                    FocusSessionDAO focusDAO = new FocusSessionDAO();
                    data.focusMinutesToday = focusDAO.getMinutesToday(userId);
                    data.focusStreak = StreakUtil.computeStreak(focusDAO.getDistinctDatesDesc(userId));
                    data.totalFocusMinutes = focusDAO.getTotalMinutes(userId);
                    List<Goal> goals = new GoalDAO().getAllByUser(userId);
                    data.goalsCompleted = (int) goals.stream().filter(g -> g.getProgress() >= 100).count();
                    return data;
                },
                (DashboardData data) -> {
                    render(data);
                    BackupManager.autoBackup(userId);
                },
                error -> doNowTitleLabel.setText("Could not load dashboard: " + error.getMessage())
        );
    }

    private void render(DashboardData data) {
        String today = LocalDate.now().toString();
        List<Task> tasks = data.tasks;

        // Your Day stats
        long overdue = tasks.stream().filter(t -> !"DONE".equals(t.getStatus()))
                .filter(t -> hasDate(t) && t.getDeadline().compareTo(today) < 0).count();
        long dueToday = tasks.stream().filter(t -> !"DONE".equals(t.getStatus()))
                .filter(t -> hasDate(t) && t.getDeadline().equals(today)).count();
        long completedToday = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null && t.getCompletedAt().startsWith(today))
                .count();

        overdueLabel.setText(String.valueOf(overdue));
        dueTodayLabel.setText(String.valueOf(dueToday));
        completedTodayLabel.setText(String.valueOf(completedToday));
        focusedTodayLabel.setText(data.focusMinutesToday + " min");
        streakLabel.setText(data.focusStreak > 0 ? "🔥 " + data.focusStreak + "-day focus streak" : "");

        long highPriority = tasks.stream().filter(t -> t.getPriority() >= 4 && !"DONE".equals(t.getStatus())).count();
        if (tasks.size() > 10 || highPriority >= 5) {
            workloadWarningLabel.setText("⚠ Your schedule contains a high number of active or high-priority tasks. Consider re-prioritizing. (This is a workload notice, not a medical diagnosis.)");
            workloadWarningLabel.setVisible(true);
            workloadWarningLabel.setManaged(true);
        } else {
            workloadWarningLabel.setVisible(false);
            workloadWarningLabel.setManaged(false);
        }

        // What Should I Do Now? — 3-tier
        RecommendationEngine.Triage triage = RecommendationEngine.build(tasks);
        if (triage.doNow.isPresent()) {
            doNowTitleLabel.setText(triage.doNow.get().task.getTitle());
            doNowReasonLabel.setText("Because: " + triage.doNow.get().reason);
            doNowFocusButton.setVisible(true);
            doNowFocusButton.setManaged(true);
        } else {
            doNowTitleLabel.setText("Nothing urgent right now 🎉");
            doNowReasonLabel.setText("");
            doNowFocusButton.setVisible(false);
            doNowFocusButton.setManaged(false);
        }
        if (triage.doNext.isPresent()) {
            doNextTitleLabel.setText(triage.doNext.get().task.getTitle());
            doNextReasonLabel.setText("Because: " + triage.doNext.get().reason);
        } else {
            doNextTitleLabel.setText("—");
            doNextReasonLabel.setText("");
        }
        if (triage.quickWin.isPresent()) {
            quickWinTitleLabel.setText(triage.quickWin.get().task.getTitle());
            quickWinReasonLabel.setText("Because: " + triage.quickWin.get().reason);
        } else {
            quickWinTitleLabel.setText("—");
            quickWinReasonLabel.setText("");
        }

        // Today's Schedule
        scheduleBox.getChildren().clear();
        if (data.todayReminders.isEmpty() && dueToday == 0) {
            Label empty = new Label("Nothing scheduled for today.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            scheduleBox.getChildren().add(empty);
        } else {
            for (Reminder r : data.todayReminders) {
                scheduleBox.getChildren().add(scheduleRow(r.getTime(), r.getTitle()));
            }
            for (Task t : tasks) {
                if (!"DONE".equals(t.getStatus()) && hasDate(t) && t.getDeadline().equals(today)) {
                    scheduleBox.getChildren().add(scheduleRow("Due", t.getTitle()));
                }
            }
        }

        // Where is your time going?
        categoryBreakdownBox.getChildren().clear();
        Map<String, Integer> tagCounts = new LinkedHashMap<>();
        int totalTagInstances = 0;
        for (Task t : tasks) {
            if (t.getCategory() == null || t.getCategory().isBlank()) continue;
            for (String tag : t.getCategory().split(",")) {
                String trimmed = tag.trim();
                if (trimmed.isEmpty()) continue;
                tagCounts.merge(trimmed, 1, Integer::sum);
                totalTagInstances++;
            }
        }
        if (totalTagInstances == 0) {
            Label empty = new Label("Add categories to your tasks to see a breakdown here.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            categoryBreakdownBox.getChildren().add(empty);
        } else {
            int finalTotal = totalTagInstances;
            tagCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(6)
                    .forEach(e -> {
                        int percent = (int) Math.round(100.0 * e.getValue() / finalTotal);
                        categoryBreakdownBox.getChildren().add(categoryRow(e.getKey(), percent));
                    });
        }

        // Achievements
        achievementsBox.getChildren().clear();
        List<String> badges = AchievementEngine.unlocked(
                (int) tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count(),
                data.focusStreak, data.totalFocusMinutes, data.goalsCompleted);
        if (badges.isEmpty()) {
            Label empty = new Label("Complete tasks and focus sessions to start earning badges.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            achievementsBox.getChildren().add(empty);
        } else {
            for (String badge : badges) {
                Label chip = new Label(badge);
                chip.getStyleClass().add("category-chip");
                achievementsBox.getChildren().add(chip);
            }
        }

        // All Saved Tasks
        savedTasksList.setItems(FXCollections.observableArrayList(tasks));
    }

    private boolean hasDate(Task t) {
        return t.getDeadline() != null && !t.getDeadline().isEmpty();
    }

    private HBox scheduleRow(String time, String title) {
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1C6FEB; -fx-font-size: 11px; -fx-min-width: 50;");
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        HBox row = new HBox(10, timeLabel, titleLabel);
        row.setStyle("-fx-padding: 4 0;");
        return row;
    }

    private VBox categoryRow(String label, int percent) {
        HBox header = new HBox();
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label percentLabel = new Label(percent + "%");
        percentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        header.getChildren().addAll(nameLabel, spacer, percentLabel);

        ProgressBar bar = new ProgressBar(percent / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(4, header, bar);
        return box;
    }

    @FXML
    private void handleQuickAdd() {
        Optional<Task> result = TaskDialogs.showAddTaskDialog();
        result.ifPresent(task -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            task.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new TaskDAO().insert(task),
                    saved -> loadDashboard(),
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
