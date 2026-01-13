package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "care_plan_activities")
public class CarePlanActivity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "care_plan_id", nullable = false)
    private CarePlan carePlan;

    private String title;
    
    @Column(length = 1000)
    private String description;
    
    private String category; // 'self-care', 'learning', 'counseling', 'assessment'
    private String priority; // 'high', 'medium', 'low'
    private boolean completed;
    private String dueDate;

    public CarePlanActivity() {}

    public CarePlanActivity(String title, String description, String category, String priority, String dueDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CarePlan getCarePlan() { return carePlan; }
    public void setCarePlan(CarePlan carePlan) { this.carePlan = carePlan; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    // --- Frontend Helper Methods (Required for your HTML) ---
    
    public String getPriorityBadgeClass() {
        if (priority == null) return "badge-default";
        switch (priority) {
            case "high": return "badge-high";
            case "medium": return "badge-medium";
            case "low": return "badge-low";
            default: return "badge-default";
        }
    }

    public String getCategoryIcon() {
        if (category == null) return "📌";
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