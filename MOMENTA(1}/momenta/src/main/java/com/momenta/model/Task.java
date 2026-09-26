package com.momenta.model;

import javafx.beans.property.*;

public class Task {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty deadline = new SimpleStringProperty();   // yyyy-MM-dd
    private final IntegerProperty priority = new SimpleIntegerProperty(); // 1-5
    private final IntegerProperty progress = new SimpleIntegerProperty(); // 0-100
    private final StringProperty status = new SimpleStringProperty();    // PENDING / IN_PROGRESS / DONE

    public Task() {}

    public Task(int id, int userId, String title, String description, String deadline,
                int priority, int progress, String status) {
        setId(id); setUserId(userId); setTitle(title); setDescription(description);
        setDeadline(deadline); setPriority(priority); setProgress(progress); setStatus(status);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }
    public StringProperty descriptionProperty() { return description; }

    public String getDeadline() { return deadline.get(); }
    public void setDeadline(String v) { deadline.set(v); }
    public StringProperty deadlineProperty() { return deadline; }

    public int getPriority() { return priority.get(); }
    public void setPriority(int v) { priority.set(v); }
    public IntegerProperty priorityProperty() { return priority; }

    public int getProgress() { return progress.get(); }
    public void setProgress(int v) { progress.set(v); }
    public IntegerProperty progressProperty() { return progress; }

    public String getStatus() { return status.get(); }
    public void setStatus(String v) { status.set(v); }
    public StringProperty statusProperty() { return status; }
}
