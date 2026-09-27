package com.momenta.util;

import java.time.LocalDate;
import java.util.List;

public class StreakUtil {

    /**
     * Counts consecutive days ending today (or yesterday, so the streak
     * doesn't reset to 0 first thing in the morning before today's first
     * session is logged) from a descending list of ISO date strings.
     */
    public static int computeStreak(List<String> datesDesc) {
        if (datesDesc == null || datesDesc.isEmpty()) return 0;

        LocalDate cursor = LocalDate.now();
        // Allow the streak to still show as "alive" if today has no entry yet.
        if (!datesDesc.contains(cursor.toString())) {
            cursor = cursor.minusDays(1);
        }

        int streak = 0;
        for (String dateStr : datesDesc) {
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                continue;
            }
            if (date.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (date.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }
}
