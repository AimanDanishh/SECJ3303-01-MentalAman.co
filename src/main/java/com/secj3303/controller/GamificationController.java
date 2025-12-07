package com.secj3303.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.GamificationData;
import com.secj3303.service.AuthenticationService; // Import the service

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private final AuthenticationService authenticationService; // Inject the service
    private static final String DEFAULT_VIEW = "gamification";

    // Constructor Injection (just like in PeerSupportController)
    public GamificationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String gamificationDashboard(Model model, HttpSession session) {
        
        // 1. ADD THE USER (Crucial for Sidebar to work)
        model.addAttribute("user", authenticationService.getAuthenticatedUser(session));

        // 2. Pass core user progress data
        model.addAttribute("userPoints", GamificationData.USER_POINTS);
        model.addAttribute("nextLevelPoints", GamificationData.NEXT_LEVEL_POINTS);
        model.addAttribute("currentLevel", GamificationData.CURRENT_LEVEL);
        model.addAttribute("progressPercentage", (GamificationData.USER_POINTS / (double) GamificationData.NEXT_LEVEL_POINTS) * 100);

        // 3. Pass lists
        model.addAttribute("badges", GamificationData.getBadges());
        model.addAttribute("leaderboard", GamificationData.getLeaderboard());
        model.addAttribute("recentAchievements", GamificationData.getRecentAchievements());
        model.addAttribute("pointsActivities", GamificationData.getPointsActivities());

        // 4. Set the view for app-layout
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        return "app-layout";
    }
}