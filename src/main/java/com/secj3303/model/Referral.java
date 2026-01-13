package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "referrals")
public class Referral implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relationship to Student Entity
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_ref_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private String urgency; // 'low' | 'medium' | 'high'

    @Transient // Not saved to DB, used for form transfer
    private String additionalNotes; 

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "submitted_date")
    private String submittedDate;

    @Column(nullable = false)
    private String status; // 'pending' | 'reviewed' | 'in-progress'
    
    // Transient fields to hold string IDs from forms before linking
    @Transient
    private String tempStudentId;

    public Referral() {
        this.urgency = "medium";
        this.status = "pending";
        this.submittedBy = "Current Faculty"; // ideally get this from session user
        this.submittedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    // Helper getters to maintain compatibility with your UI (thymeleaf)
    public String getStudentName() { return student != null ? student.getName() : "Unknown"; }
    public String getStudentId() { return student != null ? student.getStudentId() : tempStudentId; }
    public void setStudentId(String studentId) { this.tempStudentId = studentId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public String getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(String submittedDate) { this.submittedDate = submittedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Utility methods for UI Styling
    public String getUrgencyColor() {
        if (urgency == null) return "bg-slate-100 text-slate-800";
        switch (urgency.toLowerCase()) {
            case "low": return "bg-green-100 text-green-800";
            case "medium": return "bg-yellow-100 text-yellow-800";
            case "high": return "bg-red-100 text-red-800";
            default: return "bg-slate-100 text-slate-800";
        }
    }
    
    public String getStatusColor() {
        if (status == null) return "bg-slate-100 text-slate-800";
        switch (status.toLowerCase()) {
            case "pending": return "bg-yellow-100 text-yellow-800";
            case "reviewed": return "bg-blue-100 text-blue-800";
            case "in-progress": return "bg-purple-100 text-purple-800";
            default: return "bg-slate-100 text-slate-800";
        }
    }
}