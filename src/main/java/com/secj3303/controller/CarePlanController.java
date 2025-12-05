package com.secj3303.controller;

import javax.servlet.http.HttpSession;

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
    
    @GetMapping
    public String showCarePlan(
        @RequestParam(required = false) String userName,
        @RequestParam(required = false) String userRole,
        HttpSession session,
        Model model) {
        
        // Get user from session or use demo
        String name = (String) session.getAttribute("userName");
        String role = (String) session.getAttribute("userRole");
        
        if (name == null) {
            name = userName != null ? userName : "Demo User";
        }
        if (role == null) {
            role = userRole != null ? userRole : "student";
        }
        
        // Generate care plan
        CarePlanModels.CarePlanData carePlan = CarePlanModels.getDemoCarePlan(name, role);
        
        // Add to model
        model.addAttribute("userData", carePlan.userData);
        model.addAttribute("riskAssessment", carePlan.riskAssessment);
        model.addAttribute("activities", carePlan.activities);
        model.addAttribute("currentView", "careplan");
        model.addAttribute("userRole", role);
        model.addAttribute("userName", name);
        
        return "app-layout";
    }
    
    @PostMapping("/complete/{id}")
    public String completeActivity(@PathVariable int id, HttpSession session) {
        // Logic to mark activity as complete
        return "redirect:/careplan";
    }
    
    @PostMapping("/generate")
    public String generateNewPlan(HttpSession session) {
        // Logic to generate new care plan
        return "redirect:/careplan";
    }
}