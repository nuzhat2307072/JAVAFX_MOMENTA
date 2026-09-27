package com.momenta;

/**
 * Separate entry point that does NOT extend javafx.application.Application.
 *
 * Why this exists: if you run a class that extends Application directly
 * (e.g. MainApp) from an IDE's plain "Run" button, the JVM checks whether
 * JavaFX is on the --module-path and fails with "JavaFX runtime components
 * are missing" even though JavaFX is present on the classpath as a normal
 * Maven dependency. Launching through this unrelated class avoids that
 * check entirely — JavaFX then loads fine from the classpath.
 *
 * Run THIS class from IntelliJ (or set it as the run configuration's main
 * class) instead of MainApp.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
