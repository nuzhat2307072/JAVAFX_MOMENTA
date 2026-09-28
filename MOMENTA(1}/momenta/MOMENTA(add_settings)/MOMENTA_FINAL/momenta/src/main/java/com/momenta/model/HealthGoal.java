package com.momenta.model;

import javafx.beans.property.*;

/** A long-term health/wellness goal (e.g. "Run a 5K"), tracked separately from work Goals. */
public class HealthGoal {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty target = new SimpleStringProperty();
    private final IntegerProperty progress = new SimpleIntegerProperty();

    public HealthGoal() {}

    public HealthGoal(int id, int userId, String title, String target, int progress) {
        setId(id); setUserId(userId); setTitle(title); setTarget(target); setProgress(progress);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public String getTarget() { return target.get(); }
    public void setTarget(String v) { target.set(v); }
    public StringProperty targetProperty() { return target; }

    public int getProgress() { return progress.get(); }
    public void setProgress(int v) { progress.set(v); }
    public IntegerProperty progressProperty() { return progress; }
}
