package com.secj3303.dao;

import com.secj3303.model.QuizQuestion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class QuizQuestionDaoHibernate implements QuizQuestionDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<QuizQuestion> findById(Long id) {
        return Optional.ofNullable(em.find(QuizQuestion.class, id));
    }

    @Override
    public List<QuizQuestion> findByModuleId(Long moduleId) {
        TypedQuery<QuizQuestion> query = em.createQuery(
            "SELECT q FROM QuizQuestion q WHERE q.module.id = :moduleId ORDER BY q.id",
            QuizQuestion.class
        );
        query.setParameter("moduleId", moduleId);
        return query.getResultList();
    }

    @Override
    public QuizQuestion save(QuizQuestion quiz) {
        if (quiz.getId() == null) {
            em.persist(quiz);
            return quiz;
        }
        return em.merge(quiz);
    }

    @Override
    public void deleteById(Long id) {
        QuizQuestion quiz = em.find(QuizQuestion.class, id);
        if (quiz != null) {
            em.remove(quiz);
        }
    }
}
