package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "questions")
public class Question implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(length = 1000, nullable = false)
    private String text;
    
    @Column(nullable = false)
    private String type; // 'scale' or 'multiple'
    
    @Column(name = "scale_max")
    private Integer scaleMax;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    // Constructors
    public Question() {}
    
    public Question(String text, String type, Integer scaleMax, Assessment assessment) {
        this.text = text;
        this.type = type;
        this.scaleMax = scaleMax;
        this.assessment = assessment;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Integer getScaleMax() { return scaleMax; }
    public void setScaleMax(Integer scaleMax) { this.scaleMax = scaleMax; }
    
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
}