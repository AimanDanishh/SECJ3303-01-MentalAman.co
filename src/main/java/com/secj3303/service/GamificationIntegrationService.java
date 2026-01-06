package com.secj3303.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.repository.ModuleProgressRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GamificationIntegrationService {
    
    private final ModuleProgressRepository progressRepo;
    
    // In-memory storage for gamification stats
    private final Map<String, GamificationUserStats> userStatsMap = new HashMap<>();
    
    // Points configuration
    private static final int POINTS_PER_MODULE = 100;
    private static final int POINTS_PER_QUIZ = 50;
    private static final int POINTS_PER_STREAK_DAY = 5;
    private static final int POINTS_PER_LEVEL = 200;
    
    public GamificationIntegrationService(ModuleProgressRepository progressRepo) {
        this.progressRepo = progressRepo;
    }
    
    // This class stores gamification stats in memory
    private static class GamificationUserStats {
        String userEmail;
        int points;
        int level;
        int dayStreak;
        LocalDate lastActivityDate;
        List<String> badges = new ArrayList<>();
        
        GamificationUserStats(String userEmail) {
            this.userEmail = userEmail;
            this.points = 0;
            this.level = 1;
            this.dayStreak = 0;
            this.lastActivityDate = LocalDate.now().minusDays(1);
        }
    }
    
    // Calculate gamification data for a user
    public Map<String, Object> calculateGamificationData(String userEmail) {
        Map<String, Object> result = new HashMap<>();
        
        // Get user progress from existing module_progress table
        List<com.secj3303.model.ModuleProgress> userProgress = 
            progressRepo.findAllByUserEmail(userEmail);
        
        // Calculate points based on progress
        int totalPoints = 0;
        int completedModules = 0;
        int passedQuizzes = 0;
        
        for (com.secj3303.model.ModuleProgress progress : userProgress) {
            if (progress.getProgress() == 100) {
                totalPoints += POINTS_PER_MODULE;
                completedModules++;
                
                if (progress.isQuizPassed()) {
                    totalPoints += POINTS_PER_QUIZ;
                    passedQuizzes++;
                }
            } else if (progress.getProgress() > 0) {
                totalPoints += (int)(progress.getProgress() * 0.5);
            }
        }
        
        // Calculate level
        int currentLevel = calculateLevel(totalPoints);
        
        // Calculate streak
        int dayStreak = calculateDayStreak(userEmail);
        totalPoints += dayStreak * POINTS_PER_STREAK_DAY;
        
        // Recalculate level with streak bonus
        currentLevel = calculateLevel(totalPoints);
        
        // Calculate next level points
        int nextLevelPoints = currentLevel * POINTS_PER_LEVEL;
        
        // Calculate progress percentage
        double progressPercentage = calculateProgressPercentage(totalPoints, currentLevel);
        
        // Get badges
        List<Map<String, Object>> badges = calculateBadges(completedModules, passedQuizzes, dayStreak, totalPoints);
        
        // Get leaderboard position (simulated)
        int userRank = calculateUserRank();
        
        // Get recent achievements
        List<Map<String, Object>> recentAchievements = getRecentAchievements(completedModules, passedQuizzes);
        
        // Build result
        result.put("userPoints", totalPoints);
        result.put("currentLevel", currentLevel);
        result.put("nextLevelPoints", nextLevelPoints);
        result.put("progressPercentage", progressPercentage);
        result.put("badges", badges);
        result.put("userRank", userRank);
        result.put("dayStreak", dayStreak);
        result.put("completedModules", completedModules);
        result.put("passedQuizzes", passedQuizzes);
        result.put("recentAchievements", recentAchievements);
        
        return result;
    }
    
    private int calculateLevel(int points) {
        return Math.max(1, points / POINTS_PER_LEVEL + 1);
    }
    
    private double calculateProgressPercentage(int points, int currentLevel) {
        int pointsInCurrentLevel = points - ((currentLevel - 1) * POINTS_PER_LEVEL);
        return Math.min((pointsInCurrentLevel * 100.0) / POINTS_PER_LEVEL, 100.0);
    }
    
    private int calculateDayStreak(String userEmail) {
        GamificationUserStats stats = userStatsMap.computeIfAbsent(userEmail, 
            email -> new GamificationUserStats(email));
        
        LocalDate today = LocalDate.now();
        
        if (stats.lastActivityDate.isEqual(today.minusDays(1))) {
            stats.dayStreak++;
        } else if (!stats.lastActivityDate.isEqual(today)) {
            stats.dayStreak = 1;
        }
        
        stats.lastActivityDate = today;
        return stats.dayStreak;
    }
    
    private List<Map<String, Object>> calculateBadges(int completedModules, int passedQuizzes, int dayStreak, int totalPoints) {
        List<Map<String, Object>> badges = new ArrayList<>();
        
        // Module completion badges
        if (completedModules >= 1) {
            badges.add(createBadge("🎯", "First Steps", "Completed your first module", "common", true));
        }
        if (completedModules >= 3) {
            badges.add(createBadge("📚", "Knowledge Seeker", "Completed 3 modules", "rare", completedModules >= 3));
        }
        if (completedModules >= 5) {
            badges.add(createBadge("👑", "Wellness Champion", "Completed 5 modules", "epic", completedModules >= 5));
        }
        
        // Quiz badges
        if (passedQuizzes >= 1) {
            badges.add(createBadge("🧠", "Quiz Master", "Passed your first quiz", "uncommon", true));
        }
        
        // Streak badges
        if (dayStreak >= 7) {
            badges.add(createBadge("🔥", "Consistent Learner", "7-day learning streak", "uncommon", dayStreak >= 7));
        }
        
        // Points badges
        if (totalPoints >= 500) {
            badges.add(createBadge("💎", "Point Collector", "Reached 500 points", "rare", totalPoints >= 500));
        }
        
        return badges;
    }
    
    private Map<String, Object> createBadge(String icon, String name, String description, String rarity, boolean earned) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("icon", icon);
        badge.put("name", name);
        badge.put("description", description);
        badge.put("rarity", rarity);
        badge.put("earned", earned);
        return badge;
    }
    
    private int calculateUserRank() {
        // Simple rank calculation - returns random rank 1-10
        return (int) (Math.random() * 10) + 1;
    }
    
    private List<Map<String, Object>> getRecentAchievements(int completedModules, int passedQuizzes) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        if (completedModules > 0) {
            achievements.add(createAchievement(
                "Module Complete", 
                "Finished " + completedModules + " module" + (completedModules > 1 ? "s" : ""),
                completedModules * 100,
                "Recently"
            ));
        }
        
        if (passedQuizzes > 0) {
            achievements.add(createAchievement(
                "Quiz Master", 
                "Passed " + passedQuizzes + " quiz" + (passedQuizzes > 1 ? "zes" : ""),
                passedQuizzes * 50,
                "Recently"
            ));
        }
        
        // Add a placeholder if no achievements
        if (achievements.isEmpty()) {
            achievements.add(createAchievement(
                "Welcome!", 
                "Start your first module to earn achievements",
                0,
                "Get started"
            ));
        }
        
        return achievements;
    }
    
    private Map<String, Object> createAchievement(String title, String description, int points, String time) {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("title", title);
        achievement.put("description", description);
        achievement.put("points", points);
        achievement.put("time", time);
        return achievement;
    }
    
    // Get leaderboard data (simplified)
    public List<Map<String, Object>> getLeaderboard(String currentUserEmail) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        
        // Example leaderboard entries
        leaderboard.add(createLeaderboardEntry(1, "Student A", 1250, 7, "👑", false));
        leaderboard.add(createLeaderboardEntry(2, "Student B", 1180, 6, "🥈", false));
        leaderboard.add(createLeaderboardEntry(3, "Student C", 1050, 6, "🥉", false));
        
        // Add current user
        Map<String, Object> currentUserData = calculateGamificationData(currentUserEmail);
        leaderboard.add(createLeaderboardEntry(
            4,
            "You",
            (int) currentUserData.get("userPoints"),
            (int) currentUserData.get("currentLevel"),
            "⭐",
            true
        ));
        
        leaderboard.add(createLeaderboardEntry(5, "Student D", 820, 5, "", false));
        
        return leaderboard;
    }
    
    private Map<String, Object> createLeaderboardEntry(int rank, String name, int points, int level, String badge, boolean isCurrentUser) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("rank", rank);
        entry.put("name", name);
        entry.put("points", points);
        entry.put("level", level);
        entry.put("badge", badge);
        entry.put("isCurrentUser", isCurrentUser);
        return entry;
    }
    
    // Get points activities (static data)
    public List<Map<String, Object>> getPointsActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        activities.add(createPointsActivity("Complete a learning module", 100));
        activities.add(createPointsActivity("Pass a quiz (70%+)", 50));
        activities.add(createPointsActivity("Daily learning activity", 10));
        activities.add(createPointsActivity("Help in peer forum", 5));
        activities.add(createPointsActivity("Maintain 7-day streak", 75));
        activities.add(createPointsActivity("Complete self-assessment", 25));
        
        return activities;
    }
    
    private Map<String, Object> createPointsActivity(String activity, int points) {
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("activity", activity);
        activityMap.put("points", points);
        return activityMap;
    }
}