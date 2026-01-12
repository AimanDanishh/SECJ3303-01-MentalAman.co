package com.secj3303.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secj3303.dao.LearningModuleDao;
import com.secj3303.dao.ModuleProgressDao;
import com.secj3303.dao.PersonDao;
import com.secj3303.model.DashboardData;
import com.secj3303.model.DashboardData.ActivityItem;
import com.secj3303.model.DashboardData.StatItem;
import com.secj3303.model.Gamification;
import com.secj3303.model.LearningModule;
import com.secj3303.model.ModuleProgress;
import com.secj3303.model.Person;
import com.secj3303.service.GamificationService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String DEFAULT_VIEW = "dashboard";

    private final PersonDao personDao;
    private final GamificationService gamificationService;
    private final LearningModuleDao moduleDao;
    private final ModuleProgressDao progressDao;

    public DashboardController(PersonDao personDao, 
                               GamificationService gamificationService,
                               LearningModuleDao moduleDao,
                               ModuleProgressDao progressDao) {
        this.personDao = personDao;
        this.gamificationService = gamificationService;
        this.moduleDao = moduleDao;
        this.progressDao = progressDao;
    }

    @GetMapping
    public String dashboardView(Authentication authentication, Model model) {

        // 1. Logged-in user email
        String email = authentication.getName();

        // 2. Load REAL person from DB
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found: " + email);
        }

        // 3. Fetch Real Gamification Stats (Points & Streak)
        Gamification gamification = gamificationService.getUserGamificationProfile(email);
        String points = String.valueOf(gamification.getXpPoints());
        String streak = String.valueOf(gamification.getDailyStreak());

        // 4. Calculate Real Learning Progress (%)
        List<LearningModule> modules = moduleDao.findAllWithLessonsAndQuiz();
        long totalModules = modules.size();
        long completedModules = 0;

        for (LearningModule m : modules) {
            ModuleProgress p = progressDao.findByUserEmailAndModuleId(email, m.getId()).orElse(null);
            if (p != null && p.getProgress() == 100) {
                completedModules++;
            }
        }

        int progressPercent = (totalModules == 0) ? 0 : (int) ((completedModules * 100) / totalModules);
        String progressString = progressPercent + "%";

        // 5. Construct Dynamic Stats List
        List<StatItem> realStats = new ArrayList<>();
        realStats.add(new StatItem("Learning Progress", progressString, "book-open", "blue"));
        realStats.add(new StatItem("Mood Score", "7.5/10", "trending-up", "green")); // Static (No Mood Dao yet)
        realStats.add(new StatItem("Streak Days", streak, "target", "purple"));
        realStats.add(new StatItem("Points Earned", points, "award", "orange"));

        // 6. Map Recent Achievements to Activity Items (Unifying with Gamification Page)
        List<ActivityItem> recentActivities = Gamification.getRecentAchievements().stream()
                .map(ach -> new ActivityItem(ach.title, ach.time, "assessment")) // Mapping achievements to activity feed
                .collect(Collectors.toList());

        // Layout attributes
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("user", person);
        model.addAttribute("userName", person.getName());

        // Pass Computed Data
        model.addAttribute("studentStats", realStats);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("upcomingEvents", DashboardData.getUpcomingEvents());

        return "app-layout";
    }
}