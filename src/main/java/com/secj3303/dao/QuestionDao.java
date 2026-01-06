package com.secj3303.dao;

import com.secj3303.model.Question;
import java.util.List;

public interface QuestionDao extends GenericDao<Question> {
    List<Question> findByAssessmentId(Integer assessmentId);
    List<Question> findByType(String type);
}