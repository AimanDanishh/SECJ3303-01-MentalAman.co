package com.secj3303.controller;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.User;

@Controller
@SessionAttributes("user")
public class MainController {
    
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "redirect:/dashboard";
    }
    
    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session,
            Model model) {
        
        // Basic validation
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "login";
        }
        
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "login";
        }
        
        if (!email.contains("@")) {
            model.addAttribute("error", "Invalid email format");
            return "login";
        }
        
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "login";
        }
        
        if (role == null || role.trim().isEmpty()) {
            model.addAttribute("error", "Please select a role");
            return "login";
        }
        
        // Extract name from email
        String name = email.split("@")[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        // Create user
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        
        // Store in session
        session.setAttribute("user", user);
        session.setAttribute("currentUser", user);
        session.setAttribute("userName", name);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        
        return "redirect:/dashboard";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
        return "redirect:/login";
    }
    
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("currentView", "profile");
        return "app-layout";
    }
    
    // Helper method to check view access
    private boolean canAccessView(User user, String view) {
        if (user == null) return false;
        
        switch (view) {
            case "admin":
            case "analytics":
            case "content":
            case "api":
                return user.isAdministrator() || user.isCounsellor();
            case "counselling":
            case "referral":
            case "careplan":
                return user.isCounsellor() || user.isAdministrator();
            case "profile":
                return true;
            default:
                return true; // Other views are accessible
        }
    }
    
    @GetMapping("/{view}")
    public String getView(@PathVariable String view, 
                         HttpSession session, 
                         Model model,
                         RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Skip views that have dedicated controllers
        String[] dedicatedViews = {"dashboard", "admin", "counselling", "assessment", "learning", 
                                   "coach", "forum", "gamification", "analytics", "content", 
                                   "api", "referral", "careplan"};
        
        for (String dedicated : dedicatedViews) {
            if (dedicated.equals(view)) {
                // Redirect to the dedicated controller's endpoint
                return "redirect:/" + view;
            }
        }
        
        // Check access for other views
        if (!canAccessView(user, view)) {
            redirectAttributes.addFlashAttribute("error", 
                "You don't have permission to access this page.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("currentView", view);
        
        return "app-layout";
    }
    
    // Demo login endpoints for quick testing
    @GetMapping("/demo/login/{role}")
    public String demoLogin(@PathVariable String role, HttpSession session) {
        String email, name;
        
        switch (role) {
            case "student":
                email = "student@demo.com";
                name = "John Student";
                break;
            case "faculty":
                email = "faculty@demo.com";
                name = "Dr. Jane Faculty";
                break;
            case "counsellor":
                email = "counsellor@demo.com";
                name = "Dr. Smith Counsellor";
                break;
            case "administrator":
                email = "admin@demo.com";
                name = "Admin User";
                break;
            default:
                return "redirect:/login";
        }
        
        User user = new User(email, name, role);
        session.setAttribute("user", user);
        session.setAttribute("currentUser", user);
        session.setAttribute("userName", name);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);
        session.setMaxInactiveInterval(30 * 60);
        
        return "redirect:/dashboard";
    }
}