package com.secj3303.dao;

import com.secj3303.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDao extends GenericDao<Student> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);
    List<Student> findByRiskLevel(String riskLevel);
    List<Student> findByDepartment(String department);
    List<Student> findByYear(String year);
    List<Student> findWithAssessmentCount();
}