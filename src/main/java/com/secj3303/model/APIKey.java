package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class APIKey implements Serializable {
    private int id;
    private String name;
    private String key;
    private String service;
    private String status; // 'active' or 'inactive'
    private String createdDate;
    private String lastUsed;

    // Default Constructor for form binding
    public APIKey() {
        this.status = "active";
        this.createdDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        this.lastUsed = "Never";
        this.key = "sk_auto_" + UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    }

    // Constructor for initial mock data
    public APIKey(int id, String name, String key, String service, String status, String createdDate, String lastUsed) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.service = service;
        this.status = status;
        this.createdDate = createdDate;
        this.lastUsed = lastUsed;
    }

    // --- Getters and Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public String getLastUsed() { return lastUsed; }
    public void setLastUsed(String lastUsed) { this.lastUsed = lastUsed; }

    public boolean isActive() { return "active".equals(status); }
    public boolean isInactive() { return "inactive".equals(status); }

    // Utility for masking (replicates TSX logic)
    public String getMaskedKey() {
        if (key == null || key.length() <= 8) return "••••••••";
        int len = key.length();
        return key.substring(0, 8) + "•".repeat(len - 12) + key.substring(len - 4);
    }
}