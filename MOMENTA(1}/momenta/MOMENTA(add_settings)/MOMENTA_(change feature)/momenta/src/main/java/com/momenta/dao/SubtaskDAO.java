package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Subtask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubtaskDAO {

    public Subtask insert(Subtask subtask) throws SQLException {
        String sql = "INSERT INTO subtasks(task_id, title, done) VALUES (?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, subtask.getTaskId());
            ps.setString(2, subtask.getTitle());
            ps.setInt(3, subtask.isDone() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) subtask.setId(keys.getInt(1));
            }
        }
        return subtask;
    }

    public void toggleDone(int subtaskId, boolean done) throws SQLException {
        String sql = "UPDATE subtasks SET done = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, done ? 1 : 0);
            ps.setInt(2, subtaskId);
            ps.executeUpdate();
        }
    }

    public void delete(int subtaskId) throws SQLException {
        String sql = "DELETE FROM subtasks WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, subtaskId);
            ps.executeUpdate();
        }
    }

    public List<Subtask> getAllByTask(int taskId) throws SQLException {
        String sql = "SELECT * FROM subtasks WHERE task_id = ? ORDER BY id ASC";
        List<Subtask> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Subtask(rs.getInt("id"), rs.getInt("task_id"),
                            rs.getString("title"), rs.getInt("done") == 1));
                }
            }
        }
        return result;
    }

    /** Count of (total, done) subtasks for a task — used for a quick "2/5" style summary. */
    public int[] countTotalAndDone(int taskId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, SUM(done) AS done FROM subtasks WHERE task_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("total"), rs.getInt("done")};
                }
                return new int[]{0, 0};
            }
        }
    }
}
