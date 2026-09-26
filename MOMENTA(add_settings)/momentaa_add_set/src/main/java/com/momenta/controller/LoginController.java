package com.momenta.controller;

import com.momenta.dao.UserDAO;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.NavigationManager;
import com.momenta.util.PasswordUtil;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }
        errorLabel.setText("");

        // Database lookup runs off the JavaFX Application Thread.
        AsyncUtil.run(
                () -> new UserDAO().findByUsername(username),
                (Optional<User> found) -> {
                    if (found.isPresent() && PasswordUtil.matches(password, found.get().getPasswordHash())) {
                        SessionManager.login(found.get());
                        NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard");
                    } else {
                        errorLabel.setText("Invalid username or password.");
                    }
                },
                error -> errorLabel.setText("Login failed: " + error.getMessage())
        );
    }

    @FXML
    private void goToRegister() {
        NavigationManager.navigate("register.fxml", "MOMENTA - Create Account");
    }
}
