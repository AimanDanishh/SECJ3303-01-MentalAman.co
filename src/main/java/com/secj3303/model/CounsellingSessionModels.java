package com.secj3303.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CounsellingSessionModels implements Serializable {

    // --- Data Structures ---
    
    public static class Session implements Serializable {
        private int id;
        private String counsellor;
        private String counsellorId;
        private String specialty;
        private String date;
        private String time;
        private String type;
        private String status; // 'scheduled' | 'confirmed' | 'completed' | 'cancelled' | 'pending-reschedule'
        private boolean studentConfirmed;
        private String notes;
        private String cancellationReason;
        private boolean reportAvailable;
        private String reportContent;

        // Constructor for mock data
        public Session(int id, String counsellor, String counsellorId, String specialty, String date, String time, String type, String status, boolean studentConfirmed, String notes, String cancellationReason, boolean reportAvailable, String reportContent) {
            this.id = id;
            this.counsellor = counsellor;
            this.counsellorId = counsellorId;
            this.specialty = specialty;
            this.date = date;
            this.time = time;
            this.type = type;
            this.status = status;
            this.studentConfirmed = studentConfirmed;
            this.notes = notes;
            this.cancellationReason = cancellationReason;
            this.reportAvailable = reportAvailable;
            this.reportContent = reportContent;
        }

        // Default constructor for form submission
        public Session() {}

        // --- Getters and Setters (omitted for brevity, but necessary for Thymeleaf binding) ---
        public int getId() { return id; }
        public String getCounsellor() { return counsellor; }
        public String getCounsellorId() { return counsellorId; }
        public String getSpecialty() { return specialty; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public boolean isStudentConfirmed() { return studentConfirmed; }
        public String getNotes() { return notes; }
        public String getCancellationReason() { return cancellationReason; }
        public boolean isReportAvailable() { return reportAvailable; }
        public String getReportContent() { return reportContent; }

        public void setId(int id) { this.id = id; }
        public void setStatus(String status) { this.status = status; }
        public void setDate(String date) { this.date = date; }
        public void setTime(String time) { this.time = time; }
        public void setStudentConfirmed(boolean studentConfirmed) { this.studentConfirmed = studentConfirmed; }
        public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
        public void setReportAvailable(boolean reportAvailable) { this.reportAvailable = reportAvailable; }
        public void setReportContent(String reportContent) { this.reportContent = reportContent; }
        public void setType(String type) { this.type = type; }
        public void setCounsellor(String counsellor) { this.counsellor = counsellor; }
        public void setCounsellorId(String counsellorId) { this.counsellorId = counsellorId; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }
        public void setNotes(String notes) { this.notes = notes; }

        // --- Utility Methods for Thymeleaf Styling ---
        public String getCardClass() {
            switch (status) {
                case "completed": return "bg-slate-50 border-slate-200";
                case "cancelled": return "bg-red-50 border-red-200";
                case "confirmed": return "bg-green-50 border-green-200";
                default: return "bg-blue-50 border-blue-200";
            }
        }
        public String getAvatarClass() {
            switch (status) {
                case "cancelled": return "bg-red-200 text-red-700";
                case "completed": return "bg-slate-300 text-slate-700";
                default: return "bg-blue-600 text-white";
            }
        }
    }

    public static class TimeSlot implements Serializable {
        public String date;
        public String time;
        public TimeSlot(String date, String time) {
            this.date = date;
            this.time = time;
        }
    }
    
    public static class Counsellor implements Serializable {
        public String name;
        public String id;
        public String specialty;
        public Counsellor(String name, String id, String specialty) {
            this.name = name;
            this.id = id;
            this.specialty = specialty;
        }
    }

    public static final List<TimeSlot> AVAILABLE_SLOTS = Arrays.asList(
        new TimeSlot("Nov 20, 2025", "10:00 AM - 11:00 AM"),
        new TimeSlot("Nov 20, 2025", "2:00 PM - 3:00 PM"),
        new TimeSlot("Nov 21, 2025", "11:00 AM - 12:00 PM"),
        new TimeSlot("Nov 21, 2025", "3:00 PM - 4:00 PM"),
        new TimeSlot("Nov 22, 2025", "9:00 AM - 10:00 AM"),
        new TimeSlot("Nov 22, 2025", "1:00 PM - 2:00 PM")
    );
    
    public static final List<Counsellor> COUNSELLOR_LIST = Arrays.asList(
        new Counsellor("Dr. Sarah Johnson", "SJ", "Anxiety & Stress"),
        new Counsellor("Dr. Michael Chen", "MC", "Academic Stress"),
        new Counsellor("Dr. Emily Williams", "EW", "Depression & Anxiety"),
        new Counsellor("Dr. David Lee", "DL", "Career Counselling")
    );

    public static List<Session> getInitialSessions() {
        return new ArrayList<>(Arrays.asList(
            new Session(1, "Dr. Sarah Johnson", "SJ", "Anxiety & Stress", "Nov 15, 2025", "2:00 PM - 3:00 PM", "Video Call", "scheduled", false, "First session - General consultation", null, false, null),
            new Session(2, "Dr. Michael Chen", "MC", "Academic Stress", "Nov 18, 2025", "4:00 PM - 5:00 PM", "Video Call", "confirmed", true, "Follow-up session", null, false, null),
            new Session(3, "Dr. Emily Williams", "EW", "Depression & Anxiety", "Nov 6, 2025", "3:00 PM - 4:00 PM", "Video Call", "completed", true, null, null, true, "Student showed good progress in managing exam stress. Discussed coping strategies including time management and breathing exercises. Recommended follow-up in 2 weeks.")
        ));
    }
}