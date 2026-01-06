package com.secj3303.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.secj3303.model.LearningModule;

public interface LearningModuleRepository
        extends JpaRepository<LearningModule, Long> {

    // Fetch BOTH lessons and quiz
    @Query("""
        SELECT DISTINCT m
        FROM LearningModule m
        LEFT JOIN FETCH m.lessons l
        LEFT JOIN FETCH m.quiz q
        ORDER BY m.id, l.id, q.id
    """)
    List<LearningModule> findAllWithLessonsAndQuiz();
}