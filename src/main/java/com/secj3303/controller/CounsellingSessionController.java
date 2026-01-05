package com.secj3303.controller;

import com.secj3303.model.CounsellingSession;
import com.secj3303.model.Counsellor;
import com.secj3303.model.TimeSlot;
import com.secj3303.model.User;
import com.secj3303.service.CounsellingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/counselling")
public class CounsellingSessionController {

    @Autowired
    private CounsellingSessionService sessionService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---------------------------
    // Dashboard
    // ---------------------------
    @GetMapping
    public String sessionDashboard(Model model, Authentication authentication,
                                   @RequestParam(required = false) Integer detailId,
                                   @RequestParam(required = false) String modal,
                                   @RequestParam(required = false) Integer counsellorId) {
        
        // Build user from authentication
        User user = buildUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", "counselling");

        // Fetch data
        List<CounsellingSession> sessions = sessionService.getAllSessions();
        model.addAttribute("sessions", sessions);

        List<Counsellor> counsellors = sessionService.getAllCounsellors();
        model.addAttribute("counsellorList", counsellors);

         // Get selected counsellor
        Counsellor selectedCounsellor = null;
        if (counsellorId != null) {
            selectedCounsellor = sessionService.getCounsellorById(counsellorId);
            model.addAttribute("selectedCounsellorId", counsellorId);
        }
        
        // Generate available time slots ONLY if counsellor is selected
        List<TimeSlot> slots = Collections.emptyList();
        if (selectedCounsellor != null) {
            slots = sessionService.generateAvailableSlotsForCounsellor(selectedCounsellor);
        }
        model.addAttribute("availableTimeSlots", slots);

        // Initialize modal states
        initializeModalStates(model);

        // Handle modal display based on parameters
        handleModalDisplay(model, detailId, modal, sessions);

        return "counselling-session";
    }

    // ---------------------------
    // Confirm attendance
    // ---------------------------
    @PostMapping("/confirm/{id}")
    public String confirmSession(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            sessionService.confirmSession(id);
            redirect.addFlashAttribute("alert", "✓ Attendance confirmed!");
            redirect.addFlashAttribute("alertType", "success");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("alert", "Error: " + e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
        } catch (Exception e) {
            redirect.addFlashAttribute("alert", "An unexpected error occurred");
            redirect.addFlashAttribute("alertType", "error");
        }
        return "redirect:/counselling";
    }

    // ---------------------------
    // Cancel session
    // ---------------------------
    @PostMapping("/cancel/{id}")
    public String cancelSession(@PathVariable Integer id,
                                @RequestParam String cancellationReason,
                                RedirectAttributes redirect) {
        
        try {
            sessionService.cancelSession(id, cancellationReason);
            redirect.addFlashAttribute("alert", "✓ Session cancelled successfully.");
            redirect.addFlashAttribute("alertType", "success");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("alert", e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "cancel");
        } catch (Exception e) {
            redirect.addFlashAttribute("alert", "An unexpected error occurred");
            redirect.addFlashAttribute("alertType", "error");
        }
        return "redirect:/counselling";
    }

    // ---------------------------
    // Reschedule session
    // ---------------------------
    @PostMapping("/reschedule/{id}")
    public String rescheduleSession(@PathVariable Integer id,
                                    @RequestParam String selectedSlot,  // Changed from separate date/time
                                    @RequestParam(required = false) String rescheduleNotes,
                                    RedirectAttributes redirect) {
        
        try {
            // Parse the slot identifier (format: "2024-01-15|09:00")
            String[] slotParts = selectedSlot.split("\\|");
            if (slotParts.length != 2) {
                throw new IllegalArgumentException("Invalid slot format");
            }
            
            LocalDate date = LocalDate.parse(slotParts[0], DATE_FORMATTER);
            LocalTime time = LocalTime.parse(slotParts[1]);  // Should be in ISO format (HH:mm)
            
            // Call service with the parsed date and time
            sessionService.rescheduleSession(id, date, time);
            
            // Optionally update notes if provided
            if (rescheduleNotes != null && !rescheduleNotes.trim().isEmpty()) {
                CounsellingSession session = sessionService.getSessionById(id);
                String currentNotes = session.getNotes() != null ? session.getNotes() + "\n" : "";
                session.setNotes(currentNotes + "Reschedule note: " + rescheduleNotes.trim());
                sessionService.updateSession(session);
            }
            
            redirect.addFlashAttribute("alert", 
                "✓ Reschedule request sent! New time: " + 
                date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + 
                " at " + time.format(TIME_FORMATTER));
            redirect.addFlashAttribute("alertType", "success");
            
        } catch (DateTimeParseException e) {
            redirect.addFlashAttribute("alert", "Invalid date or time format in slot selection");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "reschedule");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("alert", e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "reschedule");
        } catch (Exception e) {
            redirect.addFlashAttribute("alert", "An unexpected error occurred");
            redirect.addFlashAttribute("alertType", "error");
        }
        return "redirect:/counselling";
    }

    // ---------------------------
    // Book session
    // ---------------------------
    @PostMapping("/book")
    public String bookSession(@RequestParam Integer counsellorId,
                            @RequestParam String selectedSlot,  // ← CHANGED THIS
                            @RequestParam String bookingReason,
                            @RequestParam String sessionType,
                            @RequestParam(required = false) String sessionLocation,
                            Authentication authentication,
                            RedirectAttributes redirect) {
        
        User user = buildUser(authentication);
        
        if (!"student".equals(user.getRole())) {
            redirect.addFlashAttribute("alert", "Only students can book counselling sessions.");
            redirect.addFlashAttribute("alertType", "error");
            return "redirect:/counselling?modal=book";
        }
        
        try {
            // Parse slot exactly like reschedule does
            String[] slotParts = selectedSlot.split("\\|");
            if (slotParts.length != 2) {
                throw new IllegalArgumentException("Invalid slot format");
            }
            
            LocalDate date = LocalDate.parse(slotParts[0]);  // Use default ISO format
            LocalTime time = LocalTime.parse(slotParts[1]);  // Use default ISO format
            
            // Call service - make sure it accepts these parameters
            sessionService.bookSession(counsellorId, date, time, 
                                    sessionType, sessionLocation, bookingReason);
            
            redirect.addFlashAttribute("alert", "✓ Session booked successfully!");
            redirect.addFlashAttribute("alertType", "success");
            
        } catch (Exception e) {
            redirect.addFlashAttribute("alert", "Error: " + e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("modal", "book");
        }
        
        return "redirect:/counselling";
    }

    // ---------------------------
    // Mark session as completed
    // ---------------------------
    @PostMapping("/complete/{id}")
    public String completeSession(@PathVariable Integer id,
                                  @RequestParam String reportContent,
                                  RedirectAttributes redirect) {
        
        try {
            sessionService.markSessionAsCompleted(id, reportContent);
            redirect.addFlashAttribute("alert", "✓ Session marked as completed with report.");
            redirect.addFlashAttribute("alertType", "success");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("alert", e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
        } catch (Exception e) {
            redirect.addFlashAttribute("alert", "An unexpected error occurred");
            redirect.addFlashAttribute("alertType", "error");
        }
        return "redirect:/counselling";
    }

    // ---------------------------
    // Helper Methods
    // ---------------------------
    private User buildUser(Authentication authentication) {
        User user = new User();
        if (authentication != null && authentication.isAuthenticated()) {
            user.setEmail(authentication.getName());
            user.setName(authentication.getName().split("@")[0]);
            if (authentication.getAuthorities() != null && 
                authentication.getAuthorities().iterator().hasNext()) {
                String authority = authentication.getAuthorities().iterator().next().getAuthority();
                user.setRole(authority.replace("ROLE_", "").toLowerCase());
            } else {
                user.setRole("guest");
            }
        } else {
            user.setName("Guest");
            user.setRole("guest");
        }
        return user;
    }

    private void initializeModalStates(Model model) {
        model.addAttribute("showDetailsModal", false);
        model.addAttribute("showCancelModal", false);
        model.addAttribute("showRescheduleModal", false);
        model.addAttribute("showReportModal", false);
        model.addAttribute("showUploadModal", false);
        model.addAttribute("showBookingModal", false);
        model.addAttribute("showCompleteModal", false);
    }

    private void handleModalDisplay(Model model, Integer detailId, String modal, 
                                   List<CounsellingSession> sessions) {
        if (detailId != null) {
            Optional<CounsellingSession> sessionOpt = sessions.stream()
                    .filter(s -> s.getId().equals(detailId))
                    .findFirst();
            
            if (sessionOpt.isPresent()) {
                CounsellingSession session = sessionOpt.get();
                model.addAttribute("selectedSession", session);
                
                if (modal == null || "details".equals(modal)) {
                    model.addAttribute("showDetailsModal", true);
                } else if ("cancel".equals(modal)) {
                    model.addAttribute("showCancelModal", true);
                } else if ("reschedule".equals(modal)) {
                    model.addAttribute("showRescheduleModal", true);
                } else if ("report".equals(modal)) {
                    model.addAttribute("showReportModal", true);
                } else if ("upload".equals(modal)) {
                    model.addAttribute("showUploadModal", true);
                } else if ("complete".equals(modal)) {
                    model.addAttribute("showCompleteModal", true);
                }
            }
        } else if ("book".equals(modal)) {
            model.addAttribute("showBookingModal", true);
        }
    }

    private LocalTime parseTimeString(String timeString) throws DateTimeParseException {
        try {
            // Try ISO format first (HH:mm)
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e1) {
            try {
                // Try formatted time (h:mm a)
                return LocalTime.parse(timeString, TIME_FORMATTER);
            } catch (DateTimeParseException e2) {
                // If it contains a pipe, extract time part
                if (timeString.contains("|")) {
                    String timePart = timeString.split("\\|")[1];
                    return LocalTime.parse(timePart); // Should be in ISO format
                }
                // If it contains a dash, extract start time
                if (timeString.contains("-")) {
                    String startTime = timeString.split("-")[0].trim();
                    return LocalTime.parse(startTime, TIME_FORMATTER);
                }
                throw e2;
            }
        }
    }
}