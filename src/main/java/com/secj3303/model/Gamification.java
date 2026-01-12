package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "gamifications")
public class Gamification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;

    @Column(name = "xp_points")
    private int xpPoints;

    @Column(name = "current_level")
    private int currentLevel;

    @Column(name = "daily_streak")
    private int dailyStreak;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Transient
    private int leaderboardRank;

    public Gamification() {}

    public Gamification(String userEmail) {
        this.userEmail = userEmail;
        this.xpPoints = 0;
        this.currentLevel = 1;
        this.dailyStreak = 0;
        this.lastActivity = null;
    }

    // ==========================================
    // INNER CLASSES (For UI / Display)
    // ==========================================
    
    public static class Badge implements Serializable {
        public String name;
        public String description;
        public boolean earned;
        public String icon;
        public String rarity;

        public Badge(String name, String description, boolean earned, String icon, String rarity) {
            this.name = name;
            this.description = description;
            this.earned = earned;
            this.icon = icon;
            this.rarity = rarity;
        }
        
        public String getBadgeClass() {
            switch (rarity) {
                case "common": return "bg-slate-200";
                case "uncommon": return "bg-green-200 text-green-800";
                case "rare": return "bg-blue-200 text-blue-800";
                case "epic": return "bg-purple-200 text-purple-800";
                default: return "bg-slate-200";
            }
        }
    }

    public static class LeaderboardEntry implements Serializable {
        public int rank;
        public String name;
        public int points;
        public int level;
        public String badge;
        public boolean isCurrentUser;

        public LeaderboardEntry(int rank, String name, int points, int level, String badge, boolean isCurrentUser) {
            this.rank = rank;
            this.name = name;
            this.points = points;
            this.level = level;
            this.badge = badge;
            this.isCurrentUser = isCurrentUser;
        }
    }

    public static class Achievement implements Serializable {
        public String title;
        public String description;
        public int points;
        public String time;

        public Achievement(String title, String description, int points, String time) {
            this.title = title;
            this.description = description;
            this.points = points;
            this.time = time;
        }
    }

    public static class PointsActivity implements Serializable {
        public String activity;
        public int points;

        public PointsActivity(String activity, int points) {
            this.activity = activity;
            this.points = points;
        }
    }

    // ==========================================
    // STATIC DATA GENERATORS
    // ==========================================

    public static List<Badge> getBadges() {
        // ALL BADGES DEFAULT TO FALSE (UNEARNED)
        return Arrays.asList(
            new Badge("First Steps", "Completed your first module", false, "🎯", "common"),
            new Badge("Consistent Learner", "Maintained a 7-day streak", false, "🔥", "uncommon"),
            new Badge("Mood Master", "Logged mood for 30 days", false, "📊", "uncommon"),
            new Badge("Community Helper", "Received 50 helpful votes", false, "🤝", "rare"),
            new Badge("Knowledge Seeker", "Completed 5 modules", false, "📚", "rare"),
            new Badge("Wellness Champion", "Perfect attendance for a month", false, "👑", "epic")
        );
    }

    // Static dummy data for fallback leaderboard
    public static List<LeaderboardEntry> getStaticLeaderboard() {
        return Arrays.asList(
            new LeaderboardEntry(1, "Student A", 1250, 7, "👑", false),
            new LeaderboardEntry(2, "Student B", 1180, 6, "🥈", false),
            new LeaderboardEntry(3, "Student C", 1050, 6, "🥉", false),
            new LeaderboardEntry(4, "Student D", 820, 5, "", false),
            new LeaderboardEntry(5, "Student E", 750, 4, "", false)
        );
    }
    
    public static List<Achievement> getRecentAchievements() {
        return Arrays.asList(
            new Achievement("Quiz Master", "Scored 100% on Stress Management quiz", 50, "2 hours ago"),
            new Achievement("Daily Check-in", "Completed daily mood tracking", 10, "5 hours ago"),
            new Achievement("Module Complete", "Finished Anxiety Awareness module", 100, "1 day ago")
        );
    }

    public static List<PointsActivity> getPointsActivities() {
        return Arrays.asList(
            new PointsActivity("Complete a learning module", 100),
            new PointsActivity("Pass a quiz (70%+)", 50),
            new PointsActivity("Daily mood check-in", 10),
            new PointsActivity("Help in peer forum (helpful vote)", 5),
            new PointsActivity("Maintain 7-day streak", 75),
            new PointsActivity("Complete self-assessment", 25)
        );
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public int getDailyStreak() { return dailyStreak; }
    public void setDailyStreak(int dailyStreak) { this.dailyStreak = dailyStreak; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public int getLeaderboardRank() { return leaderboardRank; }
    public void setLeaderboardRank(int leaderboardRank) { this.leaderboardRank = leaderboardRank; }
}