package com.secj3303.dao;

import com.secj3303.model.AssessmentResult;
import java.util.List;
import java.util.Optional;

public interface AssessmentResultDao extends GenericDao<AssessmentResult> {
    List<AssessmentResult> findByStudentId(Integer studentId);
    List<AssessmentResult> findByAssessmentId(Integer assessmentId);
    List<AssessmentResult> findBySeverity(String severity);
    Optional<AssessmentResult> findLatestByStudentId(Integer studentId);
    Integer getTotalScoreByStudentAndAssessment(Integer studentId, Integer assessmentId);
}