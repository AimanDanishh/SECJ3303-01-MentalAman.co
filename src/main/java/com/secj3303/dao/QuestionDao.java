package com.secj3303.dao;

import java.util.List;

import com.secj3303.model.Question;

public interface QuestionDao extends GenericDao<Question> {
    List<Question> findByAssessmentId(Integer assessmentId);
    List<Question> findByType(String type);
}