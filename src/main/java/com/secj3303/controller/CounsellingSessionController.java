package com.secj3303.controller;

import com.secj3303.model.CounsellingSessionModels;
import com.secj3303.model.CounsellingSessionModels.Session;
import com.secj3303.model.CounsellingSessionModels.TimeSlot;
import com.secj3303.model.CounsellingSessionModels.Counsellor;
import com.secj3303.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/counselling")
public class CounsellingSessionController {

    private static final String SESSIONS_KEY = "counsellingSessions";
    private static final String DEFAULT_VIEW = "counselling";

    private List<Session> getSessions(HttpSession session) {
        List<Session> sessions = (List<Session>) session.getAttribute(SESSIONS_KEY);
        if (sessions == null) {
            sessions = CounsellingSessionModels.getInitialSessions();
            session.setAttribute(SESSIONS_KEY, sessions);
        }
        return sessions;
    }

    private Optional<Session> findSession(List<Session> sessions, int id) {
        return sessions.stream().filter(s -> s.getId() == id).findFirst();
    }

    // --- Main View & Modal State Handler (GET) ---

    @GetMapping
    public String sessionDashboard(
        @RequestParam(required = false) Integer detailId,
        @RequestParam(required = false) String modal,
        Model model, 
        HttpSession httpSession // Renamed to avoid confusion with Session model
    ) {
        // Get user from session
        User user = (User) httpSession.getAttribute("user");
        
        // Check authentication
        if (user == null) {
            return "redirect:/login";
        }
        
        // Add user to model (REQUIRED for app-layout)
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole()); // Use actual user role
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("sessions", getSessions(httpSession));
        model.addAttribute("availableTimeSlots", CounsellingSessionModels.AVAILABLE_SLOTS);
        model.addAttribute("counsellorList", CounsellingSessionModels.COUNSELLOR_LIST);

        // Handle Modal States
        model.addAttribute("showDetailsModal", false);
        model.addAttribute("showCancelModal", false);
        model.addAttribute("showRescheduleModal", false);
        model.addAttribute("showReportModal", false);
        model.addAttribute("showUploadModal", false);
        model.addAttribute("showBookingModal", false);

        if (detailId != null) {
            Optional<Session> sessionOpt = findSession(getSessions(httpSession), detailId);
            sessionOpt.ifPresent(s -> {
                model.addAttribute("selectedSession", s);
                if (modal == null) {
                    model.addAttribute("showDetailsModal", true);
                } else {
                    switch (modal) {
                        case "details": 
                            model.addAttribute("showDetailsModal", true); 
                            break;
                        case "cancel": 
                            model.addAttribute("showCancelModal", true); 
                            break;
                        case "reschedule": 
                            model.addAttribute("showRescheduleModal", true); 
                            break;
                        case "report": 
                            model.addAttribute("showReportModal", true); 
                            break;
                        case "upload": 
                            model.addAttribute("showUploadModal", true); 
                            break;
                    }
                }
            });
        } else if ("book".equals(modal)) {
            model.addAttribute("showBookingModal", true);
        }

        return "counselling-session";
    }
    
    // --- Session Action Handlers (POSTs) ---

    @PostMapping("/confirm/{id}")
    public String handleConfirm(@PathVariable int id, 
                              HttpSession httpSession, 
                              RedirectAttributes redirect) {
        
        // Check authentication
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        findSession(getSessions(httpSession), id).ifPresent(s -> {
            s.setStudentConfirmed(true);
            s.setStatus("confirmed");
            redirect.addFlashAttribute("alert", "✓ Attendance confirmed! You will receive a reminder 1 hour before the session.");
            redirect.addFlashAttribute("alertType", "success");
        });
        return "redirect:/counselling";
    }

    @PostMapping("/cancel/{id}")
    public String handleCancel(@PathVariable int id, 
                             @RequestParam String cancellationReason, 
                             HttpSession httpSession, 
                             RedirectAttributes redirect) {
        
        // Check authentication
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (cancellationReason.trim().isEmpty()) {
            redirect.addFlashAttribute("alert", "Please provide a cancellation reason.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "cancel");
            return "redirect:/counselling";
        }

        findSession(getSessions(httpSession), id).ifPresent(s -> {
            s.setStatus("cancelled");
            s.setCancellationReason(cancellationReason);
            redirect.addFlashAttribute("alert", "✓ Session cancelled. " + s.getCounsellor() + " has been notified.");
            redirect.addFlashAttribute("alertType", "success");
        });
        return "redirect:/counselling";
    }

    @PostMapping("/reschedule/{id}")
    public String handleReschedule(@PathVariable int id, 
                                 @RequestParam String newDate, 
                                 @RequestParam String newTime, 
                                 HttpSession httpSession, 
                                 RedirectAttributes redirect) {
        
        // Check authentication
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        findSession(getSessions(httpSession), id).ifPresent(s -> {
            s.setStatus("pending-reschedule");
            s.setDate(newDate);
            s.setTime(newTime);
            redirect.addFlashAttribute("alert", "✓ Reschedule request sent! New time: " + newDate + " at " + newTime);
            redirect.addFlashAttribute("alertType", "success");
        });
        return "redirect:/counselling";
    }
    
    @PostMapping("/report/upload/{id}")
    public String handleReportUpload(@PathVariable int id, 
                                   @RequestParam String reportContent, 
                                   HttpSession httpSession, 
                                   RedirectAttributes redirect) {
        
        // Check authentication
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check if user is counsellor or admin
        if (!user.getRole().equals("counsellor") && !user.getRole().equals("administrator")) {
            redirect.addFlashAttribute("alert", "You don't have permission to upload reports.");
            redirect.addFlashAttribute("alertType", "error");
            return "redirect:/counselling";
        }
        
        if (reportContent.trim().isEmpty()) {
            redirect.addFlashAttribute("alert", "Report content cannot be empty.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("detailId", id);
            redirect.addAttribute("modal", "upload");
            return "redirect:/counselling";
        }
        
        findSession(getSessions(httpSession), id).ifPresent(s -> {
            s.setReportAvailable(true);
            s.setReportContent(reportContent);
            redirect.addFlashAttribute("alert", "✓ Report uploaded successfully! The student has been notified.");
            redirect.addFlashAttribute("alertType", "success");
        });
        return "redirect:/counselling";
    }

    @PostMapping("/book")
    public String handleBookNewSession(@RequestParam String selectedCounsellor, 
                                     @RequestParam String selectedDate,
                                     @RequestParam String selectedTime,
                                     @RequestParam String bookingReason,
                                     @RequestParam String sessionType,
                                     @RequestParam(required = false) String sessionLocation,
                                     HttpSession httpSession, 
                                     RedirectAttributes redirect) {

        // Check authentication
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check if user is student (only students can book sessions)
        if (!user.getRole().equals("student")) {
            redirect.addFlashAttribute("alert", "Only students can book counselling sessions.");
            redirect.addFlashAttribute("alertType", "error");
            return "redirect:/counselling?modal=book";
        }

        // Find Counsellor details
        Optional<Counsellor> counsellorOpt = CounsellingSessionModels.COUNSELLOR_LIST.stream()
            .filter(c -> c.name.equals(selectedCounsellor))
            .findFirst();
        
        if (counsellorOpt.isEmpty()) {
            redirect.addFlashAttribute("alert", "Invalid counsellor selected.");
            redirect.addFlashAttribute("alertType", "error");
            return "redirect:/counselling?modal=book";
        }
        
        Counsellor counsellor = counsellorOpt.get();
        List<Session> sessions = getSessions(httpSession);
        AtomicInteger maxId = new AtomicInteger(sessions.stream()
            .mapToInt(Session::getId)
            .max()
            .orElse(0));

        String sessionTypeText = "online".equals(sessionType) ? "Video Call" : 
                                (sessionLocation != null ? sessionLocation : "In-Person");

        Session newSession = new Session();
        newSession.setId(maxId.incrementAndGet());
        newSession.setCounsellor(counsellor.name);
        newSession.setCounsellorId(counsellor.id);
        newSession.setSpecialty(counsellor.specialty);
        newSession.setDate(selectedDate);
        newSession.setTime(selectedTime);
        newSession.setType(sessionTypeText);
        newSession.setStatus("scheduled");
        newSession.setStudentConfirmed(false);
        newSession.setNotes(bookingReason);

        sessions.add(0, newSession);
        httpSession.setAttribute(SESSIONS_KEY, sessions);
        
        redirect.addFlashAttribute("alert", "✓ Session Booked Successfully! Counsellor: " + counsellor.name + " on " + selectedDate + " at " + selectedTime);
        redirect.addFlashAttribute("alertType", "success");
        return "redirect:/counselling";
    }
}