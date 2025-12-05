package com.secj3303.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.secj3303.model.AssessmentModels;
import com.secj3303.model.AssessmentModels.Assessment;
import com.secj3303.model.AssessmentModels.AssessmentAnswers;
import com.secj3303.model.AssessmentModels.AssessmentResult;
import com.secj3303.model.AssessmentModels.StudentData;

@Service
public class AssessmentService {

    public List<Assessment> getAllAssessments() {
        return AssessmentModels.AVAILABLE_ASSESSMENTS;
    }

    public Optional<Assessment> findAssessment(int id) {
        return AssessmentModels.AVAILABLE_ASSESSMENTS.stream().filter(a -> a.id == id).findFirst();
    }
    
    public List<StudentData> getAllAssignedStudents() {
        return AssessmentModels.ASSIGNED_STUDENTS;
    }

    // --- Filtering Logic (Replicates student filtering in TSX) ---
    public List<StudentData> filterStudents(String search, String risk) {
        return AssessmentModels.ASSIGNED_STUDENTS.stream()
            .filter(student -> {
                boolean matchesSearch = search == null || search.isEmpty() ||
                                        student.name.toLowerCase().contains(search.toLowerCase()) ||
                                        student.studentId.toLowerCase().contains(search.toLowerCase()) ||
                                        student.email.toLowerCase().contains(search.toLowerCase());
                
                boolean matchesRisk = risk == null || "all".equals(risk) || student.riskLevel.equalsIgnoreCase(risk);
                
                return matchesSearch && matchesRisk;
            })
            .collect(Collectors.toList());
    }

    // --- Score Calculation (Replicates handleSubmit logic) ---
    public AssessmentResult calculateScore(Assessment assessment, AssessmentAnswers answers) {
        
        int totalScore = answers.answers.values().stream().mapToInt(Integer::intValue).sum();
        int maxScore = assessment.questions.stream().mapToInt(q -> q.scaleMax).sum();
        // Since scaleMax is max score for one question, total max score is sum of all scaleMax.
        
        // Normalize score to 0-100 range
        int normalizedScore = (int) Math.round((totalScore / (double) maxScore) * 100);
        
        String severity;
        if (normalizedScore < 33) {
            severity = "Mild";
        } else if (normalizedScore < 66) {
            severity = "Moderate";
        } else {
            severity = "Severe";
        }

        // Mock ID generation
        int id = ThreadLocalRandom.current().nextInt(1000) + 10;
        
        return new AssessmentResult(
            id,
            assessment.title,
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            normalizedScore,
            severity,
            true
        );
    }
    
    // Utility to get scale labels
    public List<String> getScaleLabels(int scaleMax) {
        if (scaleMax == 3) return AssessmentModels.SCALE_LABELS_4;
        if (scaleMax == 4) return AssessmentModels.SCALE_LABELS_5; // Assuming PSS-10 has 5 options (0-4)
        if (scaleMax == 5) return AssessmentModels.SCALE_LABELS_6; // Assuming Well-being has 6 options (0-5)
        return new ArrayList<>();
    }
}