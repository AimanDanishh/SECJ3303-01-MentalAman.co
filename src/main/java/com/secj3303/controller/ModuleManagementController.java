package com.secj3303.controller;

import java.util.List;

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
    public String root() {
        return "redirect:/learning/manage/modules";
    }

    // ================= MODULE =================

    @GetMapping("/modules")
    public String listModules(Model model) {
        model.addAttribute("modules", moduleDao.findAllWithLessonsAndQuiz());
        model.addAttribute("currentView", "counsellor/modules");
        return APP_LAYOUT;
    }

    @GetMapping("/modules/create")
    public String createModule(Model model) {
        model.addAttribute("module", new LearningModule());
        model.addAttribute("currentView", "counsellor/module-form");
        return APP_LAYOUT;
    }

    @GetMapping("/modules/edit/{id}")
    public String editModule(@PathVariable Long id, Model model) {
        model.addAttribute("module",
                moduleDao.findByIdWithDetails(id)
                        .orElseThrow(() -> new RuntimeException("Module not found")));
        model.addAttribute("currentView", "counsellor/module-form");
        return APP_LAYOUT;
    }

    @PostMapping("/modules/save")
    public String saveModule(@ModelAttribute LearningModule form) {

        LearningModule module = (form.getId() != null)
                ? moduleDao.findByIdWithDetails(form.getId())
                    .orElseThrow(() -> new RuntimeException("Module not found"))
                : new LearningModule();

        module.setTitle(form.getTitle());
        module.setDescription(form.getDescription());
        module.setCategory(form.getCategory());
        module.setDuration(form.getDuration());
        module.setLocked(form.isLocked());

        moduleDao.save(module);
        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/modules/delete/{id}")
    public String deleteModule(@PathVariable Long id) {
        moduleDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }

    // ================= LESSON =================

    @GetMapping("/lessons/create")
    public String createLesson(@RequestParam Long moduleId, Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("currentView", "counsellor/lesson-form");
        return APP_LAYOUT;
    }

    @GetMapping("/lessons/edit/{id}")
    public String editLesson(@PathVariable Long id, Model model) {
        Lesson lesson = lessonDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        model.addAttribute("lesson", lesson);
        model.addAttribute("moduleId", lesson.getModule().getId());
        model.addAttribute("currentView", "counsellor/lesson-form");
        return APP_LAYOUT;
    }

    @PostMapping("/lessons/save")
    public String saveLesson(@ModelAttribute Lesson form,
                             @RequestParam Long moduleId) {

        Lesson lesson;

        if (form.getId() != null) {
            lesson = lessonDao.findById(form.getId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
        } else {
            lesson = new Lesson();
            lesson.setModule(
                    moduleDao.findById(moduleId)
                            .orElseThrow(() -> new RuntimeException("Module not found"))
            );
        }

        lesson.setTitle(form.getTitle());
        lesson.setContent(form.getContent());
        lesson.setDuration(form.getDuration());
        lesson.setType(form.getType());
        lesson.setUrl(form.getUrl());

        lessonDao.save(lesson);
        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/lessons/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {
        lessonDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }

    // ================= QUIZ =================

    @GetMapping("/quizzes/create")
    public String createQuiz(@RequestParam Long moduleId, Model model) {
        model.addAttribute("quiz", new QuizQuestion());
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("currentView", "counsellor/quiz-form");
        return APP_LAYOUT;
    }

    @GetMapping("/quizzes/edit/{id}")
    public String editQuiz(@PathVariable Long id, Model model) {
        QuizQuestion quiz = quizDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        model.addAttribute("quiz", quiz);
        model.addAttribute("moduleId", quiz.getModule().getId());
        model.addAttribute("currentView", "counsellor/quiz-form");
        return APP_LAYOUT;
    }

    @PostMapping("/quizzes/save")
    public String saveQuiz(@ModelAttribute QuizQuestion form,
                           @RequestParam Long moduleId,
                           @RequestParam("optionTexts") List<String> optionTexts) {

        QuizQuestion quiz;

        if (form.getId() != null) {
            quiz = quizDao.findById(form.getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            quiz.getOptions().clear();
        } else {
            quiz = new QuizQuestion();
            quiz.setModule(
                    moduleDao.findById(moduleId)
                            .orElseThrow(() -> new RuntimeException("Module not found"))
            );
        }

        quiz.setQuestion(form.getQuestion());
        quiz.getOptions().addAll(optionTexts);
        quiz.setCorrectAnswer(form.getCorrectAnswer());

        quizDao.save(quiz);
        return "redirect:/learning/manage/modules";
    }

    @PostMapping("/quizzes/delete/{id}")
    public String deleteQuiz(@PathVariable Long id) {
        quizDao.deleteById(id);
        return "redirect:/learning/manage/modules";
    }
}
