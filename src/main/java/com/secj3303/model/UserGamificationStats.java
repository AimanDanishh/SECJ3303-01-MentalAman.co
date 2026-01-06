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
    
    @Column(name = "points_from_modules")
    private int pointsFromModules = 0;
    
    @Column(name = "points_from_quizzes")
    private int pointsFromQuizzes = 0;
    
    @Column(name = "points_from_streaks")
    private int pointsFromStreaks = 0;
    
    @Column(name = "points_from_bonuses")
    private int pointsFromBonuses = 0;
    
    @Column(name = "total_days_active")
    private int totalDaysActive = 0;
    
    @Column(name = "longest_streak")
    private int longestStreak = 0;
    
    @Column(name = "last_streak_update")
    private LocalDateTime lastStreakUpdate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
    
    public int getPointsFromModules() { return pointsFromModules; }
    public void setPointsFromModules(int pointsFromModules) { this.pointsFromModules = pointsFromModules; }
    
    public int getPointsFromQuizzes() { return pointsFromQuizzes; }
    public void setPointsFromQuizzes(int pointsFromQuizzes) { this.pointsFromQuizzes = pointsFromQuizzes; }
    
    public int getPointsFromStreaks() { return pointsFromStreaks; }
    public void setPointsFromStreaks(int pointsFromStreaks) { this.pointsFromStreaks = pointsFromStreaks; }
    
    public int getPointsFromBonuses() { return pointsFromBonuses; }
    public void setPointsFromBonuses(int pointsFromBonuses) { this.pointsFromBonuses = pointsFromBonuses; }
    
    public int getTotalDaysActive() { return totalDaysActive; }
    public void setTotalDaysActive(int totalDaysActive) { this.totalDaysActive = totalDaysActive; }
    
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    
    public LocalDateTime getLastStreakUpdate() { return lastStreakUpdate; }
    public void setLastStreakUpdate(LocalDateTime lastStreakUpdate) { this.lastStreakUpdate = lastStreakUpdate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public int getTotalPoints() {
        return pointsFromModules + pointsFromQuizzes + pointsFromStreaks + pointsFromBonuses;
    }
}