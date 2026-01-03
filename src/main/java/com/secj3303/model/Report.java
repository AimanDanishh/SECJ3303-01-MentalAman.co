package com.secj3303.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "forum_reports")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "post_id")  // Explicit mapping to database column
    private int postId;
    
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "reported_at")
    private LocalDateTime reportedAt;
    
    private String status;
    
    // For form binding - simple getter/setter
    @Transient
    private String reportedAtString;
    
    public Report() {
        this.reportedAt = LocalDateTime.now();
        this.status = "pending";
    }
    
    public Report(int postId, String reason, String details) {
        this.postId = postId;
        this.reason = reason;
        this.details = details;
        this.reportedAt = LocalDateTime.now();
        this.status = "pending";
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Transient
    public String getReportedAtString() {
        if (reportedAt != null) {
            return reportedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return "";
    }
    
    @Transient
    public void setReportedAtString(String reportedAtString) {
        // Not needed for form binding, but included for completeness
    }
}