package com.secj3303.controller;

// CRUCIAL: java.util imports prevent 'ArrayList error'
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import com.secj3303.model.StudentEngagement;
import com.secj3303.service.AnalyticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private static final String DEFAULT_VIEW = "analytics";

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public String showAnalytics(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "all") String filterRisk,
            @RequestParam(defaultValue = "all") String filterDepartment,
            Model model) {

        // 1. Analytics Logic
        List<StudentEngagement> filtered = analyticsService.filterStudents(searchQuery, filterRisk, filterDepartment);
        
        model.addAttribute("totalStudents", analyticsService.getTotalStudents());
        model.addAttribute("highRiskCount", analyticsService.getHighRiskCount());
        model.addAttribute("moderateRiskCount", analyticsService.getModerateRiskCount());
        model.addAttribute("avgCompletion", analyticsService.getAvgCompletion(analyticsService.getAllStudents()));
        model.addAttribute("avgLoginFrequency", analyticsService.getAvgLoginFrequency(analyticsService.getAllStudents()));
        
        model.addAttribute("filteredStudents", filtered);
        model.addAttribute("departments", analyticsService.getAllDepartments());
        model.addAttribute("searchQuery", searchQuery == null ? "" : searchQuery);
        model.addAttribute("filterRisk", filterRisk);
        model.addAttribute("filterDepartment", filterDepartment);

        // 2. Sidebar & Layout Compatibility (Crucial for ROLE_ADMINISTRATOR view)
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        Map<String, String> user = new HashMap<>();
        user.put("name", "Admin");
        user.put("role", "ROLE_ADMINISTRATOR"); 
        model.addAttribute("user", user);

        // 3. Placeholder Attributes for app-layout
        model.addAttribute("modules", new ArrayList<Object>());
        model.addAttribute("lessons", new ArrayList<Object>());
        model.addAttribute("quizQuestions", new ArrayList<Object>());
        model.addAttribute("completedCount", 0);
        model.addAttribute("inProgressCount", 0);
        model.addAttribute("totalModules", 0);

        return "app-layout";
    }

    @GetMapping("/flag")
    public String flagStudent(@RequestParam int id, RedirectAttributes redirect) {
        redirect.addFlashAttribute("alertMessage", "🚩 Student Flagged for Intervention: ID " + id);
        return "redirect:/analytics";
    }
}