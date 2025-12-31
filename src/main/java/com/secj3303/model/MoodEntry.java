package com.secj3303.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodEntry {
    private int id;
    private String date;
    private String mood;
    private String notes;
    private String timestamp;
    
    // Default constructor
    public MoodEntry() {}
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    // Mood definitions
    public static final List<Map<String, String>> MOOD_DEFINITIONS = new ArrayList<>();
    
    static {
        // Initialize mood definitions
        MOOD_DEFINITIONS.add(Map.of(
            "id", "happy",
            "label", "Happy",
            "color", "text-green-600",
            "bg", "bg-green-100",
            "border", "border-green-400",
            "bg-bar", "bg-green-500"
        ));
        MOOD_DEFINITIONS.add(Map.of(
            "id", "sad",
            "label", "Sad",
            "color", "text-blue-600",
            "bg", "bg-blue-100",
            "border", "border-blue-400",
            "bg-bar", "bg-blue-500"
        ));
        MOOD_DEFINITIONS.add(Map.of(
            "id", "stressed",
            "label", "Stressed",
            "color", "text-red-600",
            "bg", "bg-red-100",
            "border", "border-red-400",
            "bg-bar", "bg-red-500"
        ));
        MOOD_DEFINITIONS.add(Map.of(
            "id", "neutral",
            "label", "Neutral",
            "color", "text-gray-600",
            "bg", "bg-gray-100",
            "border", "border-gray-400",
            "bg-bar", "bg-gray-500"
        ));
        MOOD_DEFINITIONS.add(Map.of(
            "id", "excited",
            "label", "Excited",
            "color", "text-yellow-600",
            "bg", "bg-yellow-100",
            "border", "border-yellow-400",
            "bg-bar", "bg-yellow-500"
        ));
        MOOD_DEFINITIONS.add(Map.of(
            "id", "anxious",
            "label", "Anxious",
            "color", "text-purple-600",
            "bg", "bg-purple-100",
            "border", "border-purple-400",
            "bg-bar", "bg-purple-500"
        ));
    }
    
    // Get initial mood entries
    public static List<MoodEntry> getInitialMoodEntries() {
        List<MoodEntry> entries = new ArrayList<>();
        
        // Add sample entries for the last 5 days
        for (int i = 0; i < 5; i++) {
            MoodEntry entry = new MoodEntry();
            entry.setId(i + 1);
            entry.setDate(LocalDate.now().minusDays(i).toString());
            entry.setMood(i % 2 == 0 ? "happy" : "neutral");
            entry.setNotes("Sample mood entry " + (i + 1));
            entry.setTimestamp(LocalDateTime.now().minusDays(i).toString());
            entries.add(entry);
        }
        
        return entries;
    }
    
    // Calculate mood statistics
    public static Map<String, Object> getMoodStats(List<MoodEntry> entries) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> moodCounts = new HashMap<>();
        
        // Initialize mood counts
        for (Map<String, String> moodDef : MOOD_DEFINITIONS) {
            moodCounts.put(moodDef.get("id"), 0);
        }
        
        // Get entries from last 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // Last 7 days inclusive
        
        List<MoodEntry> last7DaysEntries = new ArrayList<>();
        
        // Count moods from last 7 days only
        for (MoodEntry entry : entries) {
            try {
                LocalDate entryDate = LocalDate.parse(entry.getDate());
                // Check if entry is within last 7 days (inclusive)
                if (!entryDate.isBefore(sevenDaysAgo) && !entryDate.isAfter(today)) {
                    last7DaysEntries.add(entry);
                    
                    if (entry.getMood() != null && moodCounts.containsKey(entry.getMood())) {
                        moodCounts.put(entry.getMood(), moodCounts.get(entry.getMood()) + 1);
                    }
                }
            } catch (Exception e) {
                // Skip invalid dates
            }
        }
        
        // Find most frequent mood
        String mostFrequentMoodId = "neutral";
        int mostFrequentCount = 0;
        
        for (Map.Entry<String, Integer> count : moodCounts.entrySet()) {
            if (count.getValue() > mostFrequentCount) {
                mostFrequentCount = count.getValue();
                mostFrequentMoodId = count.getKey();
            }
        }
        
        // Get mood data for most frequent mood
        Map<String, String> mostFrequentMoodData = new HashMap<>();
        for (Map<String, String> moodDef : MOOD_DEFINITIONS) {
            if (moodDef.get("id").equals(mostFrequentMoodId)) {
                mostFrequentMoodData = moodDef;
                break;
            }
        }
        
        // If no mood data found, use neutral as default
        if (mostFrequentMoodData.isEmpty()) {
            mostFrequentMoodData = Map.of(
                "id", "neutral",
                "color", "text-gray-600",
                "bg", "bg-gray-100"
            );
        }
        
        stats.put("last7Days", last7DaysEntries);
        stats.put("moodCounts", moodCounts);
        stats.put("mostFrequentMoodId", mostFrequentMoodId);
        stats.put("totalEntriesLast7Days", last7DaysEntries.size()); // FIXED: Use last7DaysEntries.size()
        stats.put("mostFrequentCount", mostFrequentCount);
        stats.put("mostFrequentMoodData", mostFrequentMoodData);
        
        return stats;
    }
    
    // Calculate current streak
    public static int calculateStreak(List<MoodEntry> entries) {
        if (entries.isEmpty()) return 0;
        
        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        
        // Sort by date descending
        entries.sort((e1, e2) -> {
            try {
                LocalDate date1 = LocalDate.parse(e1.getDate());
                LocalDate date2 = LocalDate.parse(e2.getDate());
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        for (MoodEntry entry : entries) {
            try {
                LocalDate entryDate = LocalDate.parse(entry.getDate());
                
                if (entryDate.equals(currentDate.minusDays(streak))) {
                    streak++;
                } else {
                    break;
                }
            } catch (Exception e) {
                // Skip invalid dates
            }
        }
        
        return streak;
    }
}