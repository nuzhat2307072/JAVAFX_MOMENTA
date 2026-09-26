package com.momenta.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String occupation;
    private String theme = "LIGHT";              // LIGHT / DARK / AUTO
    private int defaultFocusMinutes = 25;
    private String accentColor = "BLUE";          // BLUE / TEAL / PURPLE / ORANGE
    private String vaultPinHash;                  // null until the user sets a Private Vault PIN

    public User() {}

    public User(int id, String username, String passwordHash, String occupation) {
        this(id, username, passwordHash, occupation, "LIGHT", 25);
    }

    public User(int id, String username, String passwordHash, String occupation,
                String theme, int defaultFocusMinutes) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.occupation = occupation;
        this.theme = theme == null ? "LIGHT" : theme;
        this.defaultFocusMinutes = defaultFocusMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public int getDefaultFocusMinutes() { return defaultFocusMinutes; }
    public void setDefaultFocusMinutes(int defaultFocusMinutes) { this.defaultFocusMinutes = defaultFocusMinutes; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public String getVaultPinHash() { return vaultPinHash; }
    public void setVaultPinHash(String vaultPinHash) { this.vaultPinHash = vaultPinHash; }
}
