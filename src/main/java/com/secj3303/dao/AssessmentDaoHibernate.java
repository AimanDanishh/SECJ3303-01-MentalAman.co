package com.secj3303.dao;

import com.secj3303.dao.AssessmentDao;
import com.secj3303.model.Assessment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class AssessmentDaoHibernate implements AssessmentDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Assessment> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Assessment.class, id));
    }
    
    @Override
    public List<Assessment> findAll() {
        TypedQuery<Assessment> query = entityManager.createQuery(
            "SELECT a FROM Assessment a", Assessment.class);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public Assessment save(Assessment assessment) {
        if (assessment.getId() == null) {
            entityManager.persist(assessment);
            return assessment;
        } else {
            return entityManager.merge(assessment);
        }
    }
    
    @Override
    @Transactional
    public Assessment update(Assessment assessment) {
        return entityManager.merge(assessment);
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        Assessment assessment = entityManager.find(Assessment.class, id);
        if (assessment != null) {
            entityManager.remove(assessment);
        }
    }
    
    @Override
    @Transactional
    public void delete(Assessment assessment) {
        entityManager.remove(entityManager.contains(assessment) ? assessment : entityManager.merge(assessment));
    }
    
    @Override
    public List<Assessment> findByCategory(String category) {
        TypedQuery<Assessment> query = entityManager.createQuery(
            "SELECT a FROM Assessment a WHERE a.category = :category", Assessment.class);
        query.setParameter("category", category);
        return query.getResultList();
    }
    
    @Override
    public List<Assessment> findAllWithQuestions() {
        TypedQuery<Assessment> query = entityManager.createQuery(
            "SELECT DISTINCT a FROM Assessment a LEFT JOIN FETCH a.questions", Assessment.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Assessment> findByIdWithQuestions(Integer id) {
        TypedQuery<Assessment> query = entityManager.createQuery(
            "SELECT a FROM Assessment a LEFT JOIN FETCH a.questions WHERE a.id = :id", Assessment.class);
        query.setParameter("id", id);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}