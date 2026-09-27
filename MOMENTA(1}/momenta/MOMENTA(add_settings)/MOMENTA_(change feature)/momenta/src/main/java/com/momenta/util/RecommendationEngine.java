package com.momenta.util;

import com.momenta.model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Upgrades the old single "What Should I Do Now?" pick into three tiers:
 * the one thing that's most urgent, the next most important thing after
 * that, and a small task that can be knocked out quickly. Doesn't override
 * the user's own priority — only ranks among it, and always says why a
 * task was picked.
 */
public class RecommendationEngine {

    public static class Recommendation {
        public final Task task;
        public final String reason;
        public Recommendation(Task task, String reason) { this.task = task; this.reason = reason; }
    }

    public static class Triage {
        public Optional<Recommendation> doNow = Optional.empty();
        public Optional<Recommendation> doNext = Optional.empty();
        public Optional<Recommendation> quickWin = Optional.empty();
    }

    public static Triage build(List<Task> allTasks) {
        Triage triage = new Triage();
        String today = LocalDate.now().toString();

        List<Task> open = new ArrayList<>();
        for (Task t : allTasks) if (!"DONE".equals(t.getStatus())) open.add(t);
        if (open.isEmpty()) return triage;

        // Do Now: due today or overdue, highest priority, least progress first.
        List<Task> urgent = open.stream()
                .filter(t -> t.getDeadline() != null && !t.getDeadline().isEmpty() && t.getDeadline().compareTo(today) <= 0)
                .sorted(Comparator.comparingInt(Task::getPriority).reversed()
                        .thenComparingInt(Task::getProgress))
                .toList();
        Task doNowTask = urgent.isEmpty() ? null : urgent.get(0);
        if (doNowTask != null) {
            boolean overdue = doNowTask.getDeadline().compareTo(today) < 0;
            triage.doNow = Optional.of(new Recommendation(doNowTask,
                    "priority " + doNowTask.getPriority() + "/5 and " +
                            (overdue ? "overdue since " + doNowTask.getDeadline() : "due today") +
                            ", " + doNowTask.getProgress() + "% done"));
        }

        // Do Next: important but not urgent — highest remaining priority, excluding whatever was picked above.
        List<Task> next = open.stream()
                .filter(t -> doNowTask == null || t.getId() != doNowTask.getId())
                .filter(t -> t.getPriority() >= 3)
                .sorted(Comparator.comparingInt(Task::getPriority).reversed()
                        .thenComparing(t -> t.getDeadline() == null ? "" : t.getDeadline()))
                .toList();
        Task doNextTask = next.isEmpty() ? null : next.get(0);
        if (doNextTask != null) {
            String deadlinePart = (doNextTask.getDeadline() == null || doNextTask.getDeadline().isEmpty())
                    ? "no deadline set" : "due " + doNextTask.getDeadline();
            triage.doNext = Optional.of(new Recommendation(doNextTask,
                    "priority " + doNextTask.getPriority() + "/5, " + deadlinePart));
        }

        // Quick Win: already well underway (high progress), so it's a short push to finish.
        final int excludeDoNow = doNowTask == null ? -1 : doNowTask.getId();
        final int excludeDoNext = doNextTask == null ? -1 : doNextTask.getId();
        List<Task> quick = open.stream()
                .filter(t -> t.getId() != excludeDoNow && t.getId() != excludeDoNext)
                .sorted(Comparator.comparingInt(Task::getProgress).reversed())
                .toList();
        Task quickTask = quick.isEmpty() ? null : quick.get(0);
        if (quickTask != null && quickTask.getProgress() > 0) {
            int estimateMinutes = Math.max(5, (100 - quickTask.getProgress()) / 2);
            triage.quickWin = Optional.of(new Recommendation(quickTask,
                    "already " + quickTask.getProgress() + "% done — roughly ~" + estimateMinutes + " min estimated to finish"));
        }

        return triage;
    }
}
