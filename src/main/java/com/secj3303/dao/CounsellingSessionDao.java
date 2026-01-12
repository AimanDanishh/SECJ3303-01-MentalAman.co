package com.secj3303.dao;

import com.secj3303.model.CounsellingSession;
import com.secj3303.model.Counsellor;

import java.time.LocalDate;
import java.util.List;

public interface CounsellingSessionDao {
    void save(CounsellingSession session);
    void update(CounsellingSession session);
    void delete(CounsellingSession session);
    CounsellingSession findById(Integer id);
    List<CounsellingSession> findAll();
    List<CounsellingSession> findByCounsellor(Counsellor counsellor);
    List<CounsellingSession> findUpcomingSessions();
    
    // Add these methods for better performance
    List<CounsellingSession> findByCounsellorAndDate(Counsellor counsellor, LocalDate date);
    List<CounsellingSession> findByStatus(com.secj3303.model.CounsellingSession.SessionStatus status);
    List<CounsellingSession> findByStudentConfirmed(boolean confirmed);
    
    // Student-related methods - NEW
    List<CounsellingSession> findByStudentIdOrderByDateDescStartTimeDesc(String studentId);
    List<CounsellingSession> findByStudentIdAndStatusOrderByDateAsc(String studentId, CounsellingSession.SessionStatus status);
    List<CounsellingSession> findByStudentId(String studentId);
    List<CounsellingSession> findByStudentIdAndDate(String studentId, LocalDate date);
    List<CounsellingSession> findByStudentIdAndDateBetween(String studentId, LocalDate startDate, LocalDate endDate);
    long countByStudentId(String studentId);
    long countByStudentIdAndStatus(String studentId, CounsellingSession.SessionStatus status);
}