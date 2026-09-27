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

        String subtasks = """
            CREATE TABLE IF NOT EXISTS subtasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                done INTEGER DEFAULT 0,
                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
            )
            """;

        String notes = """
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

        String vaultEntries = """
            CREATE TABLE IF NOT EXISTS vault_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
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
            s.execute(subtasks);
            s.execute(notes);
            s.execute(vaultEntries);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database schema", e);
        }

        runMigrations();
    }

    /**
     * Adds columns introduced after the initial release to any existing
     * momenta.db so upgrading doesn't require deleting the database.
     * SQLite has no "ADD COLUMN IF NOT EXISTS" in the JDBC driver version
     * used here, so each ALTER is attempted and a "duplicate column" failure
     * (meaning it was already applied) is simply ignored.
     */
    private static void runMigrations() {
        tryAlter("ALTER TABLE tasks ADD COLUMN category TEXT DEFAULT ''");
        tryAlter("ALTER TABLE tasks ADD COLUMN recurrence TEXT DEFAULT 'NONE'");
        tryAlter("ALTER TABLE tasks ADD COLUMN completed_at TEXT");
        tryAlter("ALTER TABLE users ADD COLUMN theme TEXT DEFAULT 'LIGHT'");
        tryAlter("ALTER TABLE users ADD COLUMN default_focus_minutes INTEGER DEFAULT 25");
        tryAlter("ALTER TABLE users ADD COLUMN accent_color TEXT DEFAULT 'BLUE'");
        tryAlter("ALTER TABLE users ADD COLUMN vault_pin_hash TEXT");

        // Trash / Recycle Bin: soft-delete columns. A non-null deleted_at means
        // "in trash" — normal queries filter it out; TrashDAO reads/restores/
        // permanently deletes rows by it instead of a hard DELETE.
        tryAlter("ALTER TABLE tasks ADD COLUMN deleted_at TEXT");
        tryAlter("ALTER TABLE reminders ADD COLUMN deleted_at TEXT");
        tryAlter("ALTER TABLE goals ADD COLUMN deleted_at TEXT");
        tryAlter("ALTER TABLE notes ADD COLUMN deleted_at TEXT");
        tryAlter("ALTER TABLE finance_entries ADD COLUMN deleted_at TEXT");
        tryAlter("ALTER TABLE vault_entries ADD COLUMN deleted_at TEXT");
    }

    private static void tryAlter(String sql) {
        try (Statement s = getConnection().createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            // Column already exists from a previous run — safe to ignore.
        }
    }
}
