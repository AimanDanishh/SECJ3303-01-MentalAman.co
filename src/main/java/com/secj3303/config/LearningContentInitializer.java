package com.secj3303.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;

@Component
@Transactional
public class LearningContentInitializer {

    private final LearningModuleDao moduleDao;

    public LearningContentInitializer(LearningModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {

        // ✅ DAO-safe replacement for moduleRepo.count()
        if (!moduleDao.findAllWithLessonsAndQuiz().isEmpty()) return;

        // =========================
        // MODULE
        // =========================
        LearningModule module = new LearningModule();
        module.setTitle("Understanding Stress and Anxiety");
        module.setDescription("Learn about common mental health challenges");
        module.setDuration("45 min");
        module.setCategory("Fundamentals");
        module.setLocked(false);

        // =========================
        // LESSONS (LinkedHashSet preserves order)
        // =========================
        Set<Lesson> lessons = new LinkedHashSet<>();

        Lesson l1 = new Lesson();
        l1.setTitle("What is Stress?");
        l1.setDuration("8 min");
        l1.setType("video");
        l1.setUrl("https://www.youtube.com/embed/dQw4w9WgXcQ");
        l1.setModule(module);
        lessons.add(l1);

        Lesson l2 = new Lesson();
        l2.setTitle("Types of Anxiety Disorders");
        l2.setDuration("10 min");
        l2.setType("infographic");
        l2.setUrl("https://via.placeholder.com/800x1200");
        l2.setModule(module);
        lessons.add(l2);

        module.setLessons(lessons);

        // =========================
        // QUIZ (Set to match entity)
        // =========================
        Set<QuizQuestion> quiz = new LinkedHashSet<>();

        QuizQuestion q1 = new QuizQuestion();
        q1.setQuestion("Stress differs from anxiety because stress has a trigger");
        q1.setCorrectAnswer(0);
        q1.setOptions(List.of("True", "False"));
        q1.setModule(module);
        quiz.add(q1);

        module.setQuizzes(quiz);

        // =========================
        // SAVE (DAO Hibernate)
        // =========================
        moduleDao.save(module);
    }
}
