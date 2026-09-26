package com.momenta.util;

import com.momenta.model.Task;

import java.time.LocalDate;

/**
 * When a recurring task (DAILY / WEEKLY / MONTHLY) is marked DONE, this
 * builds the next occurrence — same title, description, priority, category
 * and recurrence, fresh progress/status, deadline advanced by one interval.
 */
public class RecurrenceUtil {

    public static boolean isRecurring(Task task) {
        String r = task.getRecurrence();
        return r != null && !r.isEmpty() && !"NONE".equals(r);
    }

    public static Task buildNextOccurrence(Task completed) {
        Task next = new Task();
        next.setUserId(completed.getUserId());
        next.setTitle(completed.getTitle());
        next.setDescription(completed.getDescription());
        next.setPriority(completed.getPriority());
        next.setCategory(completed.getCategory());
        next.setRecurrence(completed.getRecurrence());
        next.setProgress(0);
        next.setStatus("PENDING");
        next.setDeadline(nextDeadline(completed.getDeadline(), completed.getRecurrence()).toString());
        return next;
    }

    private static LocalDate nextDeadline(String currentDeadline, String recurrence) {
        LocalDate base;
        try {
            base = (currentDeadline == null || currentDeadline.isEmpty())
                    ? LocalDate.now() : LocalDate.parse(currentDeadline);
        } catch (Exception e) {
            base = LocalDate.now();
        }
        // If the old deadline has already passed, anchor the next one on today instead.
        if (base.isBefore(LocalDate.now())) {
            base = LocalDate.now();
        }
        return switch (recurrence) {
            case "DAILY" -> base.plusDays(1);
            case "WEEKLY" -> base.plusWeeks(1);
            case "MONTHLY" -> base.plusMonths(1);
            default -> base;
        };
    }
}
