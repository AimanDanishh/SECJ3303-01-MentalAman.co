package com.secj3303.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.Referral;
import com.secj3303.model.Student;

@Controller
@RequestMapping("/referral")
public class ReferralController {

    private static final String REFERRALS_KEY = "submittedReferrals";
    private static final String DEFAULT_VIEW = "referral";
    private static final List<String> REASONS = Arrays.asList(
        "Academic Performance Decline", "Attendance Issues", "Behavioral Changes", 
        "Social Withdrawal", "Emotional Distress", "Health Concerns", "Other"
    );
    
    // --- Mock Data Setup ---

    private List<Student> getMockStudents() {
        return Arrays.asList(
            new Student(1, "Emma Wilson", "emma.wilson@university.edu", "S2021001", "Computer Science", "Year 3", "B-", 78, "2 days ago"),
            new Student(2, "Michael Chen", "michael.chen@university.edu", "S2021002", "Engineering", "Year 2", "C", 65, "1 week ago"),
            new Student(3, "Sarah Johnson", "sarah.johnson@university.edu", "S2021003", "Psychology", "Year 4", "A", 95, "1 hour ago"),
            new Student(4, "David Martinez", "david.martinez@university.edu", "S2021004", "Business", "Year 1", "C+", 72, "4 days ago"),
            new Student(5, "Olivia Brown", "olivia.brown@university.edu", "S2021005", "Medicine", "Year 3", "B+", 88, "1 day ago")
        );
    }
    
    private List<Referral> getReferrals(HttpSession session) {
        List<Referral> referrals = (List<Referral>) session.getAttribute(REFERRALS_KEY);
        if (referrals == null) {
            referrals = new ArrayList<>(Arrays.asList(
                new Referral(1, "John Smith", "S2021010", "Academic Performance", "Significant drop in assignment quality over the past month", "medium", "Prof. Anderson", "Nov 10, 2025", "in-progress"),
                new Referral(2, "Lisa Chen", "S2021015", "Attendance Issues", "Missed 5 consecutive classes without explanation", "high", "Dr. Johnson", "Nov 8, 2025", "reviewed")
            ));
            session.setAttribute(REFERRALS_KEY, referrals);
        }
        return referrals;
    }

    // --- Main View and Selection Handler ---

    @GetMapping
    public String referralDashboard(
        @RequestParam(required = false) Integer studentId,
        Model model, HttpSession session
    ) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("students", getMockStudents());
        model.addAttribute("referrals", getReferrals(session));
        model.addAttribute("reasons", REASONS);
        
        Optional<Student> studentOpt = Optional.empty();
        if (studentId != null) {
            studentOpt = getMockStudents().stream().filter(s -> s.getId() == studentId).findFirst();
        }

        if (studentOpt.isPresent()) {
            // Show form mode
            model.addAttribute("selectedStudent", studentOpt.get());
            model.addAttribute("showForm", true);
            // Provide a blank form object, or pre-populate if needed
            if (!model.containsAttribute("formData")) {
                 model.addAttribute("formData", new Referral());
            }
        } else {
            // Show selection mode or submitted referral list
            model.addAttribute("selectedStudent", null);
            model.addAttribute("showForm", false);
        }

        return "app-layout";
    }
    
    // --- Form Submission Handler ---

    @PostMapping("/submit")
    public String submitReferral(
        @RequestParam String studentId, // Student ID hidden field
        @ModelAttribute("formData") Referral formData, // Referral data
        HttpSession session, RedirectAttributes redirect
    ) {
        // Find the selected student again for final data assembly
        Optional<Student> studentOpt = getMockStudents().stream().filter(s -> s.getStudentId().equals(studentId)).findFirst();
        
        if (studentOpt.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Error: Student not found.");
            return "redirect:/referral";
        }
        
        // --- Validation Logic (Replicating handleSubmit) ---
        
        if (formData.getReason() == null || formData.getReason().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Please select a reason for referral.");
        } else if (formData.getObservations() == null || formData.getObservations().trim().length() < 20) {
            redirect.addFlashAttribute("errorMessage", "Observations must be at least 20 characters long.");
        }
        
        // If there's an error message, redirect back to the form
        if (redirect.getFlashAttributes().containsKey("errorMessage")) {
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("formData", formData); // Re-populate form data
            redirect.addAttribute("studentId", studentOpt.get().getId()); // Keep student selected
            return "redirect:/referral";
        }

        // Simulate submission failure (10% chance)
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            redirect.addFlashAttribute("errorMessage", "Failed to submit referral. Network error. Please try again.");
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("formData", formData); // Re-populate form data
            redirect.addAttribute("studentId", studentOpt.get().getId());
            return "redirect:/referral";
        }

        // --- Successful Submission ---
        
        List<Referral> referrals = getReferrals(session);
        AtomicInteger maxId = new AtomicInteger(referrals.stream().mapToInt(Referral::getId).max().orElse(0));

        // Create the final Referral object
        Referral newReferral = new Referral();
        newReferral.setId(maxId.incrementAndGet());
        newReferral.setStudentName(studentOpt.get().getName());
        newReferral.setStudentId(studentOpt.get().getStudentId());
        newReferral.setReason(formData.getReason());
        newReferral.setObservations(formData.getObservations());
        newReferral.setUrgency(formData.getUrgency());
        newReferral.setAdditionalNotes(formData.getAdditionalNotes());
        newReferral.setSubmittedBy("Current Faculty");
        newReferral.setStatus("pending"); 
        
        referrals.add(0, newReferral); // Add to the top
        session.setAttribute(REFERRALS_KEY, referrals);

        redirect.addFlashAttribute("showSuccess", true);
        return "redirect:/referral";
    }
}