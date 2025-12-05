package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoodEntry implements Serializable {
    private int id;
    private String date; // YYYY-MM-DD
    private String mood; // 'happy' | 'sad' | 'stressed' | 'neutral' | 'excited' | 'anxious'
    private String notes;
    private String timestamp; // ISO 8601 string for precise time

    // Constructor for initial mock data
    public MoodEntry(int id, String date, String mood, String notes, String timestamp) {
        this.id = id;
        this.date = date;
        this.mood = mood;
        this.notes = notes;
        this.timestamp = timestamp;
    }
    
    // Default constructor for form binding
    public MoodEntry() {}

    // --- Getters and Setters (Necessary for Thymeleaf and data modification) ---

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
    
    // --- Static Data and Utilities (Replicating TSX logic) ---

    public static final List<Map<String, String>> MOOD_DEFINITIONS = Arrays.asList(
        Map.of("id", "happy", "label", "Happy", "color", "text-yellow-500", "bg", "bg-yellow-100", "border", "border-yellow-500", "bg-bar", "bg-yellow-500"),
        Map.of("id", "sad", "label", "Sad", "color", "text-blue-500", "bg", "bg-blue-100", "border", "border-blue-500", "bg-bar", "bg-blue-500"),
        Map.of("id", "stressed", "label", "Stressed", "color", "text-red-500", "bg", "bg-red-100", "border", "border-red-500", "bg-bar", "bg-red-500"),
        Map.of("id", "neutral", "label", "Neutral", "color", "text-gray-500", "bg", "bg-gray-100", "border", "border-gray-500", "bg-bar", "bg-gray-500"),
        Map.of("id", "excited", "label", "Excited", "color", "text-purple-500", "bg", "bg-purple-100", "border", "border-purple-500", "bg-bar", "bg-purple-500"),
        Map.of("id", "anxious", "label", "Anxious", "color", "text-orange-500", "bg", "bg-orange-100", "border", "border-orange-500", "bg-bar", "bg-orange-500")
    );

    public static Map<String, String> getMoodData(String moodId) {
        return MOOD_DEFINITIONS.stream()
            .filter(m -> m.get("id").equals(moodId))
            .findFirst()
            .orElse(Map.of("id", "unknown", "label", "Unknown", "color", "text-slate-500", "bg", "bg-slate-100", "border", "border-slate-500", "bg-bar", "bg-slate-500"));
    }
    
    public static List<MoodEntry> getInitialMoodEntries() {
        return new ArrayList<>(Arrays.asList(
            new MoodEntry(1, "2025-11-13", "happy", "Had a good study session with friends", "2025-11-13T10:30:00Z"),
            new MoodEntry(2, "2025-11-12", "stressed", "Feeling anxious about upcoming exams", "2025-11-12T14:20:00Z"),
            new MoodEntry(3, "2025-11-11", "neutral", "Regular day, nothing special", "2025-11-11T16:45:00Z"),
            new MoodEntry(4, "2025-11-10", "happy", "Got good feedback on my project", "2025-11-10T11:00:00Z"),
            new MoodEntry(5, "2025-11-09", "anxious", "Worried about assignment deadlines", "2025-11-09T09:15:00Z")
        ));
    }
    
    // --- Complex Calculation Logic ---

    // Replicates calculateStreak()
    public static int calculateStreak(List<MoodEntry> entries) {
        if (entries.isEmpty()) return 0;

        List<MoodEntry> sortedEntries = entries.stream()
            .sorted(Comparator.comparing(MoodEntry::getDate).reversed())
            .collect(Collectors.toList());

        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        boolean todayLogged = false;

        // Check if today is logged
        if (sortedEntries.get(0).getDate().equals(currentDate.toString())) {
            todayLogged = true;
            streak = 1;
        }

        // Check if yesterday was logged, if today is not, or continue streak
        LocalDate lastDate = todayLogged ? currentDate : currentDate.minusDays(1);
        
        for (int i = 0; i < sortedEntries.size(); i++) {
            LocalDate entryDate = LocalDate.parse(sortedEntries.get(i).getDate());
            
            if (entryDate.isEqual(lastDate)) {
                if (!todayLogged && entryDate.isEqual(currentDate.minusDays(1))) {
                    // Start streak from yesterday if today is missed
                    streak = 1;
                } else if (i > 0) {
                    LocalDate prevEntryDate = LocalDate.parse(sortedEntries.get(i - 1).getDate());
                    if (ChronoUnit.DAYS.between(entryDate, prevEntryDate) == 1) {
                        streak++;
                    } else if (ChronoUnit.DAYS.between(entryDate, prevEntryDate) > 1) {
                        // Gap found, streak broken
                        break;
                    }
                    // Handle same day entry (should be filtered out by date check)
                }
                lastDate = entryDate.minusDays(1);
            } else if (entryDate.isBefore(lastDate)) {
                break; // Streak broken by a gap
            }
        }
        return streak;
    }

    // Replicates last7Days filtering and moodCounts calculation
    public static Map<String, Object> getMoodStats(List<MoodEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<MoodEntry> last7Days = entries.stream()
            .filter(entry -> {
                LocalDate entryDate = LocalDate.parse(entry.getDate());
                return !entryDate.isBefore(sevenDaysAgo) && !entryDate.isAfter(today);
            })
            .collect(Collectors.toList());

        Map<String, Long> moodCounts = last7Days.stream()
            .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));
        
        // Find most frequent mood
        String mostFrequentMoodId = moodCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        Long mostFrequentCount = Optional.ofNullable(mostFrequentMoodId)
            .map(moodCounts::get)
            .orElse(0L);

        Map<String, String> mostFrequentMoodData = Optional.ofNullable(mostFrequentMoodId)
            .map(MoodEntry::getMoodData)
            .orElse(null);

        return Map.of(
            "last7Days", last7Days,
            "moodCounts", moodCounts,
            "mostFrequentMoodId", mostFrequentMoodId != null ? mostFrequentMoodId : "",
            "mostFrequentCount", mostFrequentCount,
            "mostFrequentMoodData", mostFrequentMoodData != null ? mostFrequentMoodData : Map.of(),
            "totalEntriesLast7Days", last7Days.size()
        );
    }
}