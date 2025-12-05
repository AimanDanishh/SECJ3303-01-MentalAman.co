package com.secj3303.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.GamificationData;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private static final String DEFAULT_VIEW = "gamification";

    @GetMapping
    public String gamificationDashboard(Model model, HttpSession session) {
        
        // Pass core user progress data
        model.addAttribute("userPoints", GamificationData.USER_POINTS);
        model.addAttribute("nextLevelPoints", GamificationData.NEXT_LEVEL_POINTS);
        model.addAttribute("currentLevel", GamificationData.CURRENT_LEVEL);
        model.addAttribute("progressPercentage", (GamificationData.USER_POINTS / (double) GamificationData.NEXT_LEVEL_POINTS) * 100);

        // Pass lists
        model.addAttribute("badges", GamificationData.getBadges());
        model.addAttribute("leaderboard", GamificationData.getLeaderboard());
        model.addAttribute("recentAchievements", GamificationData.getRecentAchievements());
        model.addAttribute("pointsActivities", GamificationData.getPointsActivities());

        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }
}