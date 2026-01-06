package com.secj3303.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Assessment;
import com.secj3303.model.AssessmentResult;
import com.secj3303.model.Person;
import com.secj3303.model.Student;
import com.secj3303.service.AssessmentService;

@Controller
@RequestMapping("/assessment")
public class SelfAssessmentController {

    private static final String DEFAULT_VIEW = "assessment";

    private final AssessmentService assessmentService;
    private final PersonDao personDao;

    @PersistenceContext
    private EntityManager entityManager;

    // ================= SESSION KEYS =================
    private static final String SELECTED_ASSESSMENT_ID_KEY = "selectedAssessmentId";
    private static final String ANSWERS_KEY = "assessmentAnswers";
    private static final String CURRENT_QUESTION_INDEX_KEY = "currentQuestionIndex";
    private static final String SAVED_PROGRESS_KEY = "assessmentSavedProgress";
    private static final String SELECTED_STUDENT_ID_KEY = "selectedStudentId";

    public SelfAssessmentController(
            AssessmentService assessmentService,
            PersonDao personDao
    ) {
        this.assessmentService = assessmentService;
        this.personDao = personDao;
    }

    // =================================================
    // AUTHENTICATED PERSON
    // =================================================
    private Person getAuthenticatedPerson(Authentication authentication) {
        String email = authentication.getName();
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found: " + email);
        }
        return person;
    }

    // =================================================
    // GET OR AUTO-CREATE STUDENT
    // =================================================
    private Optional<Student> getCurrentStudent(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Person person = getAuthenticatedPerson(authentication);
        String email = person.getEmail();

        Optional<Student> studentOpt = assessmentService.getStudentByEmail(email);

        if (studentOpt.isEmpty()) {
            Student student = new Student();

            student.setName(person.getName());
            student.setEmail(email);
            student.setStudentId("S" + System.currentTimeMillis() % 1000000);
            student.setDepartment("General Studies");
            student.setYear("Year 1");
            student.setCurrentGrade("B");
            student.setAttendance(85);
            student.setLastActivity(java.time.LocalDate.now().toString());
            student.setRiskLevel("low");

            return Optional.of(assessmentService.saveStudent(student));
        }

        return studentOpt;
    }

    // =================================================
    // MAIN DASHBOARD
    // =================================================
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
        Person person = getAuthenticatedPerson(authentication);

        model.addAttribute("user", person);   // keep attribute name
        model.addAttribute("userRole", person.getRole().toLowerCase());
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("activeTab", tab);

        Integer selectedAssessmentId =
                (Integer) session.getAttribute(SELECTED_ASSESSMENT_ID_KEY);

        Assessment selectedAssessment = null;
        if (selectedAssessmentId != null) {
            selectedAssessment =
                    assessmentService.findAssessment(selectedAssessmentId).orElse(null);
        }

        boolean showResults =
                "true".equals(session.getAttribute("showResultsFlag"));

        model.addAttribute("selectedAssessment", selectedAssessment);
        model.addAttribute("showResults", showResults);

        // =================================================
        // FACULTY / COUNSELLOR VIEW
        // =================================================
        if ("faculty".equalsIgnoreCase(person.getRole()) ||
            "counsellor".equalsIgnoreCase(person.getRole())) {

            List<Student> students =
                    assessmentService.getAllStudentWithAssessmentCount();

            List<Student> filteredStudents =
                    assessmentService.filterStudents(searchQuery, filterRisk);

            model.addAttribute("assignedStudents", students);
            model.addAttribute("filteredStudents", filteredStudents);
            model.addAttribute("searchQuery", searchQuery == null ? "" : searchQuery);
            model.addAttribute("filterRisk", filterRisk);

            model.addAttribute("highRiskCount",
                    assessmentService.getStudentCountByRiskLevel("high"));
            model.addAttribute("moderateRiskCount",
                    assessmentService.getStudentCountByRiskLevel("moderate"));
            model.addAttribute("lowRiskCount",
                    assessmentService.getStudentCountByRiskLevel("low"));

            if (selectStudentId != null) {
                assessmentService.getStudentById(selectStudentId)
                        .ifPresent(s -> {
                            model.addAttribute("selectedStudent", s);
                            session.setAttribute(SELECTED_STUDENT_ID_KEY, s.getId());
                            model.addAttribute(
                                    "studentHistory",
                                    assessmentService.getStudentAssessmentHistory(s.getId()));
                        });
            }

            return "faculty-assessment";
        }

        // =================================================
        // STUDENT VIEW
        // =================================================
        model.addAttribute("assessments", assessmentService.getAllAssessments());

        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            model.addAttribute(
                    "pastResults",
                    assessmentService.getStudentAssessmentHistory(student.getId()));
            session.setAttribute("studentId", student.getId());
        }

        return "self-assessment";
    }

    // =================================================
    // START ASSESSMENT
    // =================================================
    @GetMapping("/start/{id}")
    public String startAssessment(
            @PathVariable Integer id,
            HttpSession session,
            RedirectAttributes redirect,
            Authentication authentication
    ) {
        Optional<Assessment> assessmentOpt =
                assessmentService.findAssessment(id);

        if (assessmentOpt.isEmpty()) {
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("errorMessage", "Assessment not found.");
            return "redirect:/assessment";
        }

        Optional<Student> studentOpt = getCurrentStudent(authentication);
        if (studentOpt.isEmpty()) {
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("errorMessage", "Student not found.");
            return "redirect:/assessment";
        }

        session.setAttribute(SELECTED_ASSESSMENT_ID_KEY, id);
        session.setAttribute(CURRENT_QUESTION_INDEX_KEY, 0);
        session.setAttribute(ANSWERS_KEY, new HashMap<Integer, Integer>());

        return "redirect:/assessment/question?q=0";
    }

    // =================================================
    // DISPLAY QUESTION
    // =================================================
    @GetMapping("/question")
    public String displayQuestion(
            @RequestParam(defaultValue = "0") int q,
            Model model,
            HttpSession session,
            Authentication authentication
    ) {
        Person person = getAuthenticatedPerson(authentication);

        model.addAttribute("user", person);
        model.addAttribute("userRole", person.getRole().toLowerCase());
        model.addAttribute("currentView", DEFAULT_VIEW);

        Integer assessmentId =
                (Integer) session.getAttribute(SELECTED_ASSESSMENT_ID_KEY);

        if (assessmentId == null) {
            return "redirect:/assessment";
        }

        Assessment assessment =
                assessmentService.findAssessment(assessmentId).orElse(null);

        if (assessment == null || q < 0 || q >= assessment.getQuestions().size()) {
            return "redirect:/assessment";
        }

        Map<Integer, Integer> answers =
                (Map<Integer, Integer>) session.getAttribute(ANSWERS_KEY);

        if (answers == null) {
            answers = new HashMap<>();
            session.setAttribute(ANSWERS_KEY, answers);
        }

        model.addAttribute("assessment", assessment);
        model.addAttribute("currentQuestion", assessment.getQuestions().get(q));
        model.addAttribute("currentQuestionIndex", q);
        model.addAttribute("answers", answers);
        model.addAttribute("progress",
                ((q + 1) / (double) assessment.getQuestions().size()) * 100);

        return "self-assessment";
    }

    // =================================================
    // RESULTS
    // =================================================
    @GetMapping("/results/{resultId}")
    public String viewResult(
            @PathVariable Integer resultId,
            Model model,
            Authentication authentication
    ) {
        Person person = getAuthenticatedPerson(authentication);

        if (!"student".equalsIgnoreCase(person.getRole())) {
            return "redirect:/assessment";
        }

        AssessmentResult result =
                entityManager.find(AssessmentResult.class, resultId);

        if (result == null) {
            return "redirect:/assessment";
        }

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("showResults", true);
        model.addAttribute("assessmentScore", result.getScore());
        model.addAttribute("selectedReport", result);
        model.addAttribute("user", person);
        model.addAttribute("userRole", person.getRole().toLowerCase());

        return "self-assessment";
    }

    // =================================================
    // CLEAR SESSION
    // =================================================
    @GetMapping("/clear")
    public String clearAssessment(HttpSession session) {
        session.invalidate();
        return "redirect:/assessment";
    }
}
