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
            // Optional: Update existing lessons with content if missing
            updateExistingLessonsWithContent();
            initialized = true;
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
        m1.setCategory("Stress Management");
        m1.setLocked(false);

        Set<Lesson> lessons1 = new LinkedHashSet<>();

        lessons1.add(createLesson(
                "What Is Stress?",
                "7 min",
                "video",
                "https://www.youtube.com/embed/hnpQrMqDoqE",
                m1,
                """
                Stress is the body's natural response to pressure, demands, or threats. 
                It triggers the "fight-or-flight" response, releasing hormones like cortisol 
                and adrenaline. While short-term stress can be motivating, chronic stress 
                can negatively impact both physical and mental health.
                
                **Common Causes:**
                - Work pressure
                - Financial worries
                - Relationship issues
                - Major life changes
                - Health concerns
                
                **Physical Symptoms:**
                - Headaches
                - Fatigue
                - Muscle tension
                - Sleep disturbances
                
                **Emotional Symptoms:**
                - Irritability
                - Anxiety
                - Depression
                - Feeling overwhelmed
                """
        ));

        lessons1.add(createLesson(
                "What Is Anxiety?",
                "9 min",
                "video",
                "https://www.youtube.com/embed/z-IR48Mb3W0",
                m1,
                """
                Anxiety is characterized by persistent, excessive worry that doesn't go away 
                even in the absence of a stressor. Unlike stress, which is usually tied to 
                a specific situation, anxiety can be more generalized and long-lasting.
                
                **Types of Anxiety Disorders:**
                - Generalized Anxiety Disorder (GAD)
                - Panic Disorder
                - Social Anxiety Disorder
                - Specific Phobias
                - Separation Anxiety Disorder
                
                **Common Symptoms:**
                - Excessive worry
                - Restlessness
                - Difficulty concentrating
                - Muscle tension
                - Sleep problems
                - Panic attacks
                
                **Treatment Options:**
                - Cognitive Behavioral Therapy (CBT)
                - Medication (SSRIs, SNRIs)
                - Lifestyle changes
                - Mindfulness and meditation
                - Support groups
                """
        ));

        lessons1.add(createLesson(
                "Stress vs Anxiety Explained",
                "10 min",
                "video",
                "https://www.youtube.com/embed/YsWz4jZ7M1Q",
                m1,
                """
                While stress and anxiety share similar symptoms, they have important differences:
                
                **STRESS:**
                - Usually has an identifiable cause
                - Tends to be short-term
                - Often related to external pressures
                - Can be positive (eustress) or negative (distress)
                - Typically subsides when the stressor is removed
                
                **ANXIETY:**
                - May not have a clear trigger
                - Can persist even when stressors are removed
                - Often involves excessive worry about future events
                - May require professional treatment
                - Can be a chronic condition
                
                **Similarities:**
                - Both can cause physical symptoms (headaches, fatigue)
                - Both can affect sleep and concentration
                - Both may involve feelings of worry or fear
                - Both can benefit from similar coping strategies
                
                **Key Takeaway:**
                Understanding the difference helps in choosing appropriate coping strategies 
                and knowing when to seek professional help. While stress management techniques 
                can help with anxiety, persistent anxiety may require specialized treatment.
                """
        ));

        m1.setLessons(lessons1);

        Set<QuizQuestion> quiz1 = new LinkedHashSet<>();

        quiz1.add(createQuiz(
                "Which statement best describes anxiety?",
                List.of(
                        "A short-term response to pressure",
                        "A constant feeling of worry without a clear trigger",
                        "A physical illness only",
                        "A temporary feeling of sadness"
                ),
                1,
                m1
        ));

        quiz1.add(createQuiz(
                "Stress usually occurs when:",
                List.of(
                        "There is no identifiable cause",
                        "A specific demand or pressure is present",
                        "You are always relaxed",
                        "You are sleeping"
                ),
                1,
                m1
        ));

        quiz1.add(createQuiz(
                "Which of these is a physical symptom of stress?",
                List.of(
                        "Increased creativity",
                        "Headaches and muscle tension",
                        "Improved memory",
                        "Better sleep"
                ),
                1,
                m1
        ));

        m1.setQuizzes(quiz1);
        moduleDao.save(m1);

        // =================================================
        // MODULE 2: MINDFULNESS & MEDITATION
        // =================================================
        LearningModule m2 = new LearningModule();
        m2.setTitle("Mindfulness and Meditation Basics");
        m2.setDescription("Learn how mindfulness improves focus and emotional balance");
        m2.setDuration("60 min");
        m2.setCategory("Wellbeing");
        m2.setLocked(false);

        Set<Lesson> lessons2 = new LinkedHashSet<>();

        lessons2.add(createLesson(
                "Introduction to Mindfulness",
                "8 min",
                "video",
                "https://www.youtube.com/embed/inpok4MKVLM",
                m2,
                """
                Mindfulness is the practice of purposely bringing one's attention to the 
                present moment without judgment. It's about observing thoughts, feelings, 
                and sensations as they arise, without getting caught up in them.
                
                **Key Principles of Mindfulness:**
                1. **Present-Moment Awareness:** Focusing on the here and now
                2. **Non-Judgmental Observation:** Watching thoughts without labeling them as good or bad
                3. **Acceptance:** Allowing experiences to be as they are
                4. **Beginner's Mind:** Seeing things as if for the first time
                5. **Patience:** Allowing things to unfold in their own time
                
                **Simple Mindfulness Exercise:**
                1. Find a quiet place to sit comfortably
                2. Close your eyes and take three deep breaths
                3. Bring your attention to your breath
                4. Notice the sensation of air entering and leaving your body
                5. When your mind wanders (and it will), gently bring it back to your breath
                6. Continue for 5-10 minutes
                
                **Benefits You Might Notice:**
                - Reduced mind wandering
                - Increased awareness of thoughts and feelings
                - Greater sense of calm
                - Improved ability to focus
                """
        ));

        lessons2.add(createLesson(
                "Benefits of Mindfulness",
                "6 min",
                "video",
                "https://www.youtube.com/embed/w6T02g5hnT4",
                m2,
                """
                Research has shown numerous benefits of mindfulness practice:
                
                **Mental Health Benefits:**
                - Reduces symptoms of anxiety and depression by 30-40%
                - Improves emotional regulation and reduces reactivity
                - Enhances focus and concentration
                - Decreases rumination and negative thinking patterns
                - Increases self-awareness and self-compassion
                
                **Physical Health Benefits:**
                - Lowers blood pressure and heart rate
                - Improves sleep quality and duration
                - Reduces chronic pain intensity
                - Boosts immune system function
                - Reduces inflammation markers
                
                **Cognitive Benefits:**
                - Increases working memory capacity by 15-20%
                - Enhances decision-making abilities
                - Improves cognitive flexibility
                - Reduces age-related cognitive decline
                - Increases gray matter density in brain regions associated with learning and memory
                
                **Workplace Benefits:**
                - Reduces burnout and stress
                - Improves job satisfaction
                - Enhances creativity and problem-solving
                - Improves interpersonal relationships
                - Increases resilience to workplace challenges
                
                **How to Start:**
                Begin with just 5-10 minutes per day. Consistency is more important than duration.
                Use apps like Headspace, Calm, or Insight Timer for guided practices.
                """
        ));

        lessons2.add(createLesson(
                "Guided Breathing Exercise",
                "10 min",
                "video",
                "https://www.youtube.com/embed/SEfs5TJZ6Nk",
                m2,
                """
                Breathing exercises are a fundamental mindfulness practice. They help activate 
                the parasympathetic nervous system, promoting relaxation and reducing stress.
                
                **4-7-8 Breathing Technique (Dr. Andrew Weil):**
                1. Sit comfortably with your back straight
                2. Place the tip of your tongue against the roof of your mouth, just behind your front teeth
                3. Exhale completely through your mouth, making a "whoosh" sound
                4. Close your mouth and inhale quietly through your nose for 4 seconds
                5. Hold your breath for 7 seconds
                6. Exhale completely through your mouth for 8 seconds, making the "whoosh" sound again
                7. Repeat this cycle 4 times
                
                **Box Breathing (Navy SEAL Technique):**
                1. Inhale slowly through your nose for 4 seconds
                2. Hold your breath for 4 seconds
                3. Exhale slowly through your mouth for 4 seconds
                4. Hold your breath for 4 seconds
                5. Repeat for 5-10 cycles
                
                **Diaphragmatic Breathing (Belly Breathing):**
                1. Lie on your back with knees bent or sit comfortably
                2. Place one hand on your chest and the other on your belly
                3. Inhale slowly through your nose, feeling your belly rise
                4. Keep your chest relatively still
                5. Exhale slowly through pursed lips
                6. Practice for 5-10 minutes
                
                **Benefits of Breathing Exercises:**
                - Reduces stress and anxiety
                - Lowers blood pressure
                - Improves focus and concentration
                - Enhances sleep quality
                - Boosts energy levels
                - Improves digestion
                
                **When to Practice:**
                - Morning: To start the day calmly
                - Before stressful events
                - During breaks at work
                - Evening: To unwind before bed
                - Anytime you feel stressed or anxious
                """
        ));

        m2.setLessons(lessons2);

        Set<QuizQuestion> quiz2 = new LinkedHashSet<>();

        quiz2.add(createQuiz(
                "What is the main goal of mindfulness?",
                List.of(
                        "To stop thinking completely",
                        "To stay present and aware",
                        "To avoid emotions",
                        "To achieve enlightenment"
                ),
                1,
                m2
        ));

        quiz2.add(createQuiz(
                "Mindfulness practice can help reduce:",
                List.of(
                        "Awareness",
                        "Stress and emotional reactivity",
                        "Memory",
                        "Physical strength"
                ),
                1,
                m2
        ));

        quiz2.add(createQuiz(
                "The 4-7-8 breathing technique involves:",
                List.of(
                        "Inhale 4s, Hold 7s, Exhale 8s",
                        "Inhale 8s, Hold 4s, Exhale 7s",
                        "Inhale 7s, Hold 8s, Exhale 4s",
                        "Inhale 4s, Exhale 4s, Hold 4s"
                ),
                0,
                m2
        ));

        m2.setQuizzes(quiz2);
        moduleDao.save(m2);

        // =================================================
        // MODULE 3: BUILDING RESILIENCE
        // =================================================
        LearningModule m3 = new LearningModule();
        m3.setTitle("Building Resilience");
        m3.setDescription("Learn how to adapt and recover from challenges");
        m3.setDuration("50 min");
        m3.setCategory("Wellbeing");
        m3.setLocked(false);

        Set<Lesson> lessons3 = new LinkedHashSet<>();

        lessons3.add(createLesson(
                "What is Resilience?",
                "8 min",
                "article",
                "https://www.apa.org/topics/resilience",
                m3,
                """
                Resilience is the process of adapting well in the face of adversity, trauma, 
                tragedy, threats, or significant sources of stress. It's not about avoiding 
                difficulties but about bouncing back from them.
                
                **Characteristics of Resilient People:**
                - Realistic optimism
                - Emotional regulation
                - Strong problem-solving skills
                - Sense of purpose
                - Social support network
                - Adaptability to change
                
                **Myths About Resilience:**
                ❌ Myth: Resilient people don't experience negative emotions
                ✅ Truth: They experience emotions but manage them effectively
                
                ❌ Myth: Resilience is an innate trait you're born with
                ✅ Truth: Resilience can be developed and strengthened
                
                ❌ Myth: Resilient people handle everything on their own
                ✅ Truth: Social support is crucial for resilience
                
                **The Science of Resilience:**
                Research shows that resilience involves:
                - Neuroplasticity: The brain's ability to reorganize itself
                - Stress inoculation: Building tolerance through manageable challenges
                - Positive psychology: Focusing on strengths rather than weaknesses
                
                Resilience is like a muscle - it grows stronger with practice and use.
                """
        ));

        lessons3.add(createLesson(
                "Developing a Growth Mindset",
                "10 min",
                "video",
                "https://www.youtube.com/embed/hiiEeMN7vbQ",
                m3,
                """
                A growth mindset, coined by psychologist Carol Dweck, is the belief that 
                abilities can be developed through dedication and hard work.
                
                **Fixed Mindset vs Growth Mindset:**
                
                **Fixed Mindset:**
                - Believes intelligence and talent are fixed traits
                - Avoids challenges for fear of failure
                - Gives up easily when faced with obstacles
                - Sees effort as fruitless
                - Ignores constructive criticism
                - Feels threatened by others' success
                
                **Growth Mindset:**
                - Believes abilities can be developed
                - Embraces challenges as opportunities
                - Persists in the face of setbacks
                - Sees effort as the path to mastery
                - Learns from criticism
                - Finds inspiration in others' success
                
                **How to Develop a Growth Mindset:**
                1. **Embrace Challenges:** See them as opportunities to grow
                2. **Learn from Failure:** Ask "What can I learn from this?"
                3. **Value Effort:** Recognize that effort leads to improvement
                4. **Use "Yet":** Instead of "I can't do this," say "I can't do this yet"
                5. **Celebrate Growth:** Focus on progress, not perfection
                6. **Learn from Others:** Find mentors and role models
                
                **Growth Mindset in Practice:**
                - Replace "This is too hard" with "This may take some time and effort"
                - Replace "I made a mistake" with "I learned something new"
                - Replace "I'm not good at this" with "I'm improving with practice"
                - Replace "I give up" with "I'll try a different approach"
                """
        ));

        m3.setLessons(lessons3);

        Set<QuizQuestion> quiz3 = new LinkedHashSet<>();

        quiz3.add(createQuiz(
                "What is resilience?",
                List.of(
                        "Avoiding all difficulties",
                        "Bouncing back from adversity",
                        "Never experiencing stress",
                        "Being physically strong"
                ),
                1,
                m3
        ));

        quiz3.add(createQuiz(
                "A growth mindset believes that:",
                List.of(
                        "Abilities are fixed at birth",
                        "Intelligence cannot be changed",
                        "Abilities can be developed",
                        "Talent is everything"
                ),
                2,
                m3
        ));

        m3.setQuizzes(quiz3);
        moduleDao.save(m3);

        // =================================================
        // MODULE 4: DEPRESSION AWARENESS
        // =================================================
        LearningModule m4 = createSimpleModule(
                "Depression Awareness",
                "Recognize symptoms and understand treatment options",
                "55 min",
                "Depression",
                false
        );

        Set<Lesson> lessons4 = new LinkedHashSet<>();
        lessons4.add(createLesson(
                "Understanding Depression",
                "12 min",
                "article",
                "https://www.nimh.nih.gov/health/topics/depression",
                m4,
                """
                Depression is more than just feeling sad - it's a serious medical condition 
                that affects how you feel, think, and handle daily activities.
                
                **Common Symptoms:**
                - Persistent sad, anxious, or "empty" mood
                - Loss of interest or pleasure in activities once enjoyed
                - Changes in appetite or weight
                - Sleep disturbances (insomnia or oversleeping)
                - Loss of energy or increased fatigue
                - Difficulty concentrating or making decisions
                - Feelings of worthlessness or excessive guilt
                - Thoughts of death or suicide
                
                **Types of Depression:**
                - Major Depressive Disorder
                - Persistent Depressive Disorder (Dysthymia)
                - Seasonal Affective Disorder (SAD)
                - Postpartum Depression
                - Bipolar Depression
                
                **Treatment Options:**
                - Psychotherapy (CBT, IPT, Psychodynamic)
                - Medication (Antidepressants)
                - Brain stimulation therapies (ECT, TMS)
                - Light therapy (for SAD)
                - Lifestyle changes (exercise, diet, sleep)
                
                **When to Seek Help:**
                If symptoms last for more than two weeks and interfere with daily functioning, 
                it's important to seek professional help. Depression is treatable, and early 
                intervention leads to better outcomes.
                """
        ));

        lessons4.add(createLesson(
                "Supporting Someone with Depression",
                "10 min",
                "article",
                "https://www.mhanational.org/helping-someone-depression",
                m4,
                """
                Supporting someone with depression requires patience, understanding, and compassion.
                
                **Do's:**
                ✅ **Educate Yourself:** Learn about depression
                ✅ **Listen Without Judgment:** Let them express their feelings
                ✅ **Offer Practical Help:** Help with daily tasks
                ✅ **Encourage Treatment:** Support them in seeking help
                ✅ **Stay Connected:** Regular check-ins are important
                ✅ **Be Patient:** Recovery takes time
                ✅ **Take Care of Yourself:** You can't pour from an empty cup
                
                **Don'ts:**
                ❌ **Don't Minimize:** Avoid saying "just snap out of it"
                ❌ **Don't Give Advice:** Unless they ask for it
                ❌ **Don't Take It Personally:** Depression affects relationships
                ❌ **Don't Enable Avoidance:** Gently encourage activity
                ❌ **Don't Ignore Suicide Risk:** Take any talk of suicide seriously
                
                **What to Say:**
                - "I'm here for you"
                - "You're not alone in this"
                - "How can I best support you right now?"
                - "This must be really hard for you"
                - "I care about you"
                
                **What Not to Say:**
                - "Just think positive"
                - "Other people have it worse"
                - "You have so much to be happy about"
                - "It's all in your head"
                - "Just get over it"
                
                **Emergency Resources:**
                - National Suicide Prevention Lifeline: 988
                - Crisis Text Line: Text HOME to 741741
                - Emergency Services: 911
                """
        ));

        m4.setLessons(lessons4);
        moduleDao.save(m4);

        // =================================================
        // SIMPLE MODULES (WITHOUT DETAILED LESSONS)
        // =================================================
        LearningModule m5 = createSimpleModule(
                "Social Connection & Support",
                "Build healthy relationships and support systems",
                "40 min",
                "Wellbeing",
                false
        );

        LearningModule m6 = createSimpleModule(
                "Advanced Coping Strategies",
                "Advanced techniques for long-term wellbeing",
                "70 min",
                "Advanced",
                true
        );

        moduleDao.save(m5);
        moduleDao.save(m6);
    }

    // =================================================
    // HELPER METHODS
    // =================================================
    private Lesson createLesson(String title, String duration,
                                String type, String url,
                                LearningModule module, String content) {

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setDuration(duration);
        lesson.setType(type);
        lesson.setUrl(url);
        lesson.setContent(content);  // NEW: Set content
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
        m.setQuizzes(new LinkedHashSet<>());
        return m;
    }

    // Optional: Update existing lessons with content if missing
    private void updateExistingLessonsWithContent() {
        List<LearningModule> modules = moduleDao.findAllWithLessonsAndQuiz();
        
        boolean updated = false;
        for (LearningModule module : modules) {
            for (Lesson lesson : module.getLessons()) {
                if (lesson.getContent() == null || lesson.getContent().trim().isEmpty()) {
                    // Add default content based on lesson title
                    String defaultContent = createDefaultContent(lesson.getTitle());
                    lesson.setContent(defaultContent);
                    updated = true;
                }
            }
        }
        
        if (updated) {
            // Save all modules to persist changes
            for (LearningModule module : modules) {
                moduleDao.save(module);
            }
        }
    }

    private String createDefaultContent(String title) {
        // Create appropriate default content based on lesson title
        if (title.toLowerCase().contains("stress")) {
            return "This lesson covers important information about stress management and coping strategies.";
        } else if (title.toLowerCase().contains("anxiety")) {
            return "This lesson explores anxiety symptoms, causes, and effective treatment approaches.";
        } else if (title.toLowerCase().contains("mindfulness")) {
            return "Learn mindfulness techniques to improve focus, reduce stress, and enhance wellbeing.";
        } else if (title.toLowerCase().contains("resilience")) {
            return "Discover how to build resilience and bounce back from life's challenges.";
        } else if (title.toLowerCase().contains("depression")) {
            return "Understand depression symptoms, treatment options, and support resources.";
        } else {
            return "This lesson provides valuable information and practical strategies for improving mental health and wellbeing.";
        }
    }
}