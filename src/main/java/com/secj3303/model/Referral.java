package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Referral implements Serializable {
    private int id;
    private String studentName;
    private String studentId;
    private String reason;
    private String observations;
    private String urgency; // 'low' | 'medium' | 'high'
    private String additionalNotes; // Only used in form data
    private String submittedBy;
    private String submittedDate;
    private String status; // 'pending' | 'reviewed' | 'in-progress'

    // Default constructor for form binding
    public Referral() {
        this.urgency = "medium";
        this.status = "pending";
        this.submittedBy = "Current Faculty";
        this.submittedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    // Constructor for initial mock data
    public Referral(int id, String studentName, String studentId, String reason, String observations, String urgency, String submittedBy, String submittedDate, String status) {
        this.id = id;
        this.studentName = studentName;
        this.studentId = studentId;
        this.reason = reason;
        this.observations = observations;
        this.urgency = urgency;
        this.submittedBy = submittedBy;
        this.submittedDate = submittedDate;
        this.status = status;
    }

    // --- Getters and Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
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

    // Utility methods for Thymeleaf styling
    public String getUrgencyColor() {
        switch (urgency) {
            case "low": return "bg-green-100 text-green-800";
            case "medium": return "bg-yellow-100 text-yellow-800";
            case "high": return "bg-red-100 text-red-800";
            default: return "bg-slate-100 text-slate-800";
        }
    }
    
    public String getStatusColor() {
        switch (status) {
            case "pending": return "bg-yellow-100 text-yellow-800";
            case "reviewed": return "bg-blue-100 text-blue-800";
            case "in-progress": return "bg-purple-100 text-purple-800";
            default: return "bg-slate-100 text-slate-800";
        }
    }
}