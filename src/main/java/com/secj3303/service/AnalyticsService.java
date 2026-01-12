package com.secj3303.service;

import com.secj3303.model.StudentEngagement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    // --- Mock Data Initialization ---
    // This matches the 8 students seen in the dashboard image (image_f5684b.png)
    private final List<StudentEngagement> allStudents = new ArrayList<>(Arrays.asList(
        new StudentEngagement(1, "Emma Wilson", "emma.wilson@uni.edu", "S2021001", "Computer Science", "2 hours ago", 45, 78, 12, 5, 15, "low", "stable"),
        new StudentEngagement(2, "Michael Chen", "michael.chen@uni.edu", "S2021002", "Engineering", "5 days ago", 12, 23, 2, 3, 4, "high", "down"),
        new StudentEngagement(3, "Sarah Johnson", "sarah.johnson@uni.edu", "S2021003", "Psychology", "1 hour ago", 52, 92, 28, 7, 22, "low", "stable"),
        new StudentEngagement(4, "David Martinez", "david.martinez@uni.edu", "S2021004", "Business", "3 days ago", 18, 45, 5, 2, 8, "moderate", "down"),
        new StudentEngagement(5, "Olivia Brown", "olivia.brown@uni.edu", "S2021005", "Medicine", "1 day ago", 38, 67, 15, 4, 12, "low", "stable"),
        new StudentEngagement(6, "James Anderson", "james.anderson@uni.edu", "S2021006", "Engineering", "1 week ago", 8, 15, 0, 1, 2, "high", "down"),
        new StudentEngagement(7, "Lisa Wang", "lisa.wang@uni.edu", "S2021007", "Computer Science", "30 mins ago", 48, 85, 21, 6, 19, "low", "stable"),
        new StudentEngagement(8, "Robert Taylor", "robert.taylor@uni.edu", "S2021008", "Business", "4 days ago", 15, 34, 3, 2, 5, "moderate", "down")
    ));

    // --- Data Access Methods ---

    public List<StudentEngagement> getAllStudents() {
        return allStudents;
    }

    /**
     * Filters the student list based on search query, risk level, and department.
     */
    public List<StudentEngagement> filterStudents(String searchQuery, String filterRisk, String filterDepartment) {
        return allStudents.stream()
            .filter(s -> searchQuery == null || searchQuery.isEmpty() || 
                    s.getName().toLowerCase().contains(searchQuery.toLowerCase()) || 
                    s.getStudentId().contains(searchQuery) || 
                    s.getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
            .filter(s -> "all".equalsIgnoreCase(filterRisk) || s.getRiskLevel().equalsIgnoreCase(filterRisk))
            .filter(s -> "all".equalsIgnoreCase(filterDepartment) || s.getDepartment().equalsIgnoreCase(filterDepartment))
            .collect(Collectors.toList());
    }

    public List<String> getAllDepartments() {
        List<String> depts = new ArrayList<>();
        depts.add("all");
        depts.addAll(allStudents.stream()
            .map(StudentEngagement::getDepartment)
            .distinct()
            .collect(Collectors.toList()));
        return depts;
    }

    // --- KPI Calculation Methods (Summary Cards) ---

    public long getTotalStudents() {
        return allStudents.size();
    }

    public long getHighRiskCount() {
        return allStudents.stream().filter(StudentEngagement::isHighRisk).count();
    }

    public long getModerateRiskCount() {
        return allStudents.stream().filter(StudentEngagement::isModerateRisk).count();
    }

    /**
     * Calculates the average module completion percentage across all students.
     */
    public int getAvgCompletion(List<StudentEngagement> students) {
        if (students.isEmpty()) return 0;
        return (int) students.stream()
            .mapToInt(StudentEngagement::getModuleCompletion)
            .average()
            .orElse(0);
    }

    /**
     * Calculates average login frequency per month across the provided list.
     */
    public int getAvgLoginFrequency(List<StudentEngagement> students) {
        if (students.isEmpty()) return 0;
        return (int) students.stream()
            .mapToInt(StudentEngagement::getLoginFrequency)
            .average()
            .orElse(0);
    }
}