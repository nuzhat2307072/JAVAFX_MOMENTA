package com.momenta.controller;

import com.momenta.dao.FinanceDAO;
import com.momenta.dao.GoalDAO;
import com.momenta.dao.HealthDAO;
import com.momenta.dao.LifeEntryDAO;
import com.momenta.dao.NoteDAO;
import com.momenta.dao.ReminderDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.*;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * "Quick Add Everywhere" — reachable via Ctrl+K (chooser) or Ctrl+N (task
 * directly) from any screen, so adding something never requires navigating
 * away from what you're doing. Each quick-add inserts directly to the
 * database; the relevant screen picks it up next time it's opened (each
 * screen already reloads fresh from the database on every visit).
 */
public class QuickAddDialogs {

    public static void showChooser() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Quick Add");
        dialog.getDialogPane().getStylesheets().add(
                QuickAddDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setPrefWidth(260);

        Button taskBtn = quickButton("📋  Task", dialog, QuickAddDialogs::quickAddTask);
        Button reminderBtn = quickButton("📅  Reminder", dialog, QuickAddDialogs::quickAddReminder);
        Button noteBtn = quickButton("🧠  Note (Brain Dump)", dialog, QuickAddDialogs::quickAddNote);
        Button goalBtn = quickButton("🎯  Goal", dialog, QuickAddDialogs::quickAddGoal);
        Button expenseBtn = quickButton("💰  Finance Entry", dialog, QuickAddDialogs::quickAddExpense);
        Button healthNoteBtn = quickButton("❤️  Health Note", dialog, QuickAddDialogs::quickAddHealthNote);
        Button journalBtn = quickButton("📖  Journal Entry", dialog, QuickAddDialogs::quickAddJournalEntry);

        box.getChildren().addAll(taskBtn, reminderBtn, noteBtn, goalBtn, expenseBtn, healthNoteBtn, journalBtn);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private static Button quickButton(String label, Dialog<Void> dialog, Runnable action) {
        Button b = new Button(label);
        b.getStyleClass().add("secondary-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> {
            dialog.close();
            action.run();
        });
        return b;
    }

    public static void quickAddTask() {
        Optional<Task> result = TaskDialogs.showAddTaskDialog();
        result.ifPresent(task -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            task.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new TaskDAO().insert(task),
                    saved -> { },
                    error -> showError("Could not add task: " + error.getMessage())
            );
        });
    }

    public static void quickAddReminder() {
        Optional<Reminder> result = ReminderDialogs.showAddReminderDialog(java.time.LocalDate.now());
        result.ifPresent(reminder -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            reminder.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new ReminderDAO().insert(reminder),
                    saved -> { },
                    error -> showError("Could not add reminder: " + error.getMessage())
            );
        });
    }

    public static void quickAddNote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Brain Dump");
        dialog.setHeaderText(null);
        dialog.setContentText("Jot it down:");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            AsyncUtil.run(
                    () -> new NoteDAO().insert(new Note(0, user.getId(), text.trim(), LocalDateTime.now().toString())),
                    saved -> { },
                    error -> showError("Could not save note: " + error.getMessage())
            );
        });
    }

    public static void quickAddGoal() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Goal");
        dialog.setHeaderText(null);
        dialog.setContentText("Goal title:");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            AsyncUtil.run(
                    () -> new GoalDAO().insertGoal(new Goal(0, user.getId(), text.trim(), 0)),
                    saved -> { },
                    error -> showError("Could not create goal: " + error.getMessage())
            );
        });
    }

    public static void quickAddExpense() {
        Optional<FinanceEntry> result = FinanceDialogs.showAddEntryDialog();
        result.ifPresent(entry -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            entry.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new FinanceDAO().insert(entry),
                    saved -> { },
                    error -> showError("Could not add finance entry: " + error.getMessage())
            );
        });
    }

    /** Appends a quick note to today's health log without leaving the current screen. */
    public static void quickAddHealthNote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Health Note");
        dialog.setHeaderText(null);
        dialog.setContentText("Add to today's health log:");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            AsyncUtil.run(
                    () -> {
                        HealthDAO dao = new HealthDAO();
                        HealthLog log = dao.getOrCreateToday(user.getId());
                        String merged = log.getNotes() == null || log.getNotes().isBlank()
                                ? text.trim() : log.getNotes() + "; " + text.trim();
                        log.setNotes(merged);
                        dao.saveLog(log);
                        return null;
                    },
                    saved -> { },
                    error -> showError("Could not save health note: " + error.getMessage())
            );
        });
    }

    public static void quickAddJournalEntry() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Journal Entry");
        dialog.setHeaderText(null);
        dialog.setContentText("What's on your mind?");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            LifeEntry entry = new LifeEntry();
            entry.setUserId(user.getId());
            entry.setModule("JOURNAL");
            entry.setTitle(LocalDateTime.now().toLocalDate().toString());
            entry.setSubtitle("Daily");
            entry.setEntryDate(LocalDateTime.now().toLocalDate().toString());
            entry.setNotes(text.trim());
            AsyncUtil.run(
                    () -> new LifeEntryDAO().insert(entry),
                    saved -> { },
                    error -> showError("Could not save journal entry: " + error.getMessage())
            );
        });
    }

    private static void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
