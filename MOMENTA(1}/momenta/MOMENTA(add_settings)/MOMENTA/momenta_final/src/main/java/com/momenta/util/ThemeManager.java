package com.momenta.util;

import com.momenta.model.User;
import javafx.scene.Parent;

/**
 * Applies the logged-in user's theme (Light/Dark/Auto) and accent color to
 * a screen's root node. "AUTO" currently falls back to light, since JavaFX
 * has no simple cross-platform OS-theme query without adding a native
 * integration library — documented limitation for this project.
 */
public class ThemeManager {

    public static void apply(Parent root) {
        if (root == null) return;
        User user = SessionManager.getCurrentUser();
        boolean dark = user != null && "DARK".equals(user.getTheme());
        root.getStyleClass().remove("theme-dark");
        if (dark) {
            root.getStyleClass().add("theme-dark");
        }

        String accentHex = accentHex(user == null ? "BLUE" : user.getAccentColor());
        root.setStyle("-fx-blue: " + accentHex + ";");
    }

    private static String accentHex(String accent) {
        return switch (accent == null ? "BLUE" : accent) {
            case "TEAL" -> "#14B8A6";
            case "PURPLE" -> "#8B5CF6";
            case "ORANGE" -> "#F97316";
            default -> "#1C6FEB"; // BLUE
        };
    }
}
