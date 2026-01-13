package com.secj3303.controller;

import java.util.*;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.secj3303.model.Referral;
import com.secj3303.model.Student;
import com.secj3303.dao.StudentDao;
import com.secj3303.dao.ReferralDao;

@Controller
@RequestMapping("/referral")
public class ReferralController {

    private static final String DEFAULT_VIEW = "referral";
    
    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private ReferralDao referralDao;
    
    private static final List<String> REASONS = Arrays.asList(
        "Academic Performance Decline", "Attendance Issues", "Behavioral Changes", 
        "Social Withdrawal", "Emotional Distress", "Health Concerns", "Other"
    );

    @GetMapping
    public String referralDashboard(@RequestParam(required = false) Integer studentId, Model model, HttpSession session) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Mock Session User if not present (Keep this for login simulation)
        if (session.getAttribute("currentUser") == null) {
            Map<String, String> faculty = new HashMap<>();
            faculty.put("name", "Dr. Sarah Miller");
            faculty.put("role", "Faculty");
            session.setAttribute("currentUser", faculty);
        }

        // 1. Fetch real students from Database
        List<Student> students = studentDao.findAll();
        model.addAttribute("students", students);
        
        model.addAttribute("reasons", REASONS);
        
        // 2. Fetch real referrals from Database
        List<Referral> referrals = referralDao.findAll();
        model.addAttribute("referrals", referrals);

        // 3. Handle Form Selection
        if (studentId != null) {
            Optional<Student> studentOpt = studentDao.findById(studentId);
            if (studentOpt.isPresent()) {
                model.addAttribute("selectedStudent", studentOpt.get());
                model.addAttribute("showForm", true);
                
                if (!model.containsAttribute("formData")) {
                    Referral form = new Referral();
                    // Pre-fill the temporary ID so we can find the student on submit
                    form.setStudentId(studentOpt.get().getStudentId()); 
                    model.addAttribute("formData", form);
                }
            }
        }
        return "app-layout";
    }

    @PostMapping("/submit")
    public String submitReferral(@RequestParam String studentId, 
                                 @ModelAttribute("formData") Referral formData, 
                                 HttpSession session, 
                                 RedirectAttributes redirect) {
        
        // 1. Find the actual student entity
        Optional<Student> studentOpt = studentDao.findByStudentId(studentId);
        
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            
            // 2. Link Student to Referral
            formData.setStudent(student);
            
            // 3. Set metadata
            Map<String, String> user = (Map<String, String>) session.getAttribute("currentUser");
            if (user != null) {
                formData.setSubmittedBy(user.get("name"));
            }
            
            // 4. Save to Database
            referralDao.save(formData);
            
            redirect.addFlashAttribute("showSuccess", true);
        } else {
            // Handle case where student ID doesn't exist
            redirect.addFlashAttribute("error", "Student not found.");
        }
        
        return "redirect:/referral";
    }
}