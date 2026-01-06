package com.secj3303.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;
import com.secj3303.repository.LearningModuleRepository;

@Component
public class LearningDataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private boolean initialized = false;
    private final LearningModuleRepository moduleRepo;

    public LearningDataInitializer(LearningModuleRepository moduleRepo) {
        this.moduleRepo = moduleRepo;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) return;
        if (moduleRepo.count() > 0) return;

        initialized = true;

        // =================================================
        // MODULE 1
        // =================================================
        LearningModule m1 = new LearningModule();
        m1.setTitle("Understanding Stress and Anxiety");
        m1.setDescription("Learn about common mental health challenges faced by students");
        m1.setDuration("45 min");
        m1.setCategory("Fundamentals");
        m1.setLocked(false);

        // Using LinkedHashSet to ensure order is preserved during the save process
        Set<Lesson> lessons1 = new LinkedHashSet<>();

        Lesson l11 = new Lesson();
        l11.setTitle("What is Stress?");
        l11.setDuration("8 min");
        l11.setType("video");
        l11.setUrl("https://www.youtube.com/embed/dQw4w9WgXcQ");
        l11.setModule(m1);
        l11.setCompleted(false);
        lessons1.add(l11);

        Lesson l12 = new Lesson();
        l12.setTitle("Types of Anxiety Disorders");
        l12.setDuration("10 min");
        l12.setType("infographic");
        l12.setUrl("https://via.placeholder.com/800x1200");
        l12.setModule(m1);
        l12.setCompleted(false);
        lessons1.add(l12);

        Lesson l13 = new Lesson();
        l13.setTitle("Stress vs Anxiety");
        l13.setDuration("12 min");
        l13.setType("video");
        l13.setUrl("https://www.youtube.com/embed/dQw4w9WgXcQ");
        l13.setModule(m1);
        l13.setCompleted(false);
        lessons1.add(l13);

        m1.setLessons(lessons1);

        Set<QuizQuestion> quiz1 = new LinkedHashSet<>();

        QuizQuestion q11 = new QuizQuestion();
        q11.setQuestion("What is the primary difference between stress and anxiety?");
        // Options stay as List.of because QuizQuestion model uses List for @ElementCollection
        q11.setOptions(List.of(
                "Stress is short-term, anxiety is long-term",
                "Stress has a specific trigger, anxiety may not",
                "They are the same thing"
        ));
        q11.setCorrectAnswer(1);
        q11.setModule(m1);
        quiz1.add(q11);

        m1.setQuiz(quiz1);
        moduleRepo.save(m1);

        // =================================================
        // MODULE 2
        // =================================================
        LearningModule m2 = new LearningModule();
        m2.setTitle("Mindfulness and Meditation Basics");
        m2.setDescription("Develop practical mindfulness skills for daily life");
        m2.setDuration("60 min");
        m2.setCategory("Practice");
        m2.setLocked(false);

        Set<Lesson> lessons2 = new LinkedHashSet<>();

        Lesson l21 = new Lesson();
        l21.setTitle("Introduction to Mindfulness");
        l21.setDuration("10 min");
        l21.setType("video");
        l21.setUrl("https://www.youtube.com/embed/dQw4w9WgXcQ");
        l21.setModule(m2);
        lessons2.add(l21);

        m2.setLessons(lessons2);

        Set<QuizQuestion> quiz2 = new LinkedHashSet<>();
        QuizQuestion q21 = new QuizQuestion();
        q21.setQuestion("What is the goal of mindfulness?");
        q21.setOptions(List.of("Eliminate thoughts", "Be present in the moment", "Sleep faster"));
        q21.setCorrectAnswer(1);
        q21.setModule(m2);
        quiz2.add(q21);

        m2.setQuiz(quiz2);
        moduleRepo.save(m2);

        // =================================================
        // MODULES 3–6 (Empty Content)
        // =================================================
        moduleRepo.save(createSimpleModule("Building Resilience", "Strengthen your ability...", "50 min", "Skills", false));
        moduleRepo.save(createSimpleModule("Depression Awareness", "Understanding depression...", "55 min", "Awareness", false));
        moduleRepo.save(createSimpleModule("Social Connection & Support", "Building healthy relationships", "40 min", "Social", false));
        moduleRepo.save(createSimpleModule("Advanced Coping Strategies", "Advanced techniques...", "70 min", "Advanced", true));
    }

    private LearningModule createSimpleModule(String title, String description, String duration, String category, boolean locked) {
        LearningModule m = new LearningModule();
        m.setTitle(title);
        m.setDescription(description);
        m.setDuration(duration);
        m.setCategory(category);
        m.setLocked(locked);
        m.setLessons(new LinkedHashSet<>());
        m.setQuiz(new LinkedHashSet<>());
        return m;
    }
}