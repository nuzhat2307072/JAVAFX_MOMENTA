package com.momenta.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the single SQLite connection used across the app and creates
 * the schema on first run. The database file (momenta.db) is created in
 * the working directory (project root when run from IntelliJ / mvn).
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:momenta.db";
    private static Connection connection;

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                try (Statement s = connection.createStatement()) {
                    s.execute("PRAGMA foreign_keys = ON");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Could not connect to database", e);
        }
        return connection;
    }

    /** Creates all tables if they do not already exist. Call once at startup. */
    public static void initializeDatabase() {
        String users = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                occupation TEXT
            )
            """;

        String tasks = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                deadline TEXT,
                priority INTEGER DEFAULT 3,
                progress INTEGER DEFAULT 0,
                status TEXT DEFAULT 'PENDING',
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        String reminders = """
            CREATE TABLE IF NOT EXISTS reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                note TEXT,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        String goals = """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                progress INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        String milestones = """
            CREATE TABLE IF NOT EXISTS milestones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                goal_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                done INTEGER DEFAULT 0,
                FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE
            )
            """;

        String finance = """
            CREATE TABLE IF NOT EXISTS finance_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                category TEXT,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        String focusSessions = """
            CREATE TABLE IF NOT EXISTS focus_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        try (Statement s = getConnection().createStatement()) {
            s.execute(users);
            s.execute(tasks);
            s.execute(reminders);
            s.execute(goals);
            s.execute(milestones);
            s.execute(finance);
            s.execute(focusSessions);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database schema", e);
        }
    }
}
