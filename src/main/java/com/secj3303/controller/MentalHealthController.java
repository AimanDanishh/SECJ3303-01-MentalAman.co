package com.secj3303.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.User;
import com.secj3303.model.UserProfile;
import com.secj3303.service.AuthenticationService;

@Controller
public class MentalHealthController {

    private final AuthenticationService authenticationService;
    private static final String USER_SESSION_KEY = "currentUser";

    public MentalHealthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // --------------------------
    // LOGIN / LOGOUT HANDLERS
    // --------------------------

    @GetMapping("/")
    public String index(HttpSession session) {
        if (authenticationService.getAuthenticatedUser(session) != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(String email, String password, String role, HttpSession session,
                              RedirectAttributes redirect) {
        Optional<String> error = authenticationService.authenticateAndSetupSession(email, password, role, session);
        if (error.isPresent()) {
            redirect.addFlashAttribute("error", error.get());
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --------------------------
    // PAGE ROUTING (PROTECTED)
    // --------------------------

    private String checkAuth(HttpSession session, Model model, String viewName) {
        User user = authenticationService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("currentView", viewName);
        return "app-layout";
    }

    // --------------------------
    // PROFILE PAGE (Example)
    // --------------------------
    @GetMapping("/profile")
    public String viewProfile(@RequestParam(name="edit", required=false) Boolean edit, Model model) {
        User user = new User("test@example.com", "John Doe", "student");
        // Example preferences
        user.setEmailNotifications(true);
        user.setPushNotifications(true);
        user.setWeeklyReport(true);
        user.setAnonymousMode(false);

        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", edit != null && edit);
        return "app-layout";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user, Model model) {
        // Save user info (DB or session)
        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);
        return "app-layout";
    }

    @PostMapping("/profile/preferences")
    public String updatePreferences(@RequestParam Map<String,String> params, Model model) {
        User user = new User("test@example.com", "John Doe", "student");
        user.setEmailNotifications(params.containsKey("emailNotifications"));
        user.setPushNotifications(params.containsKey("pushNotifications"));
        user.setWeeklyReport(params.containsKey("weeklyReport"));
        user.setAnonymousMode(params.containsKey("anonymousMode"));

        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", false);
        return "app-layout";
    }


    // --------------------------
    // LEARNING PAGE (Example)
    // --------------------------
    /* @GetMapping("/learning")
    public String learning(HttpSession session, Model model) {
        return checkAuth(session, model, "learning");
    } */

    // You can add more pages (coach, forum, careplan, etc.) similarly
}
