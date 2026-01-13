package com.secj3303.controller;

import java.util.List;
import java.util.ArrayList;
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

        // 1. Analytics Data Calculation
        List<StudentEngagement> filtered = analyticsService.filterStudents(searchQuery, filterRisk, filterDepartment);
        
        // 2. Core Analytics Attributes
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

        // 3. Layout Compatibility (Matches app-layout.html's th:replace parameters)
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Using HashMap for better compatibility across Java versions
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", "Admin");
        userMap.put("role", "ROLE_ADMINISTRATOR");
        model.addAttribute("user", userMap);

        // 4. Placeholder Attributes to prevent "Property not found" errors in app-layout
        model.addAttribute("modules", new ArrayList<>());
        model.addAttribute("lessons", new ArrayList<>());
        model.addAttribute("quizQuestions", new ArrayList<>());
        model.addAttribute("completedCount", 0);
        model.addAttribute("inProgressCount", 0);
        model.addAttribute("totalModules", 0);
        model.addAttribute("students", filtered); 
        model.addAttribute("referrals", new ArrayList<>());
        model.addAttribute("reasons", new ArrayList<>());
        model.addAttribute("selectedModule", null);
        model.addAttribute("selectedLesson", null);
        model.addAttribute("showQuiz", false);
        model.addAttribute("quizScore", 0);
        model.addAttribute("achievement", null);
        model.addAttribute("selectedStudent", null);
        model.addAttribute("showForm", false);
        model.addAttribute("formData", null);
        model.addAttribute("showSuccess", false);
        model.addAttribute("showError", false);
        model.addAttribute("errorMessage", "");

        return "app-layout";
    }

    @GetMapping("/flag")
    public String flagStudent(@RequestParam int id, RedirectAttributes redirect) {
        redirect.addFlashAttribute("alertMessage", "🚩 Student Flagged for Intervention: ID " + id);
        return "redirect:/analytics";
    }
}