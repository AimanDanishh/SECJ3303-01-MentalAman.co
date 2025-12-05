package com.secj3303.model;

import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private String name;
    private String role;
    
    // Default constructor (REQUIRED for Spring)
    public User() {}
    
    // Optional constructor
    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }
    
    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    // Helper methods for Thymeleaf
    public boolean isStudent() { return "student".equals(role); }
    public boolean isFaculty() { return "faculty".equals(role); }
    public boolean isCounsellor() { return "counsellor".equals(role); }
    public boolean isAdministrator() { return "administrator".equals(role); }
    
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}