package com.secj3303.service;

import com.secj3303.dao.*;
import com.secj3303.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class AssessmentService {
    
    private final AssessmentDao assessmentDao;
    private final StudentDao studentDao;
    private final QuestionDao questionDao;
    private final AssessmentResultDao assessmentResultDao;
    private final AssessmentAnswerDao assessmentAnswerDao;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Use constructor injection
    public AssessmentService(AssessmentDao assessmentDao, 
                           StudentDao studentDao, 
                           QuestionDao questionDao,
                           AssessmentResultDao assessmentResultDao,
                           AssessmentAnswerDao assessmentAnswerDao) {
        this.assessmentDao = assessmentDao;
        this.studentDao = studentDao;
        this.questionDao = questionDao;
        this.assessmentResultDao = assessmentResultDao;
        this.assessmentAnswerDao = assessmentAnswerDao;
    }
    
    public List<Assessment> getAllAssessments() {
        return assessmentDao.findAllWithQuestions();
    }
    
    public Optional<Assessment> findAssessment(Integer id) {
        return assessmentDao.findByIdWithQuestions(id);
    }

    public List<Student> getAllAssignedStudents() {
        // Get all students
        List<Student> students = studentDao.findAll();
        
        // For each student, calculate assessment count
        for (Student student : students) {
            List<AssessmentResult> history = getStudentAssessmentHistory(student.getId());
            student.setAssessmentCount(history.size());
            
            // Also set last assessment date if available
            if (!history.isEmpty()) {
                student.setLastAssessment(history.get(0).getDate()); // Most recent
            }
        }
        
        return students;
    }
    
    public List<Student> filterStudents(String search, String risk) {
        // Build dynamic query
        StringBuilder jpql = new StringBuilder("SELECT s FROM Student s WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();
        
        if (search != null && !search.trim().isEmpty()) {
            jpql.append(" AND (LOWER(s.name) LIKE :search OR LOWER(s.email) LIKE :search OR s.studentId LIKE :search)");
            parameters.put("search", "%" + search.toLowerCase() + "%");
        }
        
        if (risk != null && !"all".equals(risk)) {
            jpql.append(" AND LOWER(s.riskLevel) = :risk");
            parameters.put("risk", risk.toLowerCase());
        }
        
        jpql.append(" ORDER BY s.name");
        
        TypedQuery<Student> query = entityManager.createQuery(jpql.toString(), Student.class);
        
        // Set parameters
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        
        List<Student> filteredStudents = query.getResultList();
        
        // Set assessment count for each filtered student
        for (Student student : filteredStudents) {
            Long count = getAssessmentCountForStudent(student.getId());
            student.setAssessmentCount(count != null ? count.intValue() : 0);
        }
        
        return filteredStudents;
    }
    
    private Long getAssessmentCountForStudent(Integer studentId) {
        String queryStr = "SELECT COUNT(r.id) FROM AssessmentResult r WHERE r.student.id = :studentId";
        try {
            return entityManager.createQuery(queryStr, Long.class)
                .setParameter("studentId", studentId)
                .getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Transactional
    public AssessmentResult calculateScore(Assessment assessment, Map<Integer, Integer> answers, Student student) {
        // Calculate total score
        int totalScore = answers.values().stream().mapToInt(Integer::intValue).sum();
        
        // Calculate max possible score
        int maxScore = assessment.getQuestions().stream()
            .mapToInt(q -> q.getScaleMax() != null ? q.getScaleMax() : 0)
            .sum();
        
        // Normalize score to 0-100 range
        int normalizedScore = maxScore > 0 ? (int) Math.round((totalScore / (double) maxScore) * 100) : 0;
        
        // Determine severity
        String severity;
        if (normalizedScore < 33) {
            severity = "Mild";
        } else if (normalizedScore < 66) {
            severity = "Moderate";
        } else {
            severity = "Severe";
        }
        
        // Update student risk level
        student.setRiskLevel(severity.toLowerCase());
        studentDao.update(student);
        
        // Create assessment result
        AssessmentResult result = new AssessmentResult();
        result.setAssessmentTitle(assessment.getTitle());
        result.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        result.setScore(normalizedScore);
        result.setSeverity(severity);
        result.setReportAvailable(true);
        result.setStudent(student);
        result.setAssessment(assessment);
        
        // Save the result
        assessmentResultDao.save(result);
        
        // Save individual answers
        assessmentAnswerDao.saveAnswers(answers, assessment.getId(), student.getId());
        
        // CLEAR SAVED PROGRESS after successful submission
        clearAssessmentProgress(student.getId(), assessment.getId());
        
        return result;
    }
    
    // NEW METHOD: Clear assessment progress
    @Transactional
    public void clearAssessmentProgress(Integer studentId, Integer assessmentId) {
        try {
            // Delete all saved answers for this student and assessment
            assessmentAnswerDao.deleteByStudentAndAssessment(studentId, assessmentId);
            System.out.println("Cleared assessment progress for student " + studentId + ", assessment " + assessmentId);
        } catch (Exception e) {
            System.err.println("Error clearing assessment progress: " + e.getMessage());
            // Continue execution even if clearing fails
        }
    }
    
    public List<AssessmentResult> getStudentAssessmentHistory(Integer studentId) {
        return assessmentResultDao.findByStudentId(studentId);
    }
    
    public Optional<AssessmentResult> getLatestAssessmentResult(Integer studentId) {
        return assessmentResultDao.findLatestByStudentId(studentId);
    }
    
    public Optional<Student> getStudentById(Integer studentId) {
        return studentDao.findById(studentId);
    }
    
    public Optional<Student> getStudentByEmail(String email) {
        return studentDao.findByEmail(email);
    }
    
    @Transactional
    public Student saveStudent(Student student) {
        if (student.getId() == null) {
            return studentDao.save(student);
        } else {
            return studentDao.update(student);
        }
    }
    
    public List<String> getScaleLabels(int scaleMax) {
        if (scaleMax == 3) return AssessmentModels.SCALE_LABELS_4;
        if (scaleMax == 4) return AssessmentModels.SCALE_LABELS_5;
        if (scaleMax == 5) return AssessmentModels.SCALE_LABELS_6;
        return new ArrayList<>();
    }
    
    public Map<Integer, Integer> getAssessmentAnswers(Integer studentId, Integer assessmentId) {
        return assessmentAnswerDao.getAnswerMapByStudentAndAssessment(studentId, assessmentId);
    }

    @Transactional
    public void saveAssessmentProgress(Integer studentId, Integer assessmentId, 
                                    Map<Integer, Integer> answers, Integer currentQuestionIndex) {
        try {
            // First, clear any existing answers to avoid duplicates
            clearAssessmentProgress(studentId, assessmentId);
            
            // Now save all new answers
            if (answers != null && !answers.isEmpty()) {
                assessmentAnswerDao.saveAnswers(answers, assessmentId, studentId);
                System.out.println("Saved " + answers.size() + " answers for student " + studentId);
            }
        } catch (Exception e) {
            // Log the error but don't crash
            System.err.println("Error saving assessment progress: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Try alternative approach
            saveAnswersWithTransaction(answers, assessmentId, studentId);
        }
    }

    @Transactional
    private void saveAnswersWithTransaction(Map<Integer, Integer> answers, Integer assessmentId, Integer studentId) {
        // Alternative implementation
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            // Check if answer already exists
            Map<Integer, Integer> existingAnswers = assessmentAnswerDao.getAnswerMapByStudentAndAssessment(studentId, assessmentId);
            
            if (existingAnswers.containsKey(entry.getKey())) {
                // Update existing answer
                List<AssessmentAnswer> existingList = assessmentAnswerDao.findByStudentAndAssessment(studentId, assessmentId);
                for (AssessmentAnswer answer : existingList) {
                    if (answer.getQuestionId().equals(entry.getKey())) {
                        answer.setAnswer(entry.getValue());
                        assessmentAnswerDao.update(answer);
                        break;
                    }
                }
            } else {
                // Create new answer
                AssessmentAnswer answer = new AssessmentAnswer();
                answer.setQuestionId(entry.getKey());
                answer.setAnswer(entry.getValue());
                answer.setAssessmentId(assessmentId);
                answer.setStudentId(studentId);
                assessmentAnswerDao.save(answer);
            }
        }
    }
    
    public List<Question> getQuestionsByAssessmentId(Integer assessmentId) {
        return questionDao.findByAssessmentId(assessmentId);
    }
    
    public long getStudentCountByRiskLevel(String riskLevel) {
        String queryStr = "SELECT COUNT(s) FROM Student s WHERE LOWER(s.riskLevel) = :riskLevel";
        try {
            return entityManager.createQuery(queryStr, Long.class)
                .setParameter("riskLevel", riskLevel.toLowerCase())
                .getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Transactional
    public void deleteAssessmentResult(Integer resultId) {
        assessmentResultDao.delete(resultId);
    }

    public List<Student> getAllStudentWithAssessmentCount() {
        String queryStr = """
            SELECT s, COUNT(r.id) as assessmentCount 
            FROM Student s 
            LEFT JOIN AssessmentResult r ON s.id = r.student.id 
            GROUP BY s.id, s.name, s.email, s.studentId, s.department, s.year, 
                     s.currentGrade, s.attendance, s.lastActivity, s.riskLevel
            ORDER BY s.name
            """;
        
        try {
            List<Object[]> results = entityManager.createQuery(queryStr, Object[].class).getResultList();
            List<Student> students = new ArrayList<>();
            
            for (Object[] result : results) {
                Student student = (Student) result[0];
                Long count = (Long) result[1];
                
                // Set assessment count
                student.setAssessmentCount(count != null ? count.intValue() : 0);
                
                // Set risk level based on latest assessment if available
                if (count != null && count > 0) {
                    try {
                        String latestRiskQuery = """
                            SELECT r.severity FROM AssessmentResult r 
                            WHERE r.student.id = :studentId 
                            ORDER BY r.date DESC 
                            """;
                        
                        List<String> severities = entityManager.createQuery(latestRiskQuery, String.class)
                            .setParameter("studentId", student.getId())
                            .setMaxResults(1)
                            .getResultList();
                        
                        if (!severities.isEmpty()) {
                            student.setRiskLevel(severities.get(0).toLowerCase());
                        }
                    } catch (Exception e) {
                        // Keep existing risk level if we can't determine new one
                    }
                }
                
                // Set assessment history as a transient field
                List<AssessmentResult> history = getStudentAssessmentHistory(student.getId());
                student.setAssessmentHistory(history);
                
                students.add(student);
            }
            
            return students;
        } catch (Exception e) {
            // Fallback to DAO method if JPQL query fails
            List<Student> students = studentDao.findAll();
            for (Student student : students) {
                Long count = getAssessmentCountForStudent(student.getId());
                student.setAssessmentCount(count != null ? count.intValue() : 0);
                
                List<AssessmentResult> history = getStudentAssessmentHistory(student.getId());
                student.setAssessmentHistory(history);
            }
            return students;
        }
    }

    @Transactional(readOnly = true)
    public AssessmentResult getResultWithAssessment(Integer resultId) {
        // Use a JPQL query with JOIN FETCH to load associations
        String queryStr = """
            SELECT r FROM AssessmentResult r 
            LEFT JOIN FETCH r.assessment a 
            LEFT JOIN FETCH a.questions 
            LEFT JOIN FETCH r.student 
            WHERE r.id = :resultId
            """;
        
        try {
            return entityManager.createQuery(queryStr, AssessmentResult.class)
                .setParameter("resultId", resultId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Alternative: If you want to fetch with assessment but not questions
    @Transactional(readOnly = true)
    public AssessmentResult getResultById(Integer resultId) {
        String queryStr = """
            SELECT r FROM AssessmentResult r 
            LEFT JOIN FETCH r.assessment 
            LEFT JOIN FETCH r.student 
            WHERE r.id = :resultId
            """;
        
        try {
            return entityManager.createQuery(queryStr, AssessmentResult.class)
                .setParameter("resultId", resultId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}