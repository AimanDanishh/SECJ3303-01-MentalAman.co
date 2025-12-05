package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class DashboardData implements Serializable {

    // --- Data Structures ---

    public static class StatItem implements Serializable {
        public String label;
        public String value;
        public String icon; // Lucide icon name
        public String color; // Tailwind color name (e.g., 'blue', 'green')
        
        public StatItem(String label, String value, String icon, String color) {
            this.label = label;
            this.value = value;
            this.icon = icon;
            this.color = color;
        }
    }
    
    public static class ActivityItem implements Serializable {
        public String title;
        public String time;
        public String type; // 'learning', 'assessment', 'community', 'ai'
        
        public ActivityItem(String title, String time, String type) {
            this.title = title;
            this.time = time;
            this.type = type;
        }

        public String getIconName() {
            switch (type) {
                case "learning": return "book-open";
                case "assessment": return "target";
                case "community": return "users";
                case "ai": return "brain";
                default: return "activity";
            }
        }
        
        public String getIconColor() {
            switch (type) {
                case "learning": return "text-blue-600";
                case "assessment": return "text-green-600";
                case "community": return "text-purple-600";
                case "ai": return "text-orange-600";
                default: return "text-slate-600";
            }
        }
    }

    public static class EventItem implements Serializable {
        public String title;
        public String date;
        public String time;

        public EventItem(String title, String date, String time) {
            this.title = title;
            this.date = date;
            this.time = time;
        }
    }
    
    // --- Initial Mock Data (Replicating TSX State) ---

    public static List<StatItem> getStudentStats() {
        return Arrays.asList(
            new StatItem("Learning Progress", "68%", "book-open", "blue"),
            new StatItem("Mood Score", "7.5/10", "trending-up", "green"),
            new StatItem("Streak Days", "12", "target", "purple"),
            new StatItem("Points Earned", "850", "award", "orange")
        );
    }
    
    public static List<ActivityItem> getRecentActivities() {
        return Arrays.asList(
            new ActivityItem("Completed: Stress Management Module", "2 hours ago", "learning"),
            new ActivityItem("Mood Check-in Completed", "5 hours ago", "assessment"),
            new ActivityItem("New post in Peer Support Forum", "1 day ago", "community"),
            new ActivityItem("AI Coach Session", "2 days ago", "ai")
        );
    }
    
    public static List<EventItem> getUpcomingEvents() {
        return Arrays.asList(
            new EventItem("Counselling Session", "Nov 15, 2025", "2:00 PM"),
            new EventItem("Group Therapy Session", "Nov 18, 2025", "4:00 PM")
        );
    }

    // Static progress data (Replicated directly in the view, as it's not dynamic based on user data here)
}