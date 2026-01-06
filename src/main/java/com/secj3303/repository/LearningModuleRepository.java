package com.secj3303.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secj3303.model.LearningModule;

public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {

    // This method must be defined here for the Controller to see it
    @Query("""
        SELECT DISTINCT m
        FROM LearningModule m
        LEFT JOIN FETCH m.lessons
        LEFT JOIN FETCH m.quiz
        WHERE m.id = :id
    """)
    Optional<LearningModule> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT m
        FROM LearningModule m
        LEFT JOIN FETCH m.lessons
        LEFT JOIN FETCH m.quiz
        ORDER BY m.id
    """)
    List<LearningModule> findAllWithLessonsAndQuiz();
}