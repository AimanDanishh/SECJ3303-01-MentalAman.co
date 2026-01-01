package com.secj3303.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.model.User;
import com.secj3303.model.UserProfile;

@Controller
public class ProfileController {

    // =========================
    // View Profile
    // =========================
    @GetMapping("/profile")
    public String viewProfile(
            @RequestParam(name = "edit", required = false) Boolean edit,
            Authentication authentication,
            Model model) {

        User user = buildUser(authentication);

        // Dummy profile (since DB-backed user profile is not implemented yet)
        UserProfile profile = new UserProfile(user.getName(), user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", edit != null && edit);

        return "app-layout";
    }

    // =========================
    // Update Profile (UI-only)
    // =========================
    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            Authentication authentication,
            Model model) {

        User user = buildUser(authentication);

        // Apply updates (UI-only, no DB persistence)
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setLocation(updatedUser.getLocation());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setEmergencyContact(updatedUser.getEmergencyContact());
        user.setBio(updatedUser.getBio());

        UserProfile profile = new UserProfile(user.getName(), user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);

        return "app-layout";
    }

    // =========================
    // Update Preferences (UI-only)
    // =========================
    @PostMapping("/profile/preferences")
    public String updatePreferences(
            @RequestParam Map<String, String> params,
            Authentication authentication,
            Model model) {

        User user = buildUser(authentication);

        UserProfile profile = new UserProfile(user.getName(), user.getEmail());

        profile.setEmailNotifications(params.containsKey("emailNotifications"));
        profile.setPushNotifications(params.containsKey("pushNotifications"));
        profile.setWeeklyReport(params.containsKey("weeklyReport"));
        profile.setAnonymousMode(params.containsKey("anonymousMode"));

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);

        return "app-layout";
    }

    // =========================
    // Helper
    // =========================
    private User buildUser(Authentication authentication) {
        User user = new User();
        user.setEmail(authentication.getName());
        user.setName(authentication.getName().split("@")[0]);
        user.setRole(
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "")
                        .toLowerCase()
        );
        return user;
    }
}
