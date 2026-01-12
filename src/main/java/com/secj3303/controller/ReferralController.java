package com.secj3303.controller;

import java.util.*;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.secj3303.model.Referral;
import com.secj3303.model.Student;

@Controller
@RequestMapping("/referral")
public class ReferralController {

    private static final String REFERRALS_KEY = "submittedReferrals";
    private static final String DEFAULT_VIEW = "referral";
    
    // REASONS list matches dropdown options
    private static final List<String> REASONS = Arrays.asList(
        "Academic Performance Decline", "Attendance Issues", "Behavioral Changes", 
        "Social Withdrawal", "Emotional Distress", "Health Concerns", "Other"
    );

    private List<Student> getMockStudents() {
        Student s1 = new Student("Emma Wilson", "emma.wilson@uni.edu", "S2021001", "Computer Science", "Year 3", "B-", 78, "2 days ago");
        s1.setId(1);
        Student s2 = new Student("Michael Chen", "michael.chen@uni.edu", "S2021002", "Engineering", "Year 2", "C", 65, "1 week ago");
        s2.setId(2);
        return Arrays.asList(s1, s2);
    }

    @GetMapping
    public String referralDashboard(@RequestParam(required = false) Integer studentId, Model model, HttpSession session) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Mock Session User if not present
        if (session.getAttribute("currentUser") == null) {
            Map<String, String> faculty = new HashMap<>();
            faculty.put("name", "Dr. Sarah Miller");
            faculty.put("role", "Faculty");
            session.setAttribute("currentUser", faculty);
        }

        model.addAttribute("students", getMockStudents());
        model.addAttribute("reasons", REASONS);
        
        // Handle Session-based Referral History
        List<Referral> referrals = (List<Referral>) session.getAttribute(REFERRALS_KEY);
        if (referrals == null) {
            referrals = new ArrayList<>(Arrays.asList(
                new Referral(1, "John Smith", "S2021010", "Academic Performance", "Significant drop in quality.", "MEDIUM", "Prof. Anderson", "Nov 10", "in-progress"),
                new Referral(2, "Lisa Chen", "S2021015", "Attendance Issues", "Missed 5 classes.", "HIGH", "Dr. Johnson", "Nov 8", "reviewed")
            ));
            session.setAttribute(REFERRALS_KEY, referrals);
        }
        model.addAttribute("referrals", referrals);

        // FORM LOGIC
        if (studentId != null) {
            Optional<Student> student = getMockStudents().stream().filter(s -> s.getId() == studentId).findFirst();
            if (student.isPresent()) {
                model.addAttribute("selectedStudent", student.get());
                model.addAttribute("showForm", true);
                if (!model.containsAttribute("formData")) {
                    Referral form = new Referral();
                    form.setUrgency("medium"); // Default to Medium as seen in image_f56089.png
                    model.addAttribute("formData", form);
                }
            }
        }
        return "app-layout";
    }

    @PostMapping("/submit")
    public String submitReferral(@RequestParam String studentId, @ModelAttribute("formData") Referral formData, HttpSession session, RedirectAttributes redirect) {
        List<Referral> history = (List<Referral>) session.getAttribute(REFERRALS_KEY);
        
        // Simplified Logic: Get student name from mock list
        String name = getMockStudents().stream()
                        .filter(s -> s.getStudentId().equals(studentId))
                        .map(Student::getName).findFirst().orElse("Unknown");

        formData.setStudentName(name);
        formData.setStudentId(studentId);
        formData.setSubmittedDate("Just now");
        formData.setStatus("pending");
        
        history.add(0, formData);
        session.setAttribute(REFERRALS_KEY, history);
        
        redirect.addFlashAttribute("showSuccess", true);
        return "redirect:/referral";
    }
}