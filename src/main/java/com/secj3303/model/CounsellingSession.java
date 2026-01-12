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
           @Index(name = "idx_student_date", columnList = "student_id, date")
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

    @Column(name = "student_id", nullable = false, length = 50)
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

    // Transient fields for student display (not stored in DB)
    @Transient
    private String studentName;

    @Transient
    private String studentInitials;

    @Transient
    private String studentAvatarColor;

    @Transient
    private String studentTextColor = "#ffffff";

    public CounsellingSession() {}

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

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

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

    // Transient field getters and setters
    public String getStudentName() { 
        return studentName; 
    }
    
    public void setStudentName(String studentName) { 
        this.studentName = studentName; 
    }

    public String getStudentInitials() {
        if (studentName != null && !studentName.trim().isEmpty()) {
            String[] nameParts = studentName.trim().split("\\s+");
            if (nameParts.length >= 2) {
                return (nameParts[0].charAt(0) + "" + nameParts[nameParts.length - 1].charAt(0)).toUpperCase();
            } else if (nameParts.length == 1 && nameParts[0].length() >= 1) {
                return nameParts[0].substring(0, Math.min(2, nameParts[0].length())).toUpperCase();
            }
        }
        // Fallback to student ID initials
        if (studentId != null && studentId.length() >= 2) {
            return studentId.substring(0, 2).toUpperCase();
        }
        return "??";
    }

    public void setStudentInitials(String studentInitials) {
        this.studentInitials = studentInitials;
    }

    public String getStudentAvatarColor() {
        if (studentId == null) return "#6b7280"; // gray as fallback
        
        int hash = Math.abs(studentId.hashCode());
        String[] colors = {
            "#ef4444", "#f97316", "#f59e0b", "#eab308",
            "#84cc16", "#22c55e", "#10b981", "#14b8a6",
            "#06b6d4", "#0ea5e9", "#3b82f6", "#6366f1",
            "#8b5cf6", "#a855f7", "#d946ef", "#ec4899"
        };
        return colors[hash % colors.length];
    }

    public void setStudentAvatarColor(String studentAvatarColor) {
        this.studentAvatarColor = studentAvatarColor;
    }

    public String getStudentTextColor() {
        return studentTextColor;
    }

    public void setStudentTextColor(String studentTextColor) {
        this.studentTextColor = studentTextColor;
    }

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
            default: return "bg-blue-50 border-blue-200";
        }
    }

    public String getAvatarClass() {
        switch (status) {
            case COMPLETED: return "bg-slate-200 text-slate-700";
            case CANCELLED: return "bg-red-200 text-red-700";
            case CONFIRMED: return "bg-green-200 text-green-700";
            case PENDING_RESCHEDULE: return "bg-yellow-200 text-yellow-700";
            default: return "bg-blue-200 text-blue-700";
        }
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case COMPLETED: return "bg-slate-200 text-slate-700";
            case CANCELLED: return "bg-red-200 text-red-700";
            case CONFIRMED: return "bg-green-200 text-green-700";
            case PENDING_RESCHEDULE: return "bg-yellow-200 text-yellow-700";
            default: return "bg-blue-200 text-blue-700";
        }
    }

    public String getStatusIcon() {
        switch (status) {
            case COMPLETED: return "check-circle";
            case CANCELLED: return "x-circle";
            case CONFIRMED: return "check-circle";
            case PENDING_RESCHEDULE: return "clock";
            default: return "calendar";
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

    // Helper methods for role-based display
    public boolean shouldShowStudentInfo(String userRole) {
        return "counsellor".equals(userRole);
    }

    public boolean shouldShowCounsellorInfo(String userRole) {
        return !"counsellor".equals(userRole);
    }

    public String getDisplayName(String userRole) {
        if ("counsellor".equals(userRole)) {
            return studentName != null ? studentName : 
                   (studentId != null ? "Student " + studentId : "Student");
        } else {
            return counsellor != null ? counsellor.getName() : "Counsellor";
        }
    }

    public String getDisplayDetail(String userRole) {
        if ("counsellor".equals(userRole)) {
            return studentId != null ? "ID: " + studentId : "";
        } else {
            return counsellor != null ? counsellor.getSpecialty() : "";
        }
    }
}