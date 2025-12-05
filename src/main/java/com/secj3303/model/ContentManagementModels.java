package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContentManagementModels implements Serializable {

    // --- Data Structure (Replacing TS Interface) ---
    
    public static class ContentItem implements Serializable {
        private int id;
        private String type; // 'module' | 'forum' | 'resource'
        private String title;
        private String description;
        private String category;
        private String status; // 'published' | 'draft'
        private String createdBy;
        private String createdDate;
        private String lastModified;

        // Default Constructor for form binding/new items
        public ContentItem() {
            this.status = "draft";
            this.type = "module"; // Default type for new content
            this.createdBy = "Current User";
            this.createdDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            this.lastModified = this.createdDate;
        }

        // Constructor for initial mock data
        public ContentItem(int id, String type, String title, String description, String category, String status, String createdBy, String createdDate, String lastModified) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.description = description;
            this.category = category;
            this.status = status;
            this.createdBy = createdBy;
            this.createdDate = createdDate;
            this.lastModified = lastModified;
        }

        // --- Getters & Setters (Essential for Thymeleaf) ---
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getCreatedDate() { return createdDate; }
        public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
        public String getLastModified() { return lastModified; }
        public void setLastModified(String lastModified) { this.lastModified = lastModified; }

        public boolean isPublished() { return "published".equals(status); }
        
        // Utility methods for view logic
        public String getTypeColor() {
             switch (type) {
                case "module": return "text-blue-600";
                case "forum": return "text-purple-600";
                case "resource": return "text-green-600";
                default: return "text-slate-600";
            }
        }
        public String getStatusBadgeClass() {
            return isPublished() ? "bg-green-100 text-green-800" : "bg-yellow-100 text-yellow-800";
        }
    }
    
    // --- Static Data (Replicating TS arrays/objects) ---

    public static List<ContentItem> getInitialContentItems() {
        return new ArrayList<>(Arrays.asList(
            new ContentItem(1, "module", "Understanding Anxiety", "Comprehensive guide to understanding and managing anxiety", "Mental Health Basics", "published", "Dr. Sarah Johnson", "Oct 15, 2025", "Nov 1, 2025"),
            new ContentItem(2, "module", "Stress Management Techniques", "Learn effective strategies for managing daily stress", "Coping Strategies", "published", "Prof. Michael Chen", "Oct 20, 2025", "Oct 28, 2025"),
            new ContentItem(3, "forum", "Exam Anxiety Support", "Discussion space for students dealing with exam-related stress", "Academic Stress", "published", "Admin", "Sep 5, 2025", "Nov 10, 2025"),
            new ContentItem(4, "forum", "Sleep and Mental Health", "Share tips and experiences about improving sleep quality", "Self-Care", "published", "Dr. Emily Brown", "Oct 1, 2025", "Nov 5, 2025"),
            new ContentItem(5, "resource", "Crisis Helpline Numbers", "Emergency contact information for mental health support", "Emergency Resources", "published", "Admin", "Aug 1, 2025", "Nov 12, 2025"),
            new ContentItem(6, "resource", "Mindfulness App Recommendations", "Curated list of recommended mindfulness and meditation apps", "Self-Help Tools", "draft", "Prof. David Lee", "Nov 8, 2025", "Nov 11, 2025"),
            new ContentItem(7, "module", "Depression Awareness", "Recognizing signs and seeking help for depression", "Mental Health Basics", "draft", "Dr. Sarah Johnson", "Nov 10, 2025", "Nov 12, 2025")
        ));
    }

    public static final Map<String, List<String>> CATEGORIES = Map.of(
        "module", Arrays.asList("Mental Health Basics", "Coping Strategies", "Wellness", "Crisis Management"),
        "forum", Arrays.asList("Academic Stress", "Self-Care", "Peer Support", "General Discussion"),
        "resource", Arrays.asList("Emergency Resources", "Self-Help Tools", "External Links", "Reading Materials")
    );
}