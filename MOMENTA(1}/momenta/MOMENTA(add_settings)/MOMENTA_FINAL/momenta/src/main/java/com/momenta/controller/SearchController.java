package com.momenta.controller;

import com.momenta.dao.GoalDAO;
import com.momenta.dao.HabitDAO;
import com.momenta.dao.HealthGoalDAO;
import com.momenta.dao.LifeEntryDAO;
import com.momenta.dao.NoteDAO;
import com.momenta.dao.ReminderDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Goal;
import com.momenta.model.Habit;
import com.momenta.model.HealthGoal;
import com.momenta.model.LifeEntry;
import com.momenta.model.Note;
import com.momenta.model.Reminder;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class SearchController {

    @FXML private SidebarController sidebarController;
    @FXML private TextField searchField;
    @FXML private VBox resultsBox;

    private List<Task> allTasks = List.of();
    private List<Reminder> allReminders = List.of();
    private List<Goal> allGoals = List.of();
    private List<Note> allNotes = List.of();
    private List<Habit> allHabits = List.of();
    private List<HealthGoal> allHealthGoals = List.of();
    private List<LifeEntry> allLifeEntries = List.of();

    @FXML
    public void initialize() {
        sidebarController.setActive("search");
        searchField.textProperty().addListener((obs, oldV, newV) -> renderResults(newV));
        loadEverything();
    }

    private void loadEverything() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> {
                    Object[] data = new Object[7];
                    data[0] = new TaskDAO().getAllByUser(user.getId());
                    data[1] = new ReminderDAO().getAllByUser(user.getId());
                    data[2] = new GoalDAO().getAllByUser(user.getId());
                    data[3] = new NoteDAO().getAllByUser(user.getId());
                    data[4] = new HabitDAO().getAllByUser(user.getId());
                    data[5] = new HealthGoalDAO().getAllByUser(user.getId());
                    data[6] = new LifeEntryDAO().getAllByUser(user.getId());
                    return data;
                },
                (Object[] data) -> {
                    allTasks = (List<Task>) data[0];
                    allReminders = (List<Reminder>) data[1];
                    allGoals = (List<Goal>) data[2];
                    allNotes = (List<Note>) data[3];
                    allHabits = (List<Habit>) data[4];
                    allHealthGoals = (List<HealthGoal>) data[5];
                    allLifeEntries = (List<LifeEntry>) data[6];
                    renderResults(searchField.getText());
                    searchField.requestFocus();
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load data to search: " + error.getMessage()).showAndWait()
        );
    }

    private void renderResults(String query) {
        resultsBox.getChildren().clear();
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            Label hint = new Label("Start typing to search your tasks, reminders, goals, notes, health and life areas.");
            hint.setStyle("-fx-text-fill: #6B7280;");
            resultsBox.getChildren().add(hint);
            return;
        }

        int matches = 0;
        for (Task t : allTasks) {
            if (contains(t.getTitle(), q) || contains(t.getDescription(), q) || contains(t.getCategory(), q)) {
                resultsBox.getChildren().add(resultRow("📋", "Task", t.getTitle(),
                        "Priority " + t.getPriority() + "/5 · " + t.getStatus()));
                matches++;
            }
        }
        for (Reminder r : allReminders) {
            if (contains(r.getTitle(), q) || contains(r.getNote(), q)) {
                resultsBox.getChildren().add(resultRow("📅", "Reminder", r.getTitle(), r.getDate() + " " + r.getTime()));
                matches++;
            }
        }
        for (Goal g : allGoals) {
            if (contains(g.getTitle(), q)) {
                resultsBox.getChildren().add(resultRow("🎯", "Goal", g.getTitle(), g.getProgress() + "% complete"));
                matches++;
            }
        }
        for (Note n : allNotes) {
            if (contains(n.getContent(), q)) {
                String preview = n.getContent().length() > 100 ? n.getContent().substring(0, 100) + "…" : n.getContent();
                resultsBox.getChildren().add(resultRow("🧠", "Note", preview, n.getCreatedAt()));
                matches++;
            }
        }
        for (Habit h : allHabits) {
            if (contains(h.getTitle(), q)) {
                resultsBox.getChildren().add(resultRow("✅", "Habit", h.getTitle(), "🔥 " + h.getStreak() + " day streak"));
                matches++;
            }
        }
        for (HealthGoal hg : allHealthGoals) {
            if (contains(hg.getTitle(), q) || contains(hg.getTarget(), q)) {
                resultsBox.getChildren().add(resultRow("❤️", "Health Goal", hg.getTitle(), hg.getProgress() + "% complete"));
                matches++;
            }
        }
        for (LifeEntry e : allLifeEntries) {
            if (contains(e.getTitle(), q) || contains(e.getSubtitle(), q) || contains(e.getNotes(), q)) {
                resultsBox.getChildren().add(resultRow("🌱", moduleLabel(e.getModule()), e.getTitle(), e.getSubtitle()));
                matches++;
            }
        }

        if (matches == 0) {
            Label empty = new Label("No matches for \"" + query + "\".");
            empty.setStyle("-fx-text-fill: #6B7280;");
            resultsBox.getChildren().add(empty);
        }
    }

    private String moduleLabel(String module) {
        if (module == null) return "Life Area";
        return switch (module) {
            case "LEARNING" -> "Learning";
            case "CAREER" -> "Career";
            case "RELATIONSHIP" -> "Relationship";
            case "HOME" -> "Life & Home";
            case "TRAVEL" -> "Travel";
            case "HOBBY" -> "Hobby";
            case "JOURNAL" -> "Journal";
            case "WORKOUT" -> "Workout";
            case "NUTRITION" -> "Nutrition";
            default -> "Life Area";
        };
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }

    private VBox resultRow(String icon, String type, String title, String subtitle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card");
        Label header = new Label(icon + "  " + type);
        header.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        Label sub = new Label(subtitle);
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        card.getChildren().addAll(header, titleLabel, sub);
        return card;
    }
}
