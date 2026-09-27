package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Reminder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {

    public Reminder insert(Reminder r) throws SQLException {
        String sql = "INSERT INTO reminders(user_id, title, note, date, time) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getTitle());
            ps.setString(3, r.getNote());
            ps.setString(4, r.getDate());
            ps.setString(5, r.getTime());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
        }
        return r;
    }

    /** Trash / Recycle Bin: moves the reminder to Trash instead of deleting it outright. */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE reminders SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void permanentlyDelete(int id) throws SQLException {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void restore(int id) throws SQLException {
        String sql = "UPDATE reminders SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Reminder> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND deleted_at IS NULL ORDER BY date ASC, time ASC";
        List<Reminder> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<Reminder> getByDate(int userId, String date) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND date = ? AND deleted_at IS NULL ORDER BY time ASC";
        List<Reminder> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    private Reminder map(ResultSet rs) throws SQLException {
        return new Reminder(
                rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"),
                rs.getString("note"), rs.getString("date"), rs.getString("time"));
    }
}
