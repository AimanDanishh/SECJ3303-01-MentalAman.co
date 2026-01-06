package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.ModuleProgress;

@Repository
@Transactional
public class ModuleProgressDaoHibernate implements ModuleProgressDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ModuleProgress> findByUserEmailAndModuleId(
            String userEmail,
            Long moduleId) {

        List<ModuleProgress> result = em.createQuery(
                "SELECT p FROM ModuleProgress p " +
                "WHERE p.userEmail = :email " +
                "AND p.module.id = :moduleId",
                ModuleProgress.class
        )
        .setParameter("email", userEmail)
        .setParameter("moduleId", moduleId)
        .getResultList();

        return result.stream().findFirst();
    }

    @Override
    public void save(ModuleProgress progress) {
        if (progress.getId() == null) {
            em.persist(progress);
        } else {
            em.merge(progress);
        }
    }
}
