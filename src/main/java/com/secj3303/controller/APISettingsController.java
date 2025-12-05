package com.secj3303.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.APIKey;

@Controller
@RequestMapping("/api")
public class APISettingsController {

    private static final String KEYS_SESSION_KEY = "apiKeys";
    private static final String DEFAULT_VIEW = "api";

    // --- Session Access Utility ---

    private List<APIKey> getApiKeys(HttpSession session) {
        List<APIKey> keys = (List<APIKey>) session.getAttribute(KEYS_SESSION_KEY);
        if (keys == null) {
            keys = initializeMockKeys();
            session.setAttribute(KEYS_SESSION_KEY, keys);
        }
        return keys;
    }

    private List<APIKey> initializeMockKeys() {
        return new ArrayList<>(Arrays.asList(
            // USE FAKE/PLACEHOLDER KEYS - NOT REAL ONES!
            new APIKey(1, "AI Coach Service", 
                      "sk_test_" + UUID.randomUUID().toString().substring(0, 24), // Fake OpenAI key
                      "OpenAI GPT-4", "active", "Oct 1, 2025", "2 hours ago"),
            
            new APIKey(2, "Email Notifications", 
                      "SG.test" + UUID.randomUUID().toString().substring(0, 20), // Fake SendGrid key
                      "SendGrid", "active", "Sep 15, 2025", "5 minutes ago"),
            
            new APIKey(3, "Video Call Integration", 
                      "app_test_" + UUID.randomUUID().toString().substring(0, 20), // Fake Zoom key
                      "Zoom API", "active", "Aug 20, 2025", "1 day ago"),
            
            new APIKey(4, "SMS Alerts", 
                      "AC" + UUID.randomUUID().toString().substring(0, 32).toUpperCase(), // Fake Twilio key
                      "Twilio", "inactive", "Jul 10, 2025", "1 week ago"),
            
            new APIKey(5, "Analytics Service", 
                      "UA-test-" + UUID.randomUUID().toString().substring(0, 8), // Fake GA key
                      "Google Analytics", "active", "Jun 5, 2025", "30 minutes ago")
        ));
    }

    // --- 1. Main View (Lists keys) ---

    @GetMapping
    public String apiSettings(HttpSession session, Model model) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("apiKeys", getApiKeys(session));
        model.addAttribute("isAddingKey", false);
        return "app-layout";
    }
    
    // --- 2. Add Key Form View ---
    
    @GetMapping("/add")
    public String addKeyForm(Model model) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("isAddingKey", true);
        // Provide a blank object for the form binding
        model.addAttribute("newKeyData", new APIKey()); 
        return "app-layout";
    }

    // --- 3. Key Actions ---

    @PostMapping("/add")
    public String handleAddKey(@ModelAttribute APIKey newKeyData, HttpSession session, RedirectAttributes redirect) {
        if (newKeyData.getName() == null || newKeyData.getName().trim().isEmpty() ||
            newKeyData.getKey() == null || newKeyData.getKey().trim().isEmpty() ||
            newKeyData.getService() == null || newKeyData.getService().trim().isEmpty()) {
            
            redirect.addFlashAttribute("errorMessage", "All fields are required to add a new API key.");
            redirect.addFlashAttribute("showError", true);
            // Redirect back to the add form, passing the invalid data
            return "redirect:/api/add";
        }
        
        List<APIKey> keys = getApiKeys(session);
        AtomicInteger maxId = new AtomicInteger(keys.stream().mapToInt(APIKey::getId).max().orElse(0));
        
        newKeyData.setId(maxId.incrementAndGet());
        newKeyData.setStatus("active");
        keys.add(newKeyData);
        session.setAttribute(KEYS_SESSION_KEY, keys);

        redirect.addFlashAttribute("successMessage", "API key \"" + newKeyData.getName() + "\" has been added successfully.");
        redirect.addFlashAttribute("showSuccess", true);
        return "redirect:/api";
    }

    @PostMapping("/delete/{id}")
    public String handleDeleteKey(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        List<APIKey> keys = getApiKeys(session);
        Optional<APIKey> keyOpt = keys.stream().filter(k -> k.getId() == id).findFirst();
        
        if (keyOpt.isPresent()) {
            String keyName = keyOpt.get().getName();
            keys.removeIf(k -> k.getId() == id);
            session.setAttribute(KEYS_SESSION_KEY, keys);
            redirect.addFlashAttribute("successMessage", "API key \"" + keyName + "\" has been successfully deleted.");
            redirect.addFlashAttribute("showSuccess", true);
        }
        return "redirect:/api";
    }

    @PostMapping("/toggle/{id}")
    public String handleToggleStatus(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        List<APIKey> keys = getApiKeys(session);
        
        keys.stream().filter(k -> k.getId() == id).findFirst().ifPresent(key -> {
            String newStatus = key.isActive() ? "inactive" : "active";
            key.setStatus(newStatus);
            redirect.addFlashAttribute("successMessage", "API key \"" + key.getName() + "\" has been " + (key.isActive() ? "activated" : "deactivated") + ".");
            redirect.addFlashAttribute("showSuccess", true);
        });
        session.setAttribute(KEYS_SESSION_KEY, keys);
        return "redirect:/api";
    }
    
    @PostMapping("/regenerate/{id}")
    public String handleRegenerateKey(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        List<APIKey> keys = getApiKeys(session);
        
        keys.stream().filter(k -> k.getId() == id).findFirst().ifPresent(key -> {
            // Generate fake key - USE test prefix
            String newKey = "sk_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 30);
            key.setKey(newKey);
            key.setLastUsed("Never");
            
            redirect.addFlashAttribute("successMessage", "API key \"" + key.getName() + "\" has been regenerated successfully.");
            redirect.addFlashAttribute("showSuccess", true);
        });
        session.setAttribute(KEYS_SESSION_KEY, keys);
        return "redirect:/api";
    }
}