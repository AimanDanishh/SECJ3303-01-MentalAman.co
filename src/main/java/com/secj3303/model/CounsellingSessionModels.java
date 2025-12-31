package com.secj3303.model;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
                case "completed": 
                    return "bg-slate-50 border-slate-200";
                case "cancelled": 
                    return "bg-red-50 border-red-200";
                case "confirmed": 
                    return "bg-green-50 border-green-200";
                case "scheduled": // scheduled and pending-reschedule will use default blue
                case "pending-reschedule": 
                default: 
                    return "bg-blue-50 border-blue-200";
            }
        }
        public String getAvatarClass() {
            switch (status) {
                case "completed": 
                    return "bg-slate-200 text-slate-700";
                case "cancelled": 
                    return "bg-red-200 text-red-700";
                case "confirmed": 
                    return "bg-green-200 text-green-700";
                case "pending-reschedule": 
                    return "bg-yellow-200 text-yellow-700"; // Use a distinct color for this new state
                default: // scheduled
                    return "bg-blue-200 text-blue-700";
            }
        }
        public String getStatusBadgeClass() {
            if (status == null) {
                return "bg-gray-200 text-gray-700"; // default for null status
            }
            
            switch (status) {
                case "completed": 
                    return "bg-slate-200 text-slate-700";
                case "cancelled": 
                    return "bg-red-200 text-red-700 x-circle";
                case "confirmed": 
                    return "bg-green-200 text-green-700 check-cricle";
                case "pending-reschedule": 
                    return "bg-yellow-200 text-yellow-700";
                default: // scheduled and any other status
                    return "bg-blue-200 text-blue-700";
            }
        }

        public String getStatusIcon() {
            if (status == null) {
                return "help-circle";
            }
            
            switch (status) {
                case "completed": 
                    return "check-circle";
                case "cancelled": 
                    return "x-circle";
                case "confirmed": 
                    return "check-circle";
                case "pending-reschedule": 
                    return "clock";
                default: // scheduled
                    return "calendar";
            }
        }
    }

    public static class TimeSlot implements Serializable {
        public String date;
        public String time;
        public LocalDate localDate; // Add for sorting
        public LocalTime localTime; // Add for sorting
        
        public TimeSlot(String date, String time, LocalDate localDate, LocalTime localTime) {
            this.date = date;
            this.time = time;
            this.localDate = localDate;
            this.localTime = localTime;
        }
        
        // For backward compatibility
        public TimeSlot(String date, String time) {
            this.date = date;
            this.time = time;
            this.localDate = parseDate(date);
            this.localTime = parseTime(time);
        }
        
        private LocalDate parseDate(String dateStr) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                return LocalDate.now();
            }
        }
        
        private LocalTime parseTime(String timeStr) {
            try {
                // Parse "10:00 AM - 11:00 AM" to get start time
                String startTime = timeStr.split(" - ")[0];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                return LocalTime.parse(startTime.toUpperCase(), formatter);
            } catch (Exception e) {
                return LocalTime.now();
            }
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

     // Generate dynamic time slots (next 14 days, excluding weekends)
    public static List<TimeSlot> getAvailableTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        
        // Define available time slots per day
        LocalTime[] morningSlots = {
            LocalTime.of(9, 0),  // 9:00 AM
            LocalTime.of(11, 0)  // 11:00 AM
        };
        
        LocalTime[] afternoonSlots = {
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0)  // 4:00 PM
        };
        
        // Generate slots for next 14 days
        for (int i = 1; i <= 3; i++) {
            LocalDate slotDate = today.plusDays(i);
            
            // Skip weekends (optional - remove if you want weekend slots)
            DayOfWeek dayOfWeek = slotDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }
            
            String formattedDate = slotDate.format(dateFormatter);
            
            // Add morning slots
            for (LocalTime time : morningSlots) {
                String timeSlot = formatTimeSlot(time, 60); // 60-minute sessions
                slots.add(new TimeSlot(formattedDate, timeSlot, slotDate, time));
            }
            
            // Add afternoon slots
            for (LocalTime time : afternoonSlots) {
                String timeSlot = formatTimeSlot(time, 60); // 60-minute sessions
                slots.add(new TimeSlot(formattedDate, timeSlot, slotDate, time));
            }
        }
        
        // Sort slots by date and time
        slots.sort((s1, s2) -> {
            int dateCompare = s1.localDate.compareTo(s2.localDate);
            if (dateCompare != 0) {
                return dateCompare;
            }
            return s1.localTime.compareTo(s2.localTime);
        });
        
        return slots;
    }
    
    // Format time slot string (e.g., "10:00 AM - 11:00 AM")
    private static String formatTimeSlot(LocalTime startTime, int durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        return startTime.format(timeFormatter) + " - " + endTime.format(timeFormatter);
    }
    
    // Filter out already booked time slots
    public static List<TimeSlot> getAvailableTimeSlots(List<Session> existingSessions) {
        List<TimeSlot> allSlots = getAvailableTimeSlots();
        List<TimeSlot> availableSlots = new ArrayList<>();
        
        // Create a set of booked time slots for quick lookup
        for (TimeSlot slot : allSlots) {
            boolean isBooked = false;
            
            // Check if this slot overlaps with any existing session
            for (Session session : existingSessions) {
                if (session.getDate().equals(slot.date) && session.getTime().equals(slot.time)) {
                    isBooked = true;
                    break;
                }
            }
            
            if (!isBooked) {
                availableSlots.add(slot);
            }
        }
        
        return availableSlots;
    }
    
    // For backward compatibility
    public static final List<TimeSlot> AVAILABLE_SLOTS = getAvailableTimeSlots();
    
    public static final List<Counsellor> COUNSELLOR_LIST = Arrays.asList(
        new Counsellor("Dr. Sarah Johnson", "SJ", "Anxiety & Stress"),
        new Counsellor("Dr. Michael Chen", "MC", "Academic Stress"),
        new Counsellor("Dr. Emily Williams", "EW", "Depression & Anxiety"),
        new Counsellor("Dr. David Lee", "DL", "Career Counselling")
    );

    public static List<Session> getInitialSessions() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        
        // Create sessions with future dates
        return new ArrayList<>(Arrays.asList(
            new Session(1, "Dr. Sarah Johnson", "SJ", "Anxiety & Stress", 
                       today.plusDays(3).format(dateFormatter), 
                       "2:00 PM - 3:00 PM", 
                       "Video Call", "scheduled", false, 
                       "First session - General consultation", null, false, null),
            
            new Session(2, "Dr. Michael Chen", "MC", "Academic Stress", 
                       today.plusDays(5).format(dateFormatter), 
                       "4:00 PM - 5:00 PM", 
                       "Video Call", "confirmed", true, 
                       "Follow-up session", null, false, null),
            
            new Session(3, "Dr. Emily Williams", "EW", "Depression & Anxiety", 
                       today.minusDays(7).format(dateFormatter), // Past session
                       "3:00 PM - 4:00 PM", 
                       "Video Call", "completed", true, 
                       null, null, true, 
                       "Student showed good progress in managing exam stress. Discussed coping strategies including time management and breathing exercises. Recommended follow-up in 2 weeks."),
            
            new Session(4, "Dr. David Lee", "DL", "Career Counselling", 
                       today.plusDays(10).format(dateFormatter), 
                       "11:00 AM - 12:00 PM", 
                       "Video Call", "scheduled", false, 
                       "Career guidance session", null, false, null)
        ));
    }
}