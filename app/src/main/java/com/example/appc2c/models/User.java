package com.example.appc2c.models;

public class User {
    private String id, name, email, avatar, role;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }
}
