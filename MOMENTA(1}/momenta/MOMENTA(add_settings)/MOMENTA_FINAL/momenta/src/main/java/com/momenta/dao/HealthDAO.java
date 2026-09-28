package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.HealthLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes the one-row-per-user-per-day health_logs table. */
public class HealthDAO {

    /** Returns today's log for the user, creating an empty row first if none exists yet. */
    public HealthLog getOrCreateToday(int userId) throws SQLException {
        String today = LocalDate.now().toString();
        HealthLog existing = getByDate(userId, today);
        if (existing != null) return existing;

        String insert = "INSERT INTO health_logs(user_id, date, sleep_hours, water_glasses, " +
                "exercise_minutes, meals, mood, weight_kg, steps, notes) " +
                "VALUES (?, ?, 0, 0, 0, '', '', 0, 0, '')";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : 0;
                return new HealthLog(id, userId, today, 0, 0, 0, "", "", 0, 0, "");
            }
        }
    }

    public HealthLog getByDate(int userId, String date) throws SQLException {
        String sql = "SELECT * FROM health_logs WHERE user_id = ? AND date = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Persists every field of an already-existing log row (id must be set). */
    public void saveLog(HealthLog log) throws SQLException {
        String sql = "UPDATE health_logs SET sleep_hours = ?, water_glasses = ?, " +
                "exercise_minutes = ?, meals = ?, mood = ?, weight_kg = ?, steps = ?, notes = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, log.getSleepHours());
            ps.setInt(2, log.getWaterGlasses());
            ps.setInt(3, log.getExerciseMinutes());
            ps.setString(4, log.getMeals());
            ps.setString(5, log.getMood());
            ps.setDouble(6, log.getWeightKg());
            ps.setInt(7, log.getSteps());
            ps.setString(8, log.getNotes());
            ps.setInt(9, log.getId());
            ps.executeUpdate();
        }
    }

    /** Most recent logs for a user, newest first, capped at limit rows. */
    public List<HealthLog> getHistory(int userId, int limit) throws SQLException {
        String sql = "SELECT * FROM health_logs WHERE user_id = ? ORDER BY date DESC LIMIT ?";
        List<HealthLog> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    private HealthLog map(ResultSet rs) throws SQLException {
        return new HealthLog(
                rs.getInt("id"), rs.getInt("user_id"), rs.getString("date"),
                rs.getDouble("sleep_hours"), rs.getInt("water_glasses"),
                rs.getInt("exercise_minutes"), rs.getString("meals"), rs.getString("mood"),
                rs.getDouble("weight_kg"), rs.getInt("steps"), rs.getString("notes"));
    }
}
