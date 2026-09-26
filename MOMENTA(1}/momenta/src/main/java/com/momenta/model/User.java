package com.momenta.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String occupation;

    public User() {}

    public User(int id, String username, String passwordHash, String occupation) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.occupation = occupation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
}
