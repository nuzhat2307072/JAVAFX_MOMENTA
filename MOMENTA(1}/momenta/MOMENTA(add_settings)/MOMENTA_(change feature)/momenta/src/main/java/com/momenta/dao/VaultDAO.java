package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.VaultEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaultDAO {

    public VaultEntry insert(VaultEntry entry) throws SQLException {
        String sql = "INSERT INTO vault_entries(user_id, title, content) VALUES (?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getUserId());
            ps.setString(2, entry.getTitle());
            ps.setString(3, entry.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setId(keys.getInt(1));
            }
        }
        return entry;
    }

    public void update(VaultEntry entry) throws SQLException {
        String sql = "UPDATE vault_entries SET title = ?, content = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            ps.setInt(3, entry.getId());
            ps.executeUpdate();
        }
    }

    /** Trash / Recycle Bin: moves the entry to Trash instead of deleting it outright. */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE vault_entries SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void permanentlyDelete(int id) throws SQLException {
        String sql = "DELETE FROM vault_entries WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void restore(int id) throws SQLException {
        String sql = "UPDATE vault_entries SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<VaultEntry> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM vault_entries WHERE user_id = ? AND deleted_at IS NULL ORDER BY title ASC";
        List<VaultEntry> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new VaultEntry(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("title"), rs.getString("content")));
                }
            }
        }
        return result;
    }
}
