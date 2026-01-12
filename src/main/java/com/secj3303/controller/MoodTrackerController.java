package com.secj3303.controller;

import com.secj3303.dao.MoodEntryDao;
import com.secj3303.model.MoodEntry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/mood")
public class MoodTrackerController {

    private final MoodEntryDao moodEntryDao;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Constructor injection
    public MoodTrackerController(MoodEntryDao moodEntryDao) {
        this.moodEntryDao = moodEntryDao;
    }
    
    // --- Main View Handler ---
    @GetMapping
    public String moodTrackerDashboard(
        @RequestParam(defaultValue = "entry") String view,
        @RequestParam(required = false) Integer editId,
        Model model,
        Authentication authentication
    ) {
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Get user from Spring Security authentication
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Get user role from authorities
        String userRole = "student"; // Default role
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String authority = authentication.getAuthorities().iterator().next().getAuthority();
            userRole = authority.replace("ROLE_", "").toLowerCase();
        }
        
        // Store user in model
        model.addAttribute("user", username);
        model.addAttribute("userRole", userRole);
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activeTab", "mood");
        
        // Get all mood entries for the user
        List<MoodEntry> entries = moodEntryDao.findByUsername(username);
        
        // --- Calculate Stats ---
        Map<String, Object> moodStats = MoodEntry.getMoodStats(entries);
        model.addAttribute("currentStreak", MoodEntry.calculateStreak(entries));
        model.addAttribute("totalEntries", entries.size());
        
        // Add all stats to model
        if (moodStats != null) {
            model.addAttribute("last7Days", moodStats.get("last7Days"));
            model.addAttribute("moodCounts", moodStats.get("moodCounts"));
            model.addAttribute("mostFrequentMoodId", moodStats.get("mostFrequentMoodId"));
            model.addAttribute("totalEntriesLast7Days", moodStats.get("totalEntriesLast7Days"));
            model.addAttribute("mostFrequentCount", moodStats.get("mostFrequentCount"));
            model.addAttribute("mostFrequentMoodData", moodStats.get("mostFrequentMoodData"));
        } else {
            // Provide default values
            model.addAttribute("last7Days", new ArrayList<MoodEntry>());
            model.addAttribute("moodCounts", new HashMap<String, Integer>());
            model.addAttribute("mostFrequentMoodId", "neutral");
            model.addAttribute("totalEntriesLast7Days", 0);
            model.addAttribute("mostFrequentCount", 0);
            model.addAttribute("mostFrequentMoodData", new HashMap<String, String>());
        }

        // --- View State ---
        model.addAttribute("currentView", "assessment");
        model.addAttribute("view", view);
        model.addAttribute("moodEntries", entries);
        model.addAttribute("moodDefinitions", MoodEntry.MOOD_DEFINITIONS);
        
        // --- Handle Edit/Create Form State ---
        boolean isEditing = false;
        MoodEntry editingEntry = null;
        MoodEntry formData = new MoodEntry();
        
        if (editId != null) {
            Optional<MoodEntry> entryOpt = moodEntryDao.findById(editId);
            if (entryOpt.isPresent() && entryOpt.get().getUsername().equals(username)) {
                isEditing = true;
                editingEntry = entryOpt.get();
                formData = editingEntry;
            }
        }
        
        model.addAttribute("isEditing", isEditing);
        model.addAttribute("editingEntry", editingEntry);
        model.addAttribute("formData", formData);

        return "mood-tracker";
    }

    // --- Form Submission Handler ---
    @PostMapping("/submit")
    public String handleSubmit(
        @ModelAttribute MoodEntry formData,
        RedirectAttributes redirect,
        Authentication authentication
    ) {
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Get current user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // Set username on the form data
        formData.setUsername(username);
        
        // --- Validation ---
        String errorMessage = null;
        
        if (formData.getMood() == null || formData.getMood().isEmpty()) {
            errorMessage = "Please select a mood.";
        } else if (formData.getDate() == null || formData.getDate().isEmpty()) {
            errorMessage = "Please select a date.";
        } else if (formData.getNotes() != null && formData.getNotes().length() > 500) {
            errorMessage = "Notes are too long (max 500 chars).";
        }
        
        // Parse the date
        LocalDate entryDate = null;
        if (formData.getDate() != null && !formData.getDate().isEmpty()) {
            try {
                entryDate = LocalDate.parse(formData.getDate());
            } catch (Exception e) {
                errorMessage = "Invalid date format.";
            }
        }
        
        // Check for existing entry on this date if not editing
        if (formData.getId() == null && entryDate != null) {
            boolean exists = moodEntryDao.existsByUsernameAndDate(username, entryDate);
            if (exists) {
                errorMessage = "You already have a mood entry for this date. Please edit the existing entry or choose a different date.";
            }
        }
        
        // Check if editing someone else's entry
        if (formData.getId() != null) {
            Optional<MoodEntry> existing = moodEntryDao.findById(formData.getId());
            if (existing.isPresent() && !existing.get().getUsername().equals(username)) {
                errorMessage = "You can only edit your own mood entries.";
            }
        }

        if (errorMessage != null) {
            redirect.addFlashAttribute("alert", errorMessage);
            redirect.addFlashAttribute("alertType", "error");
            redirect.addFlashAttribute("formData", formData);
            if (formData.getId() != null) {
                redirect.addAttribute("editId", formData.getId());
            }
            return "redirect:/mood";
        }
        
        // --- Save/Update Logic ---
        String successMessage;
        
        if (formData.getId() != null) {
            // Update existing entry
            Optional<MoodEntry> existingOpt = moodEntryDao.findById(formData.getId());
            if (existingOpt.isPresent()) {
                MoodEntry existing = existingOpt.get();
                existing.setMood(formData.getMood());
                existing.setNotes(formData.getNotes());
                existing.setEntryDate(entryDate);
                existing.setTimestamp(LocalDateTime.now());
                moodEntryDao.update(existing);
                successMessage = "Mood entry updated successfully!";
            } else {
                redirect.addFlashAttribute("alert", "Entry not found.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/mood";
            }
        } else {
            // Create new entry
            formData.setEntryDate(entryDate);
            formData.setTimestamp(LocalDateTime.now());
            moodEntryDao.save(formData);
            successMessage = "Mood entry saved successfully!";
        }

        redirect.addFlashAttribute("alert", successMessage);
        redirect.addFlashAttribute("alertType", "success");
        return "redirect:/mood";
    }
    
    // --- Delete Handler ---
    @PostMapping("/delete/{id}")
    public String handleDelete(
        @PathVariable Integer id,
        RedirectAttributes redirect,
        Authentication authentication
    ) {
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Get current user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // Check if the entry belongs to the user
        Optional<MoodEntry> entryOpt = moodEntryDao.findById(id);
        if (entryOpt.isPresent() && entryOpt.get().getUsername().equals(username)) {
            boolean deleted = moodEntryDao.delete(id);
            if (deleted) {
                redirect.addFlashAttribute("alert", "Mood entry deleted successfully.");
                redirect.addFlashAttribute("alertType", "success");
            } else {
                redirect.addFlashAttribute("alert", "Entry could not be deleted.");
                redirect.addFlashAttribute("alertType", "error");
            }
        } else {
            redirect.addFlashAttribute("alert", "Entry not found or you don't have permission to delete it.");
            redirect.addFlashAttribute("alertType", "error");
        }
        
        redirect.addAttribute("view", "history");
        return "redirect:/mood";
    }
}