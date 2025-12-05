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
import com.secj3303.model.LearningModuleData.QuizQuestion;

@Controller
@RequestMapping("/learning")
public class LearningController {

    private static final String MODULES_SESSION_KEY = "learningModules";
    private static final String DEFAULT_VIEW = "learning";

    private List<Module> getModules(HttpSession session) {
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

    // --- Main Module Grid View (Replaces initial component render) ---

    @GetMapping
    public String learningModules(HttpSession session, Model model) {
        List<Module> modules = getModules(session);
        model.addAttribute("modules", modules);
        
        // Calculate progress stats
        long completedCount = modules.stream().filter(m -> m.isQuizPassed() || m.getProgress() == 100).count();
        long inProgressCount = modules.stream().filter(m -> m.getProgress() > 0 && m.getProgress() < 100).count();
        
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("totalModules", modules.size());

        // Ensure the main app-layout renders this fragment
        model.addAttribute("currentView", DEFAULT_VIEW);
        return "app-layout";
    }

    // --- Module Detail/Lesson Viewer (Replaces selectedModule state) ---

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
            // Cannot view locked modules
            return "redirect:/learning";
        }

        Lesson selectedLesson = null;
        if (lessonId != null) {
            selectedLesson = selectedModule.getLessons().stream().filter(l -> l.getId() == lessonId).findFirst().orElse(null);
        } else if (!selectedModule.getLessons().isEmpty()) {
            // Default to the first lesson if no lesson is specified
            selectedLesson = selectedModule.getLessons().get(0);
        }

        model.addAttribute("modules", modules); // Needed for the achievement section data
        model.addAttribute("selectedModule", selectedModule);
        model.addAttribute("selectedLesson", selectedLesson);
        model.addAttribute("showQuiz", showQuiz != null ? showQuiz : false);
        model.addAttribute("currentView", DEFAULT_VIEW);
        
        // Return the main layout which will dynamically include the learning-content fragment
        return "app-layout";
    }

    // --- Lesson Completion Logic (Replaces handleLessonComplete) ---
    
    @PostMapping("/complete-lesson")
    public String completeLesson(@RequestParam int moduleId, @RequestParam int lessonId, 
                                 HttpSession session, RedirectAttributes redirect) {
        
        List<Module> modules = getModules(session);
        AtomicReference<Lesson> completedLessonRef = new AtomicReference<>();

        modules.stream().filter(m -> m.getId() == moduleId).findFirst().ifPresent(module -> {
            module.getLessons().stream().filter(l -> l.getId() == lessonId).findFirst().ifPresent(lesson -> {
                lesson.setCompleted(true);
                completedLessonRef.set(lesson);
            });
            module.updateProgress(); // Recalculate module progress
        });

        // Redirect back to the module view, optionally showing the quiz if needed
        if (completedLessonRef.get() != null) {
            Module module = findModule(modules, moduleId).get();
            long totalLessons = module.getLessons().size();
            long completedLessons = module.getLessons().stream().filter(Lesson::isCompleted).count();

            if (totalLessons > 0 && completedLessons == totalLessons && module.getQuiz().size() > 0) {
                // All lessons complete, show quiz
                redirect.addAttribute("showQuiz", true);
            }
        }
        
        redirect.addAttribute("moduleId", moduleId);
        return "redirect:/learning/module";
    }

    // --- Quiz Submission Logic (Replaces handleQuizSubmit) ---

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
        for (QuizQuestion question : module.getQuiz()) {
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