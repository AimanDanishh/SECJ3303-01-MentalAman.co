package com.secj3303.dao;

import com.secj3303.model.Referral;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ReferralDaoHibernate implements ReferralDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Referral> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Referral.class, id));
    }

    @Override
    public List<Referral> findAll() {
        // Order by ID descending so newest referrals appear first
        return entityManager.createQuery("SELECT r FROM Referral r ORDER BY r.id DESC", Referral.class)
                .getResultList();
    }

    @Override
    public Referral save(Referral referral) {
        entityManager.persist(referral);
        return referral;
    }

    @Override
    public Referral update(Referral referral) {
        return entityManager.merge(referral);
    }

    @Override
    public void delete(Integer id) {
        Referral referral = entityManager.find(Referral.class, id);
        if (referral != null) {
            entityManager.remove(referral);
        }
    }
    
    @Override
    public void delete(Referral referral) {
         entityManager.remove(entityManager.contains(referral) ? referral : entityManager.merge(referral));
    }

    @Override
    public List<Referral> findByStudentId(String studentId) {
        TypedQuery<Referral> query = entityManager.createQuery(
            "SELECT r FROM Referral r WHERE r.student.studentId = :studentId", Referral.class);
        query.setParameter("studentId", studentId);
        return query.getResultList();
    }

    @Override
    public List<Referral> findByStatus(String status) {
        TypedQuery<Referral> query = entityManager.createQuery(
            "SELECT r FROM Referral r WHERE r.status = :status", Referral.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
}