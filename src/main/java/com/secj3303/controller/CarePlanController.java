package com.secj3303.controller;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.model.CarePlanModels;

@Controller
@RequestMapping("/careplan")
public class CarePlanController {
    
    // 1. Simple User Class (Required by app-layout)
    public static class UserDTO {
        public String name;
        public String role;
        public UserDTO(String name, String role) { this.name = name; this.role = role; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }

    @GetMapping
    public String showCarePlan(
        @RequestParam(required = false) String userName,
        @RequestParam(required = false) String userRole,
        HttpSession session,
        Model model) {
        
        // 2. Setup User (Fixes the White Screen Crash)
        String name = (String) session.getAttribute("userName");
        String role = (String) session.getAttribute("userRole");
        if (name == null) name = userName != null ? userName : "Demo User";
        if (role == null) role = userRole != null ? userRole : "student";
        
        // Add 'user' to model (Layout needs this!)
        model.addAttribute("user", new UserDTO(name, role));
        model.addAttribute("currentView", "careplan");
        
        // 3. Load Data
        CarePlanModels.CarePlanData data = null;
        try {
            data = CarePlanModels.getDemoCarePlan(name, role);
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
        }

        // 4. Send Data to View
        if (data != null) {
            model.addAttribute("userData", data.userData);
            model.addAttribute("riskAssessment", data.riskAssessment);
            
            // IMPORTANT: Rename 'activities' to 'carePlan' to match your HTML
            model.addAttribute("carePlan", data.activities); 

            // Calculate Progress (Missing in your original code)
            int total = (data.activities != null) ? data.activities.size() : 0;
            int completed = 0;
            if (data.activities != null) {
                for (var act : data.activities) { if (act.completed) completed++; }
            }
            
            model.addAttribute("totalActivities", total);
            model.addAttribute("completedActivities", completed);
            model.addAttribute("progressPercentage", total > 0 ? ((double)completed/total)*100 : 0);
            
            model.addAttribute("showInsufficientDataWarning", false);
        } else {
            // Fallback (Prevents crashes if data is missing)
            model.addAttribute("carePlan", new ArrayList<>());
            model.addAttribute("totalActivities", 0);
            model.addAttribute("completedActivities", 0);
            model.addAttribute("progressPercentage", 0);
            model.addAttribute("showInsufficientDataWarning", true);
        }
        
        return "app-layout";
    }
    
    @PostMapping("/complete/{id}")
    public String completeActivity(@PathVariable int id) { return "redirect:/careplan"; }
    
    @PostMapping("/generate")
    public String generateNewPlan() { return "redirect:/careplan"; }
    
    @PostMapping("/refresh-data")
    public String refreshData() { return "redirect:/careplan"; }
}