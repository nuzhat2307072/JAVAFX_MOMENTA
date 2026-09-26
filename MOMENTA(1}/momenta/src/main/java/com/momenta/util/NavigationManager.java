package com.momenta.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Loads FXML screens into the single primary Stage. */
public class NavigationManager {
    private static Stage primaryStage;
    private static Scene scene;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void navigate(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource("/com/momenta/fxml/" + fxmlFile));
            Parent root = loader.load();

            if (scene == null) {
                scene = new Scene(root, 1200, 760);
                scene.getStylesheets().add(
                        NavigationManager.class.getResource("/com/momenta/css/styles.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.setTitle(title);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load screen: " + fxmlFile, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
