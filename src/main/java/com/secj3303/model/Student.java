package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String year;
    
    @Column(name = "current_grade")
    private String currentGrade;
    
    private Integer attendance;
    
    @Column(name = "last_activity")
    private String lastActivity;
    
    @Column(name = "risk_level")
    private String riskLevel;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssessmentResult> assessmentResults = new ArrayList<>();
    
    // Transient fields for UI
    @Transient
    private String initials;
    
    @Transient
    private Integer assessmentCount = 0;
    
    @Transient
    private String lastAssessment = "N/A";

    @Transient
    private String badgeColor;
    
    // Constructors
    public Student() {}
    
    public Student(String name, String email, String studentId, String department, 
                   String year, String currentGrade, Integer attendance, String lastActivity) {
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.department = department;
        this.year = year;
        this.currentGrade = currentGrade;
        this.attendance = attendance;
        this.lastActivity = lastActivity;
        calculateInitials();
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        calculateInitials();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public String getCurrentGrade() { return currentGrade; }
    public void setCurrentGrade(String currentGrade) { this.currentGrade = currentGrade; }
    
    public Integer getAttendance() { return attendance; }
    public void setAttendance(Integer attendance) { this.attendance = attendance; }
    
    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public List<AssessmentResult> getAssessmentResults() { return assessmentResults; }
    public void setAssessmentResults(List<AssessmentResult> assessmentResults) { this.assessmentResults = assessmentResults; }

    // Utility methods
    public void calculateInitials() {
        if (name == null || name.trim().isEmpty()) {
            initials = "";
            return;
        }
        
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            initials = parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
        } else if (parts.length == 1) {
            initials = parts[0].substring(0, Math.min(2, parts[0].length()));
        } else {
            initials = "";
        }
    }
    
    public String getAttendanceColor() {
        if (attendance == null) return "text-slate-600";
        if (attendance >= 85) return "text-green-600";
        if (attendance >= 70) return "text-yellow-600";
        return "text-red-600";
    }

     // Add getters and setters
    public Integer getAssessmentCount() { return assessmentCount; }
    public void setAssessmentCount(Integer assessmentCount) { this.assessmentCount = assessmentCount; }
    
    public String getLastAssessment() { return lastAssessment; }
    public void setLastAssessment(String lastAssessment) { this.lastAssessment = lastAssessment; }
    
    public String getInitials() {
        if (this.initials == null && this.name != null) {
            // Generate initials from name
            String[] names = this.name.split(" ");
            StringBuilder initials = new StringBuilder();
            for (String name : names) {
                if (!name.isEmpty()) {
                    initials.append(name.charAt(0));
                }
            }
            this.initials = initials.toString().toUpperCase();
        }
        return this.initials;
    }
    
    public String getInitialsColor() {
        if ("high".equalsIgnoreCase(this.riskLevel)) {
            return "bg-red-600";
        } else if ("moderate".equalsIgnoreCase(this.riskLevel)) {
            return "bg-yellow-600";
        } else {
            return "bg-green-600";
        }
    }
    
    public String getBadgeColor() {
        if ("high".equalsIgnoreCase(this.riskLevel)) {
            return "bg-red-100 text-red-700";
        } else if ("moderate".equalsIgnoreCase(this.riskLevel)) {
            return "bg-yellow-100 text-yellow-700";
        } else {
            return "bg-green-100 text-green-700";
        }
    }
}