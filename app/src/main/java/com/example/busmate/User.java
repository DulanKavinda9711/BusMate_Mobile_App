package com.example.busmate;

public class User {
    private String username;
    private String name;
    private String email;
    private String userType;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String username, String name, String email, String userType) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}