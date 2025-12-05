package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// --- Nested Model Classes (Replacing TypeScript Interfaces) ---

public class LearningModuleData implements Serializable {

    public static class Lesson implements Serializable {
        private final int id;
        private final String title;
        private final String duration;
        private final String type;
        private boolean completed;
        private final String url; // videoUrl or infographicUrl

        public Lesson(int id, String title, String duration, String type, boolean completed, String url) {
            this.id = id;
            this.title = title;
            this.duration = duration;
            this.type = type;
            this.completed = completed;
            this.url = url;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDuration() { return duration; }
        public String getType() { return type; }
        public boolean isCompleted() { return completed; }
        public String getUrl() { return url; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    public static class QuizQuestion implements Serializable {
        private final int id;
        private final String question;
        private final List<String> options;
        private final int correctAnswer; // Index of the correct option

        public QuizQuestion(int id, String question, List<String> options, int correctAnswer) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public int getId() { return id; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getCorrectAnswer() { return correctAnswer; }
    }

    public static class Module implements Serializable {
        private final int id;
        private final String title;
        private final String description;
        private final String duration;
        private int progress;
        private final List<Lesson> lessons;
        private final boolean locked;
        private final String category;
        private final List<QuizQuestion> quiz;
        private boolean quizPassed;

        public Module(int id, String title, String description, String duration, int progress, 
                      List<Lesson> lessons, boolean locked, String category, List<QuizQuestion> quiz, boolean quizPassed) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.progress = progress;
            this.lessons = lessons;
            this.locked = locked;
            this.category = category;
            this.quiz = quiz;
            this.quizPassed = quizPassed;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDuration() { return duration; }
        public int getProgress() { return progress; }
        public List<Lesson> getLessons() { return lessons; }
        public boolean isLocked() { return locked; }
        public String getCategory() { return category; }
        public List<QuizQuestion> getQuiz() { return quiz; }
        public boolean isQuizPassed() { return quizPassed; }

        public void setProgress(int progress) { this.progress = progress; }
        public void setQuizPassed(boolean quizPassed) { this.quizPassed = quizPassed; }

        public void updateProgress() {
            if (lessons.isEmpty()) {
                this.progress = 0;
                return;
            }
            long completedLessons = lessons.stream().filter(Lesson::isCompleted).count();
            this.progress = (int) Math.round((double) completedLessons / lessons.size() * 100);
        }
    }

    // --- Static Initial Data (Replicating the useState array) ---

    public static List<Module> getInitialModules() {
        return Arrays.asList(
            new Module(
                1, 
                "Understanding Stress and Anxiety",
                "Learn about common mental health challenges faced by students",
                "45 min", 85,
                Arrays.asList(
                    new Lesson(1, "What is Stress?", "8 min", "video", true, "https://www.youtube.com/embed/dQw4w9WgXcQ"),
                    new Lesson(2, "Types of Anxiety Disorders", "10 min", "infographic", true, "https://via.placeholder.com/800x1200/4F46E5/FFFFFF?text=Types+of+Anxiety+Disorders"),
                    new Lesson(3, "Stress vs Anxiety: Understanding the Difference", "12 min", "video", true, "https://www.youtube.com/embed/dQw4w9WgXcQ"),
                    new Lesson(4, "Common Triggers and Symptoms", "7 min", "infographic", false, "https://via.placeholder.com/800x1200/8B5CF6/FFFFFF?text=Anxiety+Triggers+%26+Symptoms")
                ), 
                false, 
                "Fundamentals",
                Arrays.asList(
                    new QuizQuestion(1, "What is the primary difference between stress and anxiety?", Arrays.asList("Stress is short-term, anxiety is long-term", "Stress has a specific trigger, anxiety may not", "They are the same thing", "Stress is always harmful, anxiety is not"), 1),
                    new QuizQuestion(2, "Which of the following is NOT a common symptom of anxiety?", Arrays.asList("Rapid heartbeat", "Excessive worrying", "Improved concentration", "Restlessness"), 2),
                    new QuizQuestion(3, "What percentage of college students experience significant anxiety?", Arrays.asList("About 10%", "About 30%", "About 60%", "About 90%"), 2)
                ), 
                false
            ),
            new Module(
                2, 
                "Mindfulness and Meditation Basics", 
                "Develop practical mindfulness skills for daily life", 
                "60 min", 60,
                Arrays.asList(
                    new Lesson(1, "Introduction to Mindfulness", "10 min", "video", true, "https://www.youtube.com/embed/dQw4w9WgXcQ"),
                    new Lesson(2, "Breathing Techniques", "15 min", "video", true, "https://www.youtube.com/embed/dQw4w9WgXcQ"),
                    new Lesson(3, "Body Scan Meditation Guide", "12 min", "infographic", false, "https://via.placeholder.com/800x1200/10B981/FFFFFF?text=Body+Scan+Meditation")
                ), 
                false, 
                "Practice",
                Arrays.asList(
                    new QuizQuestion(1, "What is the main goal of mindfulness practice?", Arrays.asList("To eliminate all thoughts", "To be present in the moment", "To fall asleep faster", "To increase energy levels"), 1),
                    new QuizQuestion(2, "How long should beginners practice mindfulness each day?", Arrays.asList("1 hour", "30 minutes", "5-10 minutes", "3 hours"), 2)
                ), 
                false
            ),
            new Module(3, "Building Resilience", "Strengthen your ability to cope with challenges", "50 min", 45, Collections.emptyList(), false, "Skills", Collections.emptyList(), false),
            new Module(4, "Depression Awareness", "Understanding depression and seeking help", "55 min", 0, Collections.emptyList(), false, "Awareness", Collections.emptyList(), false),
            new Module(5, "Social Connection & Support", "Building healthy relationships and support networks", "40 min", 0, Collections.emptyList(), false, "Social", Collections.emptyList(), false),
            new Module(6, "Advanced Coping Strategies", "Advanced techniques for managing mental health", "70 min", 0, Collections.emptyList(), true, "Advanced", Collections.emptyList(), false)
        );
    }
}