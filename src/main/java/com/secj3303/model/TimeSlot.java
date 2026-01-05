package com.secj3303.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimeSlot {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private String formattedDate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.formattedTime = startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
        this.formattedDate = date.format(DATE_FORMATTER);
    }

    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getFormattedTime() { return formattedTime; }
    public String getFormattedDate() { return formattedDate; }

    // For form submission
    public String getSlotIdentifier() {
        return date.toString() + "|" + startTime.toString();
    }

    // --------------------------
    // Static Methods
    // --------------------------
    public static List<TimeSlot> generateAvailableSlots(List<CounsellingSession> existingSessions) {
        return generateAvailableSlots(existingSessions, null, 7);
    }

    public static List<TimeSlot> generateAvailableSlotsForCounsellor(
            List<CounsellingSession> allSessions, 
            Integer counsellorId) {
        
        return generateAvailableSlots(allSessions, counsellorId, 7);
    }

    public static List<TimeSlot> generateAvailableSlots(
            List<CounsellingSession> allSessions,
            Integer counsellorId,
            int daysAhead) {
        
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Filter sessions for specific counsellor if provided
        List<CounsellingSession> relevantSessions = allSessions;
        if (counsellorId != null) {
            relevantSessions = allSessions.stream()
                    .filter(s -> s.getCounsellor() != null && 
                            s.getCounsellor().getId().equals(counsellorId))
                    .collect(Collectors.toList());
        }

        // Start from tomorrow (skip current day)
        for (int i = 1; i <= daysAhead; i++) {
            LocalDate date = today.plusDays(i);
            
            // Skip weekends
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || 
                date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            // Define specific time slots: 9-10, 12-1, 2-3, 4-5
            List<LocalTime[]> timeSlots = Arrays.asList(
                new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(10, 0)},   // 9-10 AM
                new LocalTime[]{LocalTime.of(12, 0), LocalTime.of(13, 0)},  // 12-1 PM
                new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(15, 0)},  // 2-3 PM
                new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(17, 0)}   // 4-5 PM
            );
            
            // Check each time slot
            for (LocalTime[] timeSlot : timeSlots) {
                LocalTime startTime = timeSlot[0];
                LocalTime endTime = timeSlot[1];
                
                boolean isBooked = relevantSessions.stream()
                        .anyMatch(s -> 
                            s.getDate().equals(date) && 
                            s.getStartTime().equals(startTime) &&
                            s.getStatus() != CounsellingSession.SessionStatus.CANCELLED
                        );
                
                if (!isBooked) {
                    slots.add(new TimeSlot(date, startTime, endTime));
                }
            }
        }

        return slots;
    }
}