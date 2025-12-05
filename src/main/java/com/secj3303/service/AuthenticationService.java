package com.secj3303.service;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.secj3303.model.User;
import com.secj3303.model.UserProfile;

@Service
public class AuthenticationService {

    private static final String USER_SESSION_KEY = "currentUser";
    private static final String PROFILE_SESSION_KEY = "userProfile";
    
    /**
     * Performs authentication validation and sets up the user session.
     */
    public Optional<String> authenticateAndSetupSession(String email, String password, String role, HttpSession session) {
        
        // 1. Validation Logic
        if (email == null || email.trim().isEmpty()) {
            return Optional.of("Email is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            return Optional.of("Password is required");
        }
        
        if (!email.contains("@")) {
            return Optional.of("Please enter a valid email address");
        }

        if (password.length() < 6 && !"demo123".equals(password)) {
            return Optional.of("Password must be at least 6 characters");
        }

        if (role == null || !Arrays.asList("student", "faculty", "counsellor", "administrator").contains(role)) {
            return Optional.of("Please select a valid user role");
        }
        
        // 2. Authentication Success: Create User model
        // Extract name from email
        String name = email.split("@")[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        
        // Create UserProfile
        UserProfile profile = new UserProfile(name, email);
        
        // 3. Store in session
        session.setAttribute(USER_SESSION_KEY, user);
        session.setAttribute(PROFILE_SESSION_KEY, profile);
        session.setAttribute("user", user); // Also set as "user" for compatibility
        session.setAttribute("userName", name);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        return Optional.empty(); // Success
    }

    /**
     * Checks if a user is authenticated in the session.
     */
    public User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(USER_SESSION_KEY);
    }
    
    /**
     * Get user profile from session
     */
    public UserProfile getUserProfile(HttpSession session) {
        return (UserProfile) session.getAttribute(PROFILE_SESSION_KEY);
    }
    
    /**
     * Update user profile in session
     */
    public void updateUserProfile(UserProfile profile, HttpSession session) {
        session.setAttribute(PROFILE_SESSION_KEY, profile);
    }
    
    /**
     * Logout user by invalidating session
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(USER_SESSION_KEY) != null;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(HttpSession session, String requiredRole) {
        User user = getAuthenticatedUser(session);
        return user != null && requiredRole.equals(user.getRole());
    }
    
    /**
     * Quick demo login for testing
     */
    public void demoLogin(String role, HttpSession session) {
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
                return;
        }
        
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        
        UserProfile profile = new UserProfile(name, email);
        
        session.setAttribute(USER_SESSION_KEY, user);
        session.setAttribute(PROFILE_SESSION_KEY, profile);
        session.setAttribute("user", user);
        session.setAttribute("userName", name);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);
        session.setMaxInactiveInterval(30 * 60);
    }
}