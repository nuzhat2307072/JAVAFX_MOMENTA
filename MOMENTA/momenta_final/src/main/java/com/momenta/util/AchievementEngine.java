package com.momenta.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Purely computed from existing counts — no separate table. Thresholds are
 * intentionally simple; add more as the app grows.
 */
public class AchievementEngine {

    public static List<String> unlocked(int tasksCompleted, int focusStreakDays, int totalFocusMinutes, int goalsCompleted) {
        List<String> badges = new ArrayList<>();
        if (tasksCompleted >= 1) badges.add("🏆 First Task Completed");
        if (tasksCompleted >= 10) badges.add("🔟 10 Tasks Completed");
        if (tasksCompleted >= 100) badges.add("💯 100 Tasks Completed");
        if (focusStreakDays >= 7) badges.add("🔥 7-Day Focus Streak");
        if (totalFocusMinutes >= 600) badges.add("⏱️ 10 Hours Focused");
        if (goalsCompleted >= 1) badges.add("🎯 First Goal Completed");
        return badges;
    }
}
