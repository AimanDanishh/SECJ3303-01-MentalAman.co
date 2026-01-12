package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.Gamification;
import com.secj3303.model.User;
import com.secj3303.service.GamificationService;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        
        // 1. Get Profile
        Gamification profile = gamificationService.getUserGamificationProfile(email);
        
        User user = new User();
        user.setEmail(email);
        user.setName(email.split("@")[0]);
        
        // 2. Populate Model
        model.addAttribute("user", user);
        model.addAttribute("userPoints", profile.getXpPoints());
        model.addAttribute("currentLevel", profile.getCurrentLevel());
        model.addAttribute("dayStreak", profile.getDailyStreak());
        model.addAttribute("pointsToNextLevel", gamificationService.getPointsToNextLevel(profile.getXpPoints()));
        
        // Calculate Progress Bar
        int levelBase = (profile.getCurrentLevel() - 1) * 200;
        int pointsInLevel = profile.getXpPoints() - levelBase;
        double progressPercentage = (pointsInLevel / 200.0) * 100;
        model.addAttribute("progressPercentage", progressPercentage);
        model.addAttribute("nextLevelPoints", (profile.getCurrentLevel() * 200));

        // 3. Lists (Dynamic badges, functional leaderboard)
        model.addAttribute("badges", gamificationService.getUnlockedBadges(profile));
        model.addAttribute("leaderboard", gamificationService.getFunctionalLeaderboard(email));
        model.addAttribute("pointsActivities", Gamification.getPointsActivities());
        model.addAttribute("recentAchievements", Gamification.getRecentAchievements());
        
        model.addAttribute("currentView", "gamification");
        return "app-layout";
    }
}