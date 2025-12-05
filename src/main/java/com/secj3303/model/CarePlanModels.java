package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CarePlanModels implements Serializable {

    // --- Data Structures ---

    public static class UserData implements Serializable {
        public int assessmentScore;
        public int moodEntries;
        public int engagementLevel;
        public int stressIndicators;
        public int sleepQuality;
        public String lastAssessmentDate;
        public int counselingSessions;
        public int learningModulesCompleted;
        public String name; // Added for personalization
        public String role; // Added for role-based views

        public UserData() {
            // Default mock data
            this.assessmentScore = 0;
            this.moodEntries = 0;
            this.engagementLevel = 0;
            this.stressIndicators = 0;
            this.sleepQuality = 0;
            this.lastAssessmentDate = "";
            this.counselingSessions = 0;
            this.learningModulesCompleted = 0;
            this.name = "User";
            this.role = "student";
        }

        // Constructor with all fields
        public UserData(int assessmentScore, int moodEntries, int engagementLevel, 
                       int stressIndicators, int sleepQuality, String lastAssessmentDate,
                       int counselingSessions, int learningModulesCompleted, 
                       String name, String role) {
            this.assessmentScore = assessmentScore;
            this.moodEntries = moodEntries;
            this.engagementLevel = engagementLevel;
            this.stressIndicators = stressIndicators;
            this.sleepQuality = sleepQuality;
            this.lastAssessmentDate = lastAssessmentDate;
            this.counselingSessions = counselingSessions;
            this.learningModulesCompleted = learningModulesCompleted;
            this.name = name;
            this.role = role;
        }
    }

    public static class RiskAssessment implements Serializable {
        public String level; // 'low' | 'medium' | 'high'
        public int score;
        public List<String> factors;
        public List<String> recommendations;
        public boolean counselorAlerted;
        
        public RiskAssessment(String level, int score, List<String> factors, 
                             List<String> recommendations, boolean counselorAlerted) {
            this.level = level;
            this.score = score;
            this.factors = factors;
            this.recommendations = recommendations;
            this.counselorAlerted = counselorAlerted;
        }

        // Default constructor
        public RiskAssessment() {
            this.level = "low";
            this.score = 0;
            this.factors = new ArrayList<>();
            this.recommendations = new ArrayList<>();
            this.counselorAlerted = false;
        }

        public String getRiskLevelBadgeClass() {
            switch (level) {
                case "low": return "bg-green-100 text-green-800";
                case "medium": return "bg-yellow-100 text-yellow-800";
                case "high": return "bg-red-100 text-red-800";
                default: return "bg-gray-100 text-gray-800";
            }
        }
    }

    public static class CarePlanActivity implements Serializable {
        public int id;
        public String title;
        public String description;
        public String category; // 'self-care' | 'learning' | 'counseling' | 'assessment' | 'community'
        public String priority; // 'high' | 'medium' | 'low'
        public boolean completed;
        public String dueDate;
        
        public CarePlanActivity(int id, String title, String description, 
                               String category, String priority, 
                               boolean completed, String dueDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = priority;
            this.completed = completed;
            this.dueDate = dueDate;
        }

        // Default constructor
        public CarePlanActivity() {
            this.id = 0;
            this.title = "";
            this.description = "";
            this.category = "self-care";
            this.priority = "medium";
            this.completed = false;
            this.dueDate = "";
        }

        // Utility methods for Thymeleaf
        public String getPriorityBadgeClass() {
            switch (priority) {
                case "high": return "badge-high";
                case "medium": return "badge-medium";
                case "low": return "badge-low";
                default: return "badge-default";
            }
        }

        public String getCategoryIcon() {
            switch (category) {
                case "self-care": return "❤️";
                case "learning": return "📚";
                case "counseling": return "💬";
                case "assessment": return "📝";
                case "community": return "👥";
                default: return "📌";
            }
        }
    }

    // --- Static Mock Data ---
    
    public static final List<CarePlanActivity> SAMPLE_ACTIVITIES = Arrays.asList(
        new CarePlanActivity(1, "Daily Mood Check-in", 
            "Track your mood every day for the next week to identify patterns", 
            "assessment", "high", false, "Nov 25, 2024"),
        new CarePlanActivity(2, "Stress Management Module", 
            "Complete the interactive learning module on managing stress and anxiety", 
            "learning", "medium", false, "Nov 28, 2024"),
        new CarePlanActivity(3, "Breathing Exercises Practice", 
            "Practice deep breathing exercises twice daily (5 minutes each)", 
            "self-care", "medium", true, "Nov 20, 2024"),
        new CarePlanActivity(4, "Weekly Self-Care Activity", 
            "Schedule and complete one enjoyable self-care activity this week", 
            "self-care", "low", false, "Nov 27, 2024"),
        new CarePlanActivity(5, "Join Peer Support Forum", 
            "Connect with other students in the peer support community", 
            "community", "low", false, "")
    );

    public static final List<RiskAssessment> SAMPLE_RISK_ASSESSMENTS = Arrays.asList(
        new RiskAssessment("medium", 45, 
            Arrays.asList("Moderate stress levels detected", "Limited mood tracking data"), 
            Arrays.asList("Complete stress management learning modules", "Start daily mood tracking"), 
            false),
        new RiskAssessment("high", 72, 
            Arrays.asList("High stress/anxiety indicators", "Low engagement with resources"), 
            Arrays.asList("Schedule immediate counseling session", "Emergency review required"), 
            true)
    );

    // --- Logic Methods ---
    
    public static UserData loadMockUserData(String name, String role) {
        UserData data = new UserData();
        
        // Generate realistic random data
        data.assessmentScore = ThreadLocalRandom.current().nextInt(20, 85);
        data.moodEntries = ThreadLocalRandom.current().nextInt(0, 30);
        data.engagementLevel = ThreadLocalRandom.current().nextInt(30, 90);
        data.stressIndicators = ThreadLocalRandom.current().nextInt(0, 10);
        data.sleepQuality = ThreadLocalRandom.current().nextInt(3, 10);
        data.counselingSessions = ThreadLocalRandom.current().nextInt(0, 5);
        data.learningModulesCompleted = ThreadLocalRandom.current().nextInt(0, 15);
        data.name = name != null ? name : "Demo User";
        data.role = role != null ? role : "student";
        
        // Generate random date within last 30 days
        int randomDays = ThreadLocalRandom.current().nextInt(0, 30);
        LocalDate lastDate = LocalDate.now().minusDays(randomDays);
        data.lastAssessmentDate = lastDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        
        return data;
    }

    public static RiskAssessment analyzeRiskLevel(UserData data) {
        int riskScore = 0;
        List<String> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Assessment score analysis (0-100 scale)
        if (data.assessmentScore > 70) {
            riskScore += 35;
            factors.add("High stress/anxiety indicators in assessments");
            recommendations.add("Schedule immediate counseling session");
        } else if (data.assessmentScore > 40) {
            riskScore += 20;
            factors.add("Moderate stress levels detected");
            recommendations.add("Complete stress management learning modules");
        } else {
            riskScore += 5;
            factors.add("Assessment scores within healthy range");
        }

        // Engagement analysis (0-100 scale)
        if (data.engagementLevel < 30) {
            riskScore += 25;
            factors.add("Low engagement with mental health resources");
            recommendations.add("Explore learning modules and self-assessment tools");
        } else if (data.engagementLevel < 60) {
            riskScore += 10;
            factors.add("Moderate engagement with platform");
        }

        // Mood tracking analysis
        if (data.moodEntries < 5) {
            riskScore += 15;
            factors.add("Limited mood tracking data");
            recommendations.add("Start daily mood tracking for better insights");
        }

        // Stress indicators (0-10 scale)
        if (data.stressIndicators > 7) {
            riskScore += 30;
            factors.add("Multiple stress indicators present");
            recommendations.add("Practice daily relaxation exercises");
        } else if (data.stressIndicators > 4) {
            riskScore += 15;
            factors.add("Some stress indicators identified");
        }

        // Sleep quality (0-10 scale, higher is better)
        if (data.sleepQuality < 4) {
            riskScore += 20;
            factors.add("Poor sleep quality reported");
            recommendations.add("Review sleep hygiene tips and establish routine");
        } else if (data.sleepQuality < 7) {
            riskScore += 10;
            factors.add("Sleep quality could be improved");
        }

        // Counseling sessions
        if (data.counselingSessions == 0 && riskScore > 40) {
            riskScore += 10;
            factors.add("No counseling sessions attended");
            recommendations.add("Book your first counseling session");
        }

        // Determine risk level based on score
        String level;
        boolean counselorAlerted = false;
        
        if (riskScore >= 60) {
            level = "high";
            counselorAlerted = true;
            recommendations.add(0, "PRIORITY: Counselor will be notified for manual review");
        } else if (riskScore >= 30) {
            level = "medium";
        } else {
            level = "low";
            recommendations.add("Continue current wellness practices");
        }

        // Ensure score doesn't exceed 100
        riskScore = Math.min(riskScore, 100);
        
        return new RiskAssessment(level, riskScore, factors, recommendations, counselorAlerted);
    }
    
    public static List<CarePlanActivity> generateCarePlan(RiskAssessment risk, UserData data) {
        List<CarePlanActivity> activities = new ArrayList<>();
        int activityId = 1;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        // Helper method for due dates
        java.util.function.Function<Integer, String> dueDate = (days) -> 
            LocalDate.now().plusDays(days).format(formatter);

        // High-priority activities for high-risk users
        if ("high".equals(risk.level)) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Emergency Counseling Session", 
                "Book an urgent session with a counselor to discuss your current wellbeing", 
                "counseling", "high", false, dueDate.apply(2)
            ));
            activities.add(new CarePlanActivity(
                activityId++, 
                "Daily Mood Check-in", 
                "Track your mood every day for the next week to identify patterns", 
                "assessment", "high", false, dueDate.apply(7)
            ));
            activities.add(new CarePlanActivity(
                activityId++, 
                "Crisis Resources Review", 
                "Familiarize yourself with emergency mental health resources", 
                "learning", "high", false, null
            ));
        }

        // Stress management activities
        if (data.stressIndicators > 4 || data.assessmentScore > 40) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Stress Management Module", 
                "Complete the interactive learning module on managing stress and anxiety", 
                "learning", "high".equals(risk.level) ? "high" : "medium", false, dueDate.apply(5)
            ));
            activities.add(new CarePlanActivity(
                activityId++, 
                "Breathing Exercises Practice", 
                "Practice deep breathing exercises twice daily (5 minutes each)", 
                "self-care", "medium", false, null
            ));
        }

        // Sleep improvement activities
        if (data.sleepQuality < 7) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Sleep Hygiene Assessment", 
                "Complete the sleep quality self-assessment", 
                "assessment", data.sleepQuality < 4 ? "high" : "medium", false, dueDate.apply(3)
            ));
            activities.add(new CarePlanActivity(
                activityId++, 
                "Better Sleep Learning Module", 
                "Learn evidence-based strategies for improving sleep quality", 
                "learning", "medium", false, null
            ));
        }

        // Engagement activities
        if (data.engagementLevel < 60) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Join Peer Support Forum", 
                "Connect with other students in the peer support community", 
                "community", "low", false, null
            ));
            activities.add(new CarePlanActivity(
                activityId++, 
                "Explore Learning Modules", 
                "Browse and start one mental health learning module", 
                "learning", "low", false, null
            ));
        }

        // General wellness activities
        activities.add(new CarePlanActivity(
            activityId++, 
            "Weekly Self-Care Activity", 
            "Schedule and complete one enjoyable self-care activity this week", 
            "self-care", "medium", false, dueDate.apply(7)
        ));

        if (data.moodEntries < 10) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Start Mood Tracking", 
                "Use the daily mood tracker to monitor your emotional wellbeing", 
                "assessment", "medium", false, null
            ));
        }

        // Counseling recommendations
        if (data.counselingSessions < 2 && !"low".equals(risk.level)) {
            activities.add(new CarePlanActivity(
                activityId++, 
                "Schedule Follow-up Counseling", 
                "Book a counseling session to discuss your progress and concerns", 
                "counseling", "high".equals(risk.level) ? "high" : "medium", false, dueDate.apply(10)
            ));
        }

        return activities;
    }

    // Helper method for demo data
    public static CarePlanData getDemoCarePlan(String userName, String userRole) {
        UserData userData = loadMockUserData(userName, userRole);
        RiskAssessment riskAssessment = analyzeRiskLevel(userData);
        List<CarePlanActivity> activities = generateCarePlan(riskAssessment, userData);
        
        return new CarePlanData(userData, riskAssessment, activities);
    }

    // Container class for complete care plan data
    public static class CarePlanData implements Serializable {
        public UserData userData;
        public RiskAssessment riskAssessment;
        public List<CarePlanActivity> activities;
        
        public CarePlanData(UserData userData, RiskAssessment riskAssessment, 
                           List<CarePlanActivity> activities) {
            this.userData = userData;
            this.riskAssessment = riskAssessment;
            this.activities = activities;
        }
    }
}