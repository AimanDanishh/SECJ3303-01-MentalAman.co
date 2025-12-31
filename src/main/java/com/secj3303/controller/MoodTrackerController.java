package com.secj3303.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.MoodEntry;

@Controller
@RequestMapping("/mood")
public class MoodTrackerController {

    private static final String HISTORY_KEY = "moodEntries";
    private static final String DEFAULT_VIEW = "assessment";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private List<MoodEntry> getMoodEntries(HttpSession session) {
        List<MoodEntry> entries = (List<MoodEntry>) session.getAttribute(HISTORY_KEY);
        if (entries == null) {
            entries = MoodEntry.getInitialMoodEntries();
            session.setAttribute(HISTORY_KEY, entries);
        }
        return entries;
    }
    
    // --- Main View Handler ---
    @GetMapping
    public String moodTrackerDashboard(
        @RequestParam(defaultValue = "entry") String view,
        @RequestParam(required = false) Integer editId,
        Model model, HttpSession session
    ) {
        // Get user from session
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return "redirect:/login";
        }

        model.addAttribute("activeTab", "mood");
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        List<MoodEntry> entries = getMoodEntries(session);
        
        // Sort entries by date descending for history and streak calculation
        entries.sort((e1, e2) -> {
            try {
                return e2.getDate().compareTo(e1.getDate());
            } catch (Exception ex) {
                return 0;
            }
        });

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
            // Provide default values if stats calculation fails
            model.addAttribute("last7Days", new ArrayList<MoodEntry>());  // Empty list
            model.addAttribute("moodCounts", new HashMap<String, Integer>());
            model.addAttribute("mostFrequentMoodId", "neutral");
            model.addAttribute("totalEntriesLast7Days", 0);  // Set to 0, not entries.size()
            model.addAttribute("mostFrequentCount", 0);
            model.addAttribute("mostFrequentMoodData", new HashMap<String, String>());
        }

        // --- View State ---
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("view", view);
        model.addAttribute("moodEntries", entries);
        model.addAttribute("moodDefinitions", MoodEntry.MOOD_DEFINITIONS);
        
        // --- Handle Edit/Create Form State ---
        boolean isEditing = false;
        MoodEntry editingEntry = null;
        MoodEntry formData = new MoodEntry();
        
        if (editId != null) {
            Optional<MoodEntry> entryOpt = entries.stream().filter(e -> e.getId() == editId).findFirst();
            if (entryOpt.isPresent()) {
                isEditing = true;
                editingEntry = entryOpt.get();
                formData = editingEntry;
            }
        }
        
        model.addAttribute("isEditing", isEditing);
        model.addAttribute("editingEntry", editingEntry);
        model.addAttribute("formData", formData);
        
        // Add user to model
        model.addAttribute("user", userObj);

        // Debug: Print what's being passed to template
System.out.println("DEBUG - totalEntriesLast7Days: " + model.getAttribute("totalEntriesLast7Days"));
System.out.println("DEBUG - moodCounts: " + model.getAttribute("moodCounts"));
System.out.println("DEBUG - view: " + view);

// Make sure the view attribute is set
model.addAttribute("view", view);

        return "mood-tracker";
    }

    // --- Form Submission Handler ---
    @PostMapping("/submit")
    public String handleSubmit(@ModelAttribute MoodEntry formData, HttpSession session, RedirectAttributes redirect) {
        List<MoodEntry> entries = getMoodEntries(session);
        
        // --- Validation ---
        String errorMessage = null;
        
        if (formData.getMood() == null || formData.getMood().isEmpty()) {
            errorMessage = "Please select a mood.";
        } else if (formData.getDate() == null || formData.getDate().isEmpty()) {
            errorMessage = "Please select a date.";
        } else if (formData.getNotes() != null && formData.getNotes().length() > 500) {
            errorMessage = "Notes are too long (max 500 chars).";
        }
        
        // Check for existing entry on this date if not editing
        if (formData.getId() == 0 && entries.stream().anyMatch(e -> e.getDate().equals(formData.getDate()))) {
            errorMessage = "You already have a mood entry for this date. Please edit the existing entry or choose a different date.";
        }

        if (errorMessage != null) {
            redirect.addFlashAttribute("errorMessage", errorMessage);
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("formData", formData);
            if (formData.getId() != 0) {
                redirect.addAttribute("editId", formData.getId());
            }
            return "redirect:/mood";
        }
        
        // Simulate database error (10% chance)
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            redirect.addFlashAttribute("errorMessage", "Your mood entry couldn't be saved. Please try again later.");
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("formData", formData);
            if (formData.getId() != 0) {
                redirect.addAttribute("editId", formData.getId());
            }
            return "redirect:/mood";
        }
        
        // --- Save/Update Logic ---
        String successMessage;
        
        if (formData.getId() != 0) {
            // Update existing entry
            entries.stream()
                .filter(e -> e.getId() == formData.getId())
                .findFirst()
                .ifPresent(entry -> {
                    entry.setMood(formData.getMood());
                    entry.setNotes(formData.getNotes());
                    entry.setDate(formData.getDate());
                    entry.setTimestamp(LocalDateTime.now().toString());
                });
            successMessage = "Mood entry updated successfully!";
        } else {
            // Create new entry
            AtomicInteger maxId = new AtomicInteger(entries.stream()
                .mapToInt(MoodEntry::getId)
                .max()
                .orElse(0));
            formData.setId(maxId.incrementAndGet());
            formData.setTimestamp(LocalDateTime.now().toString());
            entries.add(0, formData);
            successMessage = "Mood entry saved successfully!";
        }

        session.setAttribute(HISTORY_KEY, entries);
        redirect.addFlashAttribute("successMessage", successMessage);
        redirect.addFlashAttribute("showSuccess", true);
        return "redirect:/mood";
    }
    
    // --- Delete Handler ---
    @PostMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        List<MoodEntry> entries = getMoodEntries(session);
        
        boolean removed = entries.removeIf(e -> e.getId() == id);
        
        if (removed) {
            session.setAttribute(HISTORY_KEY, entries);
            redirect.addFlashAttribute("successMessage", "Mood entry deleted successfully.");
            redirect.addFlashAttribute("showSuccess", true);
        } else {
            redirect.addFlashAttribute("errorMessage", "Entry not found or could not be deleted.");
            redirect.addFlashAttribute("showError", true);
        }
        
        redirect.addAttribute("view", "history");
        return "redirect:/mood";
    }
}