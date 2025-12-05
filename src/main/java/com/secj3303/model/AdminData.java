package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// --- POJOs matching TSX interfaces ---

public class AdminData implements Serializable {

    public static class FlaggedContentItem implements Serializable {
        private final int id;
        private final String type;
        private final String reason;
        private final String reporter;
        private final String time;
        private final String content;

        public FlaggedContentItem(int id, String type, String reason, String reporter, String time, String content) {
            this.id = id;
            this.type = type;
            this.reason = reason;
            this.reporter = reporter;
            this.time = time;
            this.content = content;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public String getReason() { return reason; }
        public String getReporter() { return reporter; }
        public String getTime() { return time; }
        public String getContent() { return content; }
    }

    public static class ContentModule implements Serializable {
        private int id;
        private String module;
        private String status; // 'published' or 'draft'
        private int views;
        private String completion;
        private String description;
        private String category;
        private String createdBy;
        private String createdDate;

        // Constructor for initial data
        public ContentModule(int id, String module, String status, int views, String completion, 
                             String description, String category, String createdBy, String createdDate) {
            this.id = id;
            this.module = module;
            this.status = status;
            this.views = views;
            this.completion = completion;
            this.description = description;
            this.category = category;
            this.createdBy = createdBy;
            this.createdDate = createdDate;
        }

        // Constructor for form binding/new module
        public ContentModule() {
            this.id = 0; // Default ID for a new module
            this.status = "draft";
            this.views = 0;
            this.completion = "0%";
            this.createdBy = "Current Admin";
            // Simple date formatting
            this.createdDate = LocalDate.now().getMonth().name().substring(0, 1) + LocalDate.now().getMonth().name().substring(1).toLowerCase() + " " + LocalDate.now().getDayOfMonth() + ", " + LocalDate.now().getYear();
        }

        // Getters and Setters (Necessary for form binding and Thymeleaf)
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getViews() { return views; }
        public void setViews(int views) { this.views = views; }
        public String getCompletion() { return completion; }
        public void setCompletion(String completion) { this.completion = completion; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getCreatedDate() { return createdDate; }
        public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

        public boolean isPublished() { return "published".equals(status); }
    }
    
    // --- Initial Data Sets (Replicating TSX useState arrays) ---

    public static List<ContentModule> getInitialContentManagement() {
        return new ArrayList<>(Arrays.asList(
            new ContentModule(1, "Stress Management", "published", 456, "78%", "Learn effective strategies for managing daily stress", "Coping Strategies", "Dr. Sarah Johnson", "Oct 15, 2025"),
            new ContentModule(2, "Anxiety Awareness", "published", 389, "65%", "Understanding anxiety and its management", "Mental Health Basics", "Prof. Michael Chen", "Sep 20, 2025"),
            new ContentModule(3, "Depression Understanding", "draft", 0, "0%", "Recognizing signs and seeking help for depression", "Mental Health Basics", "Dr. Emily Brown", "Nov 10, 2025"),
            new ContentModule(4, "Mindfulness Basics", "published", 523, "82%", "Introduction to mindfulness meditation practices", "Wellness", "Prof. David Lee", "Aug 5, 2025")
        ));
    }

    public static List<FlaggedContentItem> getInitialFlaggedContent() {
        return new ArrayList<>(Arrays.asList(
            new FlaggedContentItem(1, "Forum Post", "Inappropriate language", "Student #456", "2 hours ago", "Post contained offensive language in mental health discussion"),
            new FlaggedContentItem(2, "Comment", "Spam", "Student #789", "5 hours ago", "Multiple promotional links posted in peer support forum"),
            new FlaggedContentItem(3, "Forum Post", "Misinformation", "Faculty #12", "1 day ago", "Sharing unverified medical advice about mental health treatment")
        ));
    }
    
    // --- Static Data (Replicating other arrays) ---
    
    public static final List<String> CATEGORIES = Arrays.asList(
        "Mental Health Basics", "Coping Strategies", "Wellness", "Crisis Management", "Self-Care"
    );

    public static final List<Map<String, String>> SYSTEM_STATS = Arrays.asList(
        Map.of("label", "Total Students", "value", "1,247", "change", "+12%", "icon", "users", "color", "blue"),
        Map.of("label", "Active Engagements", "value", "892", "change", "+8%", "icon", "activity", "color", "green"),
        Map.of("label", "Modules Completed", "value", "3,456", "change", "+15%", "icon", "book-open", "color", "purple"),
        Map.of("label", "Avg. Progress", "value", "67%", "change", "+5%", "icon", "trending-up", "color", "orange")
    );
    
    public static final List<Map<String, String>> RECENT_ACTIVITY = Arrays.asList(
        Map.of("user", "Student #1234", "action", "Completed Stress Management Module", "time", "5 min ago", "status", "success"),
        Map.of("user", "Faculty #567", "action", "Created new forum discussion", "time", "12 min ago", "status", "info"),
        Map.of("user", "Student #891", "action", "Booked counselling session", "time", "23 min ago", "status", "success"),
        Map.of("user", "Student #234", "action", "Reported inappropriate content", "time", "45 min ago", "status", "warning"),
        Map.of("user", "Counsellor #12", "action", "Completed session notes", "time", "1 hour ago", "status", "info")
    );

    public static final List<Map<String, String>> ANALYTICS_DATA = Arrays.asList(
        Map.of("metric", "Daily Active Users", "value", "432", "trend", "up"),
        Map.of("metric", "Average Session Time", "value", "24 min", "trend", "up"),
        Map.of("metric", "Forum Posts (Weekly)", "value", "89", "trend", "down"),
        Map.of("metric", "Counselling Bookings", "value", "156", "trend", "up")
    );
}