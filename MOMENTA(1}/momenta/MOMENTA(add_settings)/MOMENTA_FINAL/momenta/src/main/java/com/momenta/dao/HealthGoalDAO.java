package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.HealthGoal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthGoalDAO {

    public HealthGoal insertGoal(HealthGoal g) throws SQLException {
        String sql = "INSERT INTO health_goals(user_id, title, target, progress) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getUserId());
            ps.setString(2, g.getTitle());
            ps.setString(3, g.getTarget());
            ps.setInt(4, g.getProgress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) g.setId(keys.getInt(1));
            }
        }
        return g;
    }

    public void updateProgress(int goalId, int progress) throws SQLException {
        String sql = "UPDATE health_goals SET progress = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, progress);
            ps.setInt(2, goalId);
            ps.executeUpdate();
        }
    }

    /** Trash / Recycle Bin: moves the goal to Trash instead of deleting it outright. */
    public void deleteGoal(int goalId) throws SQLException {
        String sql = "UPDATE health_goals SET deleted_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().toString());
            ps.setInt(2, goalId);
            ps.executeUpdate();
        }
    }

    public void permanentlyDeleteGoal(int goalId) throws SQLException {
        String sql = "DELETE FROM health_goals WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, goalId);
            ps.executeUpdate();
        }
    }

    public void restoreGoal(int goalId) throws SQLException {
        String sql = "UPDATE health_goals SET deleted_at = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, goalId);
            ps.executeUpdate();
        }
    }

    public List<HealthGoal> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM health_goals WHERE user_id = ? AND deleted_at IS NULL ORDER BY id";
        List<HealthGoal> goals = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    goals.add(new HealthGoal(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("title"), rs.getString("target"), rs.getInt("progress")));
                }
            }
        }
        return goals;
    }
}
