package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.util.NavigationManager;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {

    @FXML private javafx.scene.control.Label userNameLabel;
    @FXML private javafx.scene.control.Label userRoleLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnTasks;
    @FXML private Button btnCalendar;
    @FXML private Button btnFocus;
    @FXML private Button btnGoals;
    @FXML private Button btnAnalytics;
    @FXML private Button btnFinance;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getOccupation());
        }
    }

    /** Called by each screen's controller right after navigation to highlight the active tab. */
    public void setActive(String page) {
        clearActive();
        Button target = switch (page) {
            case "dashboard" -> btnDashboard;
            case "tasks" -> btnTasks;
            case "calendar" -> btnCalendar;
            case "focus" -> btnFocus;
            case "goals" -> btnGoals;
            case "analytics" -> btnAnalytics;
            case "finance" -> btnFinance;
            default -> null;
        };
        if (target != null) {
            target.getStyleClass().add("nav-button-active");
        }
    }

    private void clearActive() {
        for (Button b : new Button[]{btnDashboard, btnTasks, btnCalendar, btnFocus, btnGoals, btnAnalytics, btnFinance}) {
            b.getStyleClass().remove("nav-button-active");
        }
    }

    @FXML private void goDashboard() { NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard"); }
    @FXML private void goTasks() { NavigationManager.navigate("tasks.fxml", "MOMENTA - Tasks"); }
    @FXML private void goCalendar() { NavigationManager.navigate("calendar.fxml", "MOMENTA - Calendar"); }
    @FXML private void goFocus() { NavigationManager.navigate("focus.fxml", "MOMENTA - Focus Mode"); }
    @FXML private void goGoals() { NavigationManager.navigate("goals.fxml", "MOMENTA - Goals"); }
    @FXML private void goAnalytics() { NavigationManager.navigate("analytics.fxml", "MOMENTA - Analytics"); }
    @FXML private void goFinance() { NavigationManager.navigate("finance.fxml", "MOMENTA - Finance"); }

    @FXML
    private void logout() {
        SessionManager.logout();
        NavigationManager.navigate("login.fxml", "MOMENTA - Login");
    }
}
