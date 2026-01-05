package com.secj3303.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;
import com.secj3303.repository.LearningModuleRepository;

@Component
public class LearningContentInitializer {

    private final LearningModuleRepository moduleRepo;

    public LearningContentInitializer(LearningModuleRepository moduleRepo) {
        this.moduleRepo = moduleRepo;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {

        if (moduleRepo.count() > 0) return;

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
        // LESSONS (Set, NOT List)
        // =========================
        Set<Lesson> lessons = new HashSet<>();

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
        // QUIZ (Set, NOT List)
        // =========================
        Set<QuizQuestion> quiz = new HashSet<>();

        QuizQuestion q1 = new QuizQuestion();
        q1.setQuestion("Stress differs from anxiety because stress has a trigger");
        q1.setCorrectAnswer(0);
        q1.setOptions(Set.of("True", "False")); // must be Set
        q1.setModule(module);
        quiz.add(q1);

        module.setQuiz(quiz);

        // =========================
        // SAVE
        // =========================
        moduleRepo.save(module);
    }
}
