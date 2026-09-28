package com.momenta.controller;

import com.momenta.dao.UserDAO;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.NavigationManager;
import com.momenta.util.PasswordUtil;
import com.momenta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private ComboBox<String> occupationBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        occupationBox.setItems(FXCollections.observableArrayList(
                "Doctor", "Engineer", "Student", "Teacher", "Office Employee",
                "Business Owner", "Freelancer", "Researcher", "Developer", "Manager", "Other"
        ));
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
        String occupation = occupationBox.getValue();

        if (username.isEmpty() || password.isEmpty() || occupation == null) {
            errorLabel.setText("Please fill in all fields, including occupation.");
            return;
        }
        if (username.length() < 3) {
            errorLabel.setText("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        errorLabel.setText("");

        AsyncUtil.run(
                () -> {
                    UserDAO dao = new UserDAO();
                    if (dao.usernameExists(username)) {
                        throw new IllegalStateException("That username is already taken.");
                    }
                    return dao.createUser(username, PasswordUtil.hash(password), occupation);
                },
                (User created) -> {
                    SessionManager.login(created);
                    NavigationManager.navigate("dashboard.fxml", "MOMENTA - Dashboard");
                },
                error -> errorLabel.setText(error.getMessage())
        );
    }

    @FXML
    private void goToLogin() {
        NavigationManager.navigate("login.fxml", "MOMENTA - Login");
    }
}
