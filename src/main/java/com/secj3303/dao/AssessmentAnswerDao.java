package com.secj3303.dao;

import com.secj3303.model.AssessmentAnswer;
import java.util.List;
import java.util.Map;

public interface AssessmentAnswerDao extends GenericDao<AssessmentAnswer> {
    List<AssessmentAnswer> findByStudentAndAssessment(Integer studentId, Integer assessmentId);
    Map<Integer, Integer> getAnswerMapByStudentAndAssessment(Integer studentId, Integer assessmentId);
    void saveAnswers(Map<Integer, Integer> answers, Integer assessmentId, Integer studentId);
    void deleteByStudentAndAssessment(Integer studentId, Integer assessmentId);
}