package com.secj3303.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.dao.ModuleProgressDao;
import com.secj3303.model.Gamification;
import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.ModuleProgress;
import com.secj3303.model.QuizQuestion;
import com.secj3303.model.User;
import com.secj3303.service.GamificationService;
import com.secj3303.service.LearningService;

@Controller
@RequestMapping("/learning")
public class LearningController {

    private static final String APP_LAYOUT = "app-layout";
    private static final String VIEW_NAME = "learning";

    private final LearningModuleDao moduleDao;
    private final ModuleProgressDao progressDao;
    private final LearningService learningService;
    private final GamificationService gamificationService;

    public LearningController(LearningModuleDao moduleDao,
                              ModuleProgressDao progressDao,
                              LearningService learningService,
                              GamificationService gamificationService) {
        this.moduleDao = moduleDao;
        this.progressDao = progressDao;
        this.learningService = learningService;
        this.gamificationService = gamificationService;
    }

    // ================= DASHBOARD =================
    @GetMapping
    public String dashboard(Authentication auth, Model model) {

        List<LearningModule> modules = moduleDao.findAllWithLessonsAndQuiz();
        updateDashboardStats(modules, auth.getName(), model);

        model.addAttribute("currentView", VIEW_NAME);
        model.addAttribute("user", buildUser(auth));
        model.addAttribute("selectedModule", null);
        model.addAttribute("selectedLesson", null);

        return APP_LAYOUT;
    }

    // ================= MODULE / LESSON =================
    @GetMapping("/module")
    public String viewModule(@RequestParam Long moduleId,
                             @RequestParam(required = false) Long lessonId,
                             Authentication auth,
                             Model model) {

        LearningModule module = moduleDao.findByIdWithDetails(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        List<Lesson> lessons = module.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getId))
                .collect(Collectors.toList());

        Lesson selectedLesson = lessons.stream()
                .filter(l -> lessonId != null && l.getId().equals(lessonId))
                .findFirst()
                .orElse(lessons.isEmpty() ? null : lessons.get(0));

        ModuleProgress progress = progressDao
                .findByUserEmailAndModuleId(auth.getName(), moduleId)
                .orElse(null);

        int progressValue = progress != null ? progress.getProgress() : 0;
        module.setProgress(progressValue);

        updateDashboardStats(
                moduleDao.findAllWithLessonsAndQuiz(),
                auth.getName(),
                model
        );

        model.addAttribute("currentView", VIEW_NAME);
        model.addAttribute("user", buildUser(auth));
        model.addAttribute("selectedModule", module);
        model.addAttribute("lessons", lessons);
        model.addAttribute("selectedLesson", selectedLesson);

        return APP_LAYOUT;
    }

    // ================= MARK LESSON COMPLETE =================
    @PostMapping("/lesson/complete")
    public String completeLesson(@RequestParam Long moduleId,
                                 @RequestParam Long lessonId,
                                 Authentication auth) {

        learningService.completeLesson(auth.getName(), moduleId);

        return "redirect:/learning/module?moduleId=" + moduleId + "&lessonId=" + lessonId;
    }

    // ================= QUIZ PAGE =================
    @GetMapping("/quiz")
    public String quiz(@RequestParam Long moduleId,
                       Authentication auth,
                       Model model) {

        ModuleProgress progress = progressDao
                .findByUserEmailAndModuleId(auth.getName(), moduleId)
                .orElse(null);

        if (progress == null || progress.getProgress() < 100) {
            return "redirect:/learning/module?moduleId=" + moduleId;
        }

        LearningModule module = moduleDao.findByIdWithDetails(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        List<QuizQuestion> questions = module.getQuizzes().stream()
                .sorted(Comparator.comparing(QuizQuestion::getId))
                .collect(Collectors.toList());

        updateDashboardStats(
                moduleDao.findAllWithLessonsAndQuiz(),
                auth.getName(),
                model
        );

        model.addAttribute("module", module);
        model.addAttribute("quizQuestions", questions);
        model.addAttribute("user", buildUser(auth));
        model.addAttribute("currentView", VIEW_NAME);
        model.addAttribute("selectedModule", module);
        model.addAttribute("selectedLesson", null);

        return APP_LAYOUT;
    }

    // ================= SUBMIT QUIZ =================
    @PostMapping("/quiz/submit")
    public String submitQuiz(@RequestParam Long moduleId,
                             @RequestParam Map<String, String> answers,
                             Authentication auth,
                             Model model) {

        LearningModule module = moduleDao.findByIdWithDetails(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        List<QuizQuestion> questions = module.getQuizzes().stream()
                .sorted(Comparator.comparing(QuizQuestion::getId))
                .collect(Collectors.toList());

        int correct = 0;
        for (QuizQuestion q : questions) {
            String ans = answers.get("question-" + q.getId());
            if (ans != null && Integer.parseInt(ans) == q.getCorrectAnswer()) {
                correct++;
            }
        }

        int score = questions.isEmpty()
                ? 0
                : (int) Math.round((double) correct / questions.size() * 100);

        if (score >= 70) {
            learningService.completeModule(auth.getName(), moduleId);
        }

        updateDashboardStats(
                moduleDao.findAllWithLessonsAndQuiz(),
                auth.getName(),
                model
        );

        model.addAttribute("score", score);
        model.addAttribute("module", module);
        model.addAttribute("quizQuestions", questions);
        model.addAttribute("user", buildUser(auth));
        model.addAttribute("currentView", VIEW_NAME);
        model.addAttribute("selectedModule", module);
        model.addAttribute("selectedLesson", null);

        return APP_LAYOUT;
    }

    // ================= HELPERS =================
    private void updateDashboardStats(List<LearningModule> modules,
                                      String email,
                                      Model model) {

        long completed = 0;
        long inProgress = 0;

        for (LearningModule m : modules) {
            ModuleProgress p =
                    progressDao.findByUserEmailAndModuleId(email, m.getId())
                            .orElse(null);

            int val = p != null ? p.getProgress() : 0;
            m.setProgress(val);

            if (val == 100) completed++;
            else if (val > 0) inProgress++;
        }
        Gamification profile = gamificationService.getUserGamificationProfile(email);
        int userPoints = profile != null ? profile.getXpPoints() : 0;

        model.addAttribute("modules", modules);
        model.addAttribute("completedCount", completed);
        model.addAttribute("inProgressCount", inProgress);
        model.addAttribute("totalModules", modules.size());
        model.addAttribute("userPoints", userPoints);
    }

    private User buildUser(Authentication auth) {
        User u = new User();
        u.setEmail(auth.getName());
        u.setName(auth.getName().split("@")[0]);
        return u;
    }
}
