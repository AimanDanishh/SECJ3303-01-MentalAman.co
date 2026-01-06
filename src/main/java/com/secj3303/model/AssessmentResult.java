package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "assessment_title", nullable = false)
    private String assessmentTitle;
    
    @Column(nullable = false)
    private String date;
    
    @Column(nullable = false)
    private Integer score;
    
    @Column(nullable = false)
    private String severity; // 'Mild', 'Moderate', 'Severe'
    
    @Column(name = "report_available")
    private Boolean reportAvailable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;
    
    // Constructors
    public AssessmentResult() {}
    
    public AssessmentResult(String assessmentTitle, String date, Integer score, 
                           String severity, Boolean reportAvailable, Student student) {
        this.assessmentTitle = assessmentTitle;
        this.date = date;
        this.score = score;
        this.severity = severity;
        this.reportAvailable = reportAvailable;
        this.student = student;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public Boolean getReportAvailable() { return reportAvailable; }
    public void setReportAvailable(Boolean reportAvailable) { this.reportAvailable = reportAvailable; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    
    // Convenience method for boolean getter
    public boolean isReportAvailable() {
        return reportAvailable != null && reportAvailable;
    }
}