package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

import com.secj3303.model.LearningModule;

public interface LearningModuleDao {

    Optional<LearningModule> findById(Long id);

    Optional<LearningModule> findByIdWithDetails(Long id);

    List<LearningModule> findAllWithLessonsAndQuiz();

    void save(LearningModule module);

    void deleteById(Long id);
}
