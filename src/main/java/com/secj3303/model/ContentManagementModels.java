package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContentManagementModels implements Serializable {

    public static class ContentItem implements Serializable {
        private int id;
        private String type;
        private String title;
        private String description;
        private String category;
        private String status;
        private String createdBy;
        private String createdDate;
        private String lastModified;

        public ContentItem() {}

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

        public boolean isPublished() { return "published".equalsIgnoreCase(status); }

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

    public static final Map<String, List<String>> CATEGORIES = Map.of(
        "module", Arrays.asList("Mental Health Basics", "Coping Strategies", "Wellness"),
        "forum", Arrays.asList("Academic Stress", "Self-Care", "Peer Support", "General Discussion"),
        "resource", Arrays.asList("Emergency Resources", "Self-Help Tools")
    );
}