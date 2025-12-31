package com.secj3303.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.LearningModuleData;
import com.secj3303.model.LearningModuleData.Lesson;
import com.secj3303.model.LearningModuleData.Module;
import com.secj3303.service.AuthenticationService;

@Controller
@RequestMapping("/learning")
public class LearningController {

    private static final String MODULES_SESSION_KEY = "learningModules";
    private static final String DEFAULT_VIEW = "learning";

    private final AuthenticationService authenticationService;

    public LearningController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // --- Utility methods ---

    private List<Module> getModules(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Module> modules = (List<Module>) session.getAttribute(MODULES_SESSION_KEY);
        if (modules == null) {
            modules = LearningModuleData.getInitialModules();
            session.setAttribute(MODULES_SESSION_KEY, modules);
        }
        return modules;
    }

    private Optional<Module> findModule(List<Module> modules, int moduleId) {
        return modules.stream().filter(m -> m.getId() == moduleId).findFirst();
    }

    // --- Main Module Grid View ---

    @GetMapping
    public String learningModules(HttpSession session, Model model) {
        List<Module> modules = getModules(session);

        long completedCount = modules.stream().filter(m -> m.isQuizPassed() || m.getProgress() == 100).count();
        long inProgressCount = modules.stream().filter(m -> m.getProgress() > 0 && m.getProgress() < 100).count();

        model.addAttribute("modules", modules);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("totalModules", modules.size());

        // ✅ Add current user for sidebar & fragment
        model.addAttribute("user", authenticationService.getAuthenticatedUser(session));

        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }

    // --- Module Detail / Lesson Viewer ---

    @GetMapping("/module")
    public String viewModule(@RequestParam int moduleId,
                             @RequestParam(required = false) Integer lessonId,
                             @RequestParam(required = false) Boolean showQuiz,
                             HttpSession session, Model model) {

        List<Module> modules = getModules(session);
        Optional<Module> moduleOpt = findModule(modules, moduleId);

        if (moduleOpt.isEmpty()) {
            return "redirect:/learning";
        }

        Module selectedModule = moduleOpt.get();
        if (selectedModule.isLocked()) {
            return "redirect:/learning"; // Prevent access to locked modules
        }

        Lesson selectedLesson = null;
        if (lessonId != null) {
            selectedLesson = selectedModule.getLessons().stream()
                    .filter(l -> l.getId() == lessonId)
                    .findFirst().orElse(null);
        }
        if (selectedLesson == null && !selectedModule.getLessons().isEmpty()) {
            selectedLesson = selectedModule.getLessons().get(0); // default to first lesson
        }

        model.addAttribute("modules", modules);
        model.addAttribute("selectedModule", selectedModule);
        model.addAttribute("selectedLesson", selectedLesson);
        model.addAttribute("showQuiz", showQuiz != null && showQuiz);

        // ✅ Add current user for sidebar & fragment
        model.addAttribute("user", authenticationService.getAuthenticatedUser(session));

        model.addAttribute("currentView", DEFAULT_VIEW);

        return "app-layout";
    }

    // --- Lesson Completion Logic ---

    @PostMapping("/complete-lesson")
    public String completeLesson(@RequestParam int moduleId, @RequestParam int lessonId,
                                 HttpSession session, RedirectAttributes redirect) {

        List<Module> modules = getModules(session);
        AtomicReference<Lesson> completedLessonRef = new AtomicReference<>();

        modules.stream()
            .filter(m -> m.getId() == moduleId)
            .findFirst()
            .ifPresent(module -> {
                module.getLessons().stream()
                        .filter(l -> l.getId() == lessonId)
                        .findFirst()
                        .ifPresent(lesson -> {
                            lesson.setCompleted(true);
                            completedLessonRef.set(lesson);
                        });
                module.updateProgress();
            });

        redirect.addAttribute("moduleId", moduleId);

        if (completedLessonRef.get() != null) {
            Module module = findModule(modules, moduleId).orElse(null);
            if (module != null) {
                long totalLessons = module.getLessons().size();
                long completedLessons = module.getLessons().stream().filter(Lesson::isCompleted).count();
                if (totalLessons > 0 && completedLessons == totalLessons && !module.getQuiz().isEmpty()) {
                    redirect.addAttribute("showQuiz", true);
                }
            }
        }

        return "redirect:/learning/module";
    }

    // --- Quiz Submission Logic ---

    @PostMapping("/submit-quiz")
    public String submitQuiz(@RequestParam int moduleId, @RequestParam Map<String, String> quizAnswers,
                             HttpSession session, RedirectAttributes redirect) {

        List<Module> modules = getModules(session);
        Optional<Module> moduleOpt = findModule(modules, moduleId);

        if (moduleOpt.isEmpty()) {
            return "redirect:/learning";
        }

        Module module = moduleOpt.get();
        long correct = 0;

        for (LearningModuleData.QuizQuestion question : module.getQuiz()) {
            String answerKey = "question-" + question.getId();
            String submittedAnswerIndex = quizAnswers.get(answerKey);
            if (submittedAnswerIndex != null) {
                try {
                    int submittedIndex = Integer.parseInt(submittedAnswerIndex);
                    if (submittedIndex == question.getCorrectAnswer()) {
                        correct++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int score = (int) Math.round((double) correct / module.getQuiz().size() * 100);
        redirect.addFlashAttribute("quizScore", score);

        if (score >= 70) {
            module.setQuizPassed(true);
            module.setProgress(100);
            redirect.addFlashAttribute("achievement", "🏆 Module Completed: " + module.getTitle());
        }

        redirect.addAttribute("moduleId", moduleId);
        redirect.addAttribute("showQuiz", true);
        return "redirect:/learning/module";
    }
}
