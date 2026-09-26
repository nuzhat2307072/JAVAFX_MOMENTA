package com.momenta.controller;

import com.momenta.dao.UserDAO;
import com.momenta.model.User;
import com.momenta.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private SidebarController sidebarController;

    @FXML private TextField usernameField;
    @FXML private ComboBox<String> occupationBox;
    @FXML private Label accountMessageLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordMessageLabel;

    @FXML private Slider focusDurationSlider;
    @FXML private Label focusDurationValueLabel;
    @FXML private RadioButton lightThemeRadio;
    @FXML private RadioButton darkThemeRadio;
    @FXML private RadioButton autoThemeRadio;
    @FXML private Label preferencesMessageLabel;

    @FXML private Label backupMessageLabel;

    @FXML
    public void initialize() {
        sidebarController.setActive("settings");

        occupationBox.setItems(FXCollections.observableArrayList(
                "Doctor", "Engineer", "Student", "Teacher", "Office Employee",
                "Business Owner", "Freelancer", "Researcher", "Developer", "Manager", "Other"));

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        usernameField.setText(user.getUsername());
        occupationBox.setValue(user.getOccupation());

        focusDurationSlider.setValue(user.getDefaultFocusMinutes());
        focusDurationValueLabel.setText(String.valueOf(user.getDefaultFocusMinutes()));
        focusDurationSlider.valueProperty().addListener((obs, oldV, newV) ->
                focusDurationValueLabel.setText(String.valueOf(newV.intValue())));

        switch (user.getTheme() == null ? "LIGHT" : user.getTheme()) {
            case "DARK" -> darkThemeRadio.setSelected(true);
            case "AUTO" -> autoThemeRadio.setSelected(true);
            default -> lightThemeRadio.setSelected(true);
        }
    }

    @FXML
    private void handleSaveAccount() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        String newUsername = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String occupation = occupationBox.getValue();

        if (newUsername.length() < 3) {
            accountMessageLabel.setText("Username must be at least 3 characters.");
            return;
        }
        accountMessageLabel.setText("");

        AsyncUtil.run(
                () -> {
                    UserDAO dao = new UserDAO();
                    if (!newUsername.equals(user.getUsername()) && dao.usernameTakenByAnotherUser(newUsername, user.getId())) {
                        throw new IllegalStateException("That username is already taken.");
                    }
                    dao.updateUsername(user.getId(), newUsername);
                    dao.updateOccupation(user.getId(), occupation);
                    return null;
                },
                v -> {
                    user.setUsername(newUsername);
                    user.setOccupation(occupation);
                    accountMessageLabel.setText("Saved.");
                    // Refresh so the sidebar reflects the new username/occupation immediately.
                    NavigationManager.navigate("settings.fxml", "MOMENTA - Settings");
                },
                error -> accountMessageLabel.setText(error.getMessage())
        );
    }

    @FXML
    private void handleChangePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        String current = currentPasswordField.getText() == null ? "" : currentPasswordField.getText();
        String next = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (!PasswordUtil.matches(current, user.getPasswordHash())) {
            passwordMessageLabel.setText("Current password is incorrect.");
            return;
        }
        if (next.length() < 4) {
            passwordMessageLabel.setText("New password must be at least 4 characters.");
            return;
        }
        if (!next.equals(confirm)) {
            passwordMessageLabel.setText("New passwords do not match.");
            return;
        }
        passwordMessageLabel.setText("");
        String newHash = PasswordUtil.hash(next);

        AsyncUtil.run(
                () -> { new UserDAO().updatePassword(user.getId(), newHash); return null; },
                v -> {
                    user.setPasswordHash(newHash);
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    passwordMessageLabel.setText("Password changed.");
                },
                error -> passwordMessageLabel.setText("Could not change password: " + error.getMessage())
        );
    }

    @FXML
    private void handleSavePreferences() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        int minutes = (int) focusDurationSlider.getValue();
        String theme = darkThemeRadio.isSelected() ? "DARK" : autoThemeRadio.isSelected() ? "AUTO" : "LIGHT";

        AsyncUtil.run(
                () -> {
                    UserDAO dao = new UserDAO();
                    dao.updateDefaultFocusMinutes(user.getId(), minutes);
                    dao.updateTheme(user.getId(), theme);
                    return null;
                },
                v -> {
                    user.setDefaultFocusMinutes(minutes);
                    user.setTheme(theme);
                    preferencesMessageLabel.setText("Preferences saved.");
                    // Re-navigate so ThemeManager immediately re-applies the (possibly new) theme.
                    NavigationManager.navigate("settings.fxml", "MOMENTA - Settings");
                },
                error -> preferencesMessageLabel.setText("Could not save preferences: " + error.getMessage())
        );
    }

    @FXML
    private void handleBackupNow() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> { JsonExporter.exportAll(user.getId()); return null; },
                v -> backupMessageLabel.setText("Backup written to momenta.json."),
                error -> backupMessageLabel.setText("Backup failed: " + error.getMessage())
        );
    }

    @FXML
    private void handleRestore() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This replaces your current tasks, reminders, goals and finance entries with " +
                        "whatever is in momenta.json. This cannot be undone. Continue?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            AsyncUtil.run(
                    () -> {
                        BackupBundle bundle = JsonImporter.readBackupFile();
                        JsonImporter.restoreForUser(user.getId(), bundle);
                        return null;
                    },
                    v -> backupMessageLabel.setText("Restored from momenta.json. Revisit Tasks/Goals/Finance to see the restored data."),
                    error -> backupMessageLabel.setText("Restore failed: " + error.getMessage())
            );
        });
    }
}
