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

You have two options. Both work out of the box — no manual VM options needed.

**Option A — Maven goal (always works):**
```bash
mvn clean javafx:run
```
In IntelliJ: open the **Maven** tool window (right sidebar) →
`momenta → Plugins → javafx → javafx:run` → double-click. This wires up
the JavaFX module path automatically for your OS.

**Option B — IntelliJ's green ▶ Run button:**
Run **`Launcher`** (`src/main/java/com/momenta/Launcher.java`), not
`MainApp`. `Launcher` is a plain class with a `main()` method that just
calls `MainApp.main()` — since it doesn't itself extend
`javafx.application.Application`, it sidesteps the module-path check that
causes `Error: JavaFX runtime components are missing` when you run
`MainApp` directly from an IDE. If IntelliJ already created a run
configuration for `MainApp`, either delete it and run `Launcher` instead,
or edit that configuration's **Main class** field to `com.momenta.Launcher`.

> Why this happens: when the class you run directly extends `Application`,
> the JVM specifically checks for JavaFX on `--module-path` and fails even
> though JavaFX is present as a normal classpath dependency from Maven.
> Routing through an unrelated launcher class avoids that check, and
> JavaFX then loads fine from the classpath.

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

## Additional features (this update)

Layered on top of the core MVP, these round out the project for a
university-level submission:

| Feature | Where |
|---|---|
| **Task Categories & Tags** | `Task.category` (comma-separated), editable-combo field in the Add/Edit Task dialog, chip badges in the Tasks table and Kanban cards, and a category filter dropdown on the Tasks screen. |
| **Subtasks / Task Breakdown** | New `subtasks` table + `SubtaskDAO`. Click **Subtasks** on any task row to add/check off/delete steps in a small dialog (`SubtaskDialogs`). |
| **Recurring Tasks** | `Task.recurrence` (`DAILY`/`WEEKLY`/`MONTHLY`), set in the task dialog. When a recurring task is marked `DONE` (from the Tasks table or by dragging to **Done** on the Kanban board), `RecurrenceUtil` automatically inserts the next occurrence with the deadline advanced by one interval. |
| **Smart Notification System** | `NotificationService` scans tasks/reminders on every screen load and shows a count on the sidebar's 🔔 **Notifications** button; click it to see what's overdue or due in the next 24 hours. (In-app only — see note below.) |
| **Advanced Analytics** | Analytics screen now also shows an **Overdue** count and a **Priority Distribution** pie chart, alongside the existing completed/pending/avg-progress cards and weekly focus-minutes bar chart. |
| **Dark Mode + Settings** | New **Settings** screen: change username/occupation, change password, set a default Focus Mode duration, and pick Light/Dark/Auto theme. Dark mode is implemented by toggling a `theme-dark` style class (see `ThemeManager`) that redefines the same CSS color variables used everywhere else. |
| **Automatic Backup & Restore** | Every time tasks/reminders/goals/finance entries are loaded (i.e. right after any change), `BackupManager` writes a fresh `momenta.json` in the background. Settings → **Backup & Restore** also lets you trigger one manually or **restore** your data from that file (destructive — confirmed before running). |
| **Kanban Board** | New **Kanban Board** screen with To Do / In Progress / Done columns; drag a card between columns to update its status (uses JavaFX's built-in drag-and-drop, no extra library). |

### A correctness note on the backup format

`Task`, `Goal`, `Reminder` etc. use `javafx.beans.property.*` fields for
UI data-binding. Handing those objects straight to Gson would serialize
the *property objects'* internal state instead of their values, producing
broken, unreadable JSON. To keep `momenta.json` genuinely human-readable
and round-trippable, export/import goes through plain-Java DTOs in
`BackupBundle` (`TaskDTO`, `ReminderDTO`, `GoalDTO`, `FinanceDTO`) instead
of the domain model classes directly.

## MOMENTA 2.0 additions

A further round on top of the 8 features above, picked for the most
day-to-day value without ballooning scope indefinitely:

| Feature | Where |
|---|---|
| **Smarter Home Dashboard** | Greeting + date + focus streak, a "Your Day" row (overdue / due today / completed today / focused today), and Today's Schedule (today's reminders + tasks due today) replace the old stats-only dashboard. |
| **3-Tier "What Should I Do Now?"** | `RecommendationEngine` now surfaces 🔥 **Do Now** (urgent), ⚡ **Do Next** (important, not urgent) and 🌱 **Quick Win** (nearly done), each with a one-line "Because: ..." reason. |
| **Category Time Breakdown** | Dashboard's "Where Is Your Time Going?" shows a percentage bar per category, computed from your tasks' tags. |
| **Brain Dump / Quick Notes** | New **Brain Dump** screen (`NoteDAO`/`NotesController`) — jot something down, then **Convert to Task** or delete it later. |
| **Quick Add Everywhere** | **Ctrl+K** from any screen opens a chooser (Task / Reminder / Note / Goal / Finance entry); **Ctrl+N** jumps straight to a new task. Also reachable via the sidebar's **+ Quick Add** button. |
| **Break Management** | Focus Mode now offers a 5 or 10 min break after a focus session ends (Focus → Break → Focus), and shows **Today's Focus** total. |
| **Streaks & Achievements** | A 🔥 focus streak (consecutive days with a focus session) shows on the Dashboard, alongside computed achievement badges (first task, 10/100 tasks completed, 7-day streak, 10 hours focused, first goal completed). |
| **Kanban: Waiting column** | The board is now To Do → In Progress → **Waiting** → Done. |
| **Personalization** | Settings now also has an **Accent Color** picker (Blue/Teal/Purple/Orange) alongside the existing Light/Dark/Auto theme. |
| **Private Vault** | New PIN-protected **Private Vault** screen for notes/reference info that shouldn't live in your regular tasks — separate from, and *not* included in, the JSON backup (see note below). |
| **Finance category breakdown** | Finance screen now also shows a pie chart of expenses by category. |

### A privacy note on the Vault and backups

The Private Vault is deliberately **excluded** from `momenta.json` — the whole
point of a "private" section is that it doesn't end up in a plain-text
backup file that's easy to read or share. Everything else (tasks,
reminders, goals, finance entries, notes) is still backed up and restorable
as before.

### What's deliberately not included this round

The source document had ~24 ideas; these were left out as either too large
in scope for this pass, or genuinely needing platform features beyond
plain JavaFX — each is a reasonable follow-up using the same DAO →
Dialogs → Controller pattern used throughout:

- **Task Dependencies** (a task unlocking when its predecessor finishes) —
  would need a `task_dependencies` table and status-gating logic; Subtasks
  cover simple breakdown today, but not automatic unlocking.
- **Time Blocking** (an actual hour-by-hour day planner) — a bigger UI
  than the existing Calendar; the Dashboard's "Today's Schedule" is a
  lighter step in that direction.
- **Undo / Recycle Bin** — deletes are immediate throughout the app; a
  proper trash would need a `deleted_at` column (soft-delete) on every
  table plus a restore screen.
- **Activity History log** — would need a dedicated `activity_log` table
  and a logging hook added to every create/update/complete action across
  every controller.
- **Global Search**, **Dashboard card customization (show/hide widgets)**,
  and **"My Day" as its own dedicated morning/afternoon/evening screen** —
  the new Dashboard covers a good chunk of what "My Day" was going for,
  but not the full separate page with an evening review.
- **Space as a global Play/Pause shortcut** — deliberately skipped: binding
  a bare Space key globally would break typing spaces into any text field
  app-wide, so only Ctrl-based shortcuts (Ctrl+K, Ctrl+N) were added.
- **"Auto" theme (matching OS light/dark)** — still a documented no-op
  (behaves like Light), since detecting the OS theme cross-platform needs
  a native-integration library.

### Limitations / things you can extend next

- **Smart Notifications** are computed in-app (checked whenever a screen
  loads) rather than delivered as real OS-level popups — JavaFX has no
  built-in cross-platform system-tray notifier, so wiring one up would mean
  adding a native-integration library.
- **Dark "Auto" theme** currently behaves the same as Light — detecting the
  OS-level theme cross-platform needs a native library too, so it's left as
  a documented no-op for now.
- **Client / professional work management** and a few of the doc's "more
  advanced" ideas (Global Search, Undo/Redo, Achievements, Trash/Recently
  Deleted, File Attachments) aren't included — the DAO/controller pattern
  used throughout (see `SubtaskDAO`/`SubtaskDialogs` for the newest example)
  is the template to extend for any of these.
- Password hashing uses plain SHA-256 (no per-user salt) — adequate for an
  academic project, but swap in `BCrypt`/`Argon2` before using this for
  anything real.
- **Upgrading an existing `momenta.db`:** `DatabaseManager.runMigrations()`
  runs `ALTER TABLE ... ADD COLUMN` for the new `category`/`recurrence`/
  `theme`/`default_focus_minutes` columns on every startup, so an existing
  database from before this update upgrades itself automatically — no need
  to delete `momenta.db`.
