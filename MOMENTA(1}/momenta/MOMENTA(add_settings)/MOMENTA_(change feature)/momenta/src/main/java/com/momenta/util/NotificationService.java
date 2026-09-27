package com.momenta.util;

import com.momenta.dao.ReminderDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Reminder;
import com.momenta.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the list shown under the sidebar's notification bell: tasks that
 * are overdue or due within 24 hours, and reminders due within 24 hours.
 * Computed fresh each time it's requested (no separate notifications
 * table) since the app doesn't run a persistent OS-level notifier.
 */
public class NotificationService {

    public static List<String> getUpcoming(int userId) throws Exception {
        List<String> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);

        List<Task> tasks = new TaskDAO().getAllByUser(userId);
        for (Task t : tasks) {
            if ("DONE".equals(t.getStatus())) continue;
            String d = t.getDeadline();
            if (d == null || d.isEmpty()) continue;
            try {
                LocalDate deadline = LocalDate.parse(d);
                if (deadline.isBefore(LocalDate.now())) {
                    items.add("⚠ Overdue: \"" + t.getTitle() + "\" was due " + d);
                } else if (!deadline.atStartOfDay().isAfter(in24h)) {
                    items.add("⏰ Due soon: \"" + t.getTitle() + "\" is due " + d);
                }
            } catch (DateTimeParseException ignored) { }
        }

        List<Reminder> reminders = new ReminderDAO().getAllByUser(userId);
        for (Reminder r : reminders) {
            try {
                LocalDate date = LocalDate.parse(r.getDate());
                LocalTime time = LocalTime.parse(r.getTime());
                LocalDateTime when = LocalDateTime.of(date, time);
                if (!when.isBefore(now.minusMinutes(1)) && !when.isAfter(in24h)) {
                    items.add("🔔 Reminder: \"" + r.getTitle() + "\" at " + r.getDate() + " " + r.getTime());
                }
            } catch (DateTimeParseException ignored) { }
        }

        return items;
    }
}
