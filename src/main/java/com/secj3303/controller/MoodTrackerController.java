package com.secj3303.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
    private static final String DEFAULT_VIEW = "mood";
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
        List<MoodEntry> entries = getMoodEntries(session);
        
        // Sort entries by date descending for history and streak calculation
        entries.sort(Comparator.comparing(MoodEntry::getDate).reversed());

        // --- Calculate Stats ---
        Map<String, Object> moodStats = MoodEntry.getMoodStats(entries);
        model.addAttribute("currentStreak", MoodEntry.calculateStreak(entries));
        model.addAttribute("totalEntries", entries.size());
        model.addAllAttributes(moodStats);
        
        // --- View State ---
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("view", view);
        model.addAttribute("moodEntries", entries);
        model.addAttribute("moodDefinitions", MoodEntry.MOOD_DEFINITIONS);
        
        // --- Handle Edit/Create Form State ---
        model.addAttribute("isEditing", false);
        model.addAttribute("editingEntry", null);
        model.addAttribute("formData", new MoodEntry());
        
        if (editId != null) {
            Optional<MoodEntry> entryOpt = entries.stream().filter(e -> e.getId() == editId).findFirst();
            if (entryOpt.isPresent()) {
                model.addAttribute("isEditing", true);
                model.addAttribute("editingEntry", entryOpt.get());
                model.addAttribute("formData", entryOpt.get());
                model.addAttribute("view", "entry");
            }
        }
        
        // If formData was added by redirect (error case), use it
        if (!model.containsAttribute("formData")) {
            model.addAttribute("formData", new MoodEntry());
        } else if (model.getAttribute("isEditing") != null && (Boolean) model.getAttribute("isEditing")) {
            // If editing mode was intended but failed validation, ensure entry is set
            model.addAttribute("editingEntry", entries.stream().filter(e -> e.getId() == editId).findFirst().orElse(null));
        }

        return "app-layout";
    }

    // --- Form Submission Handler ---

    @PostMapping("/submit")
    public String handleSubmit(@ModelAttribute MoodEntry formData, HttpSession session, RedirectAttributes redirect) {
        List<MoodEntry> entries = getMoodEntries(session);
        
        // --- Validation ---
        if (formData.getMood() == null || formData.getMood().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Please select a mood.");
        } else if (formData.getDate() == null || formData.getDate().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Please select a date.");
        } else if (formData.getNotes() == null || formData.getNotes().length() > 500) {
            redirect.addFlashAttribute("errorMessage", "Notes are too long (max 500 chars).");
        }
        
        // Check for existing entry on this date if not editing
        if (formData.getId() == 0) {
             boolean exists = entries.stream().anyMatch(e -> e.getDate().equals(formData.getDate()));
             if (exists) {
                 redirect.addFlashAttribute("errorMessage", "You already have a mood entry for this date. Please edit the existing entry or choose a different date.");
             }
        }

        if (redirect.getFlashAttributes().containsKey("errorMessage")) {
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
        
        if (formData.getId() != 0) {
            // Update existing entry
            entries.stream().filter(e -> e.getId() == formData.getId()).findFirst().ifPresent(entry -> {
                entry.setMood(formData.getMood());
                entry.setNotes(formData.getNotes());
                entry.setDate(formData.getDate());
                entry.setTimestamp(LocalDateTime.now().toString());
            });
            redirect.addFlashAttribute("successMessage", "Mood entry updated successfully!");
        } else {
            // Create new entry
            AtomicInteger maxId = new AtomicInteger(entries.stream().mapToInt(MoodEntry::getId).max().orElse(0));
            formData.setId(maxId.incrementAndGet());
            formData.setTimestamp(LocalDateTime.now().toString());
            entries.add(0, formData);
            redirect.addFlashAttribute("successMessage", "Mood entry saved successfully!");
        }

        session.setAttribute(HISTORY_KEY, entries);
        redirect.addFlashAttribute("showSuccess", true);
        return "redirect:/mood";
    }
    
    // --- Delete Handler ---

    @PostMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        List<MoodEntry> entries = getMoodEntries(session);
        
        entries.removeIf(e -> e.getId() == id);
        session.setAttribute(HISTORY_KEY, entries);
        
        redirect.addFlashAttribute("successMessage", "Mood entry deleted successfully.");
        redirect.addFlashAttribute("showSuccess", true);
        redirect.addAttribute("view", "history"); // Return to history view
        
        return "redirect:/mood";
    }
}