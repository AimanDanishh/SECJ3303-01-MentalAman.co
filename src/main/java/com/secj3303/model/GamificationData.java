package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class GamificationData implements Serializable {

    public static class Badge implements Serializable {
        public String name;
        public String description;
        public boolean earned;
        public String icon; // Emoji/Unicode
        public String rarity; // 'common', 'uncommon', 'rare', 'epic'

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
        public String badge; // Emoji
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

    // --- Static Data Initialization ---

    public static final int USER_POINTS = 850;
    public static final int NEXT_LEVEL_POINTS = 1000;
    public static final int CURRENT_LEVEL = 5;

    public static List<Badge> getBadges() {
        return Arrays.asList(
            new Badge("First Steps", "Completed your first module", true, "🎯", "common"),
            new Badge("Consistent Learner", "Maintained a 7-day streak", true, "🔥", "uncommon"),
            new Badge("Mood Master", "Logged mood for 30 days", true, "📊", "uncommon"),
            new Badge("Community Helper", "Received 50 helpful votes", true, "🤝", "rare"),
            new Badge("Knowledge Seeker", "Completed 5 modules", false, "📚", "rare"),
            new Badge("Wellness Champion", "Perfect attendance for a month", false, "👑", "epic")
        );
    }

    public static List<LeaderboardEntry> getLeaderboard() {
        return Arrays.asList(
            new LeaderboardEntry(1, "Student A", 1250, 7, "👑", false),
            new LeaderboardEntry(2, "Student B", 1180, 6, "🥈", false),
            new LeaderboardEntry(3, "Student C", 1050, 6, "🥉", false),
            new LeaderboardEntry(4, "You", 850, 5, "⭐", true),
            new LeaderboardEntry(5, "Student D", 820, 5, "", false)
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
}