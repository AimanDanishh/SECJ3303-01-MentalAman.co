package com.secj3303.dao;

import com.secj3303.model.Assessment;
import java.util.List;
import java.util.Optional;

public interface AssessmentDao extends GenericDao<Assessment> {
    List<Assessment> findByCategory(String category);
    List<Assessment> findAllWithQuestions();
    Optional<Assessment> findByIdWithQuestions(Integer id);
}