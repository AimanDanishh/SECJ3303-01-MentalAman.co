package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // ================= AUTH =================
    @Id
    @Column(length = 100, nullable = false)
    private String email;   // used as username

    @Column(nullable = false)
    private String password;

    private boolean enabled = true;

    @Column(nullable = false)
    private String role; // STUDENT, FACULTY, COUNSELLOR, ADMINISTRATOR

    // ================= PROFILE =================
    private String name;
    private String phone;
    private String location;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    private String emergencyContact;

    @Column(length = 500)
    private String bio;

    // ================= PREFERENCES =================
    private boolean emailNotifications = true;
    private boolean pushNotifications = true;
    private boolean weeklyReport = true;
    private boolean anonymousMode = false;

    // ================= CONSTRUCTORS =================
    public User() {}

    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.enabled = true;
    }

    // ================= GETTERS / SETTERS =================
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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
    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isPushNotifications() { return pushNotifications; }
    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public boolean isWeeklyReport() { return weeklyReport; }
    public void setWeeklyReport(boolean weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public boolean isAnonymousMode() { return anonymousMode; }
    public void setAnonymousMode(boolean anonymousMode) {
        this.anonymousMode = anonymousMode;
    }

    // ================= HELPERS =================
    public boolean isStudent() { return "STUDENT".equals(role); }
    public boolean isFaculty() { return "FACULTY".equals(role); }
    public boolean isCounsellor() { return "COUNSELLOR".equals(role); }
    public boolean isAdministrator() { return "ADMINISTRATOR".equals(role); }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
