package com.momenta.controller;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsController {

    @FXML private SidebarController sidebarController;
    @FXML private Label completedLabel;
    @FXML private Label pendingLabel;
    @FXML private Label highPriorityLabel;
    @FXML private Label avgProgressLabel;
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML
    public void initialize() {
        sidebarController.setActive("analytics");
        loadTaskStats();
        loadWeeklyFocus();
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

                    completedLabel.setText(String.valueOf(completed));
                    pendingLabel.setText(String.valueOf(pending));
                    highPriorityLabel.setText(String.valueOf(highPriority));
                    avgProgressLabel.setText(Math.round(avgProgress) + "%");
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load task stats: " + error.getMessage()).showAndWait()
        );
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
}
