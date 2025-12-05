package com.secj3303.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.secj3303.model.StudentEngagement;

@Service
public class AnalyticsService {

    private final List<StudentEngagement> students;
    private final List<String> departments = Arrays.asList("all", "Computer Science", "Engineering", "Psychology", "Business", "Medicine");

    public AnalyticsService() {
        // Mock data initialization (replicates TSX useState data)
        this.students = Arrays.asList(
            new StudentEngagement(1, "Emma Wilson", "emma.wilson@university.edu", "S2021001", "Computer Science", "2 hours ago", 45, 78, 12, 5, 23, "low", "up"),
            new StudentEngagement(2, "Michael Chen", "michael.chen@university.edu", "S2021002", "Engineering", "5 days ago", 12, 23, 2, 3, 5, "high", "down"),
            new StudentEngagement(3, "Sarah Johnson", "sarah.johnson@university.edu", "S2021003", "Psychology", "1 hour ago", 52, 92, 28, 7, 31, "low", "stable"),
            new StudentEngagement(4, "David Martinez", "david.martinez@university.edu", "S2021004", "Business", "3 days ago", 18, 45, 5, 2, 8, "moderate", "down"),
            new StudentEngagement(5, "Olivia Brown", "olivia.brown@university.edu", "S2021005", "Medicine", "1 day ago", 38, 67, 15, 4, 19, "low", "up"),
            new StudentEngagement(6, "James Anderson", "james.anderson@university.edu", "S2021006", "Engineering", "1 week ago", 8, 15, 0, 1, 2, "high", "down"),
            new StudentEngagement(7, "Lisa Wang", "lisa.wang@university.edu", "S2021007", "Computer Science", "30 mins ago", 48, 85, 21, 6, 27, "low", "up"),
            new StudentEngagement(8, "Robert Taylor", "robert.taylor@university.edu", "S2021008", "Business", "4 days ago", 15, 34, 3, 2, 6, "moderate", "down")
        );
    }

    public List<StudentEngagement> getAllStudents() {
        return students;
    }

    public List<String> getAllDepartments() {
        return departments;
    }

    // --- Filtering Logic (Replicates TSX filteredStudents) ---
    public List<StudentEngagement> filterStudents(String search, String risk, String department) {
        return students.stream()
            .filter(student -> {
                boolean matchesSearch = search == null || search.isEmpty() ||
                                        student.getName().toLowerCase().contains(search.toLowerCase()) ||
                                        student.getStudentId().toLowerCase().contains(search.toLowerCase()) ||
                                        student.getEmail().toLowerCase().contains(search.toLowerCase());
                
                boolean matchesRisk = risk == null || "all".equals(risk) || student.getRiskLevel().equalsIgnoreCase(risk);
                
                boolean matchesDepartment = department == null || "all".equals(department) || student.getDepartment().equalsIgnoreCase(department);
                
                return matchesSearch && matchesRisk && matchesDepartment;
            })
            .collect(Collectors.toList());
    }

    // --- Aggregation Logic (Replicates TSX overall stats) ---
    public int getTotalStudents() {
        return students.size();
    }
    
    public long getHighRiskCount() {
        return students.stream().filter(s -> s.getRiskLevel().equalsIgnoreCase("high")).count();
    }

    public long getModerateRiskCount() {
        return students.stream().filter(s -> s.getRiskLevel().equalsIgnoreCase("moderate")).count();
    }

    public int getAvgCompletion(List<StudentEngagement> list) {
        if (list.isEmpty()) return 0;
        int totalCompletion = list.stream().mapToInt(StudentEngagement::getModuleCompletion).sum();
        return (int) Math.round((double) totalCompletion / list.size());
    }
    
    public int getAvgLoginFrequency(List<StudentEngagement> list) {
        if (list.isEmpty()) return 0;
        int totalLogins = list.stream().mapToInt(StudentEngagement::getLoginFrequency).sum();
        return (int) Math.round((double) totalLogins / list.size());
    }
}