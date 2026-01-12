package com.secj3303.controller;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.AssessmentResultDao;
import com.secj3303.dao.CarePlanDao;
import com.secj3303.dao.MoodEntryDao;
import com.secj3303.model.AssessmentResult;
import com.secj3303.model.CarePlan;
import com.secj3303.model.CarePlanActivity;
import com.secj3303.model.CarePlanModels;
import com.secj3303.model.MoodEntry;

@Controller
@RequestMapping("/careplan")
public class CarePlanController {

    @Autowired
    private CarePlanDao carePlanDao;

    @Autowired
    private AssessmentResultDao assessmentResultDao;

    @Autowired
    private MoodEntryDao moodEntryDao;

    public static class UserDTO {
        public String name;
        public String role;
        public UserDTO(String name, String role) { this.name = name; this.role = role; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }

    @GetMapping
    public String showCarePlan(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userRole,
            HttpSession session,
            Model model) {

        // 1. Get Student Info
        Integer studentId = (Integer) session.getAttribute("studentId");
        if (studentId == null) studentId = 1; 

        // --- USERNAME LOGIC ---
        String name = (String) session.getAttribute("userName");
        if (name == null) name = userName != null ? userName : "Student User";
        
        // DEBUG: Print to Console to verify what name is being used
        System.out.println(">>> CarePlan Debug: Searching for data for username: [" + name + "]");
        
        String role = (String) session.getAttribute("userRole");
        if (role == null) role = userRole != null ? userRole : "student";

        model.addAttribute("user", new UserDTO(name, role));
        model.addAttribute("currentView", "careplan");

        // 2. Fetch Data
        Optional<AssessmentResult> latestResultOpt = assessmentResultDao.findLatestByStudentId(studentId);
        Optional<CarePlan> carePlanOpt = carePlanDao.findByStudentId(studentId);
        
        // --- Fetch Mood Data ---
        // This query counts entries strictly matching 'name'
        long moodCount = moodEntryDao.countByUsername(name);
        System.out.println(">>> CarePlan Debug: Found " + moodCount + " mood entries."); // DEBUG

        List<MoodEntry> recentMoods = moodEntryDao.findRecentByUsername(name, 7);
        
        long negativeMoodCount = recentMoods.stream()
            .filter(m -> "stressed".equalsIgnoreCase(m.getMood()) || 
                         "anxious".equalsIgnoreCase(m.getMood()) || 
                         "sad".equalsIgnoreCase(m.getMood()))
            .count();

        // Calculate Engagement
        int engagementScore = (int) Math.min(moodCount * 10, 100); 

        CarePlan activePlan = null;

        if (latestResultOpt.isPresent()) {
            AssessmentResult result = latestResultOpt.get();

            if (carePlanOpt.isPresent()) {
                activePlan = carePlanOpt.get();
                // Sync Trigger
                if (activePlan.getRiskScore() == null || !activePlan.getRiskScore().equals(result.getScore())) {
                    activePlan = generateAndSavePlan(result, recentMoods);
                }
            } else {
                activePlan = generateAndSavePlan(result, recentMoods);
            }
            
            CarePlanModels.UserData userData = new CarePlanModels.UserData();
            
            userData.assessmentScore = result.getScore();
            userData.lastAssessmentDate = result.getDate();
            userData.name = name;
            userData.moodEntries = (int) moodCount; // This will update when username matches DB
            userData.engagementLevel = engagementScore;
            
            // Calculate Stress Factors: Assessment Score (small weight) + Actual Negative Moods
            int baseStress = result.getScore() / 20; 
            userData.stressIndicators = baseStress + (int) negativeMoodCount; 
            
            CarePlanModels.RiskAssessment risk = new CarePlanModels.RiskAssessment();
            risk.level = activePlan.getRiskLevel();
            risk.score = activePlan.getRiskScore();
            risk.counselorAlerted = "high".equals(activePlan.getRiskLevel());
            
            model.addAttribute("userData", userData);
            model.addAttribute("riskAssessment", risk);
            model.addAttribute("carePlan", activePlan.getActivities());
            model.addAttribute("showInsufficientDataWarning", false);

        } else {
            CarePlanModels.UserData emptyData = new CarePlanModels.UserData();
            emptyData.moodEntries = (int) moodCount;
            emptyData.engagementLevel = engagementScore;
            emptyData.stressIndicators = (int) negativeMoodCount;
            
            model.addAttribute("userData", emptyData);
            model.addAttribute("carePlan", new ArrayList<>());
            model.addAttribute("showInsufficientDataWarning", true);
        }

        calculateProgress(model, activePlan);
        return "app-layout";
    }

    @PostMapping("/complete/{id}")
    public String completeActivity(@PathVariable int id) {
        CarePlanActivity activity = carePlanDao.findActivityById(id);
        if (activity != null) {
            activity.setCompleted(!activity.isCompleted());
            carePlanDao.updateActivity(activity);
        }
        return "redirect:/careplan";
    }
    
    @PostMapping("/generate")
    public String regeneratePlan(HttpSession session) {
        Integer studentId = (Integer) session.getAttribute("studentId");
        String name = (String) session.getAttribute("userName");
        if(studentId == null) studentId = 1;
        if(name == null) name = "Student User";
        
        Optional<AssessmentResult> result = assessmentResultDao.findLatestByStudentId(studentId);
        List<MoodEntry> recentMoods = moodEntryDao.findRecentByUsername(name, 7);

        if(result.isPresent()) {
            generateAndSavePlan(result.get(), recentMoods);
        }
        return "redirect:/careplan";
    }
    
    @PostMapping("/refresh-data")
    public String refreshData() {
        return "redirect:/careplan";
    }

    private CarePlan generateAndSavePlan(AssessmentResult result, List<MoodEntry> recentMoods) {
        Optional<CarePlan> existing = carePlanDao.findByStudentId(result.getStudent().getId());
        CarePlan plan = existing.orElse(new CarePlan());
        
        plan.setStudent(result.getStudent());
        plan.setCreatedDate(new Date());
        
        int score = result.getScore();
        String severity = result.getSeverity();
        plan.setRiskScore(score);
        
        long negativeMoods = recentMoods.stream()
            .filter(m -> "stressed".equalsIgnoreCase(m.getMood()) || "anxious".equalsIgnoreCase(m.getMood()))
            .count();
        
        if ("Severe".equalsIgnoreCase(severity) || score > 75 || negativeMoods >= 5) {
            plan.setRiskLevel("high");
        } else if ("Moderate".equalsIgnoreCase(severity) || score > 45 || negativeMoods >= 3) {
            plan.setRiskLevel("medium");
        } else {
            plan.setRiskLevel("low");
        }

        if (plan.getActivities() != null) {
            plan.getActivities().clear();
        } else {
            plan.setActivities(new ArrayList<>());
        }

        if ("high".equals(plan.getRiskLevel())) {
             plan.addActivity(new CarePlanActivity("Emergency Counseling", "Book an urgent session.", "counseling", "high", "Tomorrow"));
             plan.addActivity(new CarePlanActivity("Daily Mood Check-in", "Track your mood daily.", "assessment", "high", "Daily"));
        }
        
        if ("medium".equals(plan.getRiskLevel()) || "high".equals(plan.getRiskLevel())) {
            plan.addActivity(new CarePlanActivity("Stress Management Module", "Complete the anxiety module.", "learning", "medium", "This Week"));
        }
        
        boolean hasAnxiety = recentMoods.stream().anyMatch(m -> "anxious".equalsIgnoreCase(m.getMood()));
        if (hasAnxiety) {
            plan.addActivity(new CarePlanActivity("Breathing Exercise", "3-minute breathing to reduce anxiety.", "self-care", "medium", "Today"));
        }

        boolean hasSadness = recentMoods.stream().anyMatch(m -> "sad".equalsIgnoreCase(m.getMood()));
        if (hasSadness) {
            plan.addActivity(new CarePlanActivity("Gratitude Journal", "Write down 3 things you are grateful for.", "self-care", "low", "Today"));
        }
        
        if (recentMoods.size() < 2) {
            plan.addActivity(new CarePlanActivity("Start Mood Tracking", "Log your mood to build a history.", "assessment", "medium", "Daily"));
        }

        plan.addActivity(new CarePlanActivity("Wellness Review", "Review your recent assessment results.", "self-care", "low", "Today"));
        
        return carePlanDao.save(plan);
    }
    
    private void calculateProgress(Model model, CarePlan plan) {
        if (plan != null && plan.getActivities() != null && !plan.getActivities().isEmpty()) {
            int total = plan.getActivities().size();
            long completed = plan.getActivities().stream().filter(CarePlanActivity::isCompleted).count();
            model.addAttribute("totalActivities", total);
            model.addAttribute("completedActivities", completed);
            model.addAttribute("progressPercentage", total > 0 ? ((double) completed / total) * 100 : 0);
        } else {
            model.addAttribute("totalActivities", 0);
            model.addAttribute("completedActivities", 0);
            model.addAttribute("progressPercentage", 0);
        }
    }
}