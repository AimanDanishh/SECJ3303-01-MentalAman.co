package com.secj3303.model;

import java.io.Serializable;
import java.util.Optional;

public class Student implements Serializable {
    private final int id;
    private final String name;
    private final String email;
    private final String studentId;
    private final String department;
    private final String year;
    private final String initials;
    private final String currentGrade;
    private final int attendance;
    private final String lastActivity;

    public Student(int id, String name, String email, String studentId, String department, String year, String currentGrade, int attendance, String lastActivity) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.department = department;
        this.year = year;
        this.currentGrade = currentGrade;
        this.attendance = attendance;
        this.lastActivity = lastActivity;
        
        // Calculate initials
        String[] parts = name.split(" ");
        this.initials = Optional.ofNullable(parts[0]).map(s -> s.substring(0, 1)).orElse("") +
                        Optional.ofNullable(parts.length > 1 ? parts[parts.length - 1] : null).map(s -> s.substring(0, 1)).orElse("");
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getYear() { return year; }
    public String getInitials() { return initials; }
    public String getCurrentGrade() { return currentGrade; }
    public int getAttendance() { return attendance; }
    public String getLastActivity() { return lastActivity; }

    // Utility methods for Thymeleaf styling
    public String getAttendanceColor() {
        if (attendance >= 85) return "text-green-600";
        if (attendance >= 70) return "text-yellow-600";
        return "text-red-600";
    }
}