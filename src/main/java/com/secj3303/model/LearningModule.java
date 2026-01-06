package com.secj3303.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String duration;
    private String category;
    private boolean locked;

    // Use @OrderBy to ensure the Set is sorted by ID when loaded from DB
    @OneToMany(mappedBy = "module",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @OrderBy("id ASC") 
    private Set<Lesson> lessons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "module",
                cascade = CascadeType.ALL,
                fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<QuizQuestion> quiz = new LinkedHashSet<>();

    // =========================
    // TRANSIENT (UI-ONLY FIELDS)
    // =========================
    @Transient
    private int progress;

    @Transient
    private boolean quizPassed;

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

    // ... getters & setters ...
    
}