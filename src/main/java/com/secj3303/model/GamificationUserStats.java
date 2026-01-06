package com.secj3303.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gamification_stats")
public class GamificationUserStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;
    
    private int points;
    private int level;
    private int completedModules;
    private int passedQuizzes;
    private int dayStreak;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Transient
    private List<GamificationBadge> badges = new ArrayList<>();
    
    // Constructors
    public GamificationUserStats() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getCompletedModules() { return completedModules; }
    public void setCompletedModules(int completedModules) { this.completedModules = completedModules; }
    
    public int getPassedQuizzes() { return passedQuizzes; }
    public void setPassedQuizzes(int passedQuizzes) { this.passedQuizzes = passedQuizzes; }
    
    public int getDayStreak() { return dayStreak; }
    public void setDayStreak(int dayStreak) { this.dayStreak = dayStreak; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public List<GamificationBadge> getBadges() { return badges; }
    public void setBadges(List<GamificationBadge> badges) { this.badges = badges; }
}