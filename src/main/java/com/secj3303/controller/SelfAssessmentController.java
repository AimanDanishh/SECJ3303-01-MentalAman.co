package com.secj3303.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom; // Added missing Question import

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // Import HashMap for map initialization
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.AssessmentModels; // Import Map
import com.secj3303.model.AssessmentModels.Assessment;
import com.secj3303.model.AssessmentModels.AssessmentResult;
import com.secj3303.model.AssessmentModels.Question;
import com.secj3303.model.AssessmentModels.StudentData;
import com.secj3303.service.AssessmentService;

@Controller
@RequestMapping("/assessment")
public class SelfAssessmentController {

    private static final String DEFAULT_VIEW = "assessment";
    private final AssessmentService assessmentService;
    
    // Session keys for Student State
    private static final String SELECTED_ASSESSMENT_KEY = "selectedAssessment";
    private static final String ANSWERS_KEY = "assessmentAnswers";
    private static final String HISTORY_KEY = "studentAssessmentHistory";
    private static final String SAVED_PROGRESS_KEY = "assessmentSavedProgress";

    // Session keys for Faculty/Counsellor State
    private static final String SELECTED_STUDENT_KEY = "selectedStudent";
    private static final String FACULTY_STUDENT_DATA_KEY = "facultyStudentData";
    
    public SelfAssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }
    
    // --- Utility to initialize student history (since there's no auth/db) ---
    private List<AssessmentResult> getStudentHistory(HttpSession session) {
        List<AssessmentResult> history = (List<AssessmentResult>) session.getAttribute(HISTORY_KEY);
        if (history == null) {
            // Mock initial history for the general student view
            history = AssessmentModels.ASSIGNED_STUDENTS.stream()
                .filter(s -> s.studentId.equals("S2021003")) // Mock student SJ's history
                .findFirst()
                .map(s -> new ArrayList<>(s.assessmentHistory))
                .orElse(new ArrayList<>());
            session.setAttribute(HISTORY_KEY, history);
        }
        return history;
    }
    
    // --- Utility to manage assigned students list for Faculty/Counsellor view ---
    private List<StudentData> getAssignedStudents(HttpSession session) {
        List<StudentData> students = (List<StudentData>) session.getAttribute(FACULTY_STUDENT_DATA_KEY);
        if (students == null) {
            students = AssessmentModels.ASSIGNED_STUDENTS;
            session.setAttribute(FACULTY_STUDENT_DATA_KEY, students);
        }
        return students;
    }


    // --- Main Dashboard/List View ---

    @GetMapping
    public String dashboard(
        @RequestParam(required = false) String userRole, // MOCK role
        @RequestParam(required = false) Integer selectStudentId, // Faculty/Counsellor flow
        @RequestParam(defaultValue = "assessments") String tab, // Student flow
        @RequestParam(required = false) String searchQuery,
        @RequestParam(defaultValue = "all") String filterRisk,
        Model model, HttpSession session
    ) {
        // MOCK role setting (use authenticated user role in real app)
        model.addAttribute("userRole", userRole != null ? userRole : "student"); 
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("activeTab", tab);
        
        // --- FACULTY/COUNSELLOR VIEW ---
        if ("faculty".equals(userRole) || "counsellor".equals(userRole)) {
            
            List<StudentData> students = getAssignedStudents(session);
            model.addAttribute("assignedStudents", students);
            model.addAttribute("filteredStudents", assessmentService.filterStudents(searchQuery, filterRisk));
            model.addAttribute("searchQuery", searchQuery == null ? "" : searchQuery);
            model.addAttribute("filterRisk", filterRisk);
            
            if (selectStudentId != null) {
                students.stream().filter(s -> s.id == selectStudentId).findFirst().ifPresent(student -> {
                    model.addAttribute("selectedStudent", student);
                });
            }
            return "app-layout";
        }
        
        // --- STUDENT VIEW (Default) ---
        
        // Reset assessment flow variables
        session.removeAttribute(SELECTED_ASSESSMENT_KEY);
        session.removeAttribute(ANSWERS_KEY);
        session.removeAttribute(SELECTED_STUDENT_KEY);

        model.addAttribute("assessments", assessmentService.getAllAssessments());
        model.addAttribute("pastResults", getStudentHistory(session));
        model.addAttribute("savedProgress", session.getAttribute(SAVED_PROGRESS_KEY));
        
        return "app-layout";
    }
    
    // --- Assessment Selection and Navigation ---
    
    @GetMapping("/start/{id}")
    public String startAssessment(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        Optional<Assessment> assessmentOpt = assessmentService.findAssessment(id);
        if (assessmentOpt.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Assessment not found.");
            redirect.addFlashAttribute("showError", true);
            return "redirect:/assessment";
        }
        
        Assessment assessment = assessmentOpt.get();
        session.setAttribute(SELECTED_ASSESSMENT_KEY, assessment);
        
        // Initialize answers and index (or load saved progress)
        Map<Integer, AssessmentModels.AssessmentAnswers> savedProgress = (Map<Integer, AssessmentModels.AssessmentAnswers>) session.getAttribute(SAVED_PROGRESS_KEY);
        
        // Use AssessmentAnswers class from AssessmentModels
        AssessmentModels.AssessmentAnswers answersObject;

        if (savedProgress != null && savedProgress.containsKey(id)) {
            AssessmentModels.AssessmentAnswers progress = savedProgress.get(id);
            answersObject = progress;
            redirect.addAttribute("q", progress.currentQuestionIndex);
        } else {
            answersObject = new AssessmentModels.AssessmentAnswers();
            // Initialize the map to avoid NullPointerException
            answersObject.answers = new HashMap<>(); 
        }

        session.setAttribute(ANSWERS_KEY, answersObject);

        return "redirect:/assessment/question";
    }
    
    @GetMapping("/question")
    public String displayQuestion(
        @RequestParam(defaultValue = "0") int q, // current question index
        Model model, HttpSession session, RedirectAttributes redirect
    ) {
        Assessment assessment = (Assessment) session.getAttribute(SELECTED_ASSESSMENT_KEY);
        AssessmentModels.AssessmentAnswers answersObject = (AssessmentModels.AssessmentAnswers) session.getAttribute(ANSWERS_KEY);

        if (assessment == null || answersObject == null || q < 0 || q >= assessment.questions.size()) {
            return "redirect:/assessment";
        }
        
        Question currentQuestion = assessment.questions.get(q);
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("assessment", assessment);
        model.addAttribute("currentQuestion", currentQuestion);
        model.addAttribute("currentQuestionIndex", q);
        
        // Pass the raw map to the view for access
        model.addAttribute("answers", answersObject.answers); 
        
        model.addAttribute("progress", ((q + 1) / (double) assessment.questions.size()) * 100);
        
        // Provide scale labels dynamically
        if ("scale".equals(currentQuestion.type)) {
             model.addAttribute("scaleLabels", assessmentService.getScaleLabels(currentQuestion.scaleMax));
        }

        return "app-layout";
    }
    
    // --- Assessment Submission/Navigation Handlers ---
    
    @PostMapping("/submit")
    public String handleSubmit(
        @RequestParam Map<String, String> formData,
        @RequestParam int currentQuestionIndex,
        @RequestParam int nextStep, // 1 for Next, -1 for Previous, 2 for Submit, 3 for Save
        HttpSession session, RedirectAttributes redirect
    ) {
        Assessment assessment = (Assessment) session.getAttribute(SELECTED_ASSESSMENT_KEY);
        AssessmentModels.AssessmentAnswers answersObject = (AssessmentModels.AssessmentAnswers) session.getAttribute(ANSWERS_KEY);
        
        if (assessment == null || answersObject == null) {
             return "redirect:/assessment";
        }
        
        Map<String, Integer> sessionAnswers = answersObject.answers; // Direct reference to the map

        // 1. Process submitted answer for current question
        Question currentQuestion = assessment.questions.get(currentQuestionIndex);
        String answerKey = "q_" + currentQuestion.id;
        
        if (formData.containsKey(answerKey)) {
            try {
                 int answerValue = Integer.parseInt(formData.get(answerKey));
                 // FIX: Insert String key and Integer value into the map
                 sessionAnswers.put(answerKey, answerValue); 
                 session.setAttribute(ANSWERS_KEY, answersObject); // Save the updated object back
            } catch (NumberFormatException e) {
                 // Error handled gracefully
            }
        }
        
        // 2. Check if answer is required for navigation
        boolean answered = sessionAnswers.containsKey(currentQuestion.id);

        if (nextStep == 1 && !answered) {
            redirect.addFlashAttribute("errorMessage", "Please answer the current question before moving forward.");
            redirect.addFlashAttribute("showError", true);
            return "redirect:/assessment/question?q=" + currentQuestionIndex;
        }

        // 3. Handle action (Submit, Next, Previous, Save)
        
        if (nextStep == 2) { 
            // FINAL SUBMISSION
            if (sessionAnswers.size() < assessment.questions.size()) {
                redirect.addFlashAttribute("errorMessage", "Please answer all questions before submitting.");
                redirect.addFlashAttribute("showError", true);
                return "redirect:/assessment/question?q=" + currentQuestionIndex;
            }
            
            // Simulate save failure (10% chance)
            if (ThreadLocalRandom.current().nextDouble() < 0.1) {
                 redirect.addFlashAttribute("errorMessage", "Failed to save your assessment report due to a database error.");
                 redirect.addFlashAttribute("showError", true);
                 return "redirect:/assessment/question?q=" + currentQuestionIndex;
            }

            AssessmentResult result = assessmentService.calculateScore(assessment, answersObject);
            
            // Save result to student history
            List<AssessmentResult> history = getStudentHistory(session);
            history.add(0, result);
            session.setAttribute(HISTORY_KEY, history);

            // Clean up session state and show results
            session.removeAttribute(SELECTED_ASSESSMENT_KEY);
            session.removeAttribute(ANSWERS_KEY);
            
            // Remove from saved progress
            Map<Integer, AssessmentModels.AssessmentAnswers> savedProgress = (Map<Integer, AssessmentModels.AssessmentAnswers>) session.getAttribute(SAVED_PROGRESS_KEY);
            if (savedProgress != null) {
                savedProgress.remove(assessment.id);
                session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
            }

            redirect.addFlashAttribute("assessmentScore", result.score);
            return "redirect:/assessment/results";
        } 
        
        else if (nextStep == 3) { 
            // SAVE PROGRESS
            Map<Integer, AssessmentModels.AssessmentAnswers> savedProgress = (Map<Integer, AssessmentModels.AssessmentAnswers>) session.getAttribute(SAVED_PROGRESS_KEY);
            if (savedProgress == null) {
                 savedProgress = new HashMap<>();
            }
            
            AssessmentModels.AssessmentAnswers progress = new AssessmentModels.AssessmentAnswers();
            progress.answers = sessionAnswers;
            progress.assessmentId = assessment.id;
            progress.currentQuestionIndex = currentQuestionIndex;

            savedProgress.put(assessment.id, progress);
            session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
            
            redirect.addFlashAttribute("alert", "✓ Progress saved! You can resume this assessment later.");
            redirect.addFlashAttribute("alertType", "success");
            return "redirect:/assessment";
        }
        
        else {
            // NEXT/PREVIOUS NAVIGATION
            int newIndex = currentQuestionIndex + nextStep;
            return "redirect:/assessment/question?q=" + newIndex;
        }
    }

    // --- Results View (Redirect target after submission) ---

    @GetMapping("/results")
    public String displayResults(Model model, HttpSession session, @ModelAttribute("assessmentScore") Integer assessmentScore) {
        if (assessmentScore == null) {
             return "redirect:/assessment";
        }
        
        // Retrieve the newest result from the session history
        List<AssessmentResult> history = getStudentHistory(session);
        AssessmentResult newestResult = history.get(0);

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("showResults", true);
        model.addAttribute("assessmentScore", newestResult.score);
        model.addAttribute("selectedReport", newestResult);
        model.addAttribute("userRole", "student");

        return "app-layout";
    }

    // --- Faculty/Counsellor Report View ---
    
    @GetMapping("/report/view/{studentId}/{reportId}")
    public String viewReport(
        @PathVariable int studentId,
        @PathVariable int reportId,
        Model model, HttpSession session
    ) {
        StudentData selectedStudent = getAssignedStudents(session).stream()
            .filter(s -> s.id == studentId).findFirst().orElse(null);

        if (selectedStudent == null) {
            return "redirect:/assessment";
        }
        
        AssessmentResult selectedReport = selectedStudent.assessmentHistory.stream()
            .filter(r -> r.id == reportId).findFirst().orElse(null);

        if (selectedReport == null) {
            // Redirect back to student detail view if report not found
            return "redirect:/assessment?selectStudentId=" + studentId;
        }

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("selectedReport", selectedReport);
        model.addAttribute("selectedStudent", selectedStudent); // Pass student for context
        model.addAttribute("showReportModal", true);
        model.addAttribute("userRole", "counsellor");

        return "app-layout";
    }
}