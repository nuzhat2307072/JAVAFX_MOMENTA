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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.Optional;

public class FocusController {

    @FXML private SidebarController sidebarController;
    @FXML private Label modeLabel;
    @FXML private Label timerLabel;
    @FXML private ProgressBar timerProgress;
    @FXML private Label todayTotalLabel;
    @FXML private VBox durationBox;
    @FXML private Slider durationSlider;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Button startButton;
    @FXML private Button pauseButton;

    private enum Mode { FOCUS, BREAK }
    private Mode mode = Mode.FOCUS;

    private static final int MIN_MINUTES = 5;
    private static final int MAX_MINUTES = 240;

    private int totalSeconds = 25 * 60;
    private int remainingSeconds = 25 * 60;
    private Timeline timeline;
    private boolean syncingDuration = false;

    @FXML
    public void initialize() {
        sidebarController.setActive("focus");

        User user = SessionManager.getCurrentUser();
        int defaultMinutes = clampMinutes(user != null ? user.getDefaultFocusMinutes() : 25);

        durationSlider.setValue(defaultMinutes);

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_MINUTES, MAX_MINUTES, defaultMinutes, 5);
        durationSpinner.setValueFactory(valueFactory);
        durationSpinner.setEditable(true);

        // Slider and Spinner (typed input) stay in sync in both directions.
        durationSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (syncingDuration) return;
            syncingDuration = true;
            valueFactory.setValue(clampMinutes(newV.intValue()));
            syncingDuration = false;
        });
        durationSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            if (syncingDuration) return;
            syncingDuration = true;
            durationSlider.setValue(newV);
            syncingDuration = false;
        });

        // A typed value only becomes the Spinner's value on Enter or focus-loss
        // by default in JavaFX — commit it explicitly so typing "45" and
        // clicking SET actually uses 45, not whatever the slider still shows.
        durationSpinner.getEditor().setOnAction(e -> commitTypedDuration());
        durationSpinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) commitTypedDuration();
        });

        totalSeconds = defaultMinutes * 60;
        remainingSeconds = totalSeconds;

        updateDisplay();
        pauseButton.setDisable(true);
        refreshTodayTotal();
    }

    private void commitTypedDuration() {
        try {
            int typed = Integer.parseInt(durationSpinner.getEditor().getText().trim());
            durationSpinner.getValueFactory().setValue(clampMinutes(typed));
        } catch (NumberFormatException e) {
            // Not a valid number — revert the text box to the last valid value.
            durationSpinner.getEditor().setText(String.valueOf(durationSpinner.getValue()));
        }
    }

    private int clampMinutes(int minutes) {
        return Math.max(MIN_MINUTES, Math.min(MAX_MINUTES, minutes));
    }

    private void refreshTodayTotal() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new FocusSessionDAO().getMinutesToday(user.getId()),
                minutes -> todayTotalLabel.setText("Today's Focus: " + minutes + " min"),
                error -> { }
        );
    }

    @FXML
    private void handleSet() {
        stopTimeline();
        commitTypedDuration();
        mode = Mode.FOCUS;
        modeLabel.setText("Choose your own duration — no fixed 25-minute timer");
        durationBox.setDisable(false);
        totalSeconds = durationSpinner.getValue() * 60;
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
            if (mode == Mode.FOCUS) {
                logFocusSession();
                offerBreak();
            } else {
                offerNextFocusSession();
            }
        }
    }

    /** Break Management: after a focus session, offer Focus → Break → Focus. */
    private void offerBreak() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Focus session complete!");
        alert.setHeaderText("Great work. Take a break?");
        ButtonType break5 = new ButtonType("5 min break");
        ButtonType break10 = new ButtonType("10 min break");
        ButtonType skip = new ButtonType("Skip break", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(break5, break10, skip);

        Optional<ButtonType> choice = alert.showAndWait();
        int breakMinutes = choice.isPresent() && choice.get() == break5 ? 5
                : choice.isPresent() && choice.get() == break10 ? 10 : 0;

        if (breakMinutes > 0) {
            startBreak(breakMinutes);
        } else {
            handleReset();
        }
    }

    private void startBreak(int minutes) {
        mode = Mode.BREAK;
        modeLabel.setText("☕ Break time");
        durationBox.setDisable(true);
        totalSeconds = minutes * 60;
        remainingSeconds = totalSeconds;
        updateDisplay();
        handleStart();
    }

    private void offerNextFocusSession() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Break's over. Ready for another focus session?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Break complete");
        alert.setHeaderText(null);
        Optional<ButtonType> choice = alert.showAndWait();
        mode = Mode.FOCUS;
        modeLabel.setText("Choose your own duration — no fixed 25-minute timer");
        durationBox.setDisable(false);
        if (choice.isPresent() && choice.get() == ButtonType.YES) {
            handleSet();
        } else {
            handleReset();
        }
    }

    @FXML
    private void handlePause() {
        stopTimeline();
    }

    @FXML
    private void handleReset() {
        stopTimeline();
        mode = Mode.FOCUS;
        modeLabel.setText("Choose your own duration — no fixed 25-minute timer");
        durationBox.setDisable(false);
        totalSeconds = durationSpinner.getValue() * 60;
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

    private void logFocusSession() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        int minutesFocused = totalSeconds / 60;
        AsyncUtil.run(
                () -> {
                    new FocusSessionDAO().insert(new FocusSession(0, user.getId(), LocalDate.now().toString(), minutesFocused));
                    return null;
                },
                v -> refreshTodayTotal(),
                error -> error.printStackTrace()
        );
    }

    @FXML
    private void handleBack() {
        stopTimeline();
        NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard");
    }
}
