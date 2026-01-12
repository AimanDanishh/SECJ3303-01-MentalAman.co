package com.secj3303.controller;

import com.secj3303.dao.CounsellorDao;
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

    @Autowired
    private CounsellorDao counsellorDao;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---------------------------
    // Dashboard
    // ---------------------------
    @GetMapping
    public String sessionDashboard(Model model, Authentication authentication,
                                @RequestParam(required = false) Integer detailId,
                                @RequestParam(required = false) String modal,
                                @RequestParam(required = false) String counsellorId) {
        
        // Build user from authentication
        User user = buildUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("currentView", "counselling");
        
        // Get studentId from authentication (assuming email or username is student ID)
        String studentId = getStudentIdFromAuthentication(authentication);
        model.addAttribute("studentId", studentId);

        // Fetch data based on user role
        List<CounsellingSession> sessions;
        if ("student".equals(user.getRole())) {
            // Students can only see their own sessions
            sessions = sessionService.getSessionsByStudentId(studentId);
        } else if ("counsellor".equals(user.getRole())) {
            // Counsellors can see all sessions (or filter to their own if needed)
            sessions = sessionService.getAllSessions();
        } else {
            // Admin or other roles can see all sessions
            sessions = sessionService.getAllSessions();
        }
        
        model.addAttribute("sessions", sessions);

        List<Counsellor> counsellors = sessionService.getAllCounsellors();
        model.addAttribute("counsellorList", counsellors);

        // Get selected counsellor - check if we're in reschedule mode first
        Counsellor selectedCounsellor = null;
        
        if ("reschedule".equals(modal) && detailId != null) {
            // For reschedule modal, get counsellor from the selected session
            Optional<CounsellingSession> sessionOpt = sessions.stream()
                    .filter(s -> s.getId().equals(detailId))
                    .findFirst();
            
            if (sessionOpt.isPresent()) {
                selectedCounsellor = sessionOpt.get().getCounsellor();
                model.addAttribute("selectedCounsellorId", selectedCounsellor.getId());
            }
        } else if (counsellorId != null) {
            // For booking modal or when counsellorId is explicitly provided
            selectedCounsellor = sessionService.getCounsellorById(counsellorId);
            model.addAttribute("selectedCounsellorId", counsellorId);
        }
        
        // Generate available time slots
        List<TimeSlot> slots = Collections.emptyList();
        if (selectedCounsellor != null) {
            slots = sessionService.generateAvailableSlotsForCounsellor(selectedCounsellor);
        }
        model.addAttribute("availableTimeSlots", slots);

        // Initialize modal states
        initializeModalStates(model);

        // Handle modal display based on parameters
        handleModalDisplay(model, detailId, modal, sessions, user.getRole(), studentId);

        return "counselling-session";
    }

    // ---------------------------
    // Confirm attendance
    // ---------------------------
    @PostMapping("/confirm/{id}")
    public String confirmSession(@PathVariable Integer id, 
                                Authentication authentication,
                                RedirectAttributes redirect) {
        
        try {
            // Check if session belongs to the student
            String counsellorId = getCounsellorIdFromAuthentication(authentication);
            CounsellingSession session = sessionService.getSessionById(id);
            
            if (!session.belongsToCounsellor(counsellorId)) {
                redirect.addFlashAttribute("alert", "Access denied. You can only confirm sessions assigned to you.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/counselling";
            }
            
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
                                Authentication authentication,
                                RedirectAttributes redirect) {
        
        try {
            // Check if session belongs to the student
            String studentId = getStudentIdFromAuthentication(authentication);
            CounsellingSession session = sessionService.getSessionById(id);
            
            if (!session.belongsToStudent(studentId)) {
                redirect.addFlashAttribute("alert", "Access denied. You can only cancel your own sessions.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/counselling";
            }
            
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
                                    Authentication authentication,
                                    RedirectAttributes redirect) {
        
        try {
            // Check if session belongs to the student
            String studentId = getStudentIdFromAuthentication(authentication);
            CounsellingSession session = sessionService.getSessionById(id);
            
            if (!session.belongsToStudent(studentId)) {
                redirect.addFlashAttribute("alert", "Access denied. You can only reschedule your own sessions.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/counselling";
            }
            
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
                session = sessionService.getSessionById(id);
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
    public String bookSession(@RequestParam String counsellorId,
                            @RequestParam String selectedSlot,  // ← CHANGED THIS
                            @RequestParam String bookingReason,
                            @RequestParam String sessionType,
                            @RequestParam(required = false) String sessionLocation,
                            Authentication authentication,
                            RedirectAttributes redirect) {
        
        User user = buildUser(authentication);
        String studentId = getStudentIdFromAuthentication(authentication);
        
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
            
            // Call service with studentId
            sessionService.bookSession(counsellorId, studentId, date, time, 
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
                                Authentication authentication,
                                RedirectAttributes redirect) {
        
        try {
            // Only counsellors can mark sessions as completed
            User user = buildUser(authentication);
            if (!"counsellor".equals(user.getRole())) {
                redirect.addFlashAttribute("alert", "Only counsellors can mark sessions as completed.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/counselling";
            }
            
            // Validate required field
            if (reportContent == null || reportContent.trim().isEmpty()) {
                redirect.addFlashAttribute("alert", "Report content is required.");
                redirect.addFlashAttribute("alertType", "error");
                return "redirect:/counselling?detailId=" + id + "&modal=complete";
            }
            
            // Call service with the report content
            sessionService.markSessionAsCompleted(id, reportContent);
            
            redirect.addFlashAttribute("alert", "✓ Session marked as completed with report.");
            redirect.addFlashAttribute("alertType", "success");
            
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("alert", e.getMessage());
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "complete");
        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("alert", "An unexpected error occurred: " + e.getMessage());
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
    
    private String getStudentIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming student ID is stored as the username/principal
            return authentication.getName();
        }
        return null;
    }

    private String getCounsellorIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Get the email/username from authentication
            String email = authentication.getName();
            
            // Find counsellor by email
            Counsellor counsellor = counsellorDao.findByEmail(email);
            
            if (counsellor == null) {
                throw new IllegalStateException("Counsellor not found for email: " + email);
            }
            
            // Return the counsellor ID (the generated initials like "AR")
            return counsellor.getId();
        }
        return null;
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
                                List<CounsellingSession> sessions, String userRole, String studentId) {
        if (detailId != null) {
            Optional<CounsellingSession> sessionOpt = sessions.stream()
                    .filter(s -> s.getId().equals(detailId))
                    .findFirst();
            
            if (sessionOpt.isPresent()) {
                CounsellingSession session = sessionOpt.get();
                
                // Check if user has access to this session
                boolean hasAccess = false;
                if ("student".equals(userRole)) {
                    hasAccess = session.belongsToStudent(studentId);
                } else {
                    hasAccess = true; // Counsellors and admins have access to all
                }
                
                if (!hasAccess) {
                    // Don't show modal if student doesn't have access
                    return;
                }
                
                model.addAttribute("selectedSession", session);
                
                // For reschedule modal, we might need to add the counsellor to the model
                if ("reschedule".equals(modal)) {
                    model.addAttribute("showRescheduleModal", true);
                    model.addAttribute("selectedCounsellorId", session.getCounsellor().getId());
                } else if ("cancel".equals(modal)) {
                    model.addAttribute("showCancelModal", true);
                } else if ("details".equals(modal) || modal == null) {
                    model.addAttribute("showDetailsModal", true);
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