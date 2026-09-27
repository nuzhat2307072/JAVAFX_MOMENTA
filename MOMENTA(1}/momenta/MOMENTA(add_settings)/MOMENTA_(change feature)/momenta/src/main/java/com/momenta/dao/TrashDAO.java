package com.momenta.dao;

import com.momenta.db.DatabaseManager;
import com.momenta.model.TrashItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads soft-deleted rows (deleted_at IS NOT NULL) from every trashable
 * table and presents them as one combined, newest-first list. Restore and
 * permanent-delete dispatch to the matching per-entity DAO by type.
 */
public class TrashDAO {

    public List<TrashItem> getAllTrash(int userId) throws SQLException {
        List<TrashItem> items = new ArrayList<>();
        items.addAll(queryTrash(userId, "tasks", "title", TrashItem.Type.TASK));
        items.addAll(queryTrash(userId, "reminders", "title", TrashItem.Type.REMINDER));
        items.addAll(queryTrash(userId, "goals", "title", TrashItem.Type.GOAL));
        items.addAll(queryTrash(userId, "notes", "content", TrashItem.Type.NOTE));
        items.addAll(queryTrash(userId, "finance_entries", "category", TrashItem.Type.FINANCE));
        items.addAll(queryTrash(userId, "vault_entries", "title", TrashItem.Type.VAULT));
        items.sort(Comparator.comparing(TrashItem::getDeletedAt).reversed());
        return items;
    }

    private List<TrashItem> queryTrash(int userId, String table, String labelColumn, TrashItem.Type type) throws SQLException {
        String sql = "SELECT id, " + labelColumn + " AS label, deleted_at FROM " + table +
                " WHERE user_id = ? AND deleted_at IS NOT NULL";
        List<TrashItem> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString("label");
                    if (label == null || label.isBlank()) label = "(untitled)";
                    if (label.length() > 80) label = label.substring(0, 80) + "…";
                    result.add(new TrashItem(type, rs.getInt("id"), label, rs.getString("deleted_at")));
                }
            }
        }
        return result;
    }

    public void restore(TrashItem item) throws SQLException {
        switch (item.getType()) {
            case TASK -> new TaskDAO().restore(item.getId());
            case REMINDER -> new ReminderDAO().restore(item.getId());
            case GOAL -> new GoalDAO().restoreGoal(item.getId());
            case NOTE -> new NoteDAO().restore(item.getId());
            case FINANCE -> new FinanceDAO().restore(item.getId());
            case VAULT -> new VaultDAO().restore(item.getId());
        }
    }

    public void permanentlyDelete(TrashItem item) throws SQLException {
        switch (item.getType()) {
            case TASK -> new TaskDAO().permanentlyDelete(item.getId());
            case REMINDER -> new ReminderDAO().permanentlyDelete(item.getId());
            case GOAL -> new GoalDAO().permanentlyDeleteGoal(item.getId());
            case NOTE -> new NoteDAO().permanentlyDelete(item.getId());
            case FINANCE -> new FinanceDAO().permanentlyDelete(item.getId());
            case VAULT -> new VaultDAO().permanentlyDelete(item.getId());
        }
    }
}
