package com.momenta.util;

import com.momenta.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The shape of momenta.json. Deliberately plain (no JavaFX Property fields):
 * the domain models (Task, Goal, ...) use javafx.beans.property.* fields for
 * UI binding, and Gson would serialize those property objects' internal
 * state rather than their values if handed the domain objects directly. These
 * DTOs hold plain values instead, so the JSON stays human-readable and
 * round-trips correctly on import.
 */
public class BackupBundle {
    public String exportedForUser;
    public String occupation;
    public String exportedAt;
    public List<TaskDTO> tasks = new ArrayList<>();
    public List<ReminderDTO> reminders = new ArrayList<>();
    public List<GoalDTO> goals = new ArrayList<>();
    public List<FinanceDTO> financeEntries = new ArrayList<>();
    public List<NoteDTO> notes = new ArrayList<>();
    // Private Vault entries are intentionally NOT included here — the vault
    // exists to keep some things out of a plain, readable backup file.

    public static class NoteDTO {
        public String content;
        public String createdAt;

        public static NoteDTO from(Note n) {
            NoteDTO d = new NoteDTO();
            d.content = n.getContent();
            d.createdAt = n.getCreatedAt();
            return d;
        }

        public Note toNote(int userId) {
            Note n = new Note();
            n.setUserId(userId);
            n.setContent(content);
            n.setCreatedAt(createdAt);
            return n;
        }
    }

    public static class TaskDTO {
        public String title, description, deadline, priority, progress, status, category, recurrence;

        public static TaskDTO from(Task t) {
            TaskDTO d = new TaskDTO();
            d.title = t.getTitle();
            d.description = t.getDescription();
            d.deadline = t.getDeadline();
            d.priority = String.valueOf(t.getPriority());
            d.progress = String.valueOf(t.getProgress());
            d.status = t.getStatus();
            d.category = t.getCategory();
            d.recurrence = t.getRecurrence();
            return d;
        }

        public Task toTask(int userId) {
            Task t = new Task();
            t.setUserId(userId);
            t.setTitle(title);
            t.setDescription(description);
            t.setDeadline(deadline);
            t.setPriority(parseIntOr(priority, 3));
            t.setProgress(parseIntOr(progress, 0));
            t.setStatus(status == null ? "PENDING" : status);
            t.setCategory(category == null ? "" : category);
            t.setRecurrence(recurrence == null ? "NONE" : recurrence);
            return t;
        }
    }

    public static class ReminderDTO {
        public String title, note, date, time;

        public static ReminderDTO from(Reminder r) {
            ReminderDTO d = new ReminderDTO();
            d.title = r.getTitle();
            d.note = r.getNote();
            d.date = r.getDate();
            d.time = r.getTime();
            return d;
        }

        public Reminder toReminder(int userId) {
            Reminder r = new Reminder();
            r.setUserId(userId);
            r.setTitle(title);
            r.setNote(note);
            r.setDate(date);
            r.setTime(time);
            return r;
        }
    }

    public static class MilestoneDTO {
        public String title;
        public boolean done;

        public static MilestoneDTO from(Milestone m) {
            MilestoneDTO d = new MilestoneDTO();
            d.title = m.getTitle();
            d.done = m.isDone();
            return d;
        }
    }

    public static class GoalDTO {
        public String title;
        public String progress;
        public List<MilestoneDTO> milestones = new ArrayList<>();

        public static GoalDTO from(Goal g) {
            GoalDTO d = new GoalDTO();
            d.title = g.getTitle();
            d.progress = String.valueOf(g.getProgress());
            for (Milestone m : g.getMilestones()) d.milestones.add(MilestoneDTO.from(m));
            return d;
        }
    }

    public static class FinanceDTO {
        public String type, category, amount, date;

        public static FinanceDTO from(FinanceEntry e) {
            FinanceDTO d = new FinanceDTO();
            d.type = e.getType();
            d.category = e.getCategory();
            d.amount = String.valueOf(e.getAmount());
            d.date = e.getDate();
            return d;
        }

        public FinanceEntry toEntry(int userId) {
            FinanceEntry e = new FinanceEntry();
            e.setUserId(userId);
            e.setType(type);
            e.setCategory(category);
            e.setAmount(parseDoubleOr(amount, 0));
            e.setDate(date);
            return e;
        }
    }

    private static int parseIntOr(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private static double parseDoubleOr(String s, double fallback) {
        try { return Double.parseDouble(s); } catch (Exception e) { return fallback; }
    }
}
