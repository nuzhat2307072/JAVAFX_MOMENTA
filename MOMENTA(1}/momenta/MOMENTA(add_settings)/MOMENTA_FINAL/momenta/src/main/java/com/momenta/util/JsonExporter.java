package com.momenta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.momenta.dao.*;
import com.momenta.model.*;
import com.momenta.util.BackupBundle.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Exports the logged-in user's data to momenta.json as a human-readable
 * backup alongside the primary SQLite database (momenta.db). SQLite stays
 * the source of truth; this file is a readable snapshot/backup of it that
 * JsonImporter can restore from.
 */
public class JsonExporter {

    public static void exportAll(int userId) throws IOException, java.sql.SQLException {
        User user = SessionManager.getCurrentUser();

        BackupBundle bundle = new BackupBundle();
        bundle.exportedForUser = user != null ? user.getUsername() : "unknown";
        bundle.occupation = user != null ? user.getOccupation() : "";
        bundle.exportedAt = LocalDateTime.now().toString();

        for (Task t : new TaskDAO().getAllByUser(userId)) bundle.tasks.add(TaskDTO.from(t));
        for (Reminder r : new ReminderDAO().getAllByUser(userId)) bundle.reminders.add(ReminderDTO.from(r));
        for (Goal g : new GoalDAO().getAllByUser(userId)) bundle.goals.add(GoalDTO.from(g));
        for (FinanceEntry e : new FinanceDAO().getAllByUser(userId)) bundle.financeEntries.add(FinanceDTO.from(e));
        for (Note n : new NoteDAO().getAllByUser(userId)) bundle.notes.add(NoteDTO.from(n));
        for (HealthLog h : new HealthDAO().getHistory(userId, Integer.MAX_VALUE)) bundle.healthLogs.add(HealthLogDTO.from(h));
        for (Habit h : new HabitDAO().getAllByUser(userId)) bundle.habits.add(HabitDTO.from(h));
        for (HealthGoal g : new HealthGoalDAO().getAllByUser(userId)) bundle.healthGoals.add(HealthGoalDTO.from(g));
        for (LifeEntry e : new LifeEntryDAO().getAllByUser(userId)) bundle.lifeEntries.add(LifeEntryDTO.from(e));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("momenta.json")) {
            gson.toJson(bundle, writer);
        }
    }
}
