package com.secj3303.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "gamification_badges")
public class GamificationBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Column(name = "badge_id", nullable = false)
    private String badgeId;
    
    @Column(name = "badge_name")
    private String badgeName;
    
    private String description;
    private String icon;
    private String rarity;
    
    @Column(name = "points_value")
    private int pointsValue;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "awarded_date")
    private LocalDateTime awardedDate;
    
    @Transient
    private boolean earned;
    
    // Constructors
    public GamificationBadge() {}
    
    public GamificationBadge(String badgeId, String badgeName, String description, 
                            String icon, String rarity, int pointsValue, String category) {
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.description = description;
        this.icon = icon;
        this.rarity = rarity;
        this.pointsValue = pointsValue;
        this.category = category;
        this.awardedDate = LocalDateTime.now();
    }
    
    public GamificationBadge(GamificationBadge other) {
        this.badgeId = other.badgeId;
        this.badgeName = other.badgeName;
        this.description = other.description;
        this.icon = other.icon;
        this.rarity = other.rarity;
        this.pointsValue = other.pointsValue;
        this.category = other.category;
        this.earned = other.earned;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    
    public int getPointsValue() { return pointsValue; }
    public void setPointsValue(int pointsValue) { this.pointsValue = pointsValue; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getAwardedDate() { return awardedDate; }
    public void setAwardedDate(LocalDateTime awardedDate) { this.awardedDate = awardedDate; }
    
    public boolean isEarned() { return earned; }
    public void setEarned(boolean earned) { this.earned = earned; }
    
    // Helper method for UI
    public String getBadgeClass() {
        switch (rarity) {
            case "common": return "bg-slate-200";
            case "uncommon": return "bg-green-200 text-green-800";
            case "rare": return "bg-blue-200 text-blue-800";
            case "epic": return "bg-purple-200 text-purple-800";
            default: return "bg-slate-200";
        }
    }
}