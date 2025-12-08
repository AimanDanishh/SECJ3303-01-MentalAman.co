package com.secj3303.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.service.AuthenticationService;

@Controller
public class MentalHealthController {

    private final AuthenticationService authenticationService;

    public MentalHealthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // --------------------------
    // LOGIN / LOGOUT
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
    public String handleLogin(String email, String password, String role,
                              HttpSession session, RedirectAttributes redirect) {

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

}
