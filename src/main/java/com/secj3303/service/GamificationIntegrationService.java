package com.secj3303.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.GamificationDao;
import com.secj3303.dao.ModuleProgressDao;
import com.secj3303.model.GamificationUserStats;
// Update relevant parts of GamificationIntegrationService.java
@Service
@Transactional
public class GamificationIntegrationService {
    
    private final ModuleProgressDao progressRepo;
    private final GamificationDao gamificationDao;  // Add this
    
    public GamificationIntegrationService(ModuleProgressDao progressRepo,
                                         GamificationDao gamificationDao) {  // Update constructor
        this.progressRepo = progressRepo;
        this.gamificationDao = gamificationDao;
    }
    
    // Update calculateGamificationData method to use DAO
    public Map<String, Object> calculateGamificationData(String userEmail) {
        // Use the DAO instead of manual calculations
        GamificationUserStats stats = gamificationDao.getUserStats(userEmail)
            .orElseGet(() -> {
                GamificationUserStats defaultStats = new GamificationUserStats();
                defaultStats.setUserEmail(userEmail);
                defaultStats.setPoints(0);
                defaultStats.setLevel(1);
                return defaultStats;
            });
        
        GamificationDao.NextLevelProgress nextLevel = gamificationDao.getNextLevelProgress(userEmail);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userPoints", stats.getPoints());
        result.put("currentLevel", stats.getLevel());
        result.put("nextLevelPoints", nextLevel.getNextLevelPoints());
        result.put("progressPercentage", nextLevel.getProgressPercentage());
        result.put("badges", gamificationDao.getUserBadges(userEmail));
        result.put("userRank", gamificationDao.getUserLeaderboardRank(userEmail));
        result.put("dayStreak", stats.getDayStreak());
        result.put("completedModules", stats.getCompletedModules());
        result.put("passedQuizzes", stats.getPassedQuizzes());
        result.put("recentAchievements", gamificationDao.getRecentAchievements(userEmail, 3));
        
        return result;
    }
    
    // Update other methods similarly...
}