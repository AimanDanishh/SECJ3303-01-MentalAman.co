package com.secj3303.dao;

import com.secj3303.dao.AssessmentAnswerDao;
import com.secj3303.model.AssessmentAnswer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional
public class AssessmentAnswerDaoHibernate implements AssessmentAnswerDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<AssessmentAnswer> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(AssessmentAnswer.class, id));
    }
    
    @Override
    public List<AssessmentAnswer> findAll() {
        TypedQuery<AssessmentAnswer> query = entityManager.createQuery(
            "SELECT aa FROM AssessmentAnswer aa", AssessmentAnswer.class);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public AssessmentAnswer save(AssessmentAnswer answer) {
        if (answer.getId() == null) {
            entityManager.persist(answer);
            return answer;
        } else {
            return entityManager.merge(answer);
        }
    }
    
    @Override
    @Transactional
    public AssessmentAnswer update(AssessmentAnswer answer) {
        return entityManager.merge(answer);
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        AssessmentAnswer answer = entityManager.find(AssessmentAnswer.class, id);
        if (answer != null) {
            entityManager.remove(answer);
        }
    }
    
    @Override
    @Transactional
    public void delete(AssessmentAnswer answer) {
        entityManager.remove(entityManager.contains(answer) ? answer : entityManager.merge(answer));
    }
    
    @Override
    public List<AssessmentAnswer> findByStudentAndAssessment(Integer studentId, Integer assessmentId) {
        TypedQuery<AssessmentAnswer> query = entityManager.createQuery(
            "SELECT aa FROM AssessmentAnswer aa WHERE aa.studentId = :studentId AND aa.assessmentId = :assessmentId", 
            AssessmentAnswer.class);
        query.setParameter("studentId", studentId);
        query.setParameter("assessmentId", assessmentId);
        return query.getResultList();
    }
    
    @Override
    public Map<Integer, Integer> getAnswerMapByStudentAndAssessment(Integer studentId, Integer assessmentId) {
        List<AssessmentAnswer> answers = findByStudentAndAssessment(studentId, assessmentId);
        Map<Integer, Integer> answerMap = new HashMap<>();
        for (AssessmentAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer.getAnswer());
        }
        return answerMap;
    }
    
    @Override
    @Transactional
    public void saveAnswers(Map<Integer, Integer> answers, Integer assessmentId, Integer studentId) {
        System.out.println("DEBUG: Saving " + answers.size() + " answers for student " + studentId + ", assessment " + assessmentId);
        
        // Debug: Print what's being saved
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            System.out.println("DEBUG: Saving Q" + entry.getKey() + " = " + entry.getValue());
        }
        
        // First get existing answers to merge/update instead of delete
        Map<Integer, Integer> existingAnswers = getAnswerMapByStudentAndAssessment(studentId, assessmentId);
        System.out.println("DEBUG: Found " + existingAnswers.size() + " existing answers");
        
        // Process each answer
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            Integer questionId = entry.getKey();
            Integer answerValue = entry.getValue();
            
            if (existingAnswers.containsKey(questionId)) {
                // Update existing answer - find and update
                TypedQuery<AssessmentAnswer> findQuery = entityManager.createQuery(
                    "SELECT aa FROM AssessmentAnswer aa WHERE aa.studentId = :studentId AND aa.assessmentId = :assessmentId AND aa.questionId = :questionId", 
                    AssessmentAnswer.class);
                findQuery.setParameter("studentId", studentId);
                findQuery.setParameter("assessmentId", assessmentId);
                findQuery.setParameter("questionId", questionId);
                
                try {
                    AssessmentAnswer existingAnswer = findQuery.getSingleResult();
                    existingAnswer.setAnswer(answerValue);
                    entityManager.merge(existingAnswer);
                    System.out.println("DEBUG: Updated answer for Q" + questionId);
                } catch (Exception e) {
                    // If not found, create new
                    createNewAnswer(questionId, answerValue, assessmentId, studentId);
                }
            } else {
                // Create new answer
                createNewAnswer(questionId, answerValue, assessmentId, studentId);
            }
        }
    }

    private void createNewAnswer(Integer questionId, Integer answerValue, Integer assessmentId, Integer studentId) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setQuestionId(questionId);
        answer.setAnswer(answerValue);
        answer.setAssessmentId(assessmentId);
        answer.setStudentId(studentId);
        entityManager.persist(answer);
        System.out.println("DEBUG: Created new answer for Q" + questionId);
    }
    
    @Override
    @Transactional
    public void deleteByStudentAndAssessment(Integer studentId, Integer assessmentId) {
        // FIXED: Use untyped query for DELETE
        javax.persistence.Query query = entityManager.createQuery(
            "DELETE FROM AssessmentAnswer aa WHERE aa.studentId = :studentId AND aa.assessmentId = :assessmentId");
        query.setParameter("studentId", studentId);
        query.setParameter("assessmentId", assessmentId);
        query.executeUpdate();
    }
}