package com.secj3303.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.Report;

@Repository
@Transactional
public class ReportDaoImpl implements ReportDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int save(Report report) {
        entityManager.persist(report);
        return report.getId();
    }

    @Override
    public boolean existsByPostIdAndReason(int postId, String reason) {
        String jpql = "SELECT COUNT(r) FROM Report r WHERE r.postId = :postId AND r.reason = :reason";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("postId", postId);
        query.setParameter("reason", reason);
        return query.getSingleResult() > 0;
    }
}