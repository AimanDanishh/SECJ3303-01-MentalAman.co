package com.secj3303.dao;

import java.util.List;

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

    @Override
    public List<Report> findAll() {
        String jpql = "SELECT r FROM Report r ORDER BY r.reportedAt DESC";
        return entityManager.createQuery(jpql, Report.class).getResultList();
    }

    @Override
    public void delete(int id) {
        Report report = entityManager.find(Report.class, id);
        if (report != null) {
            entityManager.remove(report);
        }
    }

    @Override
    public void deleteByPostId(int postId) {
        String jpql = "DELETE FROM Report r WHERE r.postId = :postId";
        entityManager.createQuery(jpql).setParameter("postId", postId).executeUpdate();
    }
}