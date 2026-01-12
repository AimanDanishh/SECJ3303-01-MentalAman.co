package com.secj3303.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.dao.LessonDao;
import com.secj3303.dao.QuizQuestionDao;
import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;

@Controller
@RequestMapping("/learning/manage")
@PreAuthorize("hasAnyRole('COUNSELLOR','ADMINISTRATOR')")
public class ModuleManagementController {

    private static final String APP_LAYOUT = "app-layout";

    private final LearningModuleDao moduleDao;
    private final LessonDao lessonDao;
    private final QuizQuestionDao quizDao;

    public ModuleManagementController(
            LearningModuleDao moduleDao,
            LessonDao lessonDao,
            QuizQuestionDao quizDao) {

        this.moduleDao = moduleDao;
        this.lessonDao = lessonDao;
        this.quizDao = quizDao;
    }

    // ================= ROOT =================

    @GetMapping
    public String manageRoot() {
        return "redirect:/learning/manage/modules";
    }

    // ================= MODULES =================

    @GetMapping("/modules")
    public String listModules(Model model) {

        model.addAttribute("modules",
                moduleDao.findAllWithLessonsAndQuiz());

        // MUST match templates/counsellor/modules.html
        model.addAttribute("currentView", "counsellor/modules");
        return APP_LAYOUT;
    }

    @GetMapping("/modules/create")
    public String createModuleForm(Model model) {

        model.addAttribute("module", new LearningModule());

        // MUST match templates/counsellor/module-form.html
        model.addAttribute("currentView", "counsellor/module-form");
        return APP_LAYOUT;
    }

    @GetMapping("/modules/edit/{id}")
    public String editModuleForm(@PathVariable Long id, Model model) {

        LearningModule module = moduleDao
                .findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        model.addAttribute("module", module);
        model.addAttribute("currentView", "counsellor/module-form");

        return APP_LAYOUT;
    }

    @PostMapping("/modules/save")
    public String saveModule(@ModelAttribute LearningModule formModule) {

        LearningModule module;

        if (formModule.getId() != null) {
            // EDIT: load existing module with children
            module = moduleDao.findByIdWithDetails(formModule.getId())
                    .orElseThrow(() -> new RuntimeException("Module not found"));
        } else {
            // CREATE: new module
            module = new LearningModule();
        }

        // Update ONLY simple fields
        module.setTitle(formModule.getTitle());
        module.setDescription(formModule.getDescription());
        module.setCategory(formModule.getCategory());
        module.setDuration(formModule.getDuration());
        module.setLocked(formModule.isLocked());

        moduleDao.save(module);

        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/modules/delete/{id}")
    public String deleteModule(@PathVariable Long id) {

        moduleDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }

    // ================= LESSONS =================

    @GetMapping("/lessons/create")
    public String createLessonForm(@RequestParam Long moduleId, Model model) {

        model.addAttribute("lesson", new Lesson());
        model.addAttribute("moduleId", moduleId);

        // MUST match templates/counsellor/lesson-form.html
        model.addAttribute("currentView", "counsellor/lesson-form");

        return APP_LAYOUT;
    }

    @PostMapping("/lessons/save")
    public String saveLesson(@ModelAttribute Lesson lesson,
                             @RequestParam Long moduleId) {

        LearningModule module = moduleDao.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        lesson.setModule(module);
        lessonDao.save(lesson);

        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/lessons/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {

        lessonDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }

    // ================= QUIZZES =================

    @GetMapping("/quizzes/create")
    public String createQuizForm(@RequestParam Long moduleId, Model model) {

        model.addAttribute("quiz", new QuizQuestion());
        model.addAttribute("moduleId", moduleId);

        // MUST match templates/counsellor/quiz-form.html
        model.addAttribute("currentView", "counsellor/quiz-form");

        return APP_LAYOUT;
    }

    @PostMapping("/quizzes/save")
    public String saveQuiz(@ModelAttribute QuizQuestion quiz,
                           @RequestParam Long moduleId) {

        LearningModule module = moduleDao.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        quiz.setModule(module);
        quizDao.save(quiz);

        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/quizzes/delete/{id}")
    public String deleteQuiz(@PathVariable Long id) {

        quizDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }
}
