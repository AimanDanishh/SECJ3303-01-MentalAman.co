package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "COUNSELLING_SESSION", 
       indexes = {
           @Index(name = "idx_session_date", columnList = "date"),
           @Index(name = "idx_session_status", columnList = "status"),
           @Index(name = "idx_counsellor_date", columnList = "counsellor_id, date"),
           @Index(name = "idx_student_date", columnList = "student_id, date") // Added index
       })
public class CounsellingSession implements Serializable {

    public enum SessionStatus {
        SCHEDULED("Scheduled"),
        CONFIRMED("Confirmed"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        PENDING_RESCHEDULE("Pending Reschedule");

        private final String displayName;

        SessionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionType {
        VIDEO_CALL("Video Call"),
        IN_PERSON("In-person"),
        PHONE_CALL("Phone Call");

        private final String displayName;

        SessionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "counsellor_id", referencedColumnName = "counsellor_id", nullable = false)
    private Counsellor counsellor;

    @Column(name = "student_id", nullable = false, length = 50) // Added student_id field
    private String studentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionType type;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionStatus status;

    @Column(name = "attendance_confirmed")
    private boolean attendanceConfirmed = false;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "report_available")
    private boolean reportAvailable;

    @Column(name = "report_content", length = 2000)
    private String reportContent;

    public CounsellingSession() {}

    // Updated constructor
    public CounsellingSession(Counsellor counsellor, String studentId, LocalDate date, LocalTime startTime, LocalTime endTime,
                              SessionType type, String location, SessionStatus status, boolean attendanceConfirmed,
                              String notes, String cancellationReason, boolean reportAvailable, String reportContent) {
        this.counsellor = counsellor;
        this.studentId = studentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.location = location;
        this.status = status;
        this.attendanceConfirmed = attendanceConfirmed;
        this.notes = notes;
        this.cancellationReason = cancellationReason;
        this.reportAvailable = reportAvailable;
        this.reportContent = reportContent;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Counsellor getCounsellor() { return counsellor; }
    public void setCounsellor(Counsellor counsellor) { this.counsellor = counsellor; }

    public String getStudentId() { return studentId; } // Added getter
    public void setStudentId(String studentId) { this.studentId = studentId; } // Added setter

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public SessionType getType() { return type; }
    public void setType(SessionType type) { this.type = type; }
    
    public void setTypeFromString(String typeString) {
        for (SessionType t : SessionType.values()) {
            if (t.getDisplayName().equalsIgnoreCase(typeString) || 
                t.name().equalsIgnoreCase(typeString)) {
                this.type = t;
                return;
            }
        }
        throw new IllegalArgumentException("Invalid session type: " + typeString);
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    
    public void setStatusFromString(String statusString) {
        for (SessionStatus s : SessionStatus.values()) {
            if (s.name().equalsIgnoreCase(statusString.replace("-", "_"))) {
                this.status = s;
                return;
            }
        }
        throw new IllegalArgumentException("Invalid session status: " + statusString);
    }

    public boolean isAttendanceConfirmed() {
        return attendanceConfirmed;
    }
    
    public void setAttendanceConfirmed(boolean attendanceConfirmed) {
        this.attendanceConfirmed = attendanceConfirmed;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public boolean isReportAvailable() { return reportAvailable; }
    public void setReportAvailable(boolean reportAvailable) { this.reportAvailable = reportAvailable; }

    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }

    // ------------------------
    // Business Logic Methods
    // ------------------------
    public boolean isOverlapping(LocalTime newStart, LocalTime newEnd) {
        return !(this.endTime.isBefore(newStart) || this.startTime.isAfter(newEnd));
    }

    public boolean isPastSession() {
        java.time.LocalDateTime sessionDateTime = java.time.LocalDateTime.of(date, startTime);
        return sessionDateTime.isBefore(java.time.LocalDateTime.now());
    }

    public boolean canBeCancelled() {
        java.time.LocalDateTime sessionDateTime = java.time.LocalDateTime.of(date, startTime);
        java.time.LocalDateTime minCancellationTime = java.time.LocalDateTime.now().plusHours(24);
        return sessionDateTime.isAfter(minCancellationTime) && 
               status != SessionStatus.CANCELLED &&
               status != SessionStatus.COMPLETED;
    }

    public java.time.Duration getDuration() {
        return java.time.Duration.between(startTime, endTime);
    }

    // New method to check if session belongs to a student
    public boolean belongsToStudent(String studentId) {
        return this.studentId != null && this.studentId.equals(studentId);
    }

    public boolean belongsToCounsellor(String counsellorId) {
        return this.counsellor.getId() != null && this.counsellor.getId().equals(counsellorId);
    }

    // ------------------------
    // Utility methods for Thymeleaf
    // ------------------------
    public String getCardClass() {
        switch (status) {
            case COMPLETED: return "bg-slate-50 border-slate-200";
            case CANCELLED: return "bg-red-50 border-red-200";
            case CONFIRMED: return "bg-green-50 border-green-200";
            case PENDING_RESCHEDULE: return "bg-yellow-50 border-yellow-200";
            default: return "bg-blue-50 border-blue-200"; // SCHEDULED
        }
    }

    public String getAvatarClass() {
        switch (status) {
            case COMPLETED: return "bg-slate-200 text-slate-700";
            case CANCELLED: return "bg-red-200 text-red-700";
            case CONFIRMED: return "bg-green-200 text-green-700";
            case PENDING_RESCHEDULE: return "bg-yellow-200 text-yellow-700";
            default: return "bg-blue-200 text-blue-700"; // SCHEDULED
        }
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case COMPLETED: return "bg-slate-200 text-slate-700";
            case CANCELLED: return "bg-red-200 text-red-700";
            case CONFIRMED: return "bg-green-200 text-green-700";
            case PENDING_RESCHEDULE: return "bg-yellow-200 text-yellow-700";
            default: return "bg-blue-200 text-blue-700"; // SCHEDULED
        }
    }

    public String getStatusIcon() {
        switch (status) {
            case COMPLETED: return "check-circle";
            case CANCELLED: return "x-circle";
            case CONFIRMED: return "check-circle";
            case PENDING_RESCHEDULE: return "clock";
            default: return "calendar"; // SCHEDULED
        }
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String formatted = startTime.format(formatter) + " - " + endTime.format(formatter);
        return formatted.toUpperCase();
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return date.format(formatter);
    }
}