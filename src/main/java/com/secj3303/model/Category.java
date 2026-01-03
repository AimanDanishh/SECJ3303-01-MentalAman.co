package com.secj3303.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "forum_categories")
public class Category {
    
    @Id
    private String id;
    
    private String label;
    private int count;
    
    public Category() {}
    
    public Category(String id, String label, int count) {
        this.id = id;
        this.label = label;
        this.count = count;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}