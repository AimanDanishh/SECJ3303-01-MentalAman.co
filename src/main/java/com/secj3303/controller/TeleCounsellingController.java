package com.secj3303.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.TeleCounsellingModels;
import com.secj3303.model.User;

@Controller
@RequestMapping("/tele-counselling")
public class TeleCounsellingController {

    private static final String DEFAULT_VIEW = "tele-counselling"; 

    @GetMapping
    public String teleCounsellingDashboard(Model model, HttpSession session) {
        
        // Get user from session
        User user = (User) session.getAttribute("user");
        
        // Check if user is logged in
        if (user == null) {
            return "redirect:/login";
        }
        
        // Add user to model (REQUIRED for app-layout)
        model.addAttribute("user", user);
        
        // Pass data to the view
        model.addAttribute("upcomingSessions", TeleCounsellingModels.getUpcomingSessions());
        model.addAttribute("pastSessions", TeleCounsellingModels.getPastSessions());
        model.addAttribute("availableCounsellors", TeleCounsellingModels.getAvailableCounsellors());

        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }
}