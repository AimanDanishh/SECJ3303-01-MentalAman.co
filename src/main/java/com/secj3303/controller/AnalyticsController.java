package com.secj3303.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.StudentEngagement;
import com.secj3303.service.AnalyticsService;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private static final String DEFAULT_VIEW = "analytics";

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public String analyticsDashboard(
        @RequestParam(required = false) String searchQuery,
        @RequestParam(defaultValue = "all") String filterRisk,
        @RequestParam(defaultValue = "all") String filterDepartment,
        @RequestParam(required = false) Integer selectedStudentId,
        Model model,
        RedirectAttributes redirect
    ) {
        // --- 1. Filter and Prepare Data ---
        
        List<StudentEngagement> filteredStudents = analyticsService.filterStudents(searchQuery, filterRisk, filterDepartment);
        
        // --- 2. Calculate Aggregations ---
        
        model.addAttribute("totalStudents", analyticsService.getTotalStudents());
        model.addAttribute("highRiskCount", analyticsService.getHighRiskCount());
        model.addAttribute("moderateRiskCount", analyticsService.getModerateRiskCount());
        model.addAttribute("avgCompletion", analyticsService.getAvgCompletion(analyticsService.getAllStudents()));
        model.addAttribute("avgLoginFrequency", analyticsService.getAvgLoginFrequency(analyticsService.getAllStudents()));
        
        // --- 3. Set Filters and Student List ---
        
        model.addAttribute("searchQuery", searchQuery == null ? "" : searchQuery);
        model.addAttribute("filterRisk", filterRisk);
        model.addAttribute("filterDepartment", filterDepartment);
        model.addAttribute("departments", analyticsService.getAllDepartments());
        model.addAttribute("filteredStudents", filteredStudents);
        
        // --- 4. Handle Detail View State (Replaces selectedStudent state) ---
        
        if (selectedStudentId != null) {
            Optional<StudentEngagement> studentOpt = filteredStudents.stream()
                .filter(s -> s.getId() == selectedStudentId)
                .findFirst();
            
            if (studentOpt.isPresent()) {
                model.addAttribute("selectedStudent", studentOpt.get());
            } else {
                // If ID is invalid, clear the state
                model.addAttribute("selectedStudent", null);
            }
        } else {
            model.addAttribute("selectedStudent", null);
        }

        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }
    
    // --- Export Handler (Replaces handleExportReport) ---

    // Note: Since Thymeleaf doesn't directly handle file creation, we pass the necessary data
    // to the view and use JavaScript to create the CSV blob, as done in the original React code.
    // However, the export button will be placed on the main GET mapping.

    // --- Flag Student Handler (Replaces handleFlagStudent) ---
    
    @GetMapping("/flag")
    public String flagStudent(@RequestParam int id, RedirectAttributes redirect) {
        // In a real application, this would call a service to log the referral/notification.
        
        // Use flash attributes to show a non-persistent success message upon redirect
        redirect.addFlashAttribute("alertMessage", "🚩 Student Flagged for Follow-up: ID " + id + ". Counselor has been notified for intervention.");
        redirect.addFlashAttribute("alertType", "success");
        
        // Redirect back to the main analytics page (without opening the detail view)
        return "redirect:/analytics";
    }
}