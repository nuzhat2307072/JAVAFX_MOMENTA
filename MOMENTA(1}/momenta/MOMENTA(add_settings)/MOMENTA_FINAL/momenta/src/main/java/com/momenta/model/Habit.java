package com.momenta.model;

import javafx.beans.property.*;

/** A recurring habit the user tracks daily (e.g. "Stretch", "No sugar"). */
public class Habit {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty doneToday = new SimpleBooleanProperty();
    private final IntegerProperty streak = new SimpleIntegerProperty();

    public Habit() {}

    public Habit(int id, int userId, String title) {
        setId(id); setUserId(userId); setTitle(title);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    /** Not persisted directly — computed each load from habit_logs for today's date. */
    public boolean isDoneToday() { return doneToday.get(); }
    public void setDoneToday(boolean v) { doneToday.set(v); }

    /** Not persisted directly — computed each load via StreakUtil from habit_logs dates. */
    public int getStreak() { return streak.get(); }
    public void setStreak(int v) { streak.set(v); }
}
