package com.momenta;

import com.momenta.db.DatabaseManager;
import com.momenta.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create tables on first run; SQLite connects lazily and the file
        // momenta.db appears in the working directory once this returns.
        DatabaseManager.initializeDatabase();

        NavigationManager.setStage(primaryStage);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.setResizable(true);
        NavigationManager.navigate("login.fxml", "MOMENTA - Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
