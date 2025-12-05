package com.secj3303.model;

import java.io.Serializable;

public class APIKey implements Serializable {
    private int id;
    private String name;
    private String key; // The actual API key value
    private String service;
    private String status; // "active" or "inactive"
    private String createdDate;
    private String lastUsed;
    
    public APIKey() {}
    
    public APIKey(int id, String name, String key, String service, String status, String createdDate, String lastUsed) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.service = service;
        this.status = status;
        this.createdDate = createdDate;
        this.lastUsed = lastUsed;
    }
    
    // Getters and Setters
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
    
    // Helper methods
    public boolean isActive() {
        return "active".equals(status);
    }
    
    public String getMaskedKey() {
        if (key == null || key.length() < 8) {
            return "••••••••";
        }
        return key.substring(0, 8) + "••••••••" + key.substring(key.length() - 4);
    }
}