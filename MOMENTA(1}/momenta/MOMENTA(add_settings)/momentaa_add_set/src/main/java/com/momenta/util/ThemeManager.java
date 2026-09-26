package com.momenta.util;

import com.momenta.model.User;
import javafx.scene.Parent;

/**
 * Toggles the "theme-dark" style class on a screen's root node based on the
 * logged-in user's saved preference. "AUTO" currently falls back to light,
 * since JavaFX has no simple cross-platform OS-theme query without adding
 * a native-integration library — documented limitation for this project.
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
    }
}
