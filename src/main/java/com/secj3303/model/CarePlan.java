package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "care_plans")
public class CarePlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "risk_level") 
    private String riskLevel; // 'high', 'medium', 'low'

    @Column(name = "risk_score")
    private Integer riskScore;

    @OneToMany(mappedBy = "carePlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CarePlanActivity> activities = new ArrayList<>();

    public CarePlan() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public List<CarePlanActivity> getActivities() { return activities; }
    public void setActivities(List<CarePlanActivity> activities) { this.activities = activities; }

    // Helper to add activity
    public void addActivity(CarePlanActivity activity) {
        activities.add(activity);
        activity.setCarePlan(this);
    }
}