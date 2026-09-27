package com.momenta.model;

/** A quick "Brain Dump" note — jot something down now, organize it later. */
public class Note {
    private int id;
    private int userId;
    private String content;
    private String createdAt;

    public Note() {}

    public Note(int id, int userId, String content, String createdAt) {
        this.id = id; this.userId = userId; this.content = content; this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
