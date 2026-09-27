package com.momenta.model;

public class FocusSession {
    private int id;
    private int userId;
    private String date;      // yyyy-MM-dd
    private int durationMinutes;

    public FocusSession() {}

    public FocusSession(int id, int userId, String date, int durationMinutes) {
        this.id = id; this.userId = userId; this.date = date; this.durationMinutes = durationMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}
