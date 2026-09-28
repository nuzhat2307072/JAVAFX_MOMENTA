package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.LifeEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LifeEntryDAO {

    public LifeEntry insert(LifeEntry e) throws SQLException {
        String sql = "INSERT INTO life_entries(user_id, module, title, subtitle, entry_date, progress, done, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getUserId());
            ps.setString(2, e.getModule());
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getSubtitle());
            ps.setString(5, e.getEntryDate());
            ps.setInt(6, e.getProgress());
            ps.setInt(7, e.isDone() ? 1 : 0);
            ps.setString(8, e.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        }
        return e;
    }

    /** Persists every editable field of an already-existing entry (id must be set). */
    public void update(LifeEntry e) throws SQLException {
        String sql = "UPDATE life_entries SET title = ?, subtitle = ?, entry_date = ?, " +
                "progress = ?, done = ?, notes = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getSubtitle());
            ps.setString(3, e.getEntryDate());
            ps.setInt(4, e.getProgress());
            ps.setInt(5, e.isDone() ? 1 : 0);
            ps.setString(6, e.getNotes());
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    public void updateProgress(int id, int progress) throws SQLException {
        String sql = "UPDATE life_entries SET progress = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, progress);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void toggleDone(int id, boolean done) throws SQLException {
        String sql = "UPDATE life_entries SET done = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, done ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Trash / Recycle Bin: moves the entry to Trash instead of deleting it outright. */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE life_entries SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void restore(int id) throws SQLException {
        String sql = "UPDATE life_entries SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void permanentlyDelete(int id) throws SQLException {
        String sql = "DELETE FROM life_entries WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<LifeEntry> getAllByUserAndModule(int userId, String module) throws SQLException {
        String sql = "SELECT * FROM life_entries WHERE user_id = ? AND module = ? AND deleted_at IS NULL ORDER BY id";
        List<LifeEntry> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, module);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    /** All modules for a user in one query — used by Search, which needs every module at once. */
    public List<LifeEntry> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM life_entries WHERE user_id = ? AND deleted_at IS NULL ORDER BY id";
        List<LifeEntry> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    private LifeEntry map(ResultSet rs) throws SQLException {
        return new LifeEntry(rs.getInt("id"), rs.getInt("user_id"), rs.getString("module"),
                rs.getString("title"), rs.getString("subtitle"), rs.getString("entry_date"),
                rs.getInt("progress"), rs.getInt("done") == 1, rs.getString("notes"));
    }
}
