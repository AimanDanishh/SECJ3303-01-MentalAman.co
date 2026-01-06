package com.secj3303.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.model.LearningModule;
import com.secj3303.model.Lesson;
import com.secj3303.model.QuizQuestion;

@Component
@Transactional
public class LearningDataInitializer
        implements ApplicationListener<ContextRefreshedEvent> {

    private boolean initialized = false;
    private final LearningModuleDao moduleDao;

    public LearningDataInitializer(LearningModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // ✅ DAO-safe "count" check
        if (initialized || !moduleDao.findAllWithLessonsAndQuiz().isEmpty()) {
            return;
        }
        initialized = true;

        // =================================================
        // MODULE 1: STRESS & ANXIETY
        // =================================================
        LearningModule m1 = new LearningModule();
        m1.setTitle("Understanding Stress and Anxiety");
        m1.setDescription("Understand how stress and anxiety affect the mind and body");
        m1.setDuration("45 min");
        m1.setCategory("Fundamentals");
        m1.setLocked(false);

        Set<Lesson> lessons1 = new LinkedHashSet<>();

        lessons1.add(createLesson(
                "What Is Stress?",
                "7 min",
                "video",
                "https://www.youtube.com/embed/hnpQrMqDoqE",
                m1
        ));

        lessons1.add(createLesson(
                "What Is Anxiety?",
                "9 min",
                "video",
                "https://www.youtube.com/embed/z-IR48Mb3W0",
                m1
        ));

        lessons1.add(createLesson(
                "Stress vs Anxiety Explained",
                "10 min",
                "video",
                "https://www.youtube.com/embed/YsWz4jZ7M1Q",
                m1
        ));

        m1.setLessons(lessons1);

        Set<QuizQuestion> quiz1 = new LinkedHashSet<>();

        quiz1.add(createQuiz(
                "Which statement best describes anxiety?",
                List.of(
                        "A short-term response to pressure",
                        "A constant feeling of worry without a clear trigger",
                        "A physical illness only"
                ),
                1,
                m1
        ));

        quiz1.add(createQuiz(
                "Stress usually occurs when:",
                List.of(
                        "There is no identifiable cause",
                        "A specific demand or pressure is present",
                        "You are always relaxed"
                ),
                1,
                m1
        ));

        m1.setQuiz(quiz1);
        moduleDao.save(m1);

        // =================================================
        // MODULE 2: MINDFULNESS & MEDITATION
        // =================================================
        LearningModule m2 = new LearningModule();
        m2.setTitle("Mindfulness and Meditation Basics");
        m2.setDescription("Learn how mindfulness improves focus and emotional balance");
        m2.setDuration("60 min");
        m2.setCategory("Practice");
        m2.setLocked(false);

        Set<Lesson> lessons2 = new LinkedHashSet<>();

        lessons2.add(createLesson(
                "Introduction to Mindfulness",
                "8 min",
                "video",
                "https://www.youtube.com/embed/inpok4MKVLM",
                m2
        ));

        lessons2.add(createLesson(
                "Benefits of Mindfulness",
                "6 min",
                "video",
                "https://www.youtube.com/embed/w6T02g5hnT4",
                m2
        ));

        lessons2.add(createLesson(
                "Guided Breathing Exercise",
                "10 min",
                "video",
                "https://www.youtube.com/embed/SEfs5TJZ6Nk",
                m2
        ));

        m2.setLessons(lessons2);

        Set<QuizQuestion> quiz2 = new LinkedHashSet<>();

        quiz2.add(createQuiz(
                "What is the main goal of mindfulness?",
                List.of(
                        "To stop thinking completely",
                        "To stay present and aware",
                        "To avoid emotions"
                ),
                1,
                m2
        ));

        quiz2.add(createQuiz(
                "Mindfulness practice can help reduce:",
                List.of(
                        "Awareness",
                        "Stress and emotional reactivity",
                        "Memory"
                ),
                1,
                m2
        ));

        m2.setQuiz(quiz2);
        moduleDao.save(m2);

        // =================================================
        // SIMPLE MODULES
        // =================================================
        moduleDao.save(createSimpleModule(
                "Building Resilience",
                "Learn how to adapt and recover from challenges",
                "50 min",
                "Skills",
                false
        ));

        moduleDao.save(createSimpleModule(
                "Depression Awareness",
                "Recognize symptoms and understand treatment options",
                "55 min",
                "Awareness",
                false
        ));

        moduleDao.save(createSimpleModule(
                "Social Connection & Support",
                "Build healthy relationships and support systems",
                "40 min",
                "Social",
                false
        ));

        moduleDao.save(createSimpleModule(
                "Advanced Coping Strategies",
                "Advanced techniques for long-term wellbeing",
                "70 min",
                "Advanced",
                true
        ));
    }

    // =================================================
    // HELPER METHODS (UNCHANGED)
    // =================================================
    private Lesson createLesson(String title, String duration,
                                String type, String url,
                                LearningModule module) {

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setDuration(duration);
        lesson.setType(type);
        lesson.setUrl(url);
        lesson.setModule(module);
        lesson.setCompleted(false);
        return lesson;
    }

    private QuizQuestion createQuiz(String question,
                                    List<String> options,
                                    int correctAnswer,
                                    LearningModule module) {

        QuizQuestion quiz = new QuizQuestion();
        quiz.setQuestion(question);
        quiz.setOptions(options);
        quiz.setCorrectAnswer(correctAnswer);
        quiz.setModule(module);
        return quiz;
    }

    private LearningModule createSimpleModule(String title,
                                              String description,
                                              String duration,
                                              String category,
                                              boolean locked) {

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
