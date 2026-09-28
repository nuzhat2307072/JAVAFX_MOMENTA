package com.momenta.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Goal {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final IntegerProperty progress = new SimpleIntegerProperty();
    private final ObservableList<Milestone> milestones = FXCollections.observableArrayList();

    public Goal() {}

    public Goal(int id, int userId, String title, int progress) {
        setId(id); setUserId(userId); setTitle(title); setProgress(progress);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public int getProgress() { return progress.get(); }
    public void setProgress(int v) { progress.set(v); }
    public IntegerProperty progressProperty() { return progress; }

    public ObservableList<Milestone> getMilestones() { return milestones; }
}
