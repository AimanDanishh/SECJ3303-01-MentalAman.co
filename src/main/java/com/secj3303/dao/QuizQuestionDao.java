package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

import com.secj3303.model.QuizQuestion;

public interface QuizQuestionDao {

    Optional<QuizQuestion> findById(Long id);

    List<QuizQuestion> findByModuleId(Long moduleId);

    QuizQuestion save(QuizQuestion quiz);

    void deleteById(Long id);
}
