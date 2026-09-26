package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.Goal;
import com.momenta.model.Milestone;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public Goal insertGoal(Goal g) throws SQLException {
        String sql = "INSERT INTO goals(user_id, title, progress) VALUES (?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getUserId());
            ps.setString(2, g.getTitle());
            ps.setInt(3, g.getProgress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) g.setId(keys.getInt(1));
            }
        }
        return g;
    }

    public void updateGoalProgress(int goalId, int progress) throws SQLException {
        String sql = "UPDATE goals SET progress = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, progress);
            ps.setInt(2, goalId);
            ps.executeUpdate();
        }
    }

    public void deleteGoal(int goalId) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, goalId);
            ps.executeUpdate();
        }
    }

    public Milestone insertMilestone(Milestone m) throws SQLException {
        String sql = "INSERT INTO milestones(goal_id, title, done) VALUES (?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getGoalId());
            ps.setString(2, m.getTitle());
            ps.setInt(3, m.isDone() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
        }
        return m;
    }

    public void toggleMilestone(int milestoneId, boolean done) throws SQLException {
        String sql = "UPDATE milestones SET done = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, done ? 1 : 0);
            ps.setInt(2, milestoneId);
            ps.executeUpdate();
        }
    }

    public List<Goal> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ?";
        List<Goal> goals = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goal g = new Goal(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("title"), rs.getInt("progress"));
                    g.getMilestones().addAll(getMilestones(g.getId()));
                    goals.add(g);
                }
            }
        }
        return goals;
    }

    public List<Milestone> getMilestones(int goalId) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE goal_id = ?";
        List<Milestone> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Milestone(rs.getInt("id"), rs.getInt("goal_id"),
                            rs.getString("title"), rs.getInt("done") == 1));
                }
            }
        }
        return result;
    }
}
