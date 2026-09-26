package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.FinanceEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinanceDAO {

    public FinanceEntry insert(FinanceEntry entry) throws SQLException {
        String sql = "INSERT INTO finance_entries(user_id, type, category, amount, date) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getUserId());
            ps.setString(2, entry.getType());
            ps.setString(3, entry.getCategory());
            ps.setDouble(4, entry.getAmount());
            ps.setString(5, entry.getDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setId(keys.getInt(1));
            }
        }
        return entry;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM finance_entries WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<FinanceEntry> getAllByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM finance_entries WHERE user_id = ? ORDER BY date DESC";
        List<FinanceEntry> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new FinanceEntry(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("type"), rs.getString("category"),
                            rs.getDouble("amount"), rs.getString("date")));
                }
            }
        }
        return result;
    }
}
