package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.LearningModule;

@Repository
@Transactional
public class LearningModuleDaoHibernate implements LearningModuleDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<LearningModule> findById(Long id) {
        return Optional.ofNullable(em.find(LearningModule.class, id));
    }

    // =====================================================
    // FETCH lessons + quizzes + quiz.options (ElementCollection)
    // =====================================================
    @Override
    public Optional<LearningModule> findByIdWithDetails(Long id) {

        List<LearningModule> result = em.createQuery(
            "SELECT DISTINCT m " +
            "FROM LearningModule m " +
            "LEFT JOIN FETCH m.lessons " +
            "LEFT JOIN FETCH m.quizzes q " +   // ✅ MUST be plural
            "LEFT JOIN FETCH q.options " +     // ✅ ElementCollection
            "WHERE m.id = :id",
            LearningModule.class
        )
        .setParameter("id", id)
        .getResultList();

        return result.stream().findFirst();
    }

    // =====================================================
    // FETCH ALL modules correctly (NO collapsing)
    // =====================================================
    @Override
    public List<LearningModule> findAllWithLessonsAndQuiz() {

        return em.createQuery(
            "SELECT DISTINCT m " +
            "FROM LearningModule m " +
            "LEFT JOIN FETCH m.lessons " +
            "LEFT JOIN FETCH m.quizzes q " +   // ✅ FIXED
            "LEFT JOIN FETCH q.options " +     // ✅ SAFE with DISTINCT
            "ORDER BY m.id",
            LearningModule.class
        ).getResultList();
    }

    @Override
    public void save(LearningModule module) {
        if (module.getId() == null) {
            em.persist(module);
        } else {
            em.merge(module);
        }
    }

    @Override
    public void deleteById(Long id) {
        LearningModule module = em.find(LearningModule.class, id);
        if (module != null) {
            em.remove(module);
        }
    }
}
