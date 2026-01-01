package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.DashboardData;
import com.secj3303.model.User;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String DEFAULT_VIEW = "dashboard";

    @GetMapping
    public String dashboardView(Authentication authentication, Model model) {

        // Spring Security guarantees authentication here
        String email = authentication.getName(); // username = email
        String role = authentication.getAuthorities()
                                   .iterator()
                                   .next()
                                   .getAuthority()
                                   .replace("ROLE_", "")
                                   .toLowerCase();

        // Create lightweight User object for UI compatibility
        User user = new User();
        user.setEmail(email);
        user.setName(email.split("@")[0]);
        user.setRole(role);

        // Layout attributes
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());

        // Dashboard data
        model.addAttribute("studentStats", DashboardData.getStudentStats());
        model.addAttribute("recentActivities", DashboardData.getRecentActivities());
        model.addAttribute("upcomingEvents", DashboardData.getUpcomingEvents());

        return "app-layout";
    }
}
