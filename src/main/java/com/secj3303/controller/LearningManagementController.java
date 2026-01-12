package com.secj3303.controller;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;

@Controller
@RequestMapping("/learning/manage")
@PreAuthorize("hasRole('MENTAL_HEALTH_PROFESSIONAL')")
public class LearningManagementController {

    private final LearningModuleDao moduleDao;

    @PersistenceContext
    private EntityManager em;

    public LearningManagementController(LearningModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    // ================= MODULE =================

    @GetMapping("/modules")
    public String modules(Model model) {
        model.addAttribute("modules",
                moduleDao.findAllWithLessonsAndQuiz());
        return "mhp/modules";
    }

    @GetMapping("/modules/create")
    public String createModule(Model model) {
        model.addAttribute("module", new LearningModule());
        return "mhp/module-form";
    }

    @GetMapping("/modules/edit/{id}")
    public String editModule(@PathVariable Long id, Model model) {
        model.addAttribute("module",
                moduleDao.findByIdWithDetails(id).orElseThrow());
        return "mhp/module-form";
    }

    @PostMapping("/modules/save")
    public String saveModule(@ModelAttribute LearningModule module) {
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
        return "mhp/lesson-form";
    }

    @PostMapping("/lessons/save")
    @Transactional
    public String saveLesson(@ModelAttribute Lesson lesson,
                             @RequestParam Long moduleId) {
        LearningModule module = em.find(LearningModule.class, moduleId);
        lesson.setModule(module);
        em.persist(lesson);
        return "redirect:/learning/manage/modules";
    }

    // ================= QUIZ =================

    @GetMapping("/quizzes/create")
    public String createQuiz(@RequestParam Long moduleId, Model model) {
        model.addAttribute("quiz", new QuizQuestion());
        model.addAttribute("moduleId", moduleId);
        return "mhp/quiz-form";
    }

    @PostMapping("/quizzes/save")
    @Transactional
    public String saveQuiz(@ModelAttribute QuizQuestion quiz,
                           @RequestParam Long moduleId) {
        LearningModule module = em.find(LearningModule.class, moduleId);
        quiz.setModule(module);
        em.persist(quiz);
        return "redirect:/learning/manage/modules";
    }
}
