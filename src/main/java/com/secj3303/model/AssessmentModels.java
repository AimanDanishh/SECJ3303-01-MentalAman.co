package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class AssessmentModels implements Serializable {
    
    // This class now only contains utility methods and constants
    public static final List<String> SCALE_LABELS_4 = Arrays.asList(
        "Not at all", "Several days", "More than half the days", "Nearly every day"
    );
    
    public static final List<String> SCALE_LABELS_5 = Arrays.asList(
        "Very dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very satisfied"
    );
    
    public static final List<String> SCALE_LABELS_6 = Arrays.asList(
        "Very unhappy", "Unhappy", "Neutral", "Happy", "Very happy", "Extremely happy"
    );
    
    // Utility class for form binding
    public static class AssessmentAnswersForm implements Serializable {
        private Integer assessmentId;
        private Integer currentQuestionIndex;
        private java.util.Map<String, Integer> answers;
        
        public AssessmentAnswersForm() {}
        
        public Integer getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
        
        public Integer getCurrentQuestionIndex() { return currentQuestionIndex; }
        public void setCurrentQuestionIndex(Integer currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
        
        public java.util.Map<String, Integer> getAnswers() { return answers; }
        public void setAnswers(java.util.Map<String, Integer> answers) { this.answers = answers; }
    }
}