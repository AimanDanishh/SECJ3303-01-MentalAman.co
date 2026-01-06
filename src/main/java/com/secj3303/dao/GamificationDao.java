// File: GamificationDao.java
package com.secj3303.dao;

import com.secj3303.model.GamificationBadge;
import com.secj3303.model.GamificationLeaderboardEntry;
import com.secj3303.model.GamificationUserStats;

import java.util.List;
import java.util.Optional;

public interface GamificationDao {
    
    // Get user's gamification statistics
    Optional<GamificationUserStats> getUserStats(String userEmail);
    
    // Get leaderboard (top N users)
    List<GamificationLeaderboardEntry> getLeaderboard(int limit);
    
    // Get user's position in leaderboard
    int getUserLeaderboardRank(String userEmail);
    
    // Get user's badges
    List<GamificationBadge> getUserBadges(String userEmail);
    
    // Award badge to user
    void awardBadge(String userEmail, String badgeId);
    
    // Record points activity
    void recordPointsActivity(String userEmail, String activity, int points);
    
    // Get points activities (how to earn points)
    List<PointsActivity> getPointsActivities();
    
    // Get user's recent achievements
    List<RecentAchievement> getRecentAchievements(String userEmail, int limit);
    
    // Calculate total points for a user
    int calculateUserPoints(String userEmail);
    
    // Calculate user level based on points
    int calculateUserLevel(String userEmail);
    
    // Get progress to next level
    NextLevelProgress getNextLevelProgress(String userEmail);
    
    // Get top performers in a specific category
    List<GamificationLeaderboardEntry> getCategoryLeaderboard(String category, int limit);
    
    // Class for points activity information
    class PointsActivity {
        private String activity;
        private int points;
        private String description;
        
        public PointsActivity(String activity, int points, String description) {
            this.activity = activity;
            this.points = points;
            this.description = description;
        }
        
        // Getters
        public String getActivity() { return activity; }
        public int getPoints() { return points; }
        public String getDescription() { return description; }
    }
    
    // Class for recent achievement
    class RecentAchievement {
        private String title;
        private String description;
        private int points;
        private String timestamp;
        
        public RecentAchievement(String title, String description, int points, String timestamp) {
            this.title = title;
            this.description = description;
            this.points = points;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getPoints() { return points; }
        public String getTimestamp() { return timestamp; }
    }
    
    // Class for next level progress
    class NextLevelProgress {
        private int currentPoints;
        private int nextLevelPoints;
        private double progressPercentage;
        private int pointsToNextLevel;
        
        public NextLevelProgress(int currentPoints, int nextLevelPoints) {
            this.currentPoints = currentPoints;
            this.nextLevelPoints = nextLevelPoints;
            this.pointsToNextLevel = Math.max(0, nextLevelPoints - currentPoints);
            this.progressPercentage = nextLevelPoints > 0 ? 
                (double) currentPoints / nextLevelPoints * 100 : 0;
        }
        
        // Getters
        public int getCurrentPoints() { return currentPoints; }
        public int getNextLevelPoints() { return nextLevelPoints; }
        public double getProgressPercentage() { return progressPercentage; }
        public int getPointsToNextLevel() { return pointsToNextLevel; }
    }
}