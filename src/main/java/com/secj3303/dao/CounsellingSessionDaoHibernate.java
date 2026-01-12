package com.secj3303.dao;

import com.secj3303.model.CounsellingSession;
import com.secj3303.model.Counsellor;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public class CounsellingSessionDaoHibernate implements CounsellingSessionDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(CounsellingSession session) {
        entityManager.persist(session);
    }

    @Override
    public void update(CounsellingSession session) {
        entityManager.merge(session);
    }

    @Override
    public void delete(CounsellingSession session) {
        entityManager.remove(entityManager.contains(session) ? session : entityManager.merge(session));
    }

    @Override
    public CounsellingSession findById(Integer id) {
        return entityManager.find(CounsellingSession.class, id);
    }

    @Override
    public List<CounsellingSession> findAll() {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s " +
                "ORDER BY " +
                "CASE " +
                "   WHEN s.status = 'CANCELLED' THEN 2 " +
                "   WHEN s.date > CURRENT_DATE THEN 0 " +  // Future date = upcoming
                "   WHEN s.date < CURRENT_DATE THEN 1 " +  // Past date = past
                "   WHEN s.startTime > CURRENT_TIME THEN 0 " +  // Today, future time = upcoming
                "   ELSE 1 " +  // Today, past time = past
                "END, " +
                "CASE " +
                "   WHEN s.status = 'CANCELLED' THEN s.date " +
                "   ELSE NULL " +
                "END DESC, " +  // Within cancelled, newest first
                "s.date ASC, " +  // Within upcoming/past, chronological
                "s.startTime ASC", 
                CounsellingSession.class);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByCounsellor(Counsellor counsellor) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.counsellor = :counsellor ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("counsellor", counsellor);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findUpcomingSessions() {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.date >= :today ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("today", LocalDate.now());
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByCounsellorAndDate(Counsellor counsellor, LocalDate date) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.counsellor = :counsellor AND s.date = :date ORDER BY s.startTime",
                CounsellingSession.class);
        query.setParameter("counsellor", counsellor);
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStatus(CounsellingSession.SessionStatus status) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.status = :status ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStudentConfirmed(boolean confirmed) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentConfirmed = :confirmed ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("confirmed", confirmed);
        return query.getResultList();
    }

    // ---------------------------
    // Student-related methods - NEW
    // ---------------------------
    
    @Override
    public List<CounsellingSession> findByStudentIdOrderByDateDescStartTimeDesc(String studentId) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentId = :studentId " +
                "ORDER BY s.date DESC, s.startTime DESC",
                CounsellingSession.class);
        query.setParameter("studentId", studentId);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStudentIdAndStatusOrderByDateAsc(String studentId, CounsellingSession.SessionStatus status) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentId = :studentId AND s.status = :status " +
                "ORDER BY s.date ASC, s.startTime ASC",
                CounsellingSession.class);
        query.setParameter("studentId", studentId);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStudentId(String studentId) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentId = :studentId " +
                "ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("studentId", studentId);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStudentIdAndDate(String studentId, LocalDate date) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentId = :studentId AND s.date = :date " +
                "ORDER BY s.startTime",
                CounsellingSession.class);
        query.setParameter("studentId", studentId);
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Override
    public List<CounsellingSession> findByStudentIdAndDateBetween(String studentId, LocalDate startDate, LocalDate endDate) {
        TypedQuery<CounsellingSession> query = entityManager.createQuery(
                "SELECT s FROM CounsellingSession s WHERE s.studentId = :studentId " +
                "AND s.date BETWEEN :startDate AND :endDate " +
                "ORDER BY s.date, s.startTime",
                CounsellingSession.class);
        query.setParameter("studentId", studentId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    @Override
    public long countByStudentId(String studentId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(s) FROM CounsellingSession s WHERE s.studentId = :studentId",
                Long.class);
        query.setParameter("studentId", studentId);
        return query.getSingleResult();
    }

    @Override
    public long countByStudentIdAndStatus(String studentId, CounsellingSession.SessionStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(s) FROM CounsellingSession s WHERE s.studentId = :studentId AND s.status = :status",
                Long.class);
        query.setParameter("studentId", studentId);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
}