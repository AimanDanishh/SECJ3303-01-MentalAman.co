package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.model.User;
import com.secj3303.repository.UserRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // Allow ONLY safe fields to bind
    // =========================
    @InitBinder("user")
    protected void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(
            "email",
            "name",
            "phone",
            "location",
            "dateOfBirth",
            "emergencyContact",
            "bio",
            "emailNotifications",
            "pushNotifications",
            "weeklyReport",
            "anonymousMode"
        );
    }

    // =========================
    // View Profile
    // =========================
    @GetMapping
    public String viewProfile(
            @RequestParam(name = "edit", required = false) Boolean edit,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", edit != null && edit);

        return "app-layout";
    }

    // =========================
    // Update Profile
    // =========================
    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute("user") User formUser,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(formUser.getName());
        user.setPhone(formUser.getPhone());
        user.setLocation(formUser.getLocation());
        user.setDateOfBirth(formUser.getDateOfBirth());
        user.setEmergencyContact(formUser.getEmergencyContact());
        user.setBio(formUser.getBio());

        userRepository.save(user);

        return "redirect:/profile";
    }

    // =========================
    // Update Preferences
    // =========================
    @PostMapping("/preferences")
    public String updatePreferences(
            @ModelAttribute("user") User formUser,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailNotifications(formUser.isEmailNotifications());
        user.setPushNotifications(formUser.isPushNotifications());
        user.setWeeklyReport(formUser.isWeeklyReport());
        user.setAnonymousMode(formUser.isAnonymousMode());

        userRepository.save(user);

        return "redirect:/profile";
    }
}
