package com.momenta.controller;

import com.momenta.dao.HabitDAO;
import com.momenta.dao.HealthDAO;
import com.momenta.dao.HealthGoalDAO;
import com.momenta.dao.ReminderDAO;
import com.momenta.model.Habit;
import com.momenta.model.HealthGoal;
import com.momenta.model.HealthLog;
import com.momenta.model.Reminder;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.SessionManager;
import com.momenta.util.UndoHelper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HealthController {

    @FXML private SidebarController sidebarController;

    @FXML private Label avgSleepLabel;
    @FXML private Label avgWaterLabel;
    @FXML private Label weeklyExerciseLabel;
    @FXML private Label mealsTodayLabel;
    @FXML private Label currentMoodLabel;

    @FXML private Spinner<Double> sleepSpinner;
    @FXML private Spinner<Integer> waterSpinner;
    @FXML private Spinner<Integer> exerciseSpinner;
    @FXML private Spinner<Double> weightSpinner;
    @FXML private Spinner<Integer> stepsSpinner;
    @FXML private TextArea notesArea;
    @FXML private CheckBox cbBreakfast;
    @FXML private CheckBox cbLunch;
    @FXML private CheckBox cbDinner;
    @FXML private CheckBox cbSnacks;
    @FXML private ToggleGroup moodGroup;
    @FXML private RadioButton moodGreatRadio;
    @FXML private RadioButton moodGoodRadio;
    @FXML private RadioButton moodOkayRadio;
    @FXML private RadioButton moodLowRadio;
    @FXML private RadioButton moodBadRadio;
    @FXML private Label saveMessageLabel;

    @FXML private VBox healthHistoryBox;
    @FXML private VBox healthRemindersBox;
    @FXML private VBox workoutsBox;
    @FXML private VBox nutritionBox;
    @FXML private VBox habitsBox;
    @FXML private VBox healthGoalsBox;

    private HealthLog currentLog;

    @FXML
    public void initialize() {
        sidebarController.setActive("health");

        sleepSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 16, 7, 0.5));
        waterSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 0, 1));
        exerciseSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 300, 0, 5));
        weightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 300, 0, 0.5));
        stepsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 0, 500));
        sleepSpinner.setEditable(true);
        waterSpinner.setEditable(true);
        exerciseSpinner.setEditable(true);
        weightSpinner.setEditable(true);
        stepsSpinner.setEditable(true);

        loadToday();
        loadHistoryAndSummary();
        loadHealthReminders();
        loadWorkouts();
        loadNutrition();
        loadHabits();
        loadHealthGoals();
    }

    // ---------- Today's log ----------

    private void loadToday() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new HealthDAO().getOrCreateToday(user.getId()),
                this::populateForm,
                error -> new Alert(Alert.AlertType.ERROR, "Could not load today's health log: " + error.getMessage()).showAndWait()
        );
    }

    private void populateForm(HealthLog log) {
        currentLog = log;
        sleepSpinner.getValueFactory().setValue(log.getSleepHours());
        waterSpinner.getValueFactory().setValue(log.getWaterGlasses());
        exerciseSpinner.getValueFactory().setValue(log.getExerciseMinutes());
        weightSpinner.getValueFactory().setValue(log.getWeightKg());
        stepsSpinner.getValueFactory().setValue(log.getSteps());
        notesArea.setText(log.getNotes());
        cbBreakfast.setSelected(log.hasMeal("BREAKFAST"));
        cbLunch.setSelected(log.hasMeal("LUNCH"));
        cbDinner.setSelected(log.hasMeal("DINNER"));
        cbSnacks.setSelected(log.hasMeal("SNACKS"));
        moodGroup.selectToggle(radioForMood(log.getMood()));
        mealsTodayLabel.setText(log.getMealsCount() + " / 4");
        currentMoodLabel.setText(HealthLog.moodEmoji(log.getMood()));
    }

    private RadioButton radioForMood(String mood) {
        if (mood == null) return null;
        return switch (mood) {
            case "GREAT" -> moodGreatRadio;
            case "GOOD" -> moodGoodRadio;
            case "OKAY" -> moodOkayRadio;
            case "LOW" -> moodLowRadio;
            case "BAD" -> moodBadRadio;
            default -> null;
        };
    }

    private String moodForRadio(Toggle t) {
        if (t == moodGreatRadio) return "GREAT";
        if (t == moodGoodRadio) return "GOOD";
        if (t == moodOkayRadio) return "OKAY";
        if (t == moodLowRadio) return "LOW";
        if (t == moodBadRadio) return "BAD";
        return "";
    }

    @FXML
    private void handleWaterMinus() {
        int v = waterSpinner.getValue() == null ? 0 : waterSpinner.getValue();
        waterSpinner.getValueFactory().setValue(Math.max(0, v - 1));
    }

    @FXML
    private void handleWaterPlus() {
        int v = waterSpinner.getValue() == null ? 0 : waterSpinner.getValue();
        waterSpinner.getValueFactory().setValue(v + 1);
    }

    @FXML
    private void handleExercise15() {
        int v = exerciseSpinner.getValue() == null ? 0 : exerciseSpinner.getValue();
        exerciseSpinner.getValueFactory().setValue(v + 15);
    }

    @FXML
    private void handleExercise30() {
        int v = exerciseSpinner.getValue() == null ? 0 : exerciseSpinner.getValue();
        exerciseSpinner.getValueFactory().setValue(v + 30);
    }

    @FXML
    private void handleSaveLog() {
        if (currentLog == null) return;
        currentLog.setSleepHours(sleepSpinner.getValue());
        currentLog.setWaterGlasses(waterSpinner.getValue());
        currentLog.setExerciseMinutes(exerciseSpinner.getValue());
        currentLog.setWeightKg(weightSpinner.getValue());
        currentLog.setSteps(stepsSpinner.getValue());
        currentLog.setNotes(notesArea.getText() == null ? "" : notesArea.getText().trim());

        StringBuilder meals = new StringBuilder();
        if (cbBreakfast.isSelected()) meals.append("BREAKFAST,");
        if (cbLunch.isSelected()) meals.append("LUNCH,");
        if (cbDinner.isSelected()) meals.append("DINNER,");
        if (cbSnacks.isSelected()) meals.append("SNACKS,");
        if (meals.length() > 0) meals.setLength(meals.length() - 1);
        currentLog.setMeals(meals.toString());
        currentLog.setMood(moodForRadio(moodGroup.getSelectedToggle()));

        User user = SessionManager.getCurrentUser();
        AsyncUtil.run(
                () -> { new HealthDAO().saveLog(currentLog); return null; },
                v -> {
                    saveMessageLabel.setText("Saved " + currentLog.getDate() + ".");
                    mealsTodayLabel.setText(currentLog.getMealsCount() + " / 4");
                    currentMoodLabel.setText(HealthLog.moodEmoji(currentLog.getMood()));
                    loadHistoryAndSummary();
                    if (user != null) BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not save today's log: " + error.getMessage()).showAndWait()
        );
    }

    // ---------- Summary + history ----------

    private void loadHistoryAndSummary() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new HealthDAO().getHistory(user.getId(), 14),
                (List<HealthLog> logs) -> {
                    renderSummary(logs);
                    renderHistory(logs);
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load health history: " + error.getMessage()).showAndWait()
        );
    }

    private void renderSummary(List<HealthLog> logs) {
        List<HealthLog> lastWeek = logs.stream().limit(7).toList();
        if (lastWeek.isEmpty()) {
            avgSleepLabel.setText("0.0 h");
            avgWaterLabel.setText("0 glasses");
            weeklyExerciseLabel.setText("0 min");
            return;
        }
        double avgSleep = lastWeek.stream().mapToDouble(HealthLog::getSleepHours).average().orElse(0);
        double avgWater = lastWeek.stream().mapToInt(HealthLog::getWaterGlasses).average().orElse(0);
        int totalExercise = lastWeek.stream().mapToInt(HealthLog::getExerciseMinutes).sum();

        avgSleepLabel.setText(String.format("%.1f h", avgSleep));
        avgWaterLabel.setText(String.format("%.0f glasses", avgWater));
        weeklyExerciseLabel.setText(totalExercise + " min");
    }

    private void renderHistory(List<HealthLog> logs) {
        healthHistoryBox.getChildren().clear();
        if (logs.isEmpty()) {
            Label empty = new Label("No entries yet — save today's log to start your history.");
            empty.setStyle("-fx-text-fill: #6B7280;");
            healthHistoryBox.getChildren().add(empty);
            return;
        }
        for (HealthLog log : logs) healthHistoryBox.getChildren().add(buildHistoryRow(log));
    }

    private HBox buildHistoryRow(HealthLog log) {
        Label dateLabel = new Label(formatDate(log.getDate()));
        dateLabel.setPrefWidth(110);
        dateLabel.setStyle("-fx-font-weight: bold;");

        Label summary = new Label(String.format("😴 %.1fh   💧 %d   🏃 %dmin   👣 %d   🍽️ %d/4   %s",
                log.getSleepHours(), log.getWaterGlasses(), log.getExerciseMinutes(),
                log.getSteps(), log.getMealsCount(), HealthLog.moodEmoji(log.getMood())));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, dateLabel, summary, spacer);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    private String formatDate(String isoDate) {
        try {
            return java.time.LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (Exception e) {
            return isoDate;
        }
    }

    // ---------- Habits ----------

    private void loadHabits() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new HabitDAO().getAllByUser(user.getId()),
                (List<Habit> habits) -> {
                    habitsBox.getChildren().clear();
                    if (habits.isEmpty()) {
                        Label empty = new Label("No habits yet. Click \"+ New Habit\" to start tracking one.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        habitsBox.getChildren().add(empty);
                    } else {
                        for (Habit h : habits) habitsBox.getChildren().add(buildHabitRow(h));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load habits: " + error.getMessage()).showAndWait()
        );
    }

    private HBox buildHabitRow(Habit habit) {
        CheckBox doneBox = new CheckBox();
        doneBox.setSelected(habit.isDoneToday());
        doneBox.selectedProperty().addListener((obs, oldV, newV) -> AsyncUtil.run(
                () -> { new HabitDAO().setDoneToday(habit.getId(), newV); return null; },
                v -> loadHabits(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not update habit: " + error.getMessage()).showAndWait()
        ));

        Label title = new Label(habit.getTitle());
        title.setStyle("-fx-font-weight: bold;");

        Label streak = new Label("🔥 " + habit.getStreak() + " day streak");
        streak.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        VBox textBox = new VBox(2, title, streak);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteHabit(habit));

        HBox row = new HBox(12, doneBox, textBox, spacer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    private void deleteHabit(Habit habit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Move habit \"" + habit.getTitle() + "\" to Trash?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AsyncUtil.run(
                        () -> { new HabitDAO().deleteHabit(habit.getId()); return null; },
                        v -> {
                            loadHabits();
                            UndoHelper.offerUndo("\"" + habit.getTitle() + "\" moved to Trash.",
                                    () -> new HabitDAO().restoreHabit(habit.getId()), this::loadHabits);
                        },
                        error -> new Alert(Alert.AlertType.ERROR, "Could not delete habit: " + error.getMessage()).showAndWait()
                );
            }
        });
    }

    @FXML
    private void handleAddHabit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Habit");
        dialog.setHeaderText(null);
        dialog.setContentText("Habit (e.g. \"Stretch\", \"No sugar\"):");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            AsyncUtil.run(
                    () -> new HabitDAO().insertHabit(new Habit(0, user.getId(), text.trim())),
                    saved -> loadHabits(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not create habit: " + error.getMessage()).showAndWait()
            );
        });
    }

    // ---------- Health Goals ----------

    private void loadHealthGoals() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new HealthGoalDAO().getAllByUser(user.getId()),
                (List<HealthGoal> goals) -> {
                    healthGoalsBox.getChildren().clear();
                    if (goals.isEmpty()) {
                        Label empty = new Label("No health goals yet. Click \"+ New Health Goal\" to add one.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        healthGoalsBox.getChildren().add(empty);
                    } else {
                        for (HealthGoal g : goals) healthGoalsBox.getChildren().add(buildHealthGoalCard(g));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load health goals: " + error.getMessage()).showAndWait()
        );
    }

    private VBox buildHealthGoalCard(HealthGoal goal) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(goal.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0F2C4C;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("Delete Goal");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteHealthGoal(goal));
        header.getChildren().addAll(title, spacer, deleteBtn);

        Label targetLabel = new Label(goal.getTarget() == null || goal.getTarget().isBlank()
                ? "" : "Target: " + goal.getTarget());
        targetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        ProgressBar bar = new ProgressBar(goal.getProgress() / 100.0);
        bar.setPrefWidth(360);
        Label percentLabel = new Label(goal.getProgress() + "%");
        Button minusBtn = new Button("－10%");
        minusBtn.getStyleClass().add("secondary-button");
        minusBtn.setOnAction(e -> adjustHealthGoalProgress(goal, -10));
        Button plusBtn = new Button("＋10%");
        plusBtn.getStyleClass().add("secondary-button");
        plusBtn.setOnAction(e -> adjustHealthGoalProgress(goal, 10));

        HBox progressRow = new HBox(10, bar, percentLabel, minusBtn, plusBtn);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, targetLabel, progressRow);
        return card;
    }

    private void adjustHealthGoalProgress(HealthGoal goal, int delta) {
        int newProgress = Math.max(0, Math.min(100, goal.getProgress() + delta));
        AsyncUtil.run(
                () -> { new HealthGoalDAO().updateProgress(goal.getId(), newProgress); return null; },
                v -> loadHealthGoals(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not update progress: " + error.getMessage()).showAndWait()
        );
    }

    private void deleteHealthGoal(HealthGoal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Move health goal \"" + goal.getTitle() + "\" to Trash?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AsyncUtil.run(
                        () -> { new HealthGoalDAO().deleteGoal(goal.getId()); return null; },
                        v -> {
                            loadHealthGoals();
                            UndoHelper.offerUndo("\"" + goal.getTitle() + "\" moved to Trash.",
                                    () -> new HealthGoalDAO().restoreGoal(goal.getId()), this::loadHealthGoals);
                        },
                        error -> new Alert(Alert.AlertType.ERROR, "Could not delete health goal: " + error.getMessage()).showAndWait()
                );
            }
        });
    }

    @FXML
    private void handleAddHealthGoal() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Health Goal");
        dialog.setHeaderText(null);
        dialog.setContentText("Goal title (e.g. \"Run a 5K\"):");
        Optional<String> titleResult = dialog.showAndWait();
        titleResult.filter(s -> !s.trim().isEmpty()).ifPresent(title -> {
            TextInputDialog targetDialog = new TextInputDialog();
            targetDialog.setTitle("New Health Goal");
            targetDialog.setHeaderText(null);
            targetDialog.setContentText("Target (optional, e.g. \"By December\"):");
            Optional<String> targetResult = targetDialog.showAndWait();
            String target = targetResult.orElse("").trim();

            User user = SessionManager.getCurrentUser();
            AsyncUtil.run(
                    () -> new HealthGoalDAO().insertGoal(new HealthGoal(0, user.getId(), title.trim(), target, 0)),
                    saved -> loadHealthGoals(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not create health goal: " + error.getMessage()).showAndWait()
            );
        });
    }

    // ---------- Health Reminders (medication / appointments) ----------

    private void loadHealthReminders() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new ReminderDAO().getUpcomingByCategory(user.getId(), "HEALTH"),
                (List<Reminder> reminders) -> {
                    healthRemindersBox.getChildren().clear();
                    if (reminders.isEmpty()) {
                        Label empty = new Label("No upcoming health reminders. Click \"+ New Health Reminder\" to add one.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        healthRemindersBox.getChildren().add(empty);
                    } else {
                        for (Reminder r : reminders) healthRemindersBox.getChildren().add(buildReminderRow(r));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load health reminders: " + error.getMessage()).showAndWait()
        );
    }

    private HBox buildReminderRow(Reminder reminder) {
        Label title = new Label(reminder.getTitle());
        title.setStyle("-fx-font-weight: bold;");
        Label when = new Label(formatDate(reminder.getDate()) + " " + reminder.getTime());
        when.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        VBox textBox = new VBox(2, title, when);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Move reminder \"" + reminder.getTitle() + "\" to Trash?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    AsyncUtil.run(
                            () -> { new ReminderDAO().delete(reminder.getId()); return null; },
                            v -> loadHealthReminders(),
                            error -> new Alert(Alert.AlertType.ERROR, "Could not delete reminder: " + error.getMessage()).showAndWait()
                    );
                }
            });
        });

        HBox row = new HBox(12, textBox, spacer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    @FXML
    private void handleAddHealthReminder() {
        ReminderDialogs.showAddReminderDialog(LocalDate.now(), "HEALTH").ifPresent(reminder -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            reminder.setUserId(user.getId());
            reminder.setCategory("HEALTH");
            AsyncUtil.run(
                    () -> new ReminderDAO().insert(reminder),
                    saved -> loadHealthReminders(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not add reminder: " + error.getMessage()).showAndWait()
            );
        });
    }

    // ---------- Workouts (Fitness) — a life_entries module, rendered via the shared factory ----------

    private void loadWorkouts() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        LifeEntryCardFactory.loadModule(workoutsBox, user.getId(), "WORKOUT", false, true, true);
    }

    @FXML
    private void handleAddWorkout() {
        LifeEntryDialogs.showAddDialog("Log Workout", "Workout (e.g. \"Running\", \"Push day\")",
                "Duration / intensity (e.g. \"30 min, moderate\")", true, "Notes").ifPresent(entry -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            entry.setUserId(user.getId());
            entry.setModule("WORKOUT");
            entry.setDone(true);
            AsyncUtil.run(
                    () -> new com.momenta.dao.LifeEntryDAO().insert(entry),
                    saved -> loadWorkouts(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not log workout: " + error.getMessage()).showAndWait()
            );
        });
    }

    // ---------- Nutrition log — a life_entries module, rendered via the shared factory ----------

    private void loadNutrition() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        LifeEntryCardFactory.loadModule(nutritionBox, user.getId(), "NUTRITION", false, false, true);
    }

    @FXML
    private void handleAddNutrition() {
        LifeEntryDialogs.showAddDialog("Log Food", "Food / meal", "Meal (Breakfast/Lunch/Dinner/Snack)",
                true, "Food notes").ifPresent(entry -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            entry.setUserId(user.getId());
            entry.setModule("NUTRITION");
            AsyncUtil.run(
                    () -> new com.momenta.dao.LifeEntryDAO().insert(entry),
                    saved -> loadNutrition(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not log food: " + error.getMessage()).showAndWait()
            );
        });
    }
}
