package com.secj3303.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

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

    @GetMapping("/profile")
    public String viewProfile(
            @RequestParam(name = "edit", required = false) Boolean edit,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("currentUser");
        UserProfile profile = (UserProfile) session.getAttribute("userProfile");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", edit != null && edit);

        return "app-layout";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, HttpSession session, Model model) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Copy updated fields into the existing session user
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setLocation(updatedUser.getLocation());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setEmergencyContact(updatedUser.getEmergencyContact());
        user.setBio(updatedUser.getBio());

        // Save back to session
        session.setAttribute("currentUser", user);

        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);

        return "app-layout";
    }


    @PostMapping("/profile/preferences")
    public String updatePreferences(
            @RequestParam Map<String, String> params,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("currentUser");
        UserProfile profile = (UserProfile) session.getAttribute("userProfile");

        if (user == null) {
            return "redirect:/login";
        }

        profile.setEmailNotifications(params.containsKey("emailNotifications"));
        profile.setPushNotifications(params.containsKey("pushNotifications"));
        profile.setWeeklyReport(params.containsKey("weeklyReport"));
        profile.setAnonymousMode(params.containsKey("anonymousMode"));

        session.setAttribute("userProfile", profile);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);

        return "app-layout";
    }
}
