package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.FocusSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FocusSessionDAO {

    public void insert(FocusSession session) throws SQLException {
        String sql = "INSERT INTO focus_sessions(user_id, date, duration_minutes) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, session.getUserId());
            ps.setString(2, session.getDate());
            ps.setInt(3, session.getDurationMinutes());
            ps.executeUpdate();
        }
    }

    /** Total focus minutes per date for the last 7 distinct dates recorded (used by Analytics chart). */
    public Map<String, Integer> getMinutesByDateLast7(int userId) throws SQLException {
        String sql = "SELECT date, SUM(duration_minutes) AS total FROM focus_sessions " +
                "WHERE user_id = ? GROUP BY date ORDER BY date DESC LIMIT 7";
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("date"), rs.getInt("total"));
                }
            }
        }
        return result;
    }

    /** Every distinct date with at least one focus session, most recent first — used to compute the focus streak. */
    public List<String> getDistinctDatesDesc(int userId) throws SQLException {
        String sql = "SELECT DISTINCT date FROM focus_sessions WHERE user_id = ? ORDER BY date DESC";
        List<String> dates = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(rs.getString("date"));
            }
        }
        return dates;
    }

    /** Total focus minutes ever logged for this user — used for the "Hours Focused" achievement. */
    public int getTotalMinutes(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(duration_minutes), 0) AS total FROM focus_sessions WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    /** Total focus minutes logged today — used for the Dashboard's "Today's Focus" figure. */
    public int getMinutesToday(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(duration_minutes), 0) AS total FROM focus_sessions WHERE user_id = ? AND date = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, java.time.LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }
}
