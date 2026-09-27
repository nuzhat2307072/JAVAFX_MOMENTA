package com.momenta.model;

import javafx.beans.property.*;

public class Reminder {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty note = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty(); // yyyy-MM-dd
    private final StringProperty time = new SimpleStringProperty(); // HH:mm

    public Reminder() {}

    public Reminder(int id, int userId, String title, String note, String date, String time) {
        setId(id); setUserId(userId); setTitle(title); setNote(note); setDate(date); setTime(time);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public String getNote() { return note.get(); }
    public void setNote(String v) { note.set(v); }
    public StringProperty noteProperty() { return note; }

    public String getDate() { return date.get(); }
    public void setDate(String v) { date.set(v); }
    public StringProperty dateProperty() { return date; }

    public String getTime() { return time.get(); }
    public void setTime(String v) { time.set(v); }
    public StringProperty timeProperty() { return time; }
}
