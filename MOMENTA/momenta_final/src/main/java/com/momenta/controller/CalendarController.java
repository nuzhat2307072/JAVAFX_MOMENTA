package com.momenta.controller;

import com.momenta.dao.ReminderDAO;
import com.momenta.model.Reminder;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CalendarController {

    @FXML private SidebarController sidebarController;
    @FXML private Label monthLabel;
    @FXML private Label selectedDateLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox remindersBox;

    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    @FXML
    public void initialize() {
        sidebarController.setActive("calendar");
        renderCalendar();
        loadRemindersFor(selectedDate);
    }

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(dayNames[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 11px;");
            lbl.setMinWidth(70);
            lbl.setAlignment(Pos.CENTER);
            calendarGrid.add(lbl, i, 0);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startCol = firstOfMonth.getDayOfWeek().getValue() - 1; // Monday = 0
        int daysInMonth = currentMonth.lengthOfMonth();

        int row = 1, col = startCol;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setMinSize(70, 46);
            dayButton.setMaxSize(70, 46);

            boolean isToday = date.equals(LocalDate.now());
            boolean isSelected = date.equals(selectedDate);
            String style = "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #E2E5EA; -fx-border-radius: 8;";
            if (isSelected) style += " -fx-background-color: #1C6FEB; -fx-text-fill: white; -fx-font-weight: bold;";
            else if (isToday) style += " -fx-background-color: #E6F0FF; -fx-text-fill: #1C6FEB; -fx-font-weight: bold;";
            else style += " -fx-background-color: white; -fx-text-fill: #0F2C4C;";
            dayButton.setStyle(style);

            dayButton.setOnAction(e -> {
                selectedDate = date;
                renderCalendar();
                loadRemindersFor(date);
            });

            calendarGrid.add(dayButton, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private void loadRemindersFor(LocalDate date) {
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        AsyncUtil.run(
                () -> new ReminderDAO().getByDate(user.getId(), date.toString()),
                (List<Reminder> reminders) -> {
                    remindersBox.getChildren().clear();
                    if (reminders.isEmpty()) {
                        Label empty = new Label("No reminders for this date.");
                        empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
                        remindersBox.getChildren().add(empty);
                    } else {
                        for (Reminder r : reminders) {
                            remindersBox.getChildren().add(buildReminderRow(r));
                        }
                    }
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load reminders: " + error.getMessage()).showAndWait()
        );
    }

    private HBox buildReminderRow(Reminder r) {
        VBox textBox = new VBox(2);
        Label time = new Label(r.getTime());
        time.setStyle("-fx-font-weight: bold; -fx-text-fill: #1C6FEB; -fx-font-size: 11px;");
        Label title = new Label(r.getTitle());
        title.setStyle("-fx-font-weight: bold;");
        textBox.getChildren().addAll(time, title);
        if (r.getNote() != null && !r.getNote().isEmpty()) {
            Label note = new Label(r.getNote());
            note.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            note.setWrapText(true);
            textBox.getChildren().add(note);
        }

        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> AsyncUtil.run(
                () -> { new ReminderDAO().delete(r.getId()); return null; },
                v -> loadRemindersFor(selectedDate),
                error -> new Alert(Alert.AlertType.ERROR, "Could not delete reminder: " + error.getMessage()).showAndWait()
        ));

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(8, textBox, spacer, deleteBtn);
        row.setStyle("-fx-padding: 8; -fx-background-color: #F4F6F9; -fx-background-radius: 8;");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @FXML
    private void handleAddReminder() {
        Optional<Reminder> result = ReminderDialogs.showAddReminderDialog(selectedDate);
        result.ifPresent(reminder -> {
            User user = SessionManager.getCurrentUser();
            reminder.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new ReminderDAO().insert(reminder),
                    saved -> loadRemindersFor(selectedDate),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not save reminder: " + error.getMessage()).showAndWait()
            );
        });
    }
}
