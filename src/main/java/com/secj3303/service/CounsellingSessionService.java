package com.secj3303.service;

import com.secj3303.dao.CounsellorDao;
import com.secj3303.dao.CounsellingSessionDao;
import com.secj3303.model.Counsellor;
import com.secj3303.model.CounsellingSession;
import com.secj3303.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounsellingSessionService {
    
    @Autowired
    private CounsellingSessionDao sessionDao;

    @Autowired
    private CounsellorDao counsellorDao;

    // ---------------------------
    // Core CRUD operations
    // ---------------------------
    @Transactional(readOnly = true)
    public List<CounsellingSession> getAllSessions() {
        return sessionDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> getSessionsByCounsellor(Counsellor counsellor) {
        return sessionDao.findByCounsellor(counsellor);
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> getUpcomingSessions() {
        return sessionDao.findUpcomingSessions();
    }

    @Transactional(readOnly = true)
    public CounsellingSession getSessionById(Integer id) {
        return sessionDao.findById(id);
    }

    @Transactional
    public void saveSession(CounsellingSession session) {
        sessionDao.save(session);
    }

    @Transactional
    public void updateSession(CounsellingSession session) {
        sessionDao.update(session);
    }

    @Transactional
    public void deleteSession(Integer sessionId) {
        CounsellingSession session = getSessionById(sessionId);
        if (session != null) {
            sessionDao.delete(session);
        }
    }

    @Transactional(readOnly = true)
    public List<Counsellor> getAllCounsellors() {
        return counsellorDao.findAll();
    }

    @Transactional(readOnly = true)
    public Counsellor getCounsellorById(Integer id) {
        return counsellorDao.findById(id);
    }

    // ---------------------------
    // TimeSlot generation
    // ---------------------------
    @Transactional(readOnly = true)
    public List<TimeSlot> generateAvailableSlotsForCounsellor(Counsellor counsellor) {
        List<CounsellingSession> allSessions = getAllSessions();
        return TimeSlot.generateAvailableSlotsForCounsellor(allSessions, counsellor.getId());
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> generateAvailableSlotsForCounsellorId(Integer counsellorId) {
        List<CounsellingSession> allSessions = getAllSessions();
        return TimeSlot.generateAvailableSlotsForCounsellor(allSessions, counsellorId);
    }

    // ---------------------------
    // Custom queries needed for service methods
    // ---------------------------
    @Transactional(readOnly = true)
    public List<CounsellingSession> findByCounsellorAndDate(Integer counsellorId, LocalDate date) {
        Counsellor counsellor = counsellorDao.findById(counsellorId);
        if (counsellor == null) {
            return List.of();
        }
        return sessionDao.findByCounsellorAndDate(counsellor, date);
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> findByCounsellorAndStatusNot(Counsellor counsellor, CounsellingSession.SessionStatus excludedStatus) {
        List<CounsellingSession> counsellorSessions = sessionDao.findByCounsellor(counsellor);
        return counsellorSessions.stream()
                .filter(session -> session.getStatus() != excludedStatus)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> findByDateAfter(LocalDate date) {
        // Use findAll and filter, or add a new DAO method
        List<CounsellingSession> allSessions = sessionDao.findAll();
        return allSessions.stream()
                .filter(session -> !session.getDate().isBefore(date))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> findByStatus(CounsellingSession.SessionStatus status) {
        return sessionDao.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CounsellingSession> findByStudentConfirmed(boolean confirmed) {
        return sessionDao.findByStudentConfirmed(confirmed);
    }

    // ---------------------------
    // Session operations
    // ---------------------------
    @Transactional
    public void bookSession(Integer counsellorId, LocalDate date, LocalTime start, 
                        String sessionType, String sessionLocation, String notes) {
        
        Counsellor counsellor = counsellorDao.findById(counsellorId);
        if (counsellor == null) {
            throw new IllegalArgumentException("Invalid counsellor ID: " + counsellorId);
        }
        
        // Check for overlapping sessions (excluding cancelled sessions)
        List<CounsellingSession> existingSessions = findByCounsellorAndDate(counsellorId, date);
        boolean slotTaken = existingSessions.stream()
                .anyMatch(s -> s.getStartTime().equals(start) && 
                            s.getStatus() != CounsellingSession.SessionStatus.CANCELLED);
        
        if (slotTaken) {
            throw new IllegalArgumentException("Selected time slot is not available");
        }
        
        CounsellingSession session = new CounsellingSession();
        session.setCounsellor(counsellor);
        session.setDate(date);
        session.setStartTime(start);
        session.setEndTime(start.plusHours(1));
        session.setTypeFromString(sessionType);
        session.setLocation(sessionLocation);
        session.setStatus(CounsellingSession.SessionStatus.SCHEDULED);
        session.setStudentConfirmed(false);
        session.setNotes(notes);
        session.setReportAvailable(false);
        session.setReportContent(null);
        session.setCancellationReason(null);
        
        sessionDao.save(session);
    }
    
    @Transactional
    public void confirmSession(Integer sessionId) {
        CounsellingSession session = getSessionById(sessionId);
        validateSessionExists(session, sessionId);
        
        session.setStudentConfirmed(true);
        session.setStatus(CounsellingSession.SessionStatus.CONFIRMED);
        updateSession(session);
    }
    
    @Transactional
    public void cancelSession(Integer sessionId, String cancellationReason) {
        CounsellingSession session = getSessionById(sessionId);
        validateSessionExists(session, sessionId);
        
        // Validate cancellation reason
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        
        // Check if session can be cancelled
        if (session.getStatus() == CounsellingSession.SessionStatus.COMPLETED || 
            session.getStatus() == CounsellingSession.SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a " + session.getStatus() + " session");
        }
        
        session.setStatus(CounsellingSession.SessionStatus.CANCELLED);
        session.setCancellationReason(cancellationReason);
        updateSession(session);
    }
    
    @Transactional
    public void rescheduleSession(Integer sessionId, LocalDate newDate, LocalTime newTime) {
        CounsellingSession session = getSessionById(sessionId);
        validateSessionExists(session, sessionId);
        
        // Check if reschedule is allowed
        if (session.getStatus() == CounsellingSession.SessionStatus.COMPLETED || 
            session.getStatus() == CounsellingSession.SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule a " + session.getStatus() + " session");
        }
        
        // Check for overlapping at new time
        Integer counsellorId = session.getCounsellor().getId();
        if (!isTimeSlotAvailable(counsellorId, newDate, newTime, sessionId)) {
            throw new IllegalArgumentException("New time slot is not available");
        }
        
        session.setDate(newDate);
        session.setStartTime(newTime);
        session.setEndTime(newTime.plusHours(1));
        session.setStatus(CounsellingSession.SessionStatus.PENDING_RESCHEDULE);
        updateSession(session);
    }
    
    @Transactional
    public void markSessionAsCompleted(Integer sessionId, String reportContent) {
        CounsellingSession session = getSessionById(sessionId);
        validateSessionExists(session, sessionId);
        
        session.setStatus(CounsellingSession.SessionStatus.COMPLETED);
        session.setReportAvailable(true);
        session.setReportContent(reportContent);
        updateSession(session);
    }
    
    // ---------------------------
    // Validation methods
    // ---------------------------
    private void validateSessionExists(CounsellingSession session, Integer sessionId) {
        if (session == null) {
            throw new IllegalArgumentException("Session not found with ID: " + sessionId);
        }
    }
    
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(Integer counsellorId, LocalDate date, LocalTime startTime) {
        return isTimeSlotAvailable(counsellorId, date, startTime, null);
    }
    
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(Integer counsellorId, LocalDate date, LocalTime startTime, Integer excludeSessionId) {
        List<CounsellingSession> sessions = findByCounsellorAndDate(counsellorId, date);
        LocalTime endTime = startTime.plusHours(1);
        
        return sessions.stream()
                .filter(s -> !s.getId().equals(excludeSessionId))
                .filter(s -> s.getStatus() != CounsellingSession.SessionStatus.CANCELLED)
                .noneMatch(s -> isTimeOverlap(
                    s.getStartTime(), s.getEndTime(),
                    startTime, endTime
                ));
    }
    
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}