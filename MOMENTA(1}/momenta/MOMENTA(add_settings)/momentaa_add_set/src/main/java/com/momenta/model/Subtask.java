package com.momenta.model;

import javafx.beans.property.*;

public class Subtask {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty taskId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty();

    public Subtask() {}

    public Subtask(int id, int taskId, String title, boolean done) {
        setId(id); setTaskId(taskId); setTitle(title); setDone(done);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getTaskId() { return taskId.get(); }
    public void setTaskId(int v) { taskId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean v) { done.set(v); }
    public BooleanProperty doneProperty() { return done; }
}
