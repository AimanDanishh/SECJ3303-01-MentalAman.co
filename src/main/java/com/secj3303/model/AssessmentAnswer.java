package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "assessment_answers")
public class AssessmentAnswer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "question_id", nullable = false)
    private Integer questionId;
    
    @Column(nullable = false)
    private Integer answer;
    
    @Column(name = "assessment_id", nullable = false)
    private Integer assessmentId;
    
    @Column(name = "student_id", nullable = false)
    private Integer studentId;
    
    // Constructors
    public AssessmentAnswer() {}
    
    public AssessmentAnswer(Integer questionId, Integer answer, Integer assessmentId, Integer studentId) {
        this.questionId = questionId;
        this.answer = answer;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    
    public Integer getAnswer() { return answer; }
    public void setAnswer(Integer answer) { this.answer = answer; }
    
    public Integer getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
    
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
}