package com.secj3303.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.ModuleProgress;
import com.secj3303.model.QuizQuestion;
import com.secj3303.model.User;
import com.secj3303.repository.LearningModuleRepository;
import com.secj3303.repository.ModuleProgressRepository;
import com.secj3303.service.LearningService;

@Controller
@RequestMapping("/learning")
public class LearningController {

    private static final String CURRENT_VIEW = "learning";

    private final LearningModuleRepository moduleRepo;
    private final ModuleProgressRepository progressRepo;
    private final LearningService learningService;

    public LearningController(LearningModuleRepository moduleRepo,
                              ModuleProgressRepository progressRepo,
                              LearningService learningService) {
        this.moduleRepo = moduleRepo;
        this.progressRepo = progressRepo;
        this.learningService = learningService;
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping
    public String dashboard(Authentication auth, Model model) {

        String email = auth.getName();
        List<LearningModule> modules = moduleRepo.findAllWithLessonsAndQuiz();

        long completed = 0;
        long inProgress = 0;

        for (LearningModule m : modules) {
            ModuleProgress p = progressRepo
                    .findByUserEmailAndModuleId(email, m.getId())
                    .orElse(null);

            int progress = (p != null) ? p.getProgress() : 0;
            m.setProgress(progress);
            m.setQuizPassed(p != null && p.isQuizPassed());

            if (progress == 100) completed++;
            else if (progress > 0) inProgress++;
        }

        model.addAttribute("modules", modules);
        model.addAttribute("completedCount", completed);
        model.addAttribute("inProgressCount", inProgress);
        model.addAttribute("totalModules", modules.size());
        model.addAttribute("currentView", CURRENT_VIEW);
        model.addAttribute("user", buildUser(auth));

        model.addAttribute("selectedModule", null);
        model.addAttribute("selectedLesson", null);
        model.addAttribute("showQuiz", false);
        model.addAttribute("quizScore", null);
        model.addAttribute("achievement", null);

        return "app-layout";
    }

    // =========================
    // VIEW MODULE
    // =========================
    @GetMapping("/module")
    public String viewModule(@RequestParam Long moduleId,
                            @RequestParam(required = false) Long lessonId,
                            @RequestParam(required = false) Boolean showQuiz,
                            Authentication auth,
                            Model model) {

        String email = auth.getName();

        List<LearningModule> modules = moduleRepo.findAllWithLessonsAndQuiz();

        LearningModule selectedModule = modules.stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // ===== progress lookup =====
        ModuleProgress progress = progressRepo
                .findByUserEmailAndModuleId(email, moduleId)
                .orElse(null);

        int progressValue = progress != null ? progress.getProgress() : 0;
        boolean quizPassed = progress != null && progress.isQuizPassed();

        selectedModule.setProgress(progressValue);
        selectedModule.setQuizPassed(quizPassed);

        // ===== mark completed lessons (UI ONLY) =====
        int lessonCount = selectedModule.getLessons().size();
        int completedLessons = lessonCount > 0 ? (progressValue * lessonCount) / 100 : 0;

        int index = 0;
        for (Lesson l : selectedModule.getLessons()) {
            l.setCompleted(index < completedLessons);
            index++;
        }

        // ===== select lesson (ONLY if not showing quiz) =====
        Lesson selectedLesson = null;
        
        if (!Boolean.TRUE.equals(showQuiz)) {
            if (lessonId != null) {
                for (Lesson l : selectedModule.getLessons()) {
                    if (l.getId().equals(lessonId)) {
                        selectedLesson = l;
                        break;
                    }
                }
            }

            if (selectedLesson == null && !selectedModule.getLessons().isEmpty()) {
                selectedLesson = selectedModule.getLessons().iterator().next();
            }
        }

        // Count completed and in-progress modules
        long completedCount = 0;
        long inProgressCount = 0;
        
        for (LearningModule m : modules) {
            ModuleProgress p = progressRepo
                    .findByUserEmailAndModuleId(email, m.getId())
                    .orElse(null);
            
            int pValue = (p != null) ? p.getProgress() : 0;
            if (pValue == 100) completedCount++;
            else if (pValue > 0) inProgressCount++;
        }

        model.addAttribute("modules", modules);
        model.addAttribute("selectedModule", selectedModule);
        model.addAttribute("selectedLesson", selectedLesson);
        model.addAttribute("showQuiz", Boolean.TRUE.equals(showQuiz));

        model.addAttribute("completedCount", completedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("totalModules", modules.size());

        model.addAttribute("quizScore", null);
        model.addAttribute("achievement", null);
        model.addAttribute("currentView", "learning");
        model.addAttribute("user", buildUser(auth));

        return "app-layout";
    }

    // =========================
    // COMPLETE LESSON
    // =========================
    @PostMapping("/complete-lesson")
    public String completeLesson(@RequestParam Long moduleId,
                                 Authentication auth) {

        learningService.incrementProgress(auth.getName(), moduleId);
        return "redirect:/learning/module?moduleId=" + moduleId;
    }

    // =========================
    // SUBMIT QUIZ
    // =========================
    @PostMapping("/submit-quiz")
    public String submitQuiz(@RequestParam Long moduleId,
                             @RequestParam Map<String, String> answers,
                             Authentication auth) {

        LearningModule module = moduleRepo.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        int correct = 0;
        for (QuizQuestion q : module.getQuiz()) {
            String key = "question-" + q.getId();
            if (answers.containsKey(key)) {
                int selected = Integer.parseInt(answers.get(key));
                if (selected == q.getCorrectAnswer()) {
                    correct++;
                }
            }
        }

        int score = (int) Math.round(
                (double) correct / module.getQuiz().size() * 100
        );

        if (score >= 70) {
            learningService.completeModule(auth.getName(), moduleId);
        }

        return "redirect:/learning/module?moduleId=" + moduleId + "&showQuiz=true";
    }

    // =========================
    // HELPER
    // =========================
    private User buildUser(Authentication auth) {
        User user = new User();
        user.setEmail(auth.getName());
        user.setName(auth.getName().split("@")[0]);
        user.setRole(
                auth.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "")
                        .toLowerCase()
        );
        return user;
    }
}
