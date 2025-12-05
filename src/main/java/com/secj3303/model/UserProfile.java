package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;

public class UserProfile implements Serializable {
    // Basic fields from React formData
    private String fullName;
    private String phone;
    private String location;
    private String bio;
    private LocalDate dateOfBirth; // Use LocalDate for proper date handling
    private String emergencyContact;
    
    // Activity Stats (Simulated)
    private int modulesCompleted = 12;
    private int forumPosts = 24;
    private int daysActive = 45;
    private int pointsEarned = 850;

    // Preferences fields from React preferences
    private boolean emailNotifications = true;
    private boolean pushNotifications = true;
    private boolean weeklyReport = true;
    private boolean anonymousMode = false;

    // Default Constructor (required for Spring form binding)
    public UserProfile() {}

    // Constructor for initial load (simulated data)
    public UserProfile(String fullName, String email) {
        this.fullName = fullName;
        this.phone = "+1 (555) 123-4567";
        this.location = "San Francisco, CA";
        this.bio = "Passionate about mental health and wellness. Always learning and growing.";
        this.dateOfBirth = LocalDate.of(1995, 6, 15);
        this.emergencyContact = "Jane Doe - +1 (555) 987-6543";
    }

    // --- Getters and Setters (Omitted for brevity, but necessary for Spring/Thymeleaf) ---

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public int getModulesCompleted() { return modulesCompleted; }
    public int getForumPosts() { return forumPosts; }
    public int getDaysActive() { return daysActive; }
    public int getPointsEarned() { return pointsEarned; }
    
    // Preferences
    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isPushNotifications() { return pushNotifications; }
    public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }

    public boolean isWeeklyReport() { return weeklyReport; }
    public void setWeeklyReport(boolean weeklyReport) { this.weeklyReport = weeklyReport; }

    public boolean isAnonymousMode() { return anonymousMode; }
    public void setAnonymousMode(boolean anonymousMode) { this.anonymousMode = anonymousMode; }
}