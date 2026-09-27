package com.momenta.util;

import com.momenta.model.User;

/** Holds the currently logged-in user for the lifetime of the app run. */
public class SessionManager {
    private static User currentUser;
    private static boolean vaultUnlocked = false;

    public static void login(User user) {
        currentUser = user;
        vaultUnlocked = false;
    }

    public static void logout() {
        currentUser = null;
        vaultUnlocked = false;
    }

    public static User getCurrentUser() { return currentUser; }

    public static boolean isLoggedIn() { return currentUser != null; }

    /** Private Vault stays unlocked for the rest of this run once the correct PIN is entered once. */
    public static boolean isVaultUnlocked() { return vaultUnlocked; }
    public static void setVaultUnlocked(boolean unlocked) { vaultUnlocked = unlocked; }
}
