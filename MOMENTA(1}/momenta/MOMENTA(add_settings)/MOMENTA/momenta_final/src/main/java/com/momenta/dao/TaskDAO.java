package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public Task insert(Task task) throws SQLException {
        String sql = "INSERT INTO tasks(user_id, title, description, deadline, priority, progress, status, category, recurrence, completed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getDeadline());
            ps.setInt(5, task.getPriority());
            ps.setInt(6, task.getProgress());
            ps.setString(7, task.getStatus());
            ps.setString(8, task.getCategory());
            ps.setString(9, task.getRecurrence());
            ps.setString(10, "DONE".equals(task.getStatus()) ? LocalDateTime.now().toString() : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) task.setId(keys.getInt(1));
            }
        }
        return task;
    }

    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title=?, description=?, deadline=?, priority=?, progress=?, status=?, " +
                "category=?, recurrence=?, completed_at=? WHERE id=?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getDeadline());
            ps.setInt(4, task.getPriority());
            ps.setInt(5, task.getProgress());
            ps.setString(6, task.getStatus());
            ps.setString(7, task.getCategory());
            ps.setString(8, task.getRecurrence());
            ps.setString(9, "DONE".equals(task.getStatus())
                    ? (task.getCompletedAt() == null || task.getCompletedAt().isEmpty()
                        ? LocalDateTime.now().toString() : task.getCompletedAt())
                    : null);
            ps.setInt(10, task.getId());
            ps.executeUpdate();
        }
    }

    /** Lightweight status-only update, used by the Kanban board on drag-and-drop. */
    public void updateStatus(int taskId, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ?, completed_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, "DONE".equals(status) ? LocalDateTime.now().toString() : null);
            ps.setInt(3, taskId);
            ps.executeUpdate();
        }
    }

    public void delete(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    public List<Task> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY priority DESC, deadline ASC";
        Connection conn = DatabaseManager.getConnection();
        List<Task> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("deadline"),
                rs.getInt("priority"),
                rs.getInt("progress"),
                rs.getString("status"),
                rs.getString("category") == null ? "" : rs.getString("category"),
                rs.getString("recurrence") == null ? "NONE" : rs.getString("recurrence")
        );
        t.setCompletedAt(rs.getString("completed_at"));
        return t;
    }
}
