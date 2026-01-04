package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.DashboardData;
import com.secj3303.model.User;
import com.secj3303.repository.UserRepository;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String DEFAULT_VIEW = "dashboard";

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String dashboardView(Authentication authentication, Model model) {

        // Logged-in user email
        String email = authentication.getName();

        // Load REAL user from DB
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Layout attributes
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());

        // Dashboard data (demo / computed)
        model.addAttribute("studentStats", DashboardData.getStudentStats());
        model.addAttribute("recentActivities", DashboardData.getRecentActivities());
        model.addAttribute("upcomingEvents", DashboardData.getUpcomingEvents());

        return "app-layout";
    }
}
