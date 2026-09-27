package com.momenta.model;

import javafx.beans.property.*;

public class Milestone {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty goalId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty();

    public Milestone() {}

    public Milestone(int id, int goalId, String title, boolean done) {
        setId(id); setGoalId(goalId); setTitle(title); setDone(done);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getGoalId() { return goalId.get(); }
    public void setGoalId(int v) { goalId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean v) { done.set(v); }
    public BooleanProperty doneProperty() { return done; }
}
