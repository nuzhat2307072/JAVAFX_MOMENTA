package com.momenta.controller;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.HabitDAO;
import com.momenta.dao.HealthDAO;
import com.momenta.dao.LifeEntryDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Habit;
import com.momenta.model.HealthLog;
import com.momenta.model.LifeEntry;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsController {

    @FXML private SidebarController sidebarController;
    @FXML private Label completedLabel;
    @FXML private Label pendingLabel;
    @FXML private Label highPriorityLabel;
    @FXML private Label avgProgressLabel;
    @FXML private Label overdueLabel;
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart priorityChart;

    @FXML private Label avgSleepAnalyticsLabel;
    @FXML private Label avgWaterAnalyticsLabel;
    @FXML private Label weeklyExerciseAnalyticsLabel;
    @FXML private Label activeHabitsLabel;
    @FXML private LineChart<String, Number> sleepChart;
    @FXML private CategoryAxis sleepXAxis;
    @FXML private NumberAxis sleepYAxis;
    @FXML private HBox lifeAreasCountBox;

    @FXML
    public void initialize() {
        sidebarController.setActive("analytics");
        loadTaskStats();
        loadWeeklyFocus();
        loadHealthStats();
        loadLifeAreasOverview();
    }

    private void loadTaskStats() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new TaskDAO().getAllByUser(user.getId()),
                (List<Task> tasks) -> {
                    long completed = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
                    long pending = tasks.stream().filter(t -> !"DONE".equals(t.getStatus())).count();
                    long highPriority = tasks.stream().filter(t -> t.getPriority() >= 4).count();
                    double avgProgress = tasks.isEmpty() ? 0 :
                            tasks.stream().mapToInt(Task::getProgress).average().orElse(0);
                    String today = LocalDate.now().toString();
                    long overdue = tasks.stream()
                            .filter(t -> !"DONE".equals(t.getStatus()))
                            .filter(t -> t.getDeadline() != null && !t.getDeadline().isEmpty() && t.getDeadline().compareTo(today) < 0)
                            .count();

                    completedLabel.setText(String.valueOf(completed));
                    pendingLabel.setText(String.valueOf(pending));
                    highPriorityLabel.setText(String.valueOf(highPriority));
                    avgProgressLabel.setText(Math.round(avgProgress) + "%");
                    overdueLabel.setText(String.valueOf(overdue));

                    updatePriorityChart(tasks);
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load task stats: " + error.getMessage()).showAndWait()
        );
    }

    private void updatePriorityChart(List<Task> tasks) {
        int[] counts = new int[6]; // index 1..5
        for (Task t : tasks) {
            if (t.getPriority() >= 1 && t.getPriority() <= 5) counts[t.getPriority()]++;
        }
        javafx.collections.ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (int p = 5; p >= 1; p--) {
            if (counts[p] > 0) data.add(new PieChart.Data("Priority " + p, counts[p]));
        }
        if (data.isEmpty()) data.add(new PieChart.Data("No tasks yet", 1));
        priorityChart.setData(data);
    }

    private void loadWeeklyFocus() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new FocusSessionDAO().getMinutesByDateLast7(user.getId()),
                (Map<String, Integer> byDate) -> {
                    // Reverse to chronological order (DAO returns most-recent-first).
                    Map<String, Integer> ordered = new LinkedHashMap<>();
                    byDate.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(e -> ordered.put(e.getKey(), e.getValue()));

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    if (ordered.isEmpty()) {
                        weeklyChart.setData(FXCollections.observableArrayList());
                        return;
                    }
                    for (Map.Entry<String, Integer> entry : ordered.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    weeklyChart.setData(FXCollections.observableArrayList(series));
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load focus history: " + error.getMessage()).showAndWait()
        );
    }

    private void loadHealthStats() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new HealthDAO().getHistory(user.getId(), 7),
                (List<HealthLog> logs) -> {
                    if (logs.isEmpty()) {
                        avgSleepAnalyticsLabel.setText("0.0 h");
                        avgWaterAnalyticsLabel.setText("0 glasses");
                        weeklyExerciseAnalyticsLabel.setText("0");
                        sleepChart.setData(FXCollections.observableArrayList());
                    } else {
                        double avgSleep = logs.stream().mapToDouble(HealthLog::getSleepHours).average().orElse(0);
                        double avgWater = logs.stream().mapToInt(HealthLog::getWaterGlasses).average().orElse(0);
                        int totalExercise = logs.stream().mapToInt(HealthLog::getExerciseMinutes).sum();

                        avgSleepAnalyticsLabel.setText(String.format("%.1f h", avgSleep));
                        avgWaterAnalyticsLabel.setText(String.format("%.0f glasses", avgWater));
                        weeklyExerciseAnalyticsLabel.setText(totalExercise + " min");

                        // DAO returns most-recent-first; chart wants chronological order.
                        Map<String, Double> chronological = new TreeMap<>();
                        for (HealthLog log : logs) chronological.put(log.getDate(), log.getSleepHours());

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        for (Map.Entry<String, Double> e : chronological.entrySet()) {
                            series.getData().add(new XYChart.Data<>(formatShortDate(e.getKey()), e.getValue()));
                        }
                        sleepChart.setData(FXCollections.observableArrayList(series));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load health stats: " + error.getMessage()).showAndWait()
        );
        AsyncUtil.run(
                () -> new HabitDAO().getAllByUser(user.getId()),
                (List<Habit> habits) -> activeHabitsLabel.setText(String.valueOf(habits.size())),
                error -> { }
        );
    }

    private String formatShortDate(String isoDate) {
        try {
            return LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (Exception e) {
            return isoDate;
        }
    }

    private void loadLifeAreasOverview() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new LifeEntryDAO().getAllByUser(user.getId()),
                (List<LifeEntry> entries) -> {
                    Map<String, Long> counts = new LinkedHashMap<>();
                    for (String module : new String[]{"LEARNING", "CAREER", "RELATIONSHIP", "HOME", "TRAVEL", "HOBBY", "JOURNAL", "WORKOUT", "NUTRITION"}) {
                        counts.put(module, 0L);
                    }
                    for (LifeEntry e : entries) {
                        counts.merge(e.getModule(), 1L, Long::sum);
                    }
                    lifeAreasCountBox.getChildren().clear();
                    for (Map.Entry<String, Long> entry : counts.entrySet()) {
                        lifeAreasCountBox.getChildren().add(buildModuleCountCard(entry.getKey(), entry.getValue()));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load life areas overview: " + error.getMessage()).showAndWait()
        );
    }

    private VBox buildModuleCountCard(String module, long count) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        Label title = new Label(moduleDisplayName(module));
        title.getStyleClass().add("card-title");
        Label value = new Label(String.valueOf(count));
        value.getStyleClass().add("card-value");
        card.getChildren().addAll(title, value);
        return card;
    }

    private String moduleDisplayName(String module) {
        return switch (module) {
            case "LEARNING" -> "LEARNING";
            case "CAREER" -> "CAREER";
            case "RELATIONSHIP" -> "RELATIONSHIPS";
            case "HOME" -> "LIFE & HOME";
            case "TRAVEL" -> "TRAVEL";
            case "HOBBY" -> "HOBBIES";
            case "JOURNAL" -> "JOURNAL";
            case "WORKOUT" -> "WORKOUTS";
            case "NUTRITION" -> "NUTRITION";
            default -> module;
        };
    }
}
