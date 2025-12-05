package com.secj3303.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.model.TeleCounsellingModels;

@Controller
@RequestMapping("/tele-counselling")
public class TeleCounsellingController {

    private static final String DEFAULT_VIEW = "tele-counselling"; 

    @GetMapping
    public String teleCounsellingDashboard(Model model, HttpSession session) {
        
        // Pass data to the view
        model.addAttribute("upcomingSessions", TeleCounsellingModels.getUpcomingSessions());
        model.addAttribute("pastSessions", TeleCounsellingModels.getPastSessions());
        model.addAttribute("availableCounsellors", TeleCounsellingModels.getAvailableCounsellors());

        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }
}