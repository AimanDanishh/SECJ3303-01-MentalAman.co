package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.GamificationData;
import com.secj3303.model.User;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private static final String DEFAULT_VIEW = "gamification";

    @GetMapping
    public String gamificationDashboard(Authentication authentication, Model model) {

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

        // =========================
        // Gamification Data
        // =========================
        model.addAttribute("user", user);

        model.addAttribute("userPoints", GamificationData.USER_POINTS);
        model.addAttribute("nextLevelPoints", GamificationData.NEXT_LEVEL_POINTS);
        model.addAttribute("currentLevel", GamificationData.CURRENT_LEVEL);

        double progress =
                (GamificationData.USER_POINTS * 100.0) /
                GamificationData.NEXT_LEVEL_POINTS;

        model.addAttribute("progressPercentage", progress);

        model.addAttribute("badges", GamificationData.getBadges());
        model.addAttribute("leaderboard", GamificationData.getLeaderboard());
        model.addAttribute("recentAchievements", GamificationData.getRecentAchievements());
        model.addAttribute("pointsActivities", GamificationData.getPointsActivities());

        model.addAttribute("currentView", DEFAULT_VIEW);

        System.out.println("--- GAMIFICATION PAGE REQUESTED ---");
        System.out.println("Badges found: " + GamificationData.getBadges().size());

        return "app-layout";
    }
}
