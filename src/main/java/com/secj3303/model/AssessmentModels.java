package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.text.html.Option;

public class AssessmentModels implements Serializable {

    // --- Data Structures ---

    public static class Question implements Serializable {
        public int id;
        public String text;
        public String type; // 'scale' | 'multiple'
        public int scaleMax; // 3 or 4 or 5
        public List<Option> options;
        
        public Question(int id, String text, String type, int scaleMax) {
            this.id = id;
            this.text = text;
            this.type = type;
            this.scaleMax = scaleMax;
        }

        public int getId() { return id; }
        public String getText() { return text; }
        public String getType() { return type; }
        public int getScaleMax() { return scaleMax; }
    }

    public static class Assessment implements Serializable {
        public int id;
        public String title;
        public String description;
        public String category;
        public String duration;
        public String color;
        public List<Question> questions;
        
        public Assessment(int id, String title, String description, String category, String duration, String color, List<Question> questions) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.duration = duration;
            this.color = color;
            this.questions = questions;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getDuration() { return duration; }
        public String getColor() { return color; }
        public List<Question> getQuestions() { return questions; }
    }

    public static class AssessmentResult implements Serializable {
        public int id;
        public String assessmentTitle;
        public String date;
        public int score;
        public String severity; // 'Mild', 'Moderate', 'Severe'
        public boolean reportAvailable;
        
        public AssessmentResult(int id, String assessmentTitle, String date, int score, String severity, boolean reportAvailable) {
            this.id = id;
            this.assessmentTitle = assessmentTitle;
            this.date = date;
            this.score = score;
            this.severity = severity;
            this.reportAvailable = reportAvailable;
        }

        public int getId() { return id; }
        public String getAssessmentTitle() { return assessmentTitle; }
        public String getDate() { return date; }
        public int getScore() { return score; }
        public String getSeverity() { return severity; }
        public boolean isReportAvailable() { return reportAvailable; }
    }

    public static class MoodEntry implements Serializable {
        public int id;
        public String date;
        public String mood;
        public String notes;
        public String timestamp;
    }

    public static class StudentData implements Serializable {
        public int id;
        public String name;
        public String email;
        public String studentId;
        public String department;
        public String year;
        public String initials;
        public int assessmentCount;
        public String lastAssessment;
        public String riskLevel;
        public List<AssessmentResult> assessmentHistory;
        public Map<String, String> upcomingSession;

        public StudentData(int id, String name, String email, String studentId, String department, String year, String initials, int assessmentCount, String lastAssessment, String riskLevel, List<AssessmentResult> history, Map<String, String> session) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.studentId = studentId;
            this.department = department;
            this.year = year;
            this.initials = initials;
            this.assessmentCount = assessmentCount;
            this.lastAssessment = lastAssessment;
            this.riskLevel = riskLevel;
            this.assessmentHistory = history;
            this.upcomingSession = session;
        }
        
        // Utility method
        public String getInitialsColor() {
             switch (riskLevel) {
                case "low": return "bg-green-600";
                case "moderate": return "bg-yellow-600";
                case "high": return "bg-red-600";
                default: return "bg-slate-600";
            }
        }

        public String getBadgeColor() {
        switch (riskLevel) {
            case "low": return "bg-green-100 text-green-700";
            case "moderate": return "bg-yellow-100 text-yellow-700";
            case "high": return "bg-red-100 text-red-700";
            default: return "bg-slate-100 text-slate-700";
        }
    }
    }
    
    public static class AssessmentAnswers implements Serializable {
        public Map<String, Integer> answers;
        public int assessmentId;
        public int currentQuestionIndex;
        public AssessmentAnswers() {} // For form binding
    }

    // --- Initial Mock Data ---

    public static final List<Assessment> AVAILABLE_ASSESSMENTS = Arrays.asList(
        new Assessment(1, "Depression Screening (PHQ-9)", "Patient Health Questionnaire - 9 item depression scale", "Depression", "5-7 minutes", "blue", 
            Arrays.asList(new Question(1, "Over the last 2 weeks, how often have you felt little interest or pleasure in doing things?", "scale", 3), new Question(2, "Over the last 2 weeks, how often have you felt down, depressed, or hopeless?", "scale", 3), new Question(3, "Over the last 2 weeks, how often have you had trouble falling or staying asleep, or sleeping too much?", "scale", 3), new Question(4, "Over the last 2 weeks, how often have you felt tired or had little energy?", "scale", 3), new Question(5, "Over the last 2 weeks, how often have you had poor appetite or been overeating?", "scale", 3))
        ),
        new Assessment(2, "Anxiety Screening (GAD-7)", "Generalized Anxiety Disorder - 7 item scale", "Anxiety", "5 minutes", "purple", 
            Arrays.asList(new Question(10, "Over the last 2 weeks, how often have you felt nervous, anxious, or on edge?", "scale", 3), new Question(11, "Over the last 2 weeks, how often have you been unable to stop or control worrying?", "scale", 3), new Question(12, "Over the last 2 weeks, how often have you been worrying too much about different things?", "scale", 3), new Question(13, "Over the last 2 weeks, how often have you had trouble relaxing?", "scale", 3))
        ),
        new Assessment(3, "Stress Assessment (PSS-10)", "Perceived Stress Scale - 10 item questionnaire", "Stress", "6-8 minutes", "orange", 
            Arrays.asList(new Question(20, "In the last month, how often have you been upset because of something that happened unexpectedly?", "scale", 4), new Question(21, "In the last month, how often have you felt that you were unable to control important things in your life?", "scale", 4), new Question(22, "In the last month, how often have you felt nervous and stressed?", "scale", 4))
        ),
        new Assessment(4, "Well-being Assessment", "General mental well-being and life satisfaction", "Well-being", "4-5 minutes", "green", 
            Arrays.asList(new Question(30, "How satisfied are you with your life overall?", "scale", 5), new Question(31, "How often do you feel happy and content?", "scale", 5))
        )
    );

    public static final List<StudentData> ASSIGNED_STUDENTS = Arrays.asList(
        new StudentData(1, "Emma Wilson", "emma.wilson@university.edu", "S2021001", "Computer Science", "Year 3", "EW", 5, "Nov 8, 2025", "moderate",
            Arrays.asList(new AssessmentResult(1, "Depression Screening (PHQ-9)", "Nov 8, 2025", 58, "Moderate", true), new AssessmentResult(2, "Anxiety Screening (GAD-7)", "Nov 1, 2025", 45, "Moderate", true)),
            Map.of("date", "Nov 15, 2025", "time", "2:00 PM - 3:00 PM", "type", "Video Call", "status", "confirmed")),
        new StudentData(2, "Michael Chen", "michael.chen@university.edu", "S2021002", "Engineering", "Year 2", "MC", 3, "Nov 10, 2025", "high",
            Arrays.asList(new AssessmentResult(1, "Depression Screening (PHQ-9)", "Nov 10, 2025", 78, "Severe", true), new AssessmentResult(2, "Anxiety Screening (GAD-7)", "Nov 5, 2025", 82, "Severe", true)),
            Map.of("date", "Nov 14, 2025", "time", "10:00 AM - 11:00 AM", "type", "Video Call", "status", "confirmed")),
        new StudentData(3, "Sarah Johnson", "sarah.johnson@university.edu", "S2021003", "Psychology", "Year 4", "SJ", 7, "Nov 12, 2025", "low",
            Arrays.asList(new AssessmentResult(1, "Well-being Assessment", "Nov 12, 2025", 25, "Mild", true), new AssessmentResult(2, "Stress Assessment (PSS-10)", "Nov 7, 2025", 30, "Mild", true)),
            null)
    );

    // Utility mapping for labels (needed in view)
    public static final List<String> SCALE_LABELS_4 = Arrays.asList("Not at all", "Several days", "More than half the days", "Nearly every day");
    public static final List<String> SCALE_LABELS_5 = Arrays.asList("Very dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very satisfied");
    public static final List<String> SCALE_LABELS_6 = Arrays.asList("Very unhappy", "Unhappy", "Neutral", "Happy", "Very happy", "Extremely happy");
}