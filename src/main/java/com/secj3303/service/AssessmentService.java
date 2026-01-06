package com.secj3303.service;

import com.secj3303.dao.*;
import com.secj3303.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Student> allStudents = studentDao.findAll();
        
        return allStudents.stream()
            .filter(student -> {
                boolean matchesSearch = search == null || search.isEmpty() ||
                    student.getName().toLowerCase().contains(search.toLowerCase()) ||
                    student.getStudentId().toLowerCase().contains(search.toLowerCase()) ||
                    student.getEmail().toLowerCase().contains(search.toLowerCase());
                
                boolean matchesRisk = risk == null || "all".equals(risk) || 
                    (student.getRiskLevel() != null && student.getRiskLevel().equalsIgnoreCase(risk));
                
                return matchesSearch && matchesRisk;
            })
            .collect(java.util.stream.Collectors.toList());
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
        List<Student> students = studentDao.findByRiskLevel(riskLevel);
        return students.size();
    }
    
    @Transactional
    public void deleteAssessmentResult(Integer resultId) {
            assessmentResultDao.delete(resultId);
    }

    public List<Student> getAllStudentWithAssessmentCount() {
        // Get all students
        List<Student> students = studentDao.findAll();
        
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        
        // For each student, get their assessment history and set counts
        for (Student student : students) {
            try {
                // Get assessment history for this student
                List<AssessmentResult> history = getStudentAssessmentHistory(student.getId());
                
                // Set the assessment count
                student.setAssessmentCount(history != null ? history.size() : 0);
                
                // Set the last assessment date
                if (history != null && !history.isEmpty()) {
                    // Find the most recent assessment (assuming date format "MMM d, yyyy")
                    AssessmentResult mostRecent = history.stream()
                        .max(Comparator.comparing(r -> {
                            try {
                                // Parse the date string to LocalDate for comparison
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                                return LocalDate.parse(r.getDate(), formatter);
                            } catch (Exception e) {
                                // If parsing fails, return a very old date
                                return LocalDate.MIN;
                            }
                        }))
                        .orElse(history.get(0));
                    
                    student.setLastAssessment(mostRecent.getDate());
                } else {
                    student.setLastAssessment("No assessments");
                }
                
            } catch (Exception e) {
                // If there's an error, set default values
                student.setAssessmentCount(0);
                student.setLastAssessment("Error loading");
            }
        }
        
        return students;
    }
}