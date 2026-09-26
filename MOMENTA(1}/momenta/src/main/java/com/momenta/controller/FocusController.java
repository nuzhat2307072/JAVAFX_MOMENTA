package com.momenta.controller;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.model.FocusSession;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.NavigationManager;
import com.momenta.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.util.Duration;

import java.time.LocalDate;

public class FocusController {

    @FXML private SidebarController sidebarController;
    @FXML private Label timerLabel;
    @FXML private ProgressBar timerProgress;
    @FXML private Slider durationSlider;
    @FXML private Label durationValueLabel;
    @FXML private Button startButton;
    @FXML private Button pauseButton;

    private int totalSeconds = 25 * 60;
    private int remainingSeconds = 25 * 60;
    private Timeline timeline;

    @FXML
    public void initialize() {
        sidebarController.setActive("focus");

        durationSlider.valueProperty().addListener((obs, oldV, newV) ->
                durationValueLabel.setText(String.valueOf(newV.intValue())));

        updateDisplay();
        pauseButton.setDisable(true);
    }

    @FXML
    private void handleSet() {
        stopTimeline();
        totalSeconds = (int) durationSlider.getValue() * 60;
        remainingSeconds = totalSeconds;
        updateDisplay();
    }

    @FXML
    private void handleStart() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(remainingSeconds);
        timeline.play();

        startButton.setDisable(true);
        pauseButton.setDisable(false);
    }

    private void tick() {
        remainingSeconds--;
        updateDisplay();
        if (remainingSeconds <= 0) {
            stopTimeline();
            logSession();
            new Alert(Alert.AlertType.INFORMATION, "Focus session complete! Great work.").showAndWait();
        }
    }

    @FXML
    private void handlePause() {
        stopTimeline();
    }

    @FXML
    private void handleReset() {
        stopTimeline();
        remainingSeconds = totalSeconds;
        updateDisplay();
    }

    private void stopTimeline() {
        if (timeline != null) timeline.stop();
        startButton.setDisable(false);
        pauseButton.setDisable(true);
    }

    private void updateDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        timerProgress.setProgress(totalSeconds == 0 ? 0 : 1.0 - ((double) remainingSeconds / totalSeconds));
    }

    private void logSession() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        int minutesFocused = totalSeconds / 60;
        AsyncUtil.run(
                () -> {
                    new FocusSessionDAO().insert(new FocusSession(0, user.getId(), LocalDate.now().toString(), minutesFocused));
                    return null;
                },
                v -> { },
                error -> error.printStackTrace()
        );
    }

    @FXML
    private void handleBack() {
        stopTimeline();
        NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard");
    }
}
