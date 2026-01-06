package com.secj3303.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "mood_entries")
public class MoodEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;
    
    @Column(name = "mood_type", nullable = false, length = 50)
    private String mood;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    // Default constructor
    public MoodEntry() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Parameterized constructor
    public MoodEntry(LocalDate entryDate, String mood, String notes, String username) {
        this.entryDate = entryDate;
        this.mood = mood;
        this.notes = notes;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    
    // For backward compatibility with existing code
    public String getDate() { 
        return entryDate != null ? entryDate.toString() : null; 
    }
    public void setDate(String date) { 
        this.entryDate = date != null ? LocalDate.parse(date) : null; 
    }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // For backward compatibility with existing code
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp != null ? LocalDateTime.parse(timestamp) : LocalDateTime.now();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    // Mood definitions (static constant)
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
            LocalDate entryDate = entry.getEntryDate();
            // Check if entry is within last 7 days (inclusive)
            if (!entryDate.isBefore(sevenDaysAgo) && !entryDate.isAfter(today)) {
                last7DaysEntries.add(entry);
                
                if (entry.getMood() != null && moodCounts.containsKey(entry.getMood())) {
                    moodCounts.put(entry.getMood(), moodCounts.get(entry.getMood()) + 1);
                }
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
        stats.put("totalEntriesLast7Days", last7DaysEntries.size());
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
        entries.sort((e1, e2) -> e2.getEntryDate().compareTo(e1.getEntryDate()));
        
        for (MoodEntry entry : entries) {
            LocalDate entryDate = entry.getEntryDate();
            
            if (entryDate.equals(currentDate.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
}