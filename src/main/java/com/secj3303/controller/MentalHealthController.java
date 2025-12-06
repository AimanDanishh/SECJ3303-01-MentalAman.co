package com.secj3303.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.User;
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
        // If logged in → go to dashboard
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
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String role,
                              HttpSession session,
                              RedirectAttributes redirect) {

        Optional<String> error = authenticationService.authenticateAndSetupSession(
                email, password, role, session
        );

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

        return "app-layout";   // Main layout wrapper
    }
}
