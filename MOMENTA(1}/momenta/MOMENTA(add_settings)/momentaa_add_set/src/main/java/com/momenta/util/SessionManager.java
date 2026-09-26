package com.momenta.util;

import com.momenta.model.User;

/** Holds the currently logged-in user for the lifetime of the app run. */
public class SessionManager {
    private static User currentUser;

    public static void login(User user) { currentUser = user; }

    public static void logout() { currentUser = null; }

    public static User getCurrentUser() { return currentUser; }

    public static boolean isLoggedIn() { return currentUser != null; }
}
