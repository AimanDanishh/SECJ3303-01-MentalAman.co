package com.secj3303.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.DashboardData;
import com.secj3303.model.User;
import com.secj3303.service.AuthenticationService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String DEFAULT_VIEW = "dashboard";
    private final AuthenticationService authenticationService;

    public DashboardController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String dashboardView(HttpSession session, Model model) {

        // Use AuthenticationService to get the logged-in user
        User user = authenticationService.getAuthenticatedUser(session);
        
        // If the user isn't logged in, redirect to login page
        if (user == null) {
            return "redirect:/login";
        }

        // Set common layout attributes
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());

        // Pass mock data arrays to the view
        model.addAttribute("studentStats", DashboardData.getStudentStats());
        model.addAttribute("recentActivities", DashboardData.getRecentActivities());
        model.addAttribute("upcomingEvents", DashboardData.getUpcomingEvents());

        return "app-layout"; // The main Thymeleaf layout
    }
}
