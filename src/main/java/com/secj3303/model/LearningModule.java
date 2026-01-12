package com.secj3303.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "learning_module")
public class LearningModule {

    // =========================
    // PERSISTENT FIELDS
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    private String duration;   // e.g. "45 minutes"

    @Column(nullable = false)
    private String category;

    /**
     * locked = true  → Draft (hidden from students)
     * locked = false → Published
     */
    private boolean locked = true;

    // =========================
    // RELATIONSHIPS
    // =========================

    /**
     * Lessons contain the actual learning content.
     * Ordered by ID to preserve sequence.
     */
    @OneToMany(
        mappedBy = "module",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    private Set<Lesson> lessons = new LinkedHashSet<>();

    /**
     * Quiz questions for the module.
     */
    @OneToMany(
        mappedBy = "module",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    private Set<QuizQuestion> quiz = new LinkedHashSet<>();

    // =========================
    // TRANSIENT (UI-ONLY)
    // =========================

    /**
     * Calculated per user from ModuleProgress
     */
    @Transient
    private int progress;

    /**
     * Used by UI to display quiz completion state
     */
    @Transient
    private boolean quizPassed;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Set<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(Set<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Set<QuizQuestion> getQuiz() {
        return quiz;
    }

    public void setQuiz(Set<QuizQuestion> quiz) {
        this.quiz = quiz;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isQuizPassed() {
        return quizPassed;
    }

    public void setQuizPassed(boolean quizPassed) {
        this.quizPassed = quizPassed;
    }

    // =========================
    // OPTIONAL (GOOD PRACTICE)
    // =========================

    /**
     * Helper methods for bidirectional consistency
     */
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setModule(this);
    }

    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setModule(null);
    }

    public void addQuizQuestion(QuizQuestion question) {
        quiz.add(question);
        question.setModule(this);
    }

    public void removeQuizQuestion(QuizQuestion question) {
        quiz.remove(question);
        question.setModule(null);
    }
}
