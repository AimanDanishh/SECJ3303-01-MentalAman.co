package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.dao.GamificationDao;
import com.secj3303.model.GamificationUserStats;
import com.secj3303.model.User;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private static final String DEFAULT_VIEW = "gamification";
    private final GamificationDao gamificationDao;

    public GamificationController(GamificationDao gamificationDao) {
        this.gamificationDao = gamificationDao;
    }

    @GetMapping
    public String gamificationDashboard(Authentication authentication, Model model) {
        try {
            // =========================
            // Build logged-in user (Spring Security)
            // =========================
            User user = new User();
            user.setEmail(authentication.getName());
            user.setName(authentication.getName().split("@")[0]);
            user.setRole(
                    authentication.getAuthorities()
                            .iterator()
                            .next()
                            .getAuthority()
                            .replace("ROLE_", "")
                            .toLowerCase()
            );

            String userEmail = authentication.getName();
            
            // =========================
            // Get Gamification Data from DAO
            // =========================
            GamificationUserStats stats = gamificationDao.getUserStats(userEmail)
                    .orElseGet(() -> {
                        GamificationUserStats defaultStats = new GamificationUserStats();
                        defaultStats.setUserEmail(userEmail);
                        defaultStats.setPoints(0);
                        defaultStats.setLevel(1);
                        defaultStats.setCompletedModules(0);
                        defaultStats.setPassedQuizzes(0);
                        defaultStats.setDayStreak(0);
                        return defaultStats;
                    });
            
            GamificationDao.NextLevelProgress nextLevel = gamificationDao.getNextLevelProgress(userEmail);
            
            // =========================
            // Add attributes to model
            // =========================
            model.addAttribute("user", user);
            
            model.addAttribute("userPoints", stats.getPoints());
            model.addAttribute("nextLevelPoints", nextLevel.getNextLevelPoints());
            model.addAttribute("currentLevel", stats.getLevel());
            model.addAttribute("progressPercentage", nextLevel.getProgressPercentage());
            model.addAttribute("pointsToNextLevel", nextLevel.getPointsToNextLevel());
            
            model.addAttribute("badges", gamificationDao.getUserBadges(userEmail));
            model.addAttribute("leaderboard", gamificationDao.getLeaderboard(10));
            model.addAttribute("recentAchievements", gamificationDao.getRecentAchievements(userEmail, 5));
            model.addAttribute("pointsActivities", gamificationDao.getPointsActivities());
            
            model.addAttribute("completedModules", stats.getCompletedModules());
            model.addAttribute("passedQuizzes", stats.getPassedQuizzes());
            model.addAttribute("dayStreak", stats.getDayStreak());
            model.addAttribute("userRank", gamificationDao.getUserLeaderboardRank(userEmail));
            
            model.addAttribute("currentView", DEFAULT_VIEW);

            System.out.println("--- GAMIFICATION PAGE REQUESTED ---");
            System.out.println("User Points: " + stats.getPoints());
            System.out.println("User Level: " + stats.getLevel());

            return "app-layout";
            
        } catch (Exception e) {
            System.err.println("❌ Error in gamification dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Return a basic view even if there's an error
            User user = new User();
            user.setEmail(authentication.getName());
            user.setName(authentication.getName().split("@")[0]);
            
            model.addAttribute("user", user);
            model.addAttribute("currentView", DEFAULT_VIEW);
            model.addAttribute("error", "Unable to load gamification data. Please try again.");
            
            return "app-layout";
        }
    }
}