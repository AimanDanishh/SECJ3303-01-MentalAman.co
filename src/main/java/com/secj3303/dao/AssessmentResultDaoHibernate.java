package com.secj3303.dao;

import com.secj3303.dao.AssessmentResultDao;
import com.secj3303.model.AssessmentResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class AssessmentResultDaoHibernate implements AssessmentResultDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<AssessmentResult> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(AssessmentResult.class, id));
    }
    
    @Override
    public List<AssessmentResult> findAll() {
        TypedQuery<AssessmentResult> query = entityManager.createQuery(
            "SELECT ar FROM AssessmentResult ar", AssessmentResult.class);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public AssessmentResult save(AssessmentResult result) {
        if (result.getId() == null) {
            entityManager.persist(result);
            return result;
        } else {
            return entityManager.merge(result);
        }
    }
    
    @Override
    @Transactional
    public AssessmentResult update(AssessmentResult result) {
        return entityManager.merge(result);
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        AssessmentResult result = entityManager.find(AssessmentResult.class, id);
        if (result != null) {
            entityManager.remove(result);
        }
    }
    
    @Override
    @Transactional
    public void delete(AssessmentResult result) {
        entityManager.remove(entityManager.contains(result) ? result : entityManager.merge(result));
    }
    
    @Override
    public List<AssessmentResult> findByStudentId(Integer studentId) {
        TypedQuery<AssessmentResult> query = entityManager.createQuery(
            "SELECT ar FROM AssessmentResult ar WHERE ar.student.id = :studentId ORDER BY ar.date DESC", 
            AssessmentResult.class);
        query.setParameter("studentId", studentId);
        return query.getResultList();
    }
    
    @Override
    public List<AssessmentResult> findByAssessmentId(Integer assessmentId) {
        TypedQuery<AssessmentResult> query = entityManager.createQuery(
            "SELECT ar FROM AssessmentResult ar WHERE ar.assessment.id = :assessmentId ORDER BY ar.date DESC", 
            AssessmentResult.class);
        query.setParameter("assessmentId", assessmentId);
        return query.getResultList();
    }
    
    @Override
    public List<AssessmentResult> findBySeverity(String severity) {
        TypedQuery<AssessmentResult> query = entityManager.createQuery(
            "SELECT ar FROM AssessmentResult ar WHERE ar.severity = :severity", 
            AssessmentResult.class);
        query.setParameter("severity", severity);
        return query.getResultList();
    }
    
    @Override
    public Optional<AssessmentResult> findLatestByStudentId(Integer studentId) {
        TypedQuery<AssessmentResult> query = entityManager.createQuery(
            "SELECT ar FROM AssessmentResult ar WHERE ar.student.id = :studentId " +
            "ORDER BY ar.date DESC", AssessmentResult.class);
        query.setParameter("studentId", studentId);
        query.setMaxResults(1);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Integer getTotalScoreByStudentAndAssessment(Integer studentId, Integer assessmentId) {
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT SUM(aa.answer) FROM AssessmentAnswer aa " +
            "WHERE aa.studentId = :studentId AND aa.assessmentId = :assessmentId", 
            Integer.class);
        query.setParameter("studentId", studentId);
        query.setParameter("assessmentId", assessmentId);
        try {
            Integer sum = query.getSingleResult();
            return sum != null ? sum : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}