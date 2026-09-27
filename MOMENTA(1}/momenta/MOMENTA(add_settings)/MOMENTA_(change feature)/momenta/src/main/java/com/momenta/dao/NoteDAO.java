package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public Note insert(Note note) throws SQLException {
        String sql = "INSERT INTO notes(user_id, content, created_at) VALUES (?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, note.getUserId());
            ps.setString(2, note.getContent());
            ps.setString(3, note.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) note.setId(keys.getInt(1));
            }
        }
        return note;
    }

    /** Trash / Recycle Bin: moves the note to Trash instead of deleting it outright. */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE notes SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void permanentlyDelete(int id) throws SQLException {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void restore(int id) throws SQLException {
        String sql = "UPDATE notes SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Note> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM notes WHERE user_id = ? AND deleted_at IS NULL ORDER BY created_at DESC";
        List<Note> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Note(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("content"), rs.getString("created_at")));
                }
            }
        }
        return result;
    }
}
