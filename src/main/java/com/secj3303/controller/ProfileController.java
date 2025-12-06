package com.secj3303.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.secj3303.model.User;
import com.secj3303.model.UserProfile;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User user = new User("test@example.com", "John Doe", "student");
        UserProfile profile = new UserProfile("John Doe", user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("userProfile", profile);
        model.addAttribute("currentView", "profile"); // tells layout which template to load
        return "app-layout";
    }
}
