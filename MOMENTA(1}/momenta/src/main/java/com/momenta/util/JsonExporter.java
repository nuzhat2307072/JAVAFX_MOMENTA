package com.momenta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.momenta.dao.*;
import com.momenta.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports the logged-in user's data to momenta.json as a human-readable
 * backup alongside the primary SQLite database (momenta.db).
 */
public class JsonExporter {

    private static class ExportBundle {
        String exportedForUser;
        String occupation;
        List<Task> tasks;
        List<Reminder> reminders;
        List<Goal> goals;
        List<FinanceEntry> financeEntries;
    }

    public static void exportAll(int userId) throws IOException {
        User user = SessionManager.getCurrentUser();

        ExportBundle bundle = new ExportBundle();
        bundle.exportedForUser = user != null ? user.getUsername() : "unknown";
        bundle.occupation = user != null ? user.getOccupation() : "";
        bundle.tasks = new TaskDAO().getAllByUser(userId);
        bundle.reminders = new ReminderDAO().getAllByUser(userId);
        bundle.goals = new GoalDAO().getAllByUser(userId);
        bundle.financeEntries = new FinanceDAO().getAllByUser(userId);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("momenta.json")) {
            gson.toJson(bundle, writer);
        }
    }
}
