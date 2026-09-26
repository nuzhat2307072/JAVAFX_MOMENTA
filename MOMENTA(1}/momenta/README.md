# MOMENTA — Personal Work & Productivity Operating System

A JavaFX + FXML + Maven + SQLite desktop application implementing the core
MOMENTA feature set: authentication, dashboard, permanent task management,
calendar & reminders, a flexible focus timer, goals with milestones,
productivity analytics, and finance tracking — all backed by a local
SQLite database with a JSON backup export.

## Tech stack

| Layer            | Technology                                   |
|-------------------|-----------------------------------------------|
| UI                | JavaFX 21 + FXML (built for Scene Builder)    |
| Build             | Maven, Java 21                                |
| Database          | SQLite (via `sqlite-jdbc`), file `momenta.db` |
| Backup / export   | JSON via Gson, file `momenta.json`            |
| Concurrency       | `javafx.concurrent.Task` on a cached thread pool (`AsyncUtil`) so every DB read/write runs off the JavaFX Application Thread |
| Architecture      | MVC: `model` / `dao` (+`db`) / `controller` (+ FXML views), with `util` and `service`-style helpers |

## Project structure

```
momenta/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/momenta/
    │   ├── MainApp.java              — entry point, initializes the DB and shows the login screen
    │   ├── model/                    — Task, User, Reminder, Goal, Milestone, FinanceEntry, FocusSession
    │   ├── db/DatabaseManager.java   — SQLite connection + schema creation
    │   ├── dao/                      — UserDAO, TaskDAO, ReminderDAO, GoalDAO, FinanceDAO, FocusSessionDAO
    │   ├── util/
    │   │   ├── AsyncUtil.java        — background-thread helper (multithreading)
    │   │   ├── NavigationManager.java— swaps FXML screens on the single Stage
    │   │   ├── SessionManager.java   — holds the logged-in user for the run
    │   │   ├── PasswordUtil.java     — SHA-256 password hashing
    │   │   └── JsonExporter.java     — writes momenta.json backup via Gson
    │   └── controller/               — one controller per screen + small dialog helpers
    └── resources/com/momenta/
        ├── fxml/                     — login, register, sidebar, dashboard, tasks, calendar, focus, goals, analytics, finance
        └── css/styles.css            — professional blue / teal / navy theme
```

## Opening the project in IntelliJ IDEA

1. **File → Open...** and select the `momenta` folder (the one containing `pom.xml`).
   IntelliJ will detect it as a Maven project and import dependencies automatically
   (it needs internet access once, to download JavaFX, SQLite JDBC and Gson from Maven Central).
2. Make sure **Project SDK** is Java 21 (File → Project Structure → Project).
3. Once Maven finishes syncing, open `src/main/java/com/momenta/MainApp.java`.

## Running the app

The simplest and most reliable way to run a JavaFX + Maven project is the
`javafx-maven-plugin`, already configured in `pom.xml`:

```bash
mvn clean javafx:run
```

In IntelliJ: open the **Maven** tool window (right sidebar) →
`momenta → Plugins → javafx → javafx:run` → double-click. This launches
`MainApp` with the JavaFX module path wired up automatically for your OS
(Windows/macOS/Linux), so you don't need to configure VM options by hand.

> Running `MainApp.main()` directly from IntelliJ's green ▶ button can fail
> with "JavaFX runtime components are missing" because IntelliJ doesn't put
> JavaFX on the module path by default. If you want that to work too, add
> these VM options to the run configuration (point `--module-path` at your
> local `.m2` JavaFX jars or an installed JavaFX SDK):
> `--module-path <path-to-javafx-lib> --add-modules javafx.controls,javafx.fxml`
> — otherwise just use `mvn javafx:run`, which needs no extra setup.

## First run / demo flow

1. **Create Account** → pick a username, password, and occupation.
2. You're logged in automatically and land on the **Dashboard**.
3. Click **+ Quick Add Task**, set your own priority (1–5), a deadline and progress.
4. The task appears immediately under **All Saved Tasks**, and the
   **What Should I Do Now?** card recommends one based on your priority,
   deadline, and progress — without overriding your own priority choice.
5. Close the app completely and reopen it (`mvn javafx:run` again) →
   log back in → your tasks, reminders, goals and finance entries are
   still there, loaded from `momenta.db`.
6. Visit **Calendar** to add a dated reminder, **Focus Mode** to run a
   configurable timer (5–240 minutes) that logs a session used by
   **Analytics**, **Goals** to track milestones, and **Finance** to log
   income/expenses and export a `momenta.json` backup.

## Where the data lives

- `momenta.db` — the primary SQLite database. Created next to wherever the
  app is launched from (the project root when run via `mvn javafx:run` in
  IntelliJ). All users, tasks, reminders, goals, milestones, finance entries
  and focus sessions are stored here permanently.
- `momenta.json` — a human-readable export/backup, written on demand from
  the **Finance** screen's "Export Backup (JSON)" button (or by calling
  `JsonExporter.exportAll(userId)` from anywhere).

## Multithreading

Every database call in this project is dispatched through `AsyncUtil.run(...)`,
which submits a `javafx.concurrent.Task` to a small daemon-thread pool. This
keeps the JavaFX UI thread free while SQLite reads/writes happen in the
background; success and error callbacks are automatically marshalled back
onto the FX Application Thread by `javafx.concurrent.Task` itself, so
controllers can safely update UI controls directly inside them.

## Notes / things you can extend next

- **Client / professional work management** (feature #12 in the original
  spec) isn't included as its own screen yet — `finance_entries` and `tasks`
  already support a similar CRUD + DAO pattern you can copy to add a
  `clients` table and a `ClientsController` the same way.
- **Productivity streaks** (#14) can be derived from `focus_sessions` dates
  the same way `AnalyticsController` derives the weekly chart.
- Password hashing uses plain SHA-256 (no per-user salt) — adequate for an
  academic project, but swap in `BCrypt`/`Argon2` before using this for
  anything real.
