package com.secj3303.dao;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.GamificationBadge;
import com.secj3303.model.GamificationLeaderboardEntry;
import com.secj3303.model.GamificationUserStats;

@Repository
@Transactional
public class GamificationDaoHibernate implements GamificationDao {

    @PersistenceContext
    private EntityManager em;
    
    private boolean tablesCreated = false;
    
    // Points configuration
    private static final int POINTS_PER_MODULE = 100;
    private static final int POINTS_PER_QUIZ = 50;
    private static final int POINTS_PER_DAY_STREAK = 10;
    private static final int POINTS_PER_LEVEL = 200;
    
    // Badge definitions
    private static final Map<String, GamificationBadge> BADGE_DEFINITIONS = new HashMap<>();
    
    static {
        // Define all available badges
        BADGE_DEFINITIONS.put("first_module", new GamificationBadge(
            "first_module", "First Steps", "Completed your first module",
            "🎯", "common", 100, "module_completion"
        ));
        
        BADGE_DEFINITIONS.put("consistency_7", new GamificationBadge(
            "consistency_7", "Consistent Learner", "Maintained a 7-day streak",
            "🔥", "uncommon", 7, "streak"
        ));
        
        BADGE_DEFINITIONS.put("consistency_30", new GamificationBadge(
            "consistency_30", "Mood Master", "Logged mood for 30 days",
            "📊", "uncommon", 30, "streak"
        ));
        
        BADGE_DEFINITIONS.put("community_helper", new GamificationBadge(
            "community_helper", "Community Helper", "Received 50 helpful votes",
            "🤝", "rare", 50, "community"
        ));
        
        BADGE_DEFINITIONS.put("knowledge_seeker", new GamificationBadge(
            "knowledge_seeker", "Knowledge Seeker", "Completed 5 modules",
            "📚", "rare", 5, "module_completion"
        ));
        
        BADGE_DEFINITIONS.put("wellness_champion", new GamificationBadge(
            "wellness_champion", "Wellness Champion", "Perfect attendance for a month",
            "👑", "epic", 30, "streak"
        ));
        
        BADGE_DEFINITIONS.put("quiz_master", new GamificationBadge(
            "quiz_master", "Quiz Master", "Passed 10 quizzes",
            "🧠", "rare", 10, "quiz"
        ));
        
        BADGE_DEFINITIONS.put("early_adopter", new GamificationBadge(
            "early_adopter", "Early Adopter", "Joined in the first month",
            "🚀", "rare", 1, "special"
        ));
    }
    
    // Initialize tables lazily when first needed
    private synchronized void initializeIfNeeded() {
        if (!tablesCreated) {
            System.out.println("🚀 Initializing Gamification DAO...");
            createGamificationTables();
            tablesCreated = true;
            System.out.println("✅ Gamification tables created successfully");
        }
    }
    
    @Override
    public Optional<GamificationUserStats> getUserStats(String userEmail) {
        initializeIfNeeded();
        
        // Calculate points from module progress
        int points = calculateUserPoints(userEmail);
        int level = calculateUserLevel(userEmail);
        int completedModules = getCompletedModuleCount(userEmail);
        int passedQuizzes = getPassedQuizCount(userEmail);
        int dayStreak = calculateDayStreak(userEmail);
        
        // Check and award badges based on achievements
        checkAndAwardBadges(userEmail);
        
        // Try to get existing stats
        try {
            String statsSql = "SELECT * FROM gamification_stats WHERE user_email = ?";
            Query statsQuery = em.createNativeQuery(statsSql);
            statsQuery.setParameter(1, userEmail);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = statsQuery.getResultList();
            
            if (!results.isEmpty()) {
                Object[] row = results.get(0);
                Long id = ((Number) row[0]).longValue();
                
                // Update stats
                String updateSql = """
                    UPDATE gamification_stats 
                    SET points = ?, level = ?, completed_modules = ?, 
                        passed_quizzes = ?, day_streak = ?, last_updated = NOW()
                    WHERE id = ?
                    """;
                Query updateQuery = em.createNativeQuery(updateSql);
                updateQuery.setParameter(1, points);
                updateQuery.setParameter(2, level);
                updateQuery.setParameter(3, completedModules);
                updateQuery.setParameter(4, passedQuizzes);
                updateQuery.setParameter(5, dayStreak);
                updateQuery.setParameter(6, id);
                updateQuery.executeUpdate();
                
                GamificationUserStats stats = new GamificationUserStats();
                stats.setId(id);
                stats.setUserEmail(userEmail);
                stats.setPoints(points);
                stats.setLevel(level);
                stats.setCompletedModules(completedModules);
                stats.setPassedQuizzes(passedQuizzes);
                stats.setDayStreak(dayStreak);
                stats.setLastUpdated(LocalDateTime.now());
                stats.setBadges(getUserBadges(userEmail));
                
                return Optional.of(stats);
            }
        } catch (Exception e) {
            // Table might not exist or other error, continue to create
        }
        
        // Create new stats entry
        try {
            String insertSql = """
                INSERT INTO gamification_stats 
                (user_email, points, level, completed_modules, passed_quizzes, day_streak, last_updated) 
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """;
            Query insertQuery = em.createNativeQuery(insertSql);
            insertQuery.setParameter(1, userEmail);
            insertQuery.setParameter(2, points);
            insertQuery.setParameter(3, level);
            insertQuery.setParameter(4, completedModules);
            insertQuery.setParameter(5, passedQuizzes);
            insertQuery.setParameter(6, dayStreak);
            insertQuery.executeUpdate();
            
            // Get the generated ID
            String idSql = "SELECT LAST_INSERT_ID()";
            Query idQuery = em.createNativeQuery(idSql);
            BigInteger id = (BigInteger) idQuery.getSingleResult();
            
            GamificationUserStats stats = new GamificationUserStats();
            stats.setId(id.longValue());
            stats.setUserEmail(userEmail);
            stats.setPoints(points);
            stats.setLevel(level);
            stats.setCompletedModules(completedModules);
            stats.setPassedQuizzes(passedQuizzes);
            stats.setDayStreak(dayStreak);
            stats.setLastUpdated(LocalDateTime.now());
            stats.setBadges(getUserBadges(userEmail));
            
            return Optional.of(stats);
            
        } catch (Exception e) {
            System.err.println("❌ Error creating user stats: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<GamificationLeaderboardEntry> getLeaderboard(int limit) {
        initializeIfNeeded();
        ensureDummyDataExists(); // Make sure dummy data exists for leaderboard
        
        try {
            // Query to get leaderboard based on calculated points
            String sql = """
                SELECT 
                    u.email,
                    COALESCE(u.name, SUBSTRING(u.email, 1, LOCATE('@', u.email) - 1)) as name,
                    COALESCE(
                        (
                            SELECT COUNT(*) FROM module_progress mp 
                            WHERE mp.user_email = u.email AND mp.progress = 100
                        ) * 100 + 
                        (
                            SELECT COUNT(*) FROM module_progress mp 
                            WHERE mp.user_email = u.email AND mp.quiz_passed = true
                        ) * 50,
                        u.base_points
                    ) as total_points,
                    COALESCE(gs.day_streak, 0) as day_streak,
                    ROW_NUMBER() OVER (ORDER BY total_points DESC) as rank
                FROM (
                    SELECT DISTINCT user_email as email FROM module_progress
                    UNION
                    SELECT email FROM gamification_dummy_users
                ) u
                LEFT JOIN gamification_stats gs ON u.email = gs.user_email
                LEFT JOIN gamification_dummy_users du ON u.email = du.email
                ORDER BY total_points DESC
                LIMIT ?
                """;
            
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, limit);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            List<GamificationLeaderboardEntry> leaderboard = new ArrayList<>();
            
            for (Object[] row : results) {
                String email = (String) row[0];
                String name = (String) row[1];
                BigInteger pointsBigInt = (BigInteger) row[2];
                int points = pointsBigInt != null ? pointsBigInt.intValue() : 0;
                BigInteger streakBigInt = (BigInteger) row[3];
                int streak = streakBigInt != null ? streakBigInt.intValue() : 0;
                BigInteger rankBigInt = (BigInteger) row[4];
                int rank = rankBigInt != null ? rankBigInt.intValue() : 0;
                int level = calculateLevelFromPoints(points);
                
                // Determine badge emoji based on rank
                String badgeEmoji = getBadgeEmojiForRank(rank);
                
                // Check if it's a dummy user
                boolean isDummy = email.contains("student") || email.contains("example.com");
                
                leaderboard.add(new GamificationLeaderboardEntry(
                    email, name, rank, points, level, streak, 
                    badgeEmoji, isDummy
                ));
            }
            
            return leaderboard;
        } catch (Exception e) {
            System.err.println("❌ Error getting leaderboard: " + e.getMessage());
            // Return empty leaderboard if there's an error
            return new ArrayList<>();
        }
    }
    
    @Override
    public int getUserLeaderboardRank(String userEmail) {
        initializeIfNeeded();
        ensureDummyDataExists();
        
        try {
            String sql = """
                SELECT rank FROM (
                    SELECT 
                        user_email,
                        ROW_NUMBER() OVER (ORDER BY total_points DESC) as rank
                    FROM (
                        SELECT 
                            mp.user_email,
                            (
                                SELECT COUNT(*) FROM module_progress mp2 
                                WHERE mp2.user_email = mp.user_email AND mp2.progress = 100
                            ) * 100 + 
                            (
                                SELECT COUNT(*) FROM module_progress mp2 
                                WHERE mp2.user_email = mp.user_email AND mp2.quiz_passed = true
                            ) * 50 as total_points
                        FROM module_progress mp
                        GROUP BY mp.user_email
                        UNION
                        SELECT email, base_points FROM gamification_dummy_users
                    ) scores
                ) ranked
                WHERE user_email = ?
                """;
            
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            
            BigInteger rank = (BigInteger) query.getSingleResult();
            return rank != null ? rank.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public List<GamificationBadge> getUserBadges(String userEmail) {
        initializeIfNeeded();
        
        // First check database for awarded badges
        List<GamificationBadge> awardedBadges = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM gamification_badges WHERE user_email = ? ORDER BY awarded_date DESC";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Long id = ((Number) row[0]).longValue();
                String email = (String) row[1];
                String badgeId = (String) row[2];
                String badgeName = (String) row[3];
                String description = (String) row[4];
                String icon = (String) row[5];
                String rarity = (String) row[6];
                int pointsValue = ((Number) row[7]).intValue();
                String category = (String) row[8];
                java.sql.Timestamp awardedDate = (java.sql.Timestamp) row[9];
                
                GamificationBadge badge = new GamificationBadge(badgeId, badgeName, description, 
                    icon, rarity, pointsValue, category);
                badge.setId(id);
                badge.setUserEmail(email);
                badge.setAwardedDate(awardedDate.toLocalDateTime());
                badge.setEarned(true);
                awardedBadges.add(badge);
            }
        } catch (Exception e) {
            // Table might not exist yet, continue with empty list
        }
        
        // Add all badge definitions with earned status
        List<GamificationBadge> allBadges = new ArrayList<>();
        Set<String> awardedBadgeIds = awardedBadges.stream()
            .map(GamificationBadge::getBadgeId)
            .collect(Collectors.toSet());
        
        for (GamificationBadge definition : BADGE_DEFINITIONS.values()) {
            GamificationBadge badge = new GamificationBadge(definition);
            badge.setEarned(awardedBadgeIds.contains(badge.getBadgeId()));
            badge.setUserEmail(userEmail);
            if (badge.isEarned()) {
                // Find awarded date from database
                awardedBadges.stream()
                    .filter(b -> b.getBadgeId().equals(badge.getBadgeId()))
                    .findFirst()
                    .ifPresent(awarded -> badge.setAwardedDate(awarded.getAwardedDate()));
            }
            allBadges.add(badge);
        }
        
        return allBadges;
    }
    
    @Override
    public void awardBadge(String userEmail, String badgeId) {
        initializeIfNeeded();
        
        if (!BADGE_DEFINITIONS.containsKey(badgeId)) {
            return; // Invalid badge ID, just return silently
        }
        
        try {
            // Check if already awarded
            String checkSql = "SELECT COUNT(*) FROM gamification_badges WHERE user_email = ? AND badge_id = ?";
            Query checkQuery = em.createNativeQuery(checkSql);
            checkQuery.setParameter(1, userEmail);
            checkQuery.setParameter(2, badgeId);
            
            BigInteger count = (BigInteger) checkQuery.getSingleResult();
            if (count.intValue() > 0) {
                return; // Already awarded
            }
            
            // Award the badge
            GamificationBadge badgeDef = BADGE_DEFINITIONS.get(badgeId);
            String sql = """
                INSERT INTO gamification_badges 
                (user_email, badge_id, badge_name, description, icon, rarity, points_value, category, awarded_date) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            query.setParameter(2, badgeId);
            query.setParameter(3, badgeDef.getBadgeName());
            query.setParameter(4, badgeDef.getDescription());
            query.setParameter(5, badgeDef.getIcon());
            query.setParameter(6, badgeDef.getRarity());
            query.setParameter(7, badgeDef.getPointsValue());
            query.setParameter(8, badgeDef.getCategory());
            query.executeUpdate();
            
            // Record points for badge
            recordPointsActivity(userEmail, "badge_" + badgeId, badgeDef.getPointsValue());
            
        } catch (Exception e) {
            System.err.println("❌ Error awarding badge: " + e.getMessage());
        }
    }
    
    @Override
    public void recordPointsActivity(String userEmail, String activity, int points) {
        initializeIfNeeded();
        
        try {
            String sql = """
                INSERT INTO gamification_points_activity 
                (user_email, activity, points, activity_date) 
                VALUES (?, ?, ?, NOW())
                """;
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            query.setParameter(2, activity);
            query.setParameter(3, points);
            query.executeUpdate();
            
        } catch (Exception e) {
            System.err.println("❌ Error recording points activity: " + e.getMessage());
        }
    }
    
    @Override
    public List<PointsActivity> getPointsActivities() {
        // This doesn't need database access, just return static data
        return Arrays.asList(
            new PointsActivity("Complete a learning module", 100, "Finish all lessons in a module"),
            new PointsActivity("Pass a quiz (70%+)", 50, "Achieve 70% or higher on module quiz"),
            new PointsActivity("Daily check-in", 10, "Visit the platform daily"),
            new PointsActivity("Help in peer forum", 5, "Get helpful votes on forum posts"),
            new PointsActivity("Maintain 7-day streak", 75, "Use platform for 7 consecutive days"),
            new PointsActivity("Complete self-assessment", 25, "Finish personal assessment"),
            new PointsActivity("Earn a badge", 50, "Unlock achievement badges"),
            new PointsActivity("Reach new level", 200, "Level up your profile")
        );
    }
    
    @Override
    public List<RecentAchievement> getRecentAchievements(String userEmail, int limit) {
        initializeIfNeeded();
        
        try {
            String sql = """
                SELECT 
                    'Module Complete' as title,
                    CONCAT('Finished ', completed_modules, ' module', CASE WHEN completed_modules > 1 THEN 's' ELSE '' END) as description,
                    completed_modules * 100 as points,
                    last_updated as timestamp
                FROM gamification_stats 
                WHERE user_email = ? AND completed_modules > 0
                UNION ALL
                SELECT 
                    'Badge Earned' as title,
                    CONCAT(badge_name, ' - ', description) as description,
                    50 as points,
                    awarded_date as timestamp
                FROM gamification_badges
                WHERE user_email = ?
                UNION ALL
                SELECT 
                    'Points Earned' as title,
                    activity as description,
                    points,
                    activity_date as timestamp
                FROM gamification_points_activity
                WHERE user_email = ?
                ORDER BY timestamp DESC
                LIMIT ?
                """;
            
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            query.setParameter(2, userEmail);
            query.setParameter(3, userEmail);
            query.setParameter(4, limit);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            return results.stream()
                .map(row -> {
                    String title = (String) row[0];
                    String description = (String) row[1];
                    BigInteger pointsBigInt = (BigInteger) row[2];
                    int points = pointsBigInt != null ? pointsBigInt.intValue() : 0;
                    java.sql.Timestamp timestamp = (java.sql.Timestamp) row[3];
                    String timeAgo = timestamp != null ? 
                        formatTimeAgo(timestamp.toLocalDateTime()) : "Recently";
                    
                    return new RecentAchievement(title, description, points, timeAgo);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error getting recent achievements: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public int calculateUserPoints(String userEmail) {
        initializeIfNeeded();
        
        int points = 0;
        
        // Points from completed modules
        points += getCompletedModuleCount(userEmail) * POINTS_PER_MODULE;
        
        // Points from passed quizzes
        points += getPassedQuizCount(userEmail) * POINTS_PER_QUIZ;
        
        // Points from badges
        points += getBadgePoints(userEmail);
        
        return points;
    }
    
    @Override
    public int calculateUserLevel(String userEmail) {
        initializeIfNeeded();
        
        int points = calculateUserPoints(userEmail);
        return calculateLevelFromPoints(points);
    }
    
    @Override
    public NextLevelProgress getNextLevelProgress(String userEmail) {
        initializeIfNeeded();
        
        int points = calculateUserPoints(userEmail);
        int currentLevel = calculateLevelFromPoints(points);
        int nextLevelPoints = (currentLevel + 1) * POINTS_PER_LEVEL;
        
        return new NextLevelProgress(points, nextLevelPoints);
    }
    
    @Override
    public List<GamificationLeaderboardEntry> getCategoryLeaderboard(String category, int limit) {
        // For now, return general leaderboard
        return getLeaderboard(limit);
    }
    
    // ==================== HELPER METHODS ====================
    
    private void createGamificationTables() {
        try {
            // Create gamification_stats table
            String statsTableSql = """
                CREATE TABLE IF NOT EXISTS gamification_stats (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(255) NOT NULL UNIQUE,
                    points INT DEFAULT 0,
                    level INT DEFAULT 1,
                    completed_modules INT DEFAULT 0,
                    passed_quizzes INT DEFAULT 0,
                    day_streak INT DEFAULT 0,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_gstats_user_email (user_email)
                )
                """;
            em.createNativeQuery(statsTableSql).executeUpdate();

            // Create gamification_badges table
            String badgesTableSql = """
                CREATE TABLE IF NOT EXISTS gamification_badges (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(255) NOT NULL,
                    badge_id VARCHAR(100) NOT NULL,
                    badge_name VARCHAR(255),
                    description TEXT,
                    icon VARCHAR(50),
                    rarity VARCHAR(50),
                    points_value INT DEFAULT 0,
                    category VARCHAR(100),
                    awarded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_user_badge (user_email, badge_id),
                    INDEX idx_gbadges_user_email (user_email),
                    INDEX idx_gbadges_badge_id (badge_id)
                )
                """;
            em.createNativeQuery(badgesTableSql).executeUpdate();

            // Create gamification_points_activity table
            String activityTableSql = """
                CREATE TABLE IF NOT EXISTS gamification_points_activity (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(255) NOT NULL,
                    activity VARCHAR(255) NOT NULL,
                    points INT NOT NULL,
                    activity_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_gactivity_user_email (user_email),
                    INDEX idx_gactivity_date (activity_date)
                )
                """;
            em.createNativeQuery(activityTableSql).executeUpdate();

            // Create gamification_dummy_users table
            String dummyTableSql = """
                CREATE TABLE IF NOT EXISTS gamification_dummy_users (
                    email VARCHAR(255) PRIMARY KEY,
                    name VARCHAR(255),
                    base_points INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_dummy_email (email)
                )
                """;
            em.createNativeQuery(dummyTableSql).executeUpdate();
            
        } catch (Exception e) {
            System.err.println("❌ Error creating gamification tables: " + e.getMessage());
        }
    }
    
    private void ensureDummyDataExists() {
        try {
            // Check if dummy users already exist
            String checkSql = "SELECT COUNT(*) FROM gamification_dummy_users";
            Query checkQuery = em.createNativeQuery(checkSql);
            BigInteger count = (BigInteger) checkQuery.getSingleResult();
            
            if (count.intValue() > 0) {
                return; // Already exist
            }
            
            // Insert dummy users
            String[] dummyUsers = {
                "('student.a@example.com', 'Student A', 1250)",
                "('student.b@example.com', 'Student B', 1180)",
                "('student.c@example.com', 'Student C', 1050)",
                "('student.d@example.com', 'Student D', 820)",
                "('student.e@example.com', 'Student E', 750)",
                "('student.f@example.com', 'Student F', 680)",
                "('student.g@example.com', 'Student G', 550)",
                "('student.h@example.com', 'Student H', 420)",
                "('student.i@example.com', 'Student I', 350)",
                "('student.j@example.com', 'Student J', 280)"
            };
            
            for (String user : dummyUsers) {
                String insertSql = "INSERT IGNORE INTO gamification_dummy_users (email, name, base_points) VALUES " + user;
                em.createNativeQuery(insertSql).executeUpdate();
            }
            
        } catch (Exception e) {
            // Table might not exist yet or other error, ignore for now
        }
    }
    
    private int getCompletedModuleCount(String userEmail) {
        try {
            String sql = "SELECT COUNT(*) FROM module_progress WHERE user_email = ? AND progress = 100";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            BigInteger count = (BigInteger) query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getPassedQuizCount(String userEmail) {
        try {
            String sql = "SELECT COUNT(*) FROM module_progress WHERE user_email = ? AND quiz_passed = true";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            BigInteger count = (BigInteger) query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getBadgePoints(String userEmail) {
        try {
            String sql = "SELECT COALESCE(SUM(points_value), 0) FROM gamification_badges WHERE user_email = ?";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            BigInteger sum = (BigInteger) query.getSingleResult();
            return sum != null ? sum.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int calculateDayStreak(String userEmail) {
        try {
            // Simple streak calculation - using last activity date
            String sql = """
                SELECT DATEDIFF(NOW(), MAX(activity_date)) as days_since_last_activity
                FROM gamification_points_activity 
                WHERE user_email = ?
                """;
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userEmail);
            
            BigInteger daysSince = (BigInteger) query.getSingleResult();
            if (daysSince != null && daysSince.intValue() <= 1) {
                return 7; // Simplified: if active in last day, return 7-day streak
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int calculateLevelFromPoints(int points) {
        return Math.max(1, points / POINTS_PER_LEVEL + 1);
    }
    
    private String getBadgeEmojiForRank(int rank) {
        switch (rank) {
            case 1: return "👑";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return rank <= 10 ? "⭐" : "";
        }
    }
    
    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(dateTime, now).toHours();
        
        if (hours < 1) return "Just now";
        if (hours < 24) return hours + " hours ago";
        if (hours < 168) return (hours / 24) + " days ago";
        return (hours / 168) + " weeks ago";
    }
    
    private void checkAndAwardBadges(String userEmail) {
        int completedModules = getCompletedModuleCount(userEmail);
        int passedQuizzes = getPassedQuizCount(userEmail);
        
        // Check and award badges
        if (completedModules >= 1) {
            awardBadge(userEmail, "first_module");
        }
        if (completedModules >= 5) {
            awardBadge(userEmail, "knowledge_seeker");
        }
        if (passedQuizzes >= 10) {
            awardBadge(userEmail, "quiz_master");
        }
    }
}