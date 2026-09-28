package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Habit;
import com.momenta.util.StreakUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {

    public Habit insertHabit(Habit h) throws SQLException {
        String sql = "INSERT INTO habits(user_id, title) VALUES (?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getUserId());
            ps.setString(2, h.getTitle());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) h.setId(keys.getInt(1));
            }
        }
        return h;
    }

    /** Trash / Recycle Bin: moves the habit to Trash instead of deleting it outright. */
    public void deleteHabit(int habitId) throws SQLException {
        String sql = "UPDATE habits SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, habitId);
            ps.executeUpdate();
        }
    }

    public void restoreHabit(int habitId) throws SQLException {
        String sql = "UPDATE habits SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.executeUpdate();
        }
    }

    public void permanentlyDeleteHabit(int habitId) throws SQLException {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.executeUpdate();
        }
    }

    /** Marks (or unmarks) a habit as done for today by inserting/removing its habit_logs row. */
    public void setDoneToday(int habitId, boolean done) throws SQLException {
        String today = LocalDate.now().toString();
        if (done) {
            String sql = "INSERT OR IGNORE INTO habit_logs(habit_id, date, done) VALUES (?, ?, 1)";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, habitId);
                ps.setString(2, today);
                ps.executeUpdate();
            }
        } else {
            String sql = "DELETE FROM habit_logs WHERE habit_id = ? AND date = ?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, habitId);
                ps.setString(2, today);
                ps.executeUpdate();
            }
        }
    }

    /** Every logged date (newest first) a habit was marked done — feeds StreakUtil. */
    private List<String> getLogDates(int habitId) throws SQLException {
        String sql = "SELECT date FROM habit_logs WHERE habit_id = ? ORDER BY date DESC";
        List<String> dates = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, habitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(rs.getString("date"));
            }
        }
        return dates;
    }

    public List<Habit> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM habits WHERE user_id = ? AND deleted_at IS NULL ORDER BY id";
        String today = LocalDate.now().toString();
        List<Habit> habits = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Habit h = new Habit(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"));
                    List<String> dates = getLogDates(h.getId());
                    h.setDoneToday(dates.contains(today));
                    h.setStreak(StreakUtil.computeStreak(dates));
                    habits.add(h);
                }
            }
        }
        return habits;
    }
}
