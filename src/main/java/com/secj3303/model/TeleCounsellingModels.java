package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeleCounsellingModels implements Serializable {

    // Utility to calculate initials from name
    private static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "";
        return Arrays.stream(name.split(" "))
                     .filter(s -> !s.isEmpty())
                     .map(s -> s.substring(0, 1))
                     .collect(Collectors.joining(""));
    }

    // --- Session Structure (Upcoming & Past) ---

    public static class Session implements Serializable {
        public int id;
        public String counsellor;
        public String specialty;
        public String date;
        public String time; // Can also hold duration for past sessions
        public String type;
        public String status; // 'confirmed', 'completed', etc.
        public String notes;

        public Session(int id, String counsellor, String specialty, String date, String time, String type, String status, String notes) {
            this.id = id;
            this.counsellor = counsellor;
            this.specialty = specialty;
            this.date = date;
            this.time = time;
            this.type = type;
            this.status = status;
            this.notes = notes;
        }
        
        public String getInitials() { return TeleCounsellingModels.getInitials(counsellor); }
        public String getCardClass() { return "confirmed".equals(status) ? "border-blue-200 bg-blue-50" : "border-slate-200 bg-slate-50"; }
    }
    
    // --- Counsellor Structure ---

    public static class AvailableCounsellor implements Serializable {
        public String name;
        public String specialty;
        public String experience;
        public double rating;
        public String availability;
        public String image; // Initials placeholder

        public AvailableCounsellor(String name, String specialty, String experience, double rating, String availability, String image) {
            this.name = name;
            this.specialty = specialty;
            this.experience = experience;
            this.rating = rating;
            this.availability = availability;
            this.image = image;
        }
    }

    // --- Static Data Initialization (Replicating TSX State) ---

    public static List<Session> getUpcomingSessions() {
        return Arrays.asList(
            new Session(1, "Dr. Sarah Johnson", "Anxiety & Stress", "Nov 15, 2025", "2:00 PM - 3:00 PM", "Video Call", "confirmed", "First session - General consultation"),
            new Session(2, "Dr. Michael Chen", "Academic Stress", "Nov 18, 2025", "4:00 PM - 5:00 PM", "Video Call", "confirmed", "Follow-up session")
        );
    }

    public static List<Session> getPastSessions() {
        return Arrays.asList(
            new Session(1, "Dr. Emily Williams", null, "Nov 6, 2025", "60 min", null, "completed", "Discussed coping strategies for exam stress"),
            new Session(2, "Dr. Sarah Johnson", null, "Oct 28, 2025", "60 min", null, "completed", "Initial consultation - anxiety management")
        );
    }
    
    public static List<AvailableCounsellor> getAvailableCounsellors() {
        return Arrays.asList(
            new AvailableCounsellor("Dr. Sarah Johnson", "Anxiety & Depression", "12 years", 4.9, "Available Today", "SJ"),
            new AvailableCounsellor("Dr. Michael Chen", "Academic Stress & Burnout", "8 years", 4.8, "Available Tomorrow", "MC"),
            new AvailableCounsellor("Dr. Emily Williams", "Relationship & Social Anxiety", "15 years", 4.9, "Available in 2 days", "EW")
        );
    }
}