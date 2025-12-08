package com.secj3303.controller; // Make sure this matches your folder structure!

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.GamificationData;
import com.secj3303.service.AuthenticationService;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private final AuthenticationService authenticationService;
    private static final String DEFAULT_VIEW = "gamification";

    public GamificationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String gamificationDashboard(Model model, HttpSession session) {
        
        // 1. ADD USER (Crucial for Sidebar)
        model.addAttribute("user", authenticationService.getAuthenticatedUser(session));

        // 2. LOAD DUMMY DATA (Using your existing GamificationData file)
        // This ensures the lists are never null
        model.addAttribute("userPoints", GamificationData.USER_POINTS);
        model.addAttribute("nextLevelPoints", GamificationData.NEXT_LEVEL_POINTS);
        model.addAttribute("currentLevel", GamificationData.CURRENT_LEVEL);
        
        // Calculate progress %
        double progress = (GamificationData.USER_POINTS * 100.0) / GamificationData.NEXT_LEVEL_POINTS;
        model.addAttribute("progressPercentage", progress);

        // Load Lists from Static Methods
        model.addAttribute("badges", GamificationData.getBadges());
        model.addAttribute("leaderboard", GamificationData.getLeaderboard());
        model.addAttribute("recentAchievements", GamificationData.getRecentAchievements());
        model.addAttribute("pointsActivities", GamificationData.getPointsActivities());

        // 3. SET VIEW
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Debug Print: Check your Console/Terminal when you visit the page!
        System.out.println("--- GAMIFICATION PAGE REQUESTED ---");
        System.out.println("Badges found: " + GamificationData.getBadges().size());
        
        return "app-layout";
    }
}