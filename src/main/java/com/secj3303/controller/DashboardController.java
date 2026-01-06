package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.DashboardData;
import com.secj3303.model.Person;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String DEFAULT_VIEW = "dashboard";

    private final PersonDao personDao;

    public DashboardController(PersonDao personDao) {
        this.personDao = personDao;
    }

    @GetMapping
    public String dashboardView(Authentication authentication, Model model) {

        // Logged-in user email (from Spring Security)
        String email = authentication.getName();

        // Load REAL person from DB
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found: " + email);
        }

        // Layout attributes
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("user", person);          // keep name "user" for UI compatibility
        model.addAttribute("userName", person.getName());

        // Dashboard data (demo / computed)
        model.addAttribute("studentStats", DashboardData.getStudentStats());
        model.addAttribute("recentActivities", DashboardData.getRecentActivities());
        model.addAttribute("upcomingEvents", DashboardData.getUpcomingEvents());

        return "app-layout";
    }
}
