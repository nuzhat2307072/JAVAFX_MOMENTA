package com.momenta.util;

import java.io.IOException;

import com.momenta.controller.QuickAddDialogs;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
            ThemeManager.apply(root);

            if (scene == null) {
                scene = new Scene(root, 1200, 760);
                scene.getStylesheets().add(
                        NavigationManager.class.getResource("/com/momenta/css/styles.css").toExternalForm());
                primaryStage.setScene(scene);
                installGlobalShortcuts(scene);
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

    /**
     * Global Keyboard Shortcuts: attached once to the Scene (not per-screen,
     * since the Scene is reused across navigation) and only act while
     * someone is logged in. Deliberately limited to combinations that can't
     * collide with normal typing — a bare Space, for instance, is left out
     * because binding it globally would break typing spaces into any text
     * field.
     */
    private static void installGlobalShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                () -> { if (SessionManager.isLoggedIn()) QuickAddDialogs.showChooser(); });
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> { if (SessionManager.isLoggedIn()) QuickAddDialogs.quickAddTask(); });
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
