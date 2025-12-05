package com.secj3303.model;

import java.io.Serializable;
import java.util.Optional;

public class StudentEngagement implements Serializable {
    private final int id;
    private final String name;
    private final String email;
    private final String studentId;
    private final String department;
    private final String initials;
    private final String lastLogin;
    private final int loginFrequency;
    private final int moduleCompletion;
    private final int forumPosts;
    private final int assessmentsTaken;
    private final int aiCoachUsage;
    private final String riskLevel; // 'low', 'moderate', 'high'
    private final String trend;     // 'up', 'down', 'stable'

    public StudentEngagement(int id, String name, String email, String studentId, String department,
                             String lastLogin, int loginFrequency, int moduleCompletion,
                             int forumPosts, int assessmentsTaken, int aiCoachUsage,
                             String riskLevel, String trend) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.department = department;
        this.lastLogin = lastLogin;
        this.loginFrequency = loginFrequency;
        this.moduleCompletion = moduleCompletion;
        this.forumPosts = forumPosts;
        this.assessmentsTaken = assessmentsTaken;
        this.aiCoachUsage = aiCoachUsage;
        this.riskLevel = riskLevel;
        this.trend = trend;
        
        // Calculate initials from name (replicates TSX logic)
        String[] parts = name.split(" ");
        this.initials = Optional.ofNullable(parts[0]).map(s -> s.substring(0, 1)).orElse("") +
                        Optional.ofNullable(parts.length > 1 ? parts[parts.length - 1] : null).map(s -> s.substring(0, 1)).orElse("");
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getInitials() { return initials; }
    public String getLastLogin() { return lastLogin; }
    public int getLoginFrequency() { return loginFrequency; }
    public int getModuleCompletion() { return moduleCompletion; }
    public int getForumPosts() { return forumPosts; }
    public int getAssessmentsTaken() { return assessmentsTaken; }
    public int getAiCoachUsage() { return aiCoachUsage; }
    public String getRiskLevel() { return riskLevel; }
    public String getTrend() { return trend; }
    
    // Utility methods for Thymeleaf styling
    public boolean isHighRisk() { return "high".equals(riskLevel); }
    public boolean isModerateRisk() { return "moderate".equals(riskLevel); }
    public boolean isLowRisk() { return "low".equals(riskLevel); }
    public boolean isTrendUp() { return "up".equals(trend); }
    public boolean isTrendDown() { return "down".equals(trend); }
}