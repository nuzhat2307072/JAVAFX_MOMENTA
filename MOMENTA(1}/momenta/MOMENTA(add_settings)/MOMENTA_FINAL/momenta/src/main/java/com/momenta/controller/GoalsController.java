package com.momenta.controller;

import com.momenta.dao.GoalDAO;
import com.momenta.model.Goal;
import com.momenta.model.Milestone;
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

import java.util.List;
import java.util.Optional;

public class GoalsController {

    @FXML private SidebarController sidebarController;
    @FXML private VBox goalsBox;

    @FXML
    public void initialize() {
        sidebarController.setActive("goals");
        loadGoals();
    }

    private void loadGoals() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new GoalDAO().getAllByUser(user.getId()),
                (List<Goal> goals) -> {
                    goalsBox.getChildren().clear();
                    if (goals.isEmpty()) {
                        Label empty = new Label("No goals yet. Click \"+ New Goal\" to add your first long-term goal.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        goalsBox.getChildren().add(empty);
                    } else {
                        for (Goal g : goals) goalsBox.getChildren().add(buildGoalCard(g));
                    }
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load goals: " + error.getMessage()).showAndWait()
        );
    }

    private VBox buildGoalCard(Goal goal) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(goal.getTitle().toUpperCase());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0F2C4C;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("Delete Goal");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteGoal(goal));
        header.getChildren().addAll(title, spacer, deleteBtn);

        ProgressBar bar = new ProgressBar(goal.getProgress() / 100.0);
        bar.setPrefWidth(400);
        Label percentLabel = new Label(goal.getProgress() + "%");

        HBox progressRow = new HBox(10, bar, percentLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        VBox milestonesBox = new VBox(6);
        for (Milestone m : goal.getMilestones()) {
            CheckBox cb = new CheckBox(m.getTitle());
            cb.setSelected(m.isDone());
            cb.selectedProperty().addListener((obs, oldV, newV) -> toggleMilestone(goal, m, newV));
            milestonesBox.getChildren().add(cb);
        }

        Button addMilestoneBtn = new Button("+ Add Milestone");
        addMilestoneBtn.getStyleClass().add("secondary-button");
        addMilestoneBtn.setOnAction(e -> addMilestone(goal));

        card.getChildren().addAll(header, progressRow, milestonesBox, addMilestoneBtn);
        return card;
    }

    private void toggleMilestone(Goal goal, Milestone milestone, boolean done) {
        AsyncUtil.run(
                () -> {
                    new GoalDAO().toggleMilestone(milestone.getId(), done);
                    long total = goal.getMilestones().size();
                    long doneCount = goal.getMilestones().stream()
                            .filter(m -> m.getId() == milestone.getId() ? done : m.isDone())
                            .count();
                    int newProgress = total == 0 ? 0 : (int) Math.round(100.0 * doneCount / total);
                    new GoalDAO().updateGoalProgress(goal.getId(), newProgress);
                    return null;
                },
                v -> loadGoals(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not update milestone: " + error.getMessage()).showAndWait()
        );
    }

    private void addMilestone(Goal goal) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Milestone");
        dialog.setHeaderText(null);
        dialog.setContentText("Milestone:");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text ->
                AsyncUtil.run(
                        () -> {
                            new GoalDAO().insertMilestone(new Milestone(0, goal.getId(), text.trim(), false));
                            return null;
                        },
                        v -> loadGoals(),
                        error -> new Alert(Alert.AlertType.ERROR, "Could not add milestone: " + error.getMessage()).showAndWait()
                ));
    }

    private void deleteGoal(Goal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Move goal \"" + goal.getTitle() + "\" to Trash?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AsyncUtil.run(
                        () -> { new GoalDAO().deleteGoal(goal.getId()); return null; },
                        v -> {
                            loadGoals();
                            UndoHelper.offerUndo("\"" + goal.getTitle() + "\" moved to Trash.",
                                    () -> new GoalDAO().restoreGoal(goal.getId()), this::loadGoals);
                        },
                        error -> new Alert(Alert.AlertType.ERROR, "Could not delete goal: " + error.getMessage()).showAndWait()
                );
            }
        });
    }

    @FXML
    private void handleAddGoal() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Goal");
        dialog.setHeaderText(null);
        dialog.setContentText("Goal title:");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.trim().isEmpty()).ifPresent(text -> {
            User user = SessionManager.getCurrentUser();
            AsyncUtil.run(
                    () -> new GoalDAO().insertGoal(new Goal(0, user.getId(), text.trim(), 0)),
                    saved -> loadGoals(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not create goal: " + error.getMessage()).showAndWait()
            );
        });
    }
}
