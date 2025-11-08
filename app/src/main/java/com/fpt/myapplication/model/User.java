package com.fpt.myapplication.model;



public class User {
    private int id;
    private String email;
    private String displayName;
    private String avatar;

    // Constructors
    public User() {}

    public User(int id, String email, String displayName, String avatar) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
