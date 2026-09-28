package com.momenta.util;

import com.google.gson.Gson;
import com.momenta.dao.*;
import com.momenta.model.Task;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Restores from momenta.json into SQLite for the given user. This REPLACES
 * that user's current tasks/reminders/goals/finance entries with what's in
 * the backup file — callers should confirm with the user before calling
 * this, since it's destructive to any changes made after the backup.
 */
public class JsonImporter {

    public static BackupBundle readBackupFile() throws IOException {
        try (FileReader reader = new FileReader("momenta.json")) {
            Gson gson = new Gson();
            BackupBundle bundle = gson.fromJson(reader, BackupBundle.class);
            if (bundle == null) throw new IOException("momenta.json is empty or invalid.");
            return bundle;
        }
    }

    /** Wipes and re-populates this user's tasks, reminders, goals+milestones and finance entries. */
    public static void restoreForUser(int userId, BackupBundle bundle) throws SQLException {
        TaskDAO taskDAO = new TaskDAO();
        for (Task existing : taskDAO.getAllByUser(userId)) taskDAO.permanentlyDelete(existing.getId());
        if (bundle.tasks != null) {
            for (BackupBundle.TaskDTO dto : bundle.tasks) taskDAO.insert(dto.toTask(userId));
        }

        ReminderDAO reminderDAO = new ReminderDAO();
        for (var existing : reminderDAO.getAllByUser(userId)) reminderDAO.permanentlyDelete(existing.getId());
        if (bundle.reminders != null) {
            for (BackupBundle.ReminderDTO dto : bundle.reminders) reminderDAO.insert(dto.toReminder(userId));
        }

        GoalDAO goalDAO = new GoalDAO();
        for (var existing : goalDAO.getAllByUser(userId)) goalDAO.permanentlyDeleteGoal(existing.getId());
        if (bundle.goals != null) {
            for (BackupBundle.GoalDTO dto : bundle.goals) {
                var goal = goalDAO.insertGoal(new com.momenta.model.Goal(0, userId, dto.title,
                        parseIntOr(dto.progress, 0)));
                if (dto.milestones != null) {
                    for (BackupBundle.MilestoneDTO m : dto.milestones) {
                        goalDAO.insertMilestone(new com.momenta.model.Milestone(0, goal.getId(), m.title, m.done));
                    }
                }
            }
        }

        FinanceDAO financeDAO = new FinanceDAO();
        for (var existing : financeDAO.getAllByUser(userId)) financeDAO.permanentlyDelete(existing.getId());
        if (bundle.financeEntries != null) {
            for (BackupBundle.FinanceDTO dto : bundle.financeEntries) financeDAO.insert(dto.toEntry(userId));
        }

        NoteDAO noteDAO = new NoteDAO();
        for (var existing : noteDAO.getAllByUser(userId)) noteDAO.permanentlyDelete(existing.getId());
        if (bundle.notes != null) {
            for (BackupBundle.NoteDTO dto : bundle.notes) noteDAO.insert(dto.toNote(userId));
        }

        HealthDAO healthDAO = new HealthDAO();
        if (bundle.healthLogs != null) {
            for (BackupBundle.HealthLogDTO dto : bundle.healthLogs) {
                // health_logs is unique on (user_id, date): getOrCreateToday-style rows for
                // other dates don't exist yet, so insert directly rather than via the DAO's
                // update-only saveLog (which assumes the row is already there).
                var existing = healthDAO.getByDate(userId, dto.date);
                var log = dto.toHealthLog(userId);
                if (existing != null) {
                    log.setId(existing.getId());
                    healthDAO.saveLog(log);
                } else if (dto.date != null && !dto.date.isBlank()) {
                    insertHealthLog(log);
                }
            }
        }

        HabitDAO habitDAO = new HabitDAO();
        for (var existing : habitDAO.getAllByUser(userId)) habitDAO.permanentlyDeleteHabit(existing.getId());
        if (bundle.habits != null) {
            for (BackupBundle.HabitDTO dto : bundle.habits) habitDAO.insertHabit(dto.toHabit(userId));
        }

        HealthGoalDAO healthGoalDAO = new HealthGoalDAO();
        for (var existing : healthGoalDAO.getAllByUser(userId)) healthGoalDAO.permanentlyDeleteGoal(existing.getId());
        if (bundle.healthGoals != null) {
            for (BackupBundle.HealthGoalDTO dto : bundle.healthGoals) healthGoalDAO.insertGoal(dto.toHealthGoal(userId));
        }

        LifeEntryDAO lifeEntryDAO = new LifeEntryDAO();
        for (var existing : lifeEntryDAO.getAllByUser(userId)) lifeEntryDAO.permanentlyDelete(existing.getId());
        if (bundle.lifeEntries != null) {
            for (BackupBundle.LifeEntryDTO dto : bundle.lifeEntries) lifeEntryDAO.insert(dto.toLifeEntry(userId));
        }
    }

    /** health_logs has no plain "insert with all fields" DAO method — insert directly here. */
    private static void insertHealthLog(com.momenta.model.HealthLog log) throws SQLException {
        String sql = "INSERT INTO health_logs(user_id, date, sleep_hours, water_glasses, " +
                "exercise_minutes, meals, mood, weight_kg, steps, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var ps = com.momenta.db.DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getDate());
            ps.setDouble(3, log.getSleepHours());
            ps.setInt(4, log.getWaterGlasses());
            ps.setInt(5, log.getExerciseMinutes());
            ps.setString(6, log.getMeals());
            ps.setString(7, log.getMood());
            ps.setDouble(8, log.getWeightKg());
            ps.setInt(9, log.getSteps());
            ps.setString(10, log.getNotes());
            ps.executeUpdate();
        }
    }

    private static int parseIntOr(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }
}
