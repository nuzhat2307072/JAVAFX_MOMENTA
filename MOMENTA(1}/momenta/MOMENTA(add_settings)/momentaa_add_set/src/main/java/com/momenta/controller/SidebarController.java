package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.NavigationManager;
import com.momenta.util.NotificationService;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.util.List;

public class SidebarController {

    @FXML private javafx.scene.control.Label userNameLabel;
    @FXML private javafx.scene.control.Label userRoleLabel;
    @FXML private Button bellButton;

    @FXML private Button btnDashboard;
    @FXML private Button btnTasks;
    @FXML private Button btnKanban;
    @FXML private Button btnCalendar;
    @FXML private Button btnFocus;
    @FXML private Button btnGoals;
    @FXML private Button btnAnalytics;
    @FXML private Button btnFinance;
    @FXML private Button btnSettings;

    private List<String> latestNotifications = List.of();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getOccupation());
            loadNotifications(user.getId());
        }
    }

    private void loadNotifications(int userId) {
        AsyncUtil.run(
                () -> NotificationService.getUpcoming(userId),
                (List<String> items) -> {
                    latestNotifications = items;
                    bellButton.setText(items.isEmpty() ? "🔔  Notifications" : "🔔  Notifications (" + items.size() + ")");
                },
                error -> { }
        );
    }

    @FXML
    private void showNotifications() {
        if (latestNotifications.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "You're all caught up — no overdue tasks or reminders in the next 24 hours.").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String item : latestNotifications) sb.append(item).append("\n\n");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString().trim());
        alert.setTitle("Notifications");
        alert.setHeaderText("Upcoming & overdue");
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    /** Called by each screen's controller right after navigation to highlight the active tab. */
    public void setActive(String page) {
        clearActive();
        Button target = switch (page) {
            case "dashboard" -> btnDashboard;
            case "tasks" -> btnTasks;
            case "kanban" -> btnKanban;
            case "calendar" -> btnCalendar;
            case "focus" -> btnFocus;
            case "goals" -> btnGoals;
            case "analytics" -> btnAnalytics;
            case "finance" -> btnFinance;
            case "settings" -> btnSettings;
            default -> null;
        };
        if (target != null) {
            target.getStyleClass().add("nav-button-active");
        }
    }

    private void clearActive() {
        for (Button b : new Button[]{btnDashboard, btnTasks, btnKanban, btnCalendar, btnFocus, btnGoals, btnAnalytics, btnFinance, btnSettings}) {
            b.getStyleClass().remove("nav-button-active");
        }
    }

    @FXML private void goDashboard() { NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard"); }
    @FXML private void goTasks() { NavigationManager.navigate("tasks.fxml", "MOMENTA - Tasks"); }
    @FXML private void goKanban() { NavigationManager.navigate("kanban.fxml", "MOMENTA - Kanban Board"); }
    @FXML private void goCalendar() { NavigationManager.navigate("calendar.fxml", "MOMENTA - Calendar"); }
    @FXML private void goFocus() { NavigationManager.navigate("focus.fxml", "MOMENTA - Focus Mode"); }
    @FXML private void goGoals() { NavigationManager.navigate("goals.fxml", "MOMENTA - Goals"); }
    @FXML private void goAnalytics() { NavigationManager.navigate("analytics.fxml", "MOMENTA - Analytics"); }
    @FXML private void goFinance() { NavigationManager.navigate("finance.fxml", "MOMENTA - Finance"); }
    @FXML private void goSettings() { NavigationManager.navigate("settings.fxml", "MOMENTA - Settings"); }

    @FXML
    private void logout() {
        SessionManager.logout();
        NavigationManager.navigate("login.fxml", "MOMENTA - Login");
    }
}
