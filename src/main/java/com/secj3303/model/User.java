package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;

public class User implements Serializable {
    private String email;
    private String name;
    private String role;

    // Additional profile fields
    private String phone;
    private String location;
    private LocalDate dateOfBirth;
    private String emergencyContact;
    private String bio;

    // Notification preferences
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean weeklyReport;
    private boolean anonymousMode;

    // Default constructor
    public User() {}

    // Constructor with essential fields
    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Full constructor
    public User(String email, String name, String role, String phone, String location,
                LocalDate dateOfBirth, String emergencyContact, String bio,
                boolean emailNotifications, boolean pushNotifications, boolean weeklyReport, boolean anonymousMode) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.location = location;
        this.dateOfBirth = dateOfBirth;
        this.emergencyContact = emergencyContact;
        this.bio = bio;
        this.emailNotifications = emailNotifications;
        this.pushNotifications = pushNotifications;
        this.weeklyReport = weeklyReport;
        this.anonymousMode = anonymousMode;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isPushNotifications() { return pushNotifications; }
    public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }

    public boolean isWeeklyReport() { return weeklyReport; }
    public void setWeeklyReport(boolean weeklyReport) { this.weeklyReport = weeklyReport; }

    public boolean isAnonymousMode() { return anonymousMode; }
    public void setAnonymousMode(boolean anonymousMode) { this.anonymousMode = anonymousMode; }

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
