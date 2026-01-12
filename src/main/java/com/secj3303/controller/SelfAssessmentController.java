package com.secj3303.controller;

import com.secj3303.model.*;
import com.secj3303.service.AssessmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/assessment")
public class SelfAssessmentController {

    private static final String DEFAULT_VIEW = "assessment";
    private final AssessmentService assessmentService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Session keys
    private static final String SELECTED_ASSESSMENT_ID_KEY = "selectedAssessmentId";
    private static final String ANSWERS_KEY = "assessmentAnswers";
    private static final String CURRENT_QUESTION_INDEX_KEY = "currentQuestionIndex";
    private static final String SAVED_PROGRESS_KEY = "assessmentSavedProgress";
    private static final String SELECTED_STUDENT_ID_KEY = "selectedStudentId";
    
    public SelfAssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }
    
    // Build user from authentication
    private User buildUser(Authentication authentication) {
        User user = new User();
        user.setEmail(authentication.getName());
        user.setName(authentication.getName().split("@")[0]);
        user.setRole(
            authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "")
                .toLowerCase()
        );
        return user;
    }
    
    // Get current student from database - AUTO-CREATE if not found
    private Optional<Student> getCurrentStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        String email = authentication.getName();
        Optional<Student> studentOpt = assessmentService.getStudentByEmail(email);
        
        // AUTO-CREATE STUDENT if not found (for demo/testing)
        if (studentOpt.isEmpty()) {
            Student newStudent = new Student();
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            
            // Create a proper name from email
            String[] nameParts = username.split("\\.");
            StringBuilder nameBuilder = new StringBuilder();
            for (String part : nameParts) {
                if (!part.isEmpty()) {
                    nameBuilder.append(Character.toUpperCase(part.charAt(0)))
                               .append(part.substring(1)).append(" ");
                }
            }
            String studentName = nameBuilder.toString().trim();
            if (studentName.isEmpty()) studentName = "Student User";
            
            newStudent.setName(studentName);
            newStudent.setEmail(email);
            newStudent.setStudentId("S" + System.currentTimeMillis() % 1000000);
            newStudent.setDepartment("General Studies");
            newStudent.setYear("Year 1");
            newStudent.setCurrentGrade("B");
            newStudent.setAttendance(85);
            newStudent.setLastActivity(java.time.LocalDate.now().toString());
            newStudent.setRiskLevel("low");
            
            Student savedStudent = assessmentService.saveStudent(newStudent);
            return Optional.of(savedStudent);
        }
        
        return studentOpt;
    }
    
    // Main Dashboard
    @GetMapping
    public String dashboard(
        @RequestParam(required = false) Integer selectStudentId,
        @RequestParam(defaultValue = "assessments") String tab,
        @RequestParam(required = false) String searchQuery,
        @RequestParam(defaultValue = "all") String filterRisk,
        Model model,
        HttpSession session,
        Authentication authentication
    ) {
        User user = buildUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("activeTab", tab);
        
        // Check session for selected assessment
        Integer selectedAssessmentId = (Integer) session.getAttribute(SELECTED_ASSESSMENT_ID_KEY);
        Assessment selectedAssessment = null;
        if (selectedAssessmentId != null) {
            selectedAssessment = assessmentService.findAssessment(selectedAssessmentId).orElse(null);
        }
        
        String showResultsFlag = (String) session.getAttribute("showResultsFlag");
        boolean showResults = "true".equals(showResultsFlag);
        
        model.addAttribute("selectedAssessment", selectedAssessment);
        model.addAttribute("showResultsFlag", showResultsFlag);
        model.addAttribute("showResults", showResults);
        
        // Faculty/Counsellor View
        if ("faculty".equals(user.getRole()) || "counsellor".equals(user.getRole())) {
            List<Student> students = assessmentService.getAllStudentWithAssessmentCount();
            List<Student> filteredStudents = assessmentService.filterStudents(searchQuery, filterRisk);
            
            long severeRiskCount = assessmentService.getStudentCountByRiskLevel("severe");
            long moderateRiskCount = assessmentService.getStudentCountByRiskLevel("moderate");
            long mildRiskCount = assessmentService.getStudentCountByRiskLevel("mild");
            
            model.addAttribute("assignedStudents", students);
            model.addAttribute("filteredStudents", filteredStudents);
            model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");
            model.addAttribute("filterRisk", filterRisk);
            model.addAttribute("filteredCount", filteredStudents.size());
            model.addAttribute("totalCount", students.size());
            model.addAttribute("severeRiskCount", severeRiskCount);
            model.addAttribute("moderateRiskCount", moderateRiskCount);
            model.addAttribute("mildRiskCount", mildRiskCount);
            model.addAttribute("showReportModal", false);
            model.addAttribute("selectedReport", null);
            model.addAttribute("selectedStudent", null);
            
            if (selectStudentId != null) {
            Optional<Student> student = assessmentService.getStudentById(selectStudentId);
            if (student.isPresent()) {
                Student s = student.get();
                
                // Load assessment history
                List<AssessmentResult> history = assessmentService.getStudentAssessmentHistory(s.getId());
                
                // Set the history on the student object
                s.setAssessmentHistory(history);
                
                // Also set the assessment count
                s.setAssessmentCount(history != null ? history.size() : 0);
                
                model.addAttribute("selectedStudent", s);
                session.setAttribute(SELECTED_STUDENT_ID_KEY, s.getId());
            }
        }
            
            return "faculty-assessment";
        }
        
        // Student View - Clear session state if no active assessment
        if (selectedAssessment == null && !showResults) {
            session.removeAttribute(SELECTED_ASSESSMENT_ID_KEY);
            session.removeAttribute(ANSWERS_KEY);
            session.removeAttribute(CURRENT_QUESTION_INDEX_KEY);
        }
        
        model.addAttribute("assessments", assessmentService.getAllAssessments());
        
        // Load student's past results from database
        Optional<Student> currentStudent = getCurrentStudent(authentication);
        if (currentStudent.isPresent()) {
            Student student = currentStudent.get();
            List<AssessmentResult> pastResults = assessmentService.getStudentAssessmentHistory(student.getId());
            model.addAttribute("pastResults", pastResults);
            session.setAttribute("studentId", student.getId());
            
            // Load saved progress from database
            Map<String, AssessmentProgress> savedProgress = loadSavedProgress(student.getId(), session);
            model.addAttribute("savedProgress", savedProgress);
        }
        
        return "self-assessment";
    }
    
    // Load saved progress for a student
    private Map<String, AssessmentProgress> loadSavedProgress(Integer studentId, HttpSession session) {
        Map<String, AssessmentProgress> savedProgress = 
            (Map<String, AssessmentProgress>) session.getAttribute(SAVED_PROGRESS_KEY);
        
        if (savedProgress == null) {
            savedProgress = new HashMap<>();
        }
        
        // Load from database and merge with session data
        List<Assessment> allAssessments = assessmentService.getAllAssessments();
        for (Assessment assessment : allAssessments) {
            Map<Integer, Integer> dbAnswers = assessmentService.getAssessmentAnswers(studentId, assessment.getId());
            if (!dbAnswers.isEmpty()) {
                String progressKey = studentId + "_" + assessment.getId();
                AssessmentProgress progress = savedProgress.get(progressKey);
                
                if (progress == null) {
                    progress = new AssessmentProgress();
                    progress.setStudentId(studentId);
                    progress.setAssessmentId(assessment.getId());
                    progress.setAnswers(dbAnswers);
                    progress.setCurrentQuestionIndex(findLastAnsweredQuestionIndex(assessment, dbAnswers));
                    savedProgress.put(progressKey, progress);
                } else {
                    // Merge: database answers override session answers
                    progress.getAnswers().putAll(dbAnswers);
                }
            }
        }
        
        session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
        return savedProgress;
    }
    
    // Start Assessment - FIXED to properly load all saved answers
    @GetMapping("/start/{id}")
    public String startAssessment(@PathVariable Integer id, HttpSession session, 
                                  RedirectAttributes redirect, Authentication authentication) {
        Optional<Assessment> assessmentOpt = assessmentService.findAssessment(id);
        if (assessmentOpt.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Assessment not found.");
            redirect.addFlashAttribute("showError", true);
            return "redirect:/assessment";
        }
        
        Assessment assessment = assessmentOpt.get();
        
        // Get current student
        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Unable to create student profile.");
            redirect.addFlashAttribute("showError", true);
            return "redirect:/assessment";
        }
        
        Student student = studentOpt.get();
        session.setAttribute(SELECTED_ASSESSMENT_ID_KEY, assessment.getId());
        
        // FIRST: Load ALL saved answers from database
        Map<Integer, Integer> dbAnswers = assessmentService.getAssessmentAnswers(student.getId(), assessment.getId());
        Map<Integer, Integer> answers = new HashMap<>(dbAnswers); // Start with DB answers
        
        // SECOND: Check session for any newer answers
        Map<String, AssessmentProgress> savedProgress = 
            (Map<String, AssessmentProgress>) session.getAttribute(SAVED_PROGRESS_KEY);
        
        if (savedProgress != null) {
            String progressKey = student.getId() + "_" + assessment.getId();
            AssessmentProgress progress = savedProgress.get(progressKey);
            if (progress != null && progress.getAnswers() != null) {
                // Merge session answers (they might be more recent)
                answers.putAll(progress.getAnswers());
            }
        }
        
        // Determine starting question index
        Integer currentQuestionIndex = 0;
        if (!answers.isEmpty()) {
            currentQuestionIndex = findLastAnsweredQuestionIndex(assessment, answers);
            // If last question was answered, start from there (not next)
            if (currentQuestionIndex >= assessment.getQuestions().size() - 1) {
                currentQuestionIndex = assessment.getQuestions().size() - 1;
            }
        }
        
        session.setAttribute(ANSWERS_KEY, answers);
        session.setAttribute(CURRENT_QUESTION_INDEX_KEY, currentQuestionIndex);
        session.setAttribute("studentId", student.getId());
        
        return "redirect:/assessment/question?q=" + currentQuestionIndex;
    }
    
    private Integer findLastAnsweredQuestionIndex(Assessment assessment, Map<Integer, Integer> answers) {
        List<Question> questions = assessment.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            if (!answers.containsKey(questions.get(i).getId())) {
                return i > 0 ? i - 1 : 0;
            }
        }
        return questions.size() - 1; // All questions answered
    }
    
    // Display Question
    @GetMapping("/question")
    public String displayQuestion(
        @RequestParam(defaultValue = "0") int q,
        Model model,
        HttpSession session,
        RedirectAttributes redirect,
        Authentication authentication
    ) {
        User user = buildUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Get selected assessment
        Integer assessmentId = (Integer) session.getAttribute(SELECTED_ASSESSMENT_ID_KEY);
        if (assessmentId == null) {
            return "redirect:/assessment";
        }
        
        Optional<Assessment> assessmentOpt = assessmentService.findAssessment(assessmentId);
        if (assessmentOpt.isEmpty()) {
            return "redirect:/assessment";
        }
        
        Assessment assessment = assessmentOpt.get();
        List<Question> questions = assessment.getQuestions();
        
        // Validate question index
        if (q < 0 || q >= questions.size()) {
            return "redirect:/assessment";
        }
        
        Question currentQuestion = questions.get(q);
        Map<Integer, Integer> answers = (Map<Integer, Integer>) session.getAttribute(ANSWERS_KEY);
        if (answers == null) {
            answers = new HashMap<>();
            session.setAttribute(ANSWERS_KEY, answers);
        }
        
        // Get student for saving progress
        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isPresent()) {
            Integer studentId = studentOpt.get().getId();
            model.addAttribute("studentId", studentId);
        }
        
        model.addAttribute("assessment", assessment);
        model.addAttribute("currentQuestion", currentQuestion);
        model.addAttribute("currentQuestionIndex", q);
        model.addAttribute("answers", answers);
        model.addAttribute("selectedAssessment", assessment);
        model.addAttribute("showResults", false);
        model.addAttribute("showResultsFlag", "false");
        
        double progress = ((q + 1) / (double) questions.size()) * 100;
        model.addAttribute("progress", progress);
        
        if ("scale".equals(currentQuestion.getType())) {
            model.addAttribute("scaleLabels", 
                assessmentService.getScaleLabels(currentQuestion.getScaleMax()));
        } else {
            model.addAttribute("scaleLabels", new ArrayList<>());
        }
        
        return "self-assessment";
    }
    
    // Handle Question Submission/Navigation - FIXED to save ALL answers
    @PostMapping("/submit")
    public String handleSubmit(
        @RequestParam Map<String, String> formData,
        @RequestParam int currentQuestionIndex,
        @RequestParam int nextStep, // 1=Next, -1=Previous, 2=Submit, 3=Save
        HttpSession session,
        RedirectAttributes redirect,
        Authentication authentication
    ) {
        // Get selected assessment
        Integer assessmentId = (Integer) session.getAttribute(SELECTED_ASSESSMENT_ID_KEY);
        if (assessmentId == null) {
            return "redirect:/assessment";
        }
        
        Optional<Assessment> assessmentOpt = assessmentService.findAssessment(assessmentId);
        if (assessmentOpt.isEmpty()) {
            return "redirect:/assessment";
        }
        
        Assessment assessment = assessmentOpt.get();
        List<Question> questions = assessment.getQuestions();
        
        // Validate current question index
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return "redirect:/assessment";
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        Map<Integer, Integer> answers = (Map<Integer, Integer>) session.getAttribute(ANSWERS_KEY);
        if (answers == null) {
            answers = new HashMap<>();
        }
        
        System.out.println("DEBUG: Before processing form, answers size: " + answers.size());
        System.out.println("DEBUG: Form data keys: " + formData.keySet());
        
        // FIX 1: Process ALL question answers from the form
        // The form might have hidden inputs with previous answers
        for (Question question : questions) {
            String answerKey = "q_" + question.getId();
            if (formData.containsKey(answerKey) && !formData.get(answerKey).isEmpty()) {
                try {
                    int answerValue = Integer.parseInt(formData.get(answerKey));
                    answers.put(question.getId(), answerValue);
                    System.out.println("DEBUG: Found answer for Q" + question.getId() + " = " + answerValue);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing answer for Q" + question.getId() + ": " + formData.get(answerKey));
                }
            }
        }
        
        // FIX 2: Also check for the current question's answer (in case it wasn't in formData yet)
        String currentAnswerKey = "q_" + currentQuestion.getId();
        if (formData.containsKey("answer")) {
            // Some forms might use just "answer" for the current question
            try {
                int answerValue = Integer.parseInt(formData.get("answer"));
                answers.put(currentQuestion.getId(), answerValue);
                System.out.println("DEBUG: Current question answer: Q" + currentQuestion.getId() + " = " + answerValue);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        session.setAttribute(ANSWERS_KEY, answers);
        System.out.println("DEBUG: After processing form, answers size: " + answers.size());
        
        // Check if current question is answered for navigation
        boolean currentAnswered = answers.containsKey(currentQuestion.getId());
        
        if (nextStep == 1 && !currentAnswered) {
            redirect.addFlashAttribute("errorMessage", 
                "Please answer the current question before moving forward.");
            redirect.addFlashAttribute("showError", true);
            return "redirect:/assessment/question?q=" + currentQuestionIndex;
        }
        
        // Handle actions
        if (nextStep == 2) { // Submit
            if (answers.size() < questions.size()) {
                redirect.addFlashAttribute("errorMessage", 
                    "Please answer all questions before submitting.");
                redirect.addFlashAttribute("showError", true);
                return "redirect:/assessment/question?q=" + currentQuestionIndex;
            }
            
            // Get current student
            Optional<Student> studentOpt = getCurrentStudent(authentication);
            if (studentOpt.isEmpty()) {
                redirect.addFlashAttribute("errorMessage", "Student not found.");
                redirect.addFlashAttribute("showError", true);
                return "redirect:/assessment";
            }
            
            Student student = studentOpt.get();
            
            // Calculate and save result
            AssessmentResult result = assessmentService.calculateScore(assessment, answers, student);
            
            // Clear all session state
            clearAllAssessmentState(session);
            
            // Store result ID in session for results view
            session.setAttribute("lastResultId", result.getId());
            session.setAttribute("showResultsFlag", "true");
            
            return "redirect:/assessment/results/" + result.getId();
            
        } else if (nextStep == 3) { // Save Progress
            // Get current student
            Optional<Student> studentOpt = getCurrentStudent(authentication);
            if (studentOpt.isEmpty()) {
                redirect.addFlashAttribute("errorMessage", "Student not found.");
                redirect.addFlashAttribute("showError", true);
                return "redirect:/assessment";
            }
            
            Student student = studentOpt.get();
            
            System.out.println("DEBUG: Saving progress with " + answers.size() + " answers");
            
            // Save ALL answers to database
            assessmentService.saveAssessmentProgress(
                student.getId(), 
                assessment.getId(), 
                answers, 
                currentQuestionIndex
            );
            
            // Also save to session
            Map<String, AssessmentProgress> savedProgress = 
                (Map<String, AssessmentProgress>) session.getAttribute(SAVED_PROGRESS_KEY);
            if (savedProgress == null) {
                savedProgress = new HashMap<>();
            }
            
            AssessmentProgress progress = new AssessmentProgress();
            progress.setAnswers(new HashMap<>(answers));
            progress.setAssessmentId(assessment.getId());
            progress.setCurrentQuestionIndex(currentQuestionIndex);
            progress.setStudentId(student.getId());
            
            String progressKey = student.getId() + "_" + assessment.getId();
            savedProgress.put(progressKey, progress);
            session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
            
            // Clear current assessment state but keep saved progress
            session.removeAttribute(SELECTED_ASSESSMENT_ID_KEY);
            session.removeAttribute(ANSWERS_KEY);
            session.removeAttribute(CURRENT_QUESTION_INDEX_KEY);
            
            redirect.addFlashAttribute("alert", "✓ Progress saved! You can resume this assessment later.");
            redirect.addFlashAttribute("alertType", "success");
            return "redirect:/assessment";
            
        } else { // Next/Previous (-1 or 1)
            // Auto-save progress as they navigate
            Optional<Student> studentOpt = getCurrentStudent(authentication);
            if (studentOpt.isPresent() && !answers.isEmpty()) {
                Student student = studentOpt.get();
                
                // Update current question index based on navigation
                int newIndex = currentQuestionIndex + nextStep;
                if (newIndex < 0) newIndex = 0;
                if (newIndex >= questions.size()) newIndex = questions.size() - 1;
                
                // Save progress to session
                Map<String, AssessmentProgress> savedProgress = 
                    (Map<String, AssessmentProgress>) session.getAttribute(SAVED_PROGRESS_KEY);
                if (savedProgress == null) {
                    savedProgress = new HashMap<>();
                }
                
                String progressKey = student.getId() + "_" + assessment.getId();
                AssessmentProgress progress = savedProgress.get(progressKey);
                
                if (progress == null) {
                    progress = new AssessmentProgress();
                    progress.setStudentId(student.getId());
                    progress.setAssessmentId(assessment.getId());
                }
                
                progress.setAnswers(new HashMap<>(answers));
                progress.setCurrentQuestionIndex(newIndex);
                savedProgress.put(progressKey, progress);
                session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
                
                // Also auto-save to database every 3 questions
                if (answers.size() % 3 == 0) {
                    assessmentService.saveAssessmentProgress(
                        student.getId(), 
                        assessment.getId(), 
                        answers, 
                        newIndex
                    );
                }
                
                // Update session with new index
                session.setAttribute(CURRENT_QUESTION_INDEX_KEY, newIndex);
            }
            
            int newIndex = currentQuestionIndex + nextStep;
            return "redirect:/assessment/question?q=" + newIndex;
        }
    }
    
    // Helper method to clear all assessment state
    private void clearAllAssessmentState(HttpSession session) {
        session.removeAttribute(SELECTED_ASSESSMENT_ID_KEY);
        session.removeAttribute(ANSWERS_KEY);
        session.removeAttribute(CURRENT_QUESTION_INDEX_KEY);
        session.removeAttribute("showResultsFlag");
        session.removeAttribute("lastResultId");
    }
    
    // Display Results
    @GetMapping("/results/{resultId}")
    public String viewSpecificResult(
        @PathVariable Integer resultId,
        Model model,
        HttpSession session,
        Authentication authentication
    ) {
        User user = buildUser(authentication);
        
        if (!"student".equals(user.getRole())) {
            return "redirect:/assessment";
        }
        
        // Get current student
        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isEmpty()) {
            return "redirect:/assessment";
        }
        
        Student student = studentOpt.get();
        
        // Find the result using EntityManager
        AssessmentResult result = entityManager.find(AssessmentResult.class, resultId);
        if (result == null || !result.getStudent().getId().equals(student.getId())) {
            return "redirect:/assessment";
        }
        
        // Set results flag but clear other assessment state
        session.setAttribute("showResultsFlag", "true");
        clearAllAssessmentState(session);
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("showResults", true);
        model.addAttribute("assessmentScore", result.getScore());
        model.addAttribute("selectedReport", result);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        
        return "self-assessment";
    }
    
    // Clear Assessment State
    @GetMapping("/clear")
    public String clearAssessmentState(HttpSession session) {
        clearAllAssessmentState(session);
        return "redirect:/assessment";
    }
    
    // Clear Saved Progress for a specific assessment
    @GetMapping("/clear-progress/{assessmentId}")
    public String clearSavedProgress(
        @PathVariable Integer assessmentId,
        HttpSession session,
        Authentication authentication,
        RedirectAttributes redirect
    ) {
        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            
            // Clear from database
            assessmentService.clearAssessmentProgress(student.getId(), assessmentId);
            
            // Clear from session
            Map<String, AssessmentProgress> savedProgress = 
                (Map<String, AssessmentProgress>) session.getAttribute(SAVED_PROGRESS_KEY);
            if (savedProgress != null) {
                String progressKey = student.getId() + "_" + assessmentId;
                savedProgress.remove(progressKey);
                session.setAttribute(SAVED_PROGRESS_KEY, savedProgress);
            }
            
            redirect.addFlashAttribute("alert", "✓ Saved progress cleared.");
            redirect.addFlashAttribute("alertType", "success");
        }
        
        return "redirect:/assessment";
    }

    // View Assessment Report (for everyone)
    @GetMapping("/report/view/{resultId}")
    public String viewReport(
        @PathVariable Integer resultId,
        Model model,
        HttpSession session,
        Authentication authentication
    ) {
        User user = buildUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Find the result
        AssessmentResult result = entityManager.find(AssessmentResult.class, resultId);
        if (result == null) {
            return "redirect:/assessment";
        }
        
        // Check authorization
        if ("student".equals(user.getRole())) {
            // Student can only view their own reports
            Optional<Student> studentOpt = getCurrentStudent(authentication);
            if (studentOpt.isEmpty() || !studentOpt.get().getId().equals(result.getStudent().getId())) {
                return "redirect:/assessment";
            }
        }
        
        model.addAttribute("selectedReport", result);
        
        // Get assessment details if available
        if (result.getAssessment() != null) {
            model.addAttribute("assessmentDescription", result.getAssessment().getDescription());
            model.addAttribute("questionCount", result.getAssessment().getQuestions().size());
        } else {
            // Fallback values
            model.addAttribute("assessmentDescription", "This assessment measures various aspects of mental health and well-being.");
            model.addAttribute("questionCount", "N/A");
        }
        
        // For faculty/counsellor view, also load the student
        if ("faculty".equals(user.getRole()) || "counsellor".equals(user.getRole())) {
            Optional<Student> student = assessmentService.getStudentById(result.getStudent().getId());
            student.ifPresent(s -> {
                List<AssessmentResult> history = assessmentService.getStudentAssessmentHistory(s.getId());
                s.setAssessmentHistory(history);
                s.setAssessmentCount(history != null ? history.size() : 0);
                model.addAttribute("selectedStudent", s);
            });
            
            return "faculty-assessment";
        }
        
        return "assessment-report";
    }

    @GetMapping("/report/student/{studentId}/{resultId}")
    public String viewStudentReport(
        @PathVariable Integer studentId,
        @PathVariable Integer resultId,
        Model model,
        Authentication authentication
    ) {
        User user = buildUser(authentication);
        
        // Only faculty/counsellor can view specific student reports
        if (!"faculty".equals(user.getRole()) && !"counsellor".equals(user.getRole())) {
            return "redirect:/assessment";
        }
        
        // Use the service method to get the result with proper fetching
        AssessmentResult result = assessmentService.getResultWithAssessment(resultId);
        
        if (result == null || !result.getStudent().getId().equals(studentId)) {
            return "redirect:/assessment";
        }
        
        // Load student
        Optional<Student> student = assessmentService.getStudentById(studentId);
        if (student.isEmpty()) {
            return "redirect:/assessment";
        }
        
        Student s = student.get();
        List<AssessmentResult> history = assessmentService.getStudentAssessmentHistory(s.getId());
        s.setAssessmentHistory(history);
        s.setAssessmentCount(history != null ? history.size() : 0);
        
        model.addAttribute("selectedReport", result);
        model.addAttribute("selectedStudent", s);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Get assessment details - now it's safely loaded
        if (result.getAssessment() != null) {
            model.addAttribute("assessmentDescription", result.getAssessment().getDescription());
            // Check if questions are loaded
            if (result.getAssessment().getQuestions() != null) {
                model.addAttribute("questionCount", result.getAssessment().getQuestions().size());
            } else {
                model.addAttribute("questionCount", "N/A");
            }
        } else {
            // Fallback values
            model.addAttribute("assessmentDescription", "This assessment measures various aspects of mental health and well-being.");
            model.addAttribute("questionCount", "N/A");
        }
        
        return "assessment-report";
    }
    
    // Helper class for assessment progress
    public static class AssessmentProgress {
        private Map<Integer, Integer> answers;
        private Integer assessmentId;
        private Integer currentQuestionIndex;
        private Integer studentId;
        
        public Map<Integer, Integer> getAnswers() { return answers; }
        public void setAnswers(Map<Integer, Integer> answers) { this.answers = answers; }
        
        public Integer getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
        
        public Integer getCurrentQuestionIndex() { return currentQuestionIndex; }
        public void setCurrentQuestionIndex(Integer currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
        
        public Integer getStudentId() { return studentId; }
        public void setStudentId(Integer studentId) { this.studentId = studentId; }
    }
}