package com.secj3303.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.secj3303.dao.GamificationDao;
import com.secj3303.model.GamificationBadge;
import com.secj3303.model.GamificationLeaderboardEntry;
import com.secj3303.model.GamificationUserStats;
import com.secj3303.model.User;

@Controller
@RequestMapping("/gamification")
public class GamificationController {

    private static final String DEFAULT_VIEW = "gamification";
    private final GamificationDao gamificationDao;

    @PersistenceContext
    private EntityManager em;

    public GamificationController(GamificationDao gamificationDao) {
        this.gamificationDao = gamificationDao;
    }

    @GetMapping
    public String gamificationDashboard(Authentication authentication, Model model) {
        try {
            // =========================
            // Build logged-in user (Spring Security)
            // =========================
            User user = new User();
            user.setEmail(authentication.getName());
            user.setName(authentication.getName().split("@")[0]);
            user.setRole(
                    authentication.getAuthorities()
                            .iterator()
                            .next()
                            .getAuthority()
                            .replace("ROLE_", "")
                            .toLowerCase()
            );

            String userEmail = authentication.getName();
            
            // =========================
            // DEBUG: Print what's being loaded
            // =========================
            System.out.println("=== GAMIFICATION DEBUG ===");
            System.out.println("User Email: " + userEmail);
            
            // =========================
            // Get Gamification Data from DAO
            // =========================
            GamificationUserStats stats = gamificationDao.getUserStats(userEmail)
                    .orElseGet(() -> {
                        System.out.println("Creating default stats for user: " + userEmail);
                        GamificationUserStats defaultStats = new GamificationUserStats();
                        defaultStats.setUserEmail(userEmail);
                        defaultStats.setPoints(0);
                        defaultStats.setLevel(1);
                        defaultStats.setCompletedModules(0);
                        defaultStats.setPassedQuizzes(0);
                        defaultStats.setDayStreak(0);
                        return defaultStats;
                    });
            
            System.out.println("Stats loaded - Points: " + stats.getPoints() + 
                              ", Level: " + stats.getLevel() + 
                              ", Completed Modules: " + stats.getCompletedModules());
            
            GamificationDao.NextLevelProgress nextLevel = gamificationDao.getNextLevelProgress(userEmail);
            System.out.println("Next Level Progress - Current: " + nextLevel.getCurrentPoints() + 
                              ", Next Level: " + nextLevel.getNextLevelPoints() +
                              ", Progress: " + nextLevel.getProgressPercentage() + "%");
            
            // Get badges
            List<GamificationBadge> badges = gamificationDao.getUserBadges(userEmail);
            System.out.println("Badges count: " + badges.size());
            
            // Get leaderboard
            List<GamificationLeaderboardEntry> leaderboard = gamificationDao.getLeaderboard(10);
            System.out.println("Leaderboard count: " + leaderboard.size());
            
            // Get recent achievements
            List<GamificationDao.RecentAchievement> recentAchievements = gamificationDao.getRecentAchievements(userEmail, 5);
            System.out.println("Recent achievements count: " + recentAchievements.size());
            
            // Get points activities
            List<GamificationDao.PointsActivity> pointsActivities = gamificationDao.getPointsActivities();
            System.out.println("Points activities count: " + pointsActivities.size());
            
            System.out.println("=== END DEBUG ===");
            
            // =========================
            // Add attributes to model
            // =========================
            model.addAttribute("user", user);
            
            model.addAttribute("userPoints", stats.getPoints());
            model.addAttribute("nextLevelPoints", nextLevel.getNextLevelPoints());
            model.addAttribute("currentLevel", stats.getLevel());
            model.addAttribute("progressPercentage", nextLevel.getProgressPercentage());
            model.addAttribute("pointsToNextLevel", nextLevel.getPointsToNextLevel());
            
            model.addAttribute("badges", badges);
            model.addAttribute("leaderboard", leaderboard);
            model.addAttribute("recentAchievements", recentAchievements);
            model.addAttribute("pointsActivities", pointsActivities);
            
            model.addAttribute("completedModules", stats.getCompletedModules());
            model.addAttribute("passedQuizzes", stats.getPassedQuizzes());
            model.addAttribute("dayStreak", stats.getDayStreak());
            model.addAttribute("userRank", gamificationDao.getUserLeaderboardRank(userEmail));
            
            model.addAttribute("currentView", DEFAULT_VIEW);

            return "app-layout";
            
        } catch (Exception e) {
            System.err.println("❌ Error in gamification dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Return a basic view even if there's an error
            User user = new User();
            user.setEmail(authentication.getName());
            user.setName(authentication.getName().split("@")[0]);
            
            model.addAttribute("user", user);
            model.addAttribute("currentView", DEFAULT_VIEW);
            model.addAttribute("error", "Unable to load gamification data. Please try again.");
            
            // Add default values to prevent template errors
            model.addAttribute("userPoints", 0);
            model.addAttribute("nextLevelPoints", 1000);
            model.addAttribute("currentLevel", 1);
            model.addAttribute("progressPercentage", 0);
            model.addAttribute("pointsToNextLevel", 1000);
            model.addAttribute("badges", new ArrayList<>());
            model.addAttribute("leaderboard", new ArrayList<>());
            model.addAttribute("recentAchievements", new ArrayList<>());
            model.addAttribute("pointsActivities", new ArrayList<>());
            model.addAttribute("completedModules", 0);
            model.addAttribute("passedQuizzes", 0);
            model.addAttribute("dayStreak", 0);
            model.addAttribute("userRank", 0);
            
            return "app-layout";
        }
    }

    @GetMapping("/debug/data")
    @ResponseBody
    public Map<String, Object> debugData(Authentication auth) {
        Map<String, Object> debugInfo = new HashMap<>();
        String userEmail = auth.getName();
        
        try {
            debugInfo.put("userEmail", userEmail);
            
            // Test DAO methods
            GamificationUserStats stats = gamificationDao.getUserStats(userEmail).orElse(null);
            debugInfo.put("statsExists", stats != null);
            if (stats != null) {
                debugInfo.put("points", stats.getPoints());
                debugInfo.put("level", stats.getLevel());
                debugInfo.put("completedModules", stats.getCompletedModules());
                debugInfo.put("passedQuizzes", stats.getPassedQuizzes());
                debugInfo.put("dayStreak", stats.getDayStreak());
            }
            
            debugInfo.put("badgesCount", gamificationDao.getUserBadges(userEmail).size());
            debugInfo.put("leaderboardCount", gamificationDao.getLeaderboard(5).size());
            debugInfo.put("recentAchievementsCount", gamificationDao.getRecentAchievements(userEmail, 3).size());
            debugInfo.put("pointsActivitiesCount", gamificationDao.getPointsActivities().size());
            
            return debugInfo;
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return debugInfo;
        }
    }

    @GetMapping("/debug/progress")
    @ResponseBody
    public Map<String, Object> debugProgress(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        String userEmail = auth.getName();
        
        try {
            // Check if module_progress table exists and has data
            String checkTableSql = """
                SELECT 
                    COUNT(*) as module_count, 
                    SUM(CASE WHEN progress = 100 THEN 1 ELSE 0 END) as completed_modules,
                    SUM(CASE WHEN quizPassed = true THEN 1 ELSE 0 END) as passed_quizzes
                FROM module_progress 
                WHERE user_email = ? OR _user_email = ?
                """;
            
            Query query = em.createNativeQuery(checkTableSql);
            query.setParameter(1, userEmail);
            query.setParameter(2, userEmail);
            
            Object[] row = (Object[]) query.getSingleResult();
            BigInteger moduleCount = (BigInteger) row[0];
            BigInteger completedModules = (BigInteger) row[1];
            BigInteger passedQuizzes = (BigInteger) row[2];
            
            result.put("module_progress_records", moduleCount.intValue());
            result.put("completed_modules", completedModules.intValue());
            result.put("passed_quizzes", passedQuizzes.intValue());
            
            // Check gamification tables
            String checkStatsSql = "SELECT COUNT(*) FROM gamification_stats WHERE user_email = ?";
            Query statsQuery = em.createNativeQuery(checkStatsSql);
            statsQuery.setParameter(1, userEmail);
            BigInteger statsCount = (BigInteger) statsQuery.getSingleResult();
            result.put("has_gamification_stats", statsCount.intValue() > 0);
            
            if (statsCount.intValue() > 0) {
                String statsDataSql = "SELECT points, level, completed_modules, passed_quizzes FROM gamification_stats WHERE user_email = ?";
                Query statsDataQuery = em.createNativeQuery(statsDataSql);
                statsDataQuery.setParameter(1, userEmail);
                Object[] statsRow = (Object[]) statsDataQuery.getSingleResult();
                result.put("current_points", ((Number) statsRow[0]).intValue());
                result.put("current_level", ((Number) statsRow[1]).intValue());
                result.put("current_completed_modules", ((Number) statsRow[2]).intValue());
                result.put("current_passed_quizzes", ((Number) statsRow[3]).intValue());
            }
            
            // Expected calculations
            int expectedPoints = completedModules.intValue() * 100 + passedQuizzes.intValue() * 50;
            int expectedLevel = Math.max(1, expectedPoints / 200 + 1);
            result.put("expected_points", expectedPoints);
            result.put("expected_level", expectedLevel);
            
            return result;
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("table_might_not_exist", true);
            return result;
        }
    }

    @GetMapping("/debug/modules")
    @ResponseBody
    public Map<String, Object> debugModules(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        String userEmail = auth.getName();
        
        try {
            // Get actual data from module_progress
            String checkDataSql = """
                SELECT 
                    id,
                    module_id, 
                    progress, 
                    quizPassed,
                    user_email,
                    _user_email
                FROM module_progress 
                WHERE user_email = ? OR _user_email = ?
                """;
            
            Query dataQuery = em.createNativeQuery(checkDataSql);
            dataQuery.setParameter(1, userEmail);
            dataQuery.setParameter(2, userEmail);
            
            @SuppressWarnings("unchecked")
            List<Object[]> rows = dataQuery.getResultList();
            
            List<Map<String, Object>> progressData = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> rowData = new HashMap<>();
                rowData.put("id", row[0]);
                rowData.put("module_id", row[1]);
                rowData.put("progress", row[2]);
                rowData.put("quizPassed", row[3]);
                rowData.put("user_email", row[4]);
                rowData.put("_user_email", row[5]);
                progressData.add(rowData);
            }
            
            result.put("module_progress_data", progressData);
            
            // Calculate what should be happening
            int completedCount = 0;
            int passedQuizCount = 0;
            
            for (Map<String, Object> row : progressData) {
                Integer progress = (Integer) row.get("progress");
                Boolean quizPassed = (Boolean) row.get("quizPassed");
                
                if (progress != null && progress == 100) {
                    completedCount++;
                }
                if (quizPassed != null && quizPassed) {
                    passedQuizCount++;
                }
            }
            
            result.put("calculated_completed_modules", completedCount);
            result.put("calculated_passed_quizzes", passedQuizCount);
            result.put("expected_points", (completedCount * 100) + (passedQuizCount * 50));
            
            return result;
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/check-quiz-data")
    @ResponseBody
    public Map<String, Object> checkQuizData(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        String userEmail = auth.getName();
        
        try {
            // Check actual data in module_progress
            String checkDataSql = """
                SELECT 
                    id,
                    module_id, 
                    progress, 
                    quizPassed,
                    user_email,
                    _user_email
                FROM module_progress 
                WHERE user_email = ? OR _user_email = ?
                """;
            
            Query dataQuery = em.createNativeQuery(checkDataSql);
            dataQuery.setParameter(1, userEmail);
            dataQuery.setParameter(2, userEmail);
            
            @SuppressWarnings("unchecked")
            List<Object[]> rows = dataQuery.getResultList();
            
            List<Map<String, Object>> progressData = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> rowData = new HashMap<>();
                rowData.put("id", row[0]);
                rowData.put("module_id", row[1]);
                rowData.put("progress", row[2]);
                rowData.put("quizPassed", row[3]);
                rowData.put("user_email", row[4]);
                rowData.put("_user_email", row[5]);
                progressData.add(rowData);
            }
            
            result.put("module_progress_data", progressData);
            
            // Check column names
            String columnSql = """
                SELECT COLUMN_NAME, DATA_TYPE 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'module_progress' 
                AND TABLE_SCHEMA = DATABASE()
                ORDER BY ORDINAL_POSITION
                """;
            
            Query columnQuery = em.createNativeQuery(columnSql);
            @SuppressWarnings("unchecked")
            List<Object[]> columns = columnQuery.getResultList();
            
            List<Map<String, String>> columnInfo = new ArrayList<>();
            for (Object[] column : columns) {
                Map<String, String> colData = new HashMap<>();
                colData.put("name", (String) column[0]);
                colData.put("type", (String) column[1]);
                columnInfo.add(colData);
            }
            
            result.put("table_columns", columnInfo);
            
            // Calculate what should be happening
            int completedCount = 0;
            int passedQuizCount = 0;
            
            for (Map<String, Object> row : progressData) {
                Integer progress = (Integer) row.get("progress");
                Boolean quizPassed = (Boolean) row.get("quizPassed");
                
                if (progress != null && progress == 100) {
                    completedCount++;
                }
                if (quizPassed != null && quizPassed) {
                    passedQuizCount++;
                }
            }
            
            result.put("calculated_completed_modules", completedCount);
            result.put("calculated_passed_quizzes", passedQuizCount);
            result.put("expected_points", (completedCount * 100) + (passedQuizCount * 50));
            
            return result;
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/recalculate")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String recalculateGamification(Authentication auth) {
        String userEmail = auth.getName();
        
        try {
            // 1. Delete old stats to force recalculation
            String deleteSql = "DELETE FROM gamification_stats WHERE user_email = ?";
            Query deleteQuery = em.createNativeQuery(deleteSql);
            deleteQuery.setParameter(1, userEmail);
            deleteQuery.executeUpdate();
            
            // 2. Force gamification system to recalculate
            gamificationDao.getUserStats(userEmail);
            
            // 3. Check current calculation
            int completedModules = getCompletedModuleCount(userEmail);
            int passedQuizzes = getPassedQuizCount(userEmail);
            int points = completedModules * 100 + passedQuizzes * 50;
            int level = calculateLevelFromPoints(points);
            
            return String.format("""
                ✅ Gamification recalculated for: %s
                • Completed modules: %d
                • Passed quizzes: %d  
                • Total points: %d
                • Level: %d
                • Progress to next level: %d/%d points
                """,
                userEmail, completedModules, passedQuizzes, points, level,
                points % 200, 200);
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage() + "\nStack trace: " + Arrays.toString(e.getStackTrace());
        }
    }

    @GetMapping("/force-recalculate")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String forceRecalculate(Authentication auth) {
        String userEmail = auth.getName();
        
        try {
            // Clear old stats
            String deleteSql = "DELETE FROM gamification_stats WHERE user_email = ?";
            Query deleteQuery = em.createNativeQuery(deleteSql);
            deleteQuery.setParameter(1, userEmail);
            deleteQuery.executeUpdate();
            
            // Clear old badges
            String deleteBadgesSql = "DELETE FROM gamification_badges WHERE user_email = ?";
            Query deleteBadgesQuery = em.createNativeQuery(deleteBadgesSql);
            deleteBadgesQuery.setParameter(1, userEmail);
            deleteBadgesQuery.executeUpdate();
            
            // Clear old points activity
            String deleteActivitySql = "DELETE FROM gamification_points_activity WHERE user_email = ?";
            Query deleteActivityQuery = em.createNativeQuery(deleteActivitySql);
            deleteActivityQuery.setParameter(1, userEmail);
            deleteActivityQuery.executeUpdate();
            
            // Force recalculation
            gamificationDao.getUserStats(userEmail);
            
            return "✅ Forced recalculation for: " + userEmail + 
                   "\nAll gamification data cleared and recalculated." +
                   "\nVisit /gamification to see updated stats.";
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @PostMapping("/add-test-data")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String addTestData(Authentication auth) {
        String userEmail = auth.getName();
        
        try {
            // 1. Add test module progress if none exists
            String checkSql = "SELECT COUNT(*) FROM module_progress WHERE user_email = ? OR _user_email = ?";
            Query checkQuery = em.createNativeQuery(checkSql);
            checkQuery.setParameter(1, userEmail);
            checkQuery.setParameter(2, userEmail);
            BigInteger count = (BigInteger) checkQuery.getSingleResult();
            
            if (count.intValue() == 0) {
                // Insert test data
                String insertProgressSql = """
                    INSERT INTO module_progress (module_id, progress, quizPassed, user_email, _user_email)
                    VALUES 
                    (1, 100, true, ?, ?),
                    (2, 100, true, ?, ?),
                    (3, 80, false, ?, ?)
                    """;
                
                Query progressQuery = em.createNativeQuery(insertProgressSql);
                for (int i = 1; i <= 6; i++) {
                    progressQuery.setParameter(i, userEmail);
                }
                progressQuery.executeUpdate();
            }
            
            // 2. Force gamification recalculation
            gamificationDao.getUserStats(userEmail);
            
            // 3. Add some points activities
            gamificationDao.recordPointsActivity(userEmail, "test_quiz", 50);
            gamificationDao.recordPointsActivity(userEmail, "test_module", 100);
            gamificationDao.recordPointsActivity(userEmail, "daily_checkin", 10);
            
            return "✅ Test data added for: " + userEmail + 
                   "\nVisit /gamification to see updated stats.";
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/initialize")
    @ResponseBody
    public String initializeGamification(Authentication authentication) {
        String userEmail = authentication.getName();
        
        // Force initialization
        gamificationDao.getUserStats(userEmail);
        gamificationDao.getLeaderboard(10);
        
        return "Gamification initialized for: " + userEmail;
    }

    @GetMapping("/test-leaderboard")
    @ResponseBody
    public Map<String, Object> testLeaderboard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<GamificationLeaderboardEntry> leaderboard = gamificationDao.getLeaderboard(10);
            result.put("leaderboard_count", leaderboard.size());
            
            List<Map<String, Object>> entries = new ArrayList<>();
            for (GamificationLeaderboardEntry entry : leaderboard) {
                Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("rank", entry.getRank());
                entryMap.put("name", entry.getUserName());
                entryMap.put("points", entry.getPoints());
                entryMap.put("level", entry.getLevel());
                entryMap.put("badge", entry.getBadgeEmoji());
                entryMap.put("is_dummy", entry.isDummyUser());
                entries.add(entryMap);
            }
            result.put("entries", entries);
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/test-points-activities")
    @ResponseBody
    public Map<String, Object> testPointsActivities() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<GamificationDao.PointsActivity> activities = gamificationDao.getPointsActivities();
            result.put("activities_count", activities.size());
            
            List<Map<String, Object>> activityList = new ArrayList<>();
            for (GamificationDao.PointsActivity activity : activities) {
                Map<String, Object> activityMap = new HashMap<>();
                activityMap.put("activity", activity.getActivity());
                activityMap.put("points", activity.getPoints());
                activityMap.put("description", activity.getDescription());
                activityList.add(activityMap);
            }
            result.put("activities", activityList);
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    // Helper methods
    private int getCompletedModuleCount(String userEmail) {
        try {
            String sql = "SELECT COUNT(*) FROM module_progress WHERE (user_email = ? OR _user_email = ?) AND progress = 100";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            query.setParameter(2, userEmail);
            BigInteger count = (BigInteger) query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getPassedQuizCount(String userEmail) {
        try {
            String sql = "SELECT COUNT(*) FROM module_progress WHERE (user_email = ? OR _user_email = ?) AND quizPassed = true";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            query.setParameter(2, userEmail);
            BigInteger count = (BigInteger) query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateLevelFromPoints(int points) {
        int POINTS_PER_LEVEL = 200;
        return Math.max(1, points / POINTS_PER_LEVEL + 1);
    }
}