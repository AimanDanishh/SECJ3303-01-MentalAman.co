package com.secj3303.dao;

import com.secj3303.dao.QuestionDao;
import com.secj3303.model.Question;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class QuestionDaoHibernate implements QuestionDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Question> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Question.class, id));
    }
    
    @Override
    public List<Question> findAll() {
        TypedQuery<Question> query = entityManager.createQuery(
            "SELECT q FROM Question q", Question.class);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public Question save(Question question) {
        if (question.getId() == null) {
            entityManager.persist(question);
            return question;
        } else {
            return entityManager.merge(question);
        }
    }
    
    @Override
    @Transactional
    public Question update(Question question) {
        return entityManager.merge(question);
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        Question question = entityManager.find(Question.class, id);
        if (question != null) {
            entityManager.remove(question);
        }
    }
    
    @Override
    @Transactional
    public void delete(Question question) {
        entityManager.remove(entityManager.contains(question) ? question : entityManager.merge(question));
    }
    
    @Override
    public List<Question> findByAssessmentId(Integer assessmentId) {
        TypedQuery<Question> query = entityManager.createQuery(
            "SELECT q FROM Question q WHERE q.assessment.id = :assessmentId ORDER BY q.id", 
            Question.class);
        query.setParameter("assessmentId", assessmentId);
        return query.getResultList();
    }
    
    @Override
    public List<Question> findByType(String type) {
        TypedQuery<Question> query = entityManager.createQuery(
            "SELECT q FROM Question q WHERE q.type = :type", Question.class);
        query.setParameter("type", type);
        return query.getResultList();
    }
}