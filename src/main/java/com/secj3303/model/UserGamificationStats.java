package com.secj3303.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "user_gamification_stats")
public class UserGamificationStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;
    
    @Column(name = "total_points")
    private int totalPoints = 0;
    
    @Column(name = "current_level")
    private int currentLevel = 1;
    
    @Column(name = "day_streak")
    private int dayStreak = 0;
    
    @Column(name = "total_modules_completed")
    private int totalModulesCompleted = 0;
    
    @Column(name = "total_quizzes_passed")
    private int totalQuizzesPassed = 0;
    
    @Column(name = "earned_badges", length = 1000)
    private String earnedBadges = "";
    
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;
    
    @Column(name = "last_level_up_date")
    private LocalDateTime lastLevelUpDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastActivityDate == null) {
            lastActivityDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    
    public int getDayStreak() { return dayStreak; }
    public void setDayStreak(int dayStreak) { this.dayStreak = dayStreak; }
    
    public int getTotalModulesCompleted() { return totalModulesCompleted; }
    public void setTotalModulesCompleted(int totalModulesCompleted) { 
        this.totalModulesCompleted = totalModulesCompleted; 
    }
    
    public int getTotalQuizzesPassed() { return totalQuizzesPassed; }
    public void setTotalQuizzesPassed(int totalQuizzesPassed) { 
        this.totalQuizzesPassed = totalQuizzesPassed; 
    }
    
    public String getEarnedBadges() { return earnedBadges; }
    public void setEarnedBadges(String earnedBadges) { this.earnedBadges = earnedBadges; }
    
    public LocalDateTime getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDateTime lastActivityDate) { 
        this.lastActivityDate = lastActivityDate; 
    }
    
    public LocalDateTime getLastLevelUpDate() { return lastLevelUpDate; }
    public void setLastLevelUpDate(LocalDateTime lastLevelUpDate) { 
        this.lastLevelUpDate = lastLevelUpDate; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean hasBadge(String badgeCode) {
        if (earnedBadges == null || earnedBadges.isEmpty()) {
            return false;
        }
        String[] badges = earnedBadges.split(",");
        for (String badge : badges) {
            if (badge.trim().equals(badgeCode)) {
                return true;
            }
        }
        return false;
    }
}