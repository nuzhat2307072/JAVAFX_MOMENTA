package com.momenta.model;

/** One soft-deleted row, from any table, shown together in the Trash screen. */
public class TrashItem {
    public enum Type { TASK, REMINDER, GOAL, NOTE, FINANCE, VAULT }

    private final Type type;
    private final int id;
    private final String label;
    private final String deletedAt;

    public TrashItem(Type type, int id, String label, String deletedAt) {
        this.type = type;
        this.id = id;
        this.label = label;
        this.deletedAt = deletedAt;
    }

    public Type getType() { return type; }
    public int getId() { return id; }
    public String getLabel() { return label; }
    public String getDeletedAt() { return deletedAt; }

    public String typeIcon() {
        return switch (type) {
            case TASK -> "📋";
            case REMINDER -> "📅";
            case GOAL -> "🎯";
            case NOTE -> "🧠";
            case FINANCE -> "💰";
            case VAULT -> "🔐";
        };
    }

    public String typeLabel() {
        return switch (type) {
            case TASK -> "Task";
            case REMINDER -> "Reminder";
            case GOAL -> "Goal";
            case NOTE -> "Note";
            case FINANCE -> "Finance Entry";
            case VAULT -> "Vault Entry";
        };
    }
}
